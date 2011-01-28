package com.quub.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.quub.Globals;

public class DBConnectionPool {
	public static final String URL_PREFIX = "jdbc:mysql:";
	private static final int POOL_SOFT_LIMIT = 400;
	private static final int POOL_HARD_LIMIT = 500;
	private static long timeout = 60*1000; /*One minute*/
	
	private Vector<DBConnection> connections;
	
	private String url, user, password;
	
	private Integer hotStandby;
	
	private static Globals _globals = null;
	
	public synchronized Globals getGlobals() {
		return _globals;
	}
	
	public synchronized void setGlobals(Globals g) {
		_globals = g;
	}

	private static transient volatile Logger log = null;
	public Logger getLog(){
		if(log == null){
			log = Logger.getLogger(DBConnectionPool.class);
		}
		return log;
	}
	
	private ConnectionReaper reaper;
	
	class ConnectionReaper extends Thread {
	    private DBConnectionPool pool;
	    private final long delay = 120000;
	    private boolean quitting = false;

	    ConnectionReaper(DBConnectionPool pool) {
	        this.pool = pool;
	    }
	    
	    public synchronized void setQuitting(boolean q){
	    	quitting = q;
	    	this.notifyAll();
	    }
	    
	    public synchronized void wakeUp(){
	    	this.notifyAll();
	    }

	    public void run() {
	        while(!quitting) {
	        	pool.getLog().log(Level.DEBUG, "Connection Reaper reaping connections");
	        	pool.reapConnections();
	        	checkForHotStandby();
	        	try {
	        	   synchronized(this){
	        		   if(!quitting){
	        			   wait(delay);
	        		   }
	        	   }
	           } catch( InterruptedException e) { }
	        }
	    }
	}



	public DBConnectionPool(Globals globals,String url, String user, String password,Integer warmItUp,Integer numberOfHotStandbys) {
		/*Vectors are synchronized */
		
		setGlobals(globals);
		this.url = url;
		this.user = user;
		this.password = password;
		
		if((numberOfHotStandbys != null) && (numberOfHotStandbys > 0)){
			this.hotStandby = numberOfHotStandbys;
		}
		else{
			this.hotStandby = 0;
		}
		
		connections =  new Vector<DBConnection>(this.hotStandby);
		
		getLog().log(Level.INFO, "Warming up with:"+warmItUp);
		if((warmItUp != null) && (warmItUp > 0)){
			checkForHotStandby(warmItUp);
		}
		
		getLog().log(Level.INFO, "Making hot standby's with:"+this.hotStandby);
		checkForHotStandby(this.hotStandby);
		
		reaper = new ConnectionReaper(this);
		reaper.setName("Connection Reaper");
		reaper.setDaemon(false); /*Force an intentional/clean shutdown*/
		reaper.start();
	}

	protected static synchronized long getTimeout() {
		return timeout;
	}

	protected static synchronized void setTimeout(long timeout) {
		DBConnectionPool.timeout = timeout;
	}
	
	public synchronized Integer poolSize(){
		return(connections.size());
	}

	public synchronized void closeConnections() {
		if(connections != null){
			for(DBConnection conn:connections){
				try {
					if(conn != null){
						conn.close();
					}
				} catch (SQLException e) {
					getLog().log(Level.ERROR,"Problem closing connection",e);
				}
			}
		}
	}
	
	private synchronized void hardCloseConnection(DBConnection conn){
		if(conn != null){
			try {
				/*Soft close the connection */
				conn.close();
			} catch (SQLException e) {
				getLog().log(Level.ERROR,"Database error while soft closing a connection",e);
			}
			finally{
				if(conn.getConnection() != null){
					try{
						/* Hard destroy the underlying connection */
						conn.getConnection().close();
					} catch (SQLException e) {
						getLog().log(Level.ERROR,"Database error while hard closing a connection:",e);
					}
				}
			}
			connections.remove(conn);
		}
	}

	/** Close all open underlying connections in the pool.  Written for shutdown sequence. **/
	protected synchronized void hardCloseConnections(){
		if(connections != null){
			
			int count = connections.size();
			
			/*Avoid a concurrent modification exception */
			Vector<DBConnection> connectionsCopy = new Vector<DBConnection>(connections.size());
			connectionsCopy.addAll(connections);
			for(DBConnection conn:connectionsCopy){
				hardCloseConnection(conn);
			}
			getLog().log(Level.DEBUG, "Hard reaped "+(count-connections.size())+" connections. "+connections.size()+" connections are left.");
		}
	}
	
