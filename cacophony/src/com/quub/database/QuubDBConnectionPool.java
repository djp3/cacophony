package com.quub.database;

import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.quub.Globals;

public class QuubDBConnectionPool {
	public static final Integer DEFAULT_WARM_UP_SIZE = 25;
	private static final Integer DEFAULT_HOT_STANDBY_SIZE = 10;
	//private static Random r = new Random();
	private static long totalCount = 0;

	private DBConnectionDriver dbcd = null;
	private String databaseDomain = null;
	private String database = null;

	private static Globals _globals = null;
	public Globals getGlobals() {
		return _globals;
	}
	
	public void setGlobals(Globals g) {
		_globals = g;
	}

	private static transient volatile Logger log = null;
	public Logger getLog(){
		if(log == null){
			log = Logger.getLogger(QuubDBConnectionPool.class);
		}
		return log;
	}
	
	public static long getTotalRequests() {
		return totalCount;
	}
	
	public Integer getPoolSize(){
		return(dbcd.poolSize());
	}

	public String getDatabaseDomain() {
		return databaseDomain;
	}

	public void setDatabaseDomain(String databaseDomain) {
		this.databaseDomain = databaseDomain;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getDatabaseURL() {
		return "//" + databaseDomain + "/" + database;
	}

	public void setDatabaseURL(String databaseURL, String database) {
		this.databaseDomain = databaseURL;
		this.database = database;
	}

	public QuubDBConnectionPool(Globals globals, String databaseDomain, String database, String username, String password) {
		this(globals,databaseDomain,database,username,password,DEFAULT_WARM_UP_SIZE,DEFAULT_HOT_STANDBY_SIZE);
	}


	/**
	 * 
	 * @param globals
	 * @param database
	 * @param username
	 * @param password
	 * @param databaseURL
	 */
	public QuubDBConnectionPool(Globals globals, String databaseDomain, String database, String username, String password,Integer warmItUp,Integer numberOfHotStandbys) {
		
		setGlobals(globals);

		try {
			setDatabaseURL(databaseDomain, database);
			dbcd = new DBConnectionDriver(globals, "com.mysql.jdbc.Driver", getDatabaseURL(), username, password,warmItUp,numberOfHotStandbys);
		} catch (Exception e) {
			getLog().error("Unable to start QuubDBConnection pool:"+e);
		}

	}

	public DBConnection getConnection() {
		totalCount++;
		DBConnection c = null;
		try {
			c = (DBConnection) DriverManager.getConnection("jdbc:mysql:pool:" + getDatabaseURL());
		} catch (SQLException e) {
			getLog().log(Level.ERROR,"Unable to get a connection from the pool,"+e.toString());
		}
		
		if((c != null) && (!c.validate())){
			getLog().log(Level.FATAL,"We should never be giving back invalid connections that aren't null");
			c = null;
		}
		
		/*TODO: Remove this once I believe the pool is operating correctly */
		if(c != null){
			RuntimeException e = new RuntimeException("dummy");
			StackTraceElement[] st = e.getStackTrace();
			StringBuffer sb = new StringBuffer();
			for(int i = 0 ; i< st.length; i++){
				sb.append(st[i].toString()+"\n");
			}
			c.setStackTrace(sb.toString());
		}
		
		return(c);
	}
	
	/*
	public DBConnection getConnection() {
		DBConnection c = null;
		try {
			do{
				do{
					c = getConnection(5);
				}while(c == null);
				
				if(!c.isValid(0)){
					c.close();
					c = null;
				}
				
			}while(c == null);
			
			return(c);
		} catch (SQLException e) {
			if(c != null){
				try {
					c.close();
				} catch (SQLException e1) {
					c = null;
				}
			}
			getLog().error("SQL Exception while trying to get a connection (Retrying)\n" + e.toString());
			return getConnection();
		}
	}
	*/
/*
	protected DBConnection getConnection(int tries) {
		if (tries <= 0) {
			getLog().error("Failing at getting a connection");
			return (null);
		}

		try {
			totalCount++;
			return (DBConnection) DriverManager.getConnection("jdbc:mysql:pool:" + getDatabaseURL());
		} catch (SQLException e) {
			getLog().error("SQL Exception while trying to get a connection (Retrying " + tries + " times)\n" + e.toString());
			try {
				int pause = 100 + (int)(r.nextDouble()*100);
				if((3-tries)>0){
					pause = (int) Math.pow(pause,3-tries);
				}
				getLog().info("Pausing before retrying connection:" + pause +" ms");
				Thread.sleep(pause);
			} catch (InterruptedException e1) {
			}
			return getConnection(tries - 1);
		}
	}

*/
	public void shutdown() {
		if (dbcd != null) {
			try {
				dbcd.shutdown();
			} finally {
				dbcd = null;
			}
		}
	}

}
