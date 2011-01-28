package com.quub.database;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.quub.Globals;

public class DBConnectionDriver implements Driver{
    private static final int MAJOR_VERSION = 5;
    private static final int MINOR_VERSION = 6;
    
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
			log = Logger.getLogger(DBConnectionDriver.class);
		}
		return log;
	}
    
    private String URL_PREFIX = null;
    private DBConnectionPool pool;

    public DBConnectionDriver(Globals globals,String driver, String url,  String user, String password,Integer warmItUp,Integer numberOfHotStandbys)  throws ClassNotFoundException,  InstantiationException, IllegalAccessException, SQLException {
    	setGlobals(globals);
    	
    	URL_PREFIX = "jdbc:mysql:pool:"+url;
        DriverManager.registerDriver(this);
        Class.forName(driver).newInstance();
        
        pool = new DBConnectionPool(globals, url, user, password,warmItUp,numberOfHotStandbys);
    }

    public synchronized Connection connect(String url, Properties props)  throws SQLException {
        if(!acceptsURL(url)) {
             return null;
        }
        /*Could occur if shutdown happens before warmup is complete */
        if(pool != null){
        	return pool.getSoftConnection();
        }
        return null;
    }

    public synchronized boolean acceptsURL(String url) {
        return url.startsWith(URL_PREFIX);
    }

    public int getMajorVersion() {
        return MAJOR_VERSION;
    }

    public int getMinorVersion() {
        return MINOR_VERSION;
    }

    public DriverPropertyInfo[] getPropertyInfo(String str, Properties props) {
        return new DriverPropertyInfo[0];
    }
    
    public Integer poolSize(){
    	return pool.poolSize();
    }

    public boolean jdbcCompliant() {
        return false;
    }
    
    public synchronized void shutdown(){
    	try{
    		if(pool != null){
    			pool.shutdown();
    		}
    	}catch(Exception e){
    		getLog().error(e.toString());
    	}
    	finally{
    		pool = null;
    		try {
    			DriverManager.deregisterDriver(this);
    		} catch (SQLException e) {
    			getLog().error(e.toString());
    		}
    		finally{
    			getLog().info("DBConnectionDriver shutdown");
    		}
    	}
    }
}