	/*Remove connections from the pool */
	public synchronized void reapConnections() {
		
		if(connections != null){
			long staleTime = System.currentTimeMillis() - timeout;
			
			int total = 0;
			int inuse = 0;
			int stale = 0;
			int notinuse = 0;
			int notinuse_stale = 0;
			int notinuse_reduced = 0;
			int invalid = 0;
			/*Avoid a concurrent modification exception */
			Vector<DBConnection> connectionsCopy = new Vector<DBConnection>(connections.size());
			connectionsCopy.addAll(connections);
			for(DBConnection conn:connectionsCopy){
				total++;
				if(conn.inUse()){
					inuse++;
					if(staleTime > conn.getLastUse()){
						stale++;
						if(conn.validate()){
							getLog().log(Level.INFO, "I've got an old connection lying around with this stack trace:\n"+conn.getStackTrace());
						}
						else{
							invalid++;
							hardCloseConnection(conn);
						}
					}
				}
				else{
					notinuse++;
					if(staleTime > conn.getLastUse()){
						notinuse_stale++;
						if(notinuse > this.hotStandby){
							notinuse_reduced++;
							hardCloseConnection(conn);
						}
					}
				}
			}
			getLog().log(Level.DEBUG, total+" connections found in the pool, inuse:"+inuse+"(stale:"+stale+",invalid/reaped:"+invalid+"),not inuse:"+notinuse+"(stale:"+notinuse_stale+",over hotstandby/reaped:"+notinuse_reduced+"), hot standby:"+this.hotStandby+",ended with:"+connections.size());
		}
		else{
			getLog().log(Level.FATAL, "Why is the DBConnection Pool missing a datastructure?");
		}
	}
	
	private synchronized void checkForHotStandby(){
		checkForHotStandby(this.hotStandby);
		
	}
	/** Make sure there are enough connections in the pool **/
	private synchronized void checkForHotStandby(Integer numberToKeepHot){
		if((connections != null) && (numberToKeepHot != null)){
			int notinuse=0;
			for(DBConnection conn:connections){
				if(!conn.inUse()){
					notinuse++;
				}
			}
			for(int i = notinuse;i < numberToKeepHot;i++){
				DBConnection c = null;
				try {
					getLog().log(Level.DEBUG, "Trying to make a connection for hot standby, pool size is "+connections.size()+" < "+numberToKeepHot);
					c = getHardConnection();
					if( c == null){
						getLog().log(Level.ERROR, "Unable to make a connection for hot standby, pool size is "+connections.size()+" < "+numberToKeepHot);
					}
				}
				finally{
					if(c != null){
						try {
							c.close();
						} catch (SQLException e) {
						}
					}
				}
				getLog().log(Level.DEBUG, "Made a required connection for hot standby/warm up, pool size is "+connections.size()+", require: "+numberToKeepHot);
			}
		}
	}
	
	protected synchronized DBConnection getHardConnection(){
		DBConnection dbc = null;
		Properties p = new Properties();
		
		if((connections != null) && (p != null)){
			p.setProperty("user", user);
			p.setProperty("password", password);
	
			if(connections.size() > POOL_SOFT_LIMIT){
				getLog().error("The number of connections in the database pool is "+connections.size()+", greater than "+POOL_SOFT_LIMIT);
			}
	
			if(connections.size() > POOL_HARD_LIMIT){
				getLog().log(Level.FATAL,"The number of connections in the database pool is "+connections.size()+", greater than "+POOL_HARD_LIMIT+". Failing to deliver a connection.");
			}
		
			Connection conn = null;
			try {
				conn = DriverManager.getConnection(URL_PREFIX+url, p);
			} catch (SQLException e) {
				getLog().log(Level.ERROR, "Unable to get a hard connection from the database:"+e);
			}
			
			if(conn != null){
				dbc  = new DBConnection(getGlobals(), conn);
			}
			
			if(dbc != null){
				dbc.lease();
				connections.add(dbc);
			}
		}
		return(dbc);
	}


	public synchronized DBConnection getSoftConnection() throws SQLException {
		DBConnection dbc = null;
		boolean invokereaper = false;
		Vector<DBConnection> removeUs = new Vector<DBConnection>(connections.size());
		
		if(connections != null){
			for(DBConnection c:connections){
				if ((dbc == null) && (c.lease())) {
					if(c.validate()){
						dbc = c;
					}
					else{
						removeUs.add(c);
					}
				}
			}
			
			for(DBConnection c:removeUs){
				hardCloseConnection(c);
				/* Removing bad connections requires making sure there are enough good ones around */
				invokereaper = true;
			}
			
			if(dbc == null){
				dbc = getHardConnection();
				if(this.hotStandby > 0){
					/* We want a hot standby available, but we must be so busy they are taken
					 *  tell the reaper to wake up to make some new ones*/
					invokereaper = true;
				}
			}
		}
		
		if(invokereaper){
			reaper.wakeUp();
		}
		
		return dbc;
	} 

	/*
	public synchronized void returnConnection(DBConnection conn) {
		conn.expireLease();
	}
	*/
	
	public synchronized void shutdown(){
		
		try{
			if(reaper != null){
				reaper.setQuitting(true);
			}
			
			/*Soft close connections */
			closeConnections();
			
			/*Hard close connections */
			hardCloseConnections();
		
			if(connections != null){
				connections.clear();
			}
		}
		finally{
			reaper = null;
			connections = null;
		}
	}

	public void finalize() throws Throwable{
		try{
			shutdown();
		}
		catch(Exception e){
			getLog().error(e.toString());
		}
		finally{
			super.finalize();
		}
	}
}
