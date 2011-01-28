package com.quub;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.quub.util.MyShutdown;
import com.quub.util.Quittable;

public class Globals implements Quittable{
	
	
	private static transient volatile Logger log = null;
	protected static Globals _globals = null;
	
	protected Globals(){
		reloadLog4jProperties();
	}
	
	protected Globals(String fileName){
		reloadLog4jProperties(fileName);
	}
	
	public static Globals getGlobals(){
		if(_globals == null){
			_globals = new Globals();
		}
		return _globals;
	}
	
	public static synchronized void set_globals(Globals _globals) {
		Globals._globals = _globals;
	}
	
	public Logger getLog(){
		if(log == null){
			log = Logger.getLogger(Globals.class);
		}
		return log;
	}
	

	private static final String propertyFileNameDefault = "quub.log4j.properties";
	private static final String databaseDomain = "database.quub.com";
	
	protected String propertyFileName = propertyFileNameDefault;
	
	List<Quittable> quittables = new ArrayList<Quittable>();
	private boolean shuttingDown = false;


	public void reloadLog4jProperties(){
		File f = new File(getPropertyFileName());
		if(f.exists()){
			PropertyConfigurator.configure(getPropertyFileName());
		}
		else{
			BasicConfigurator.configure();
			getLog().log(Level.INFO,"Unable to locate property file:"+getPropertyFileName());
		}
	}
	
	public void reloadLog4jProperties(String propertyFileName) {
		setPropertyFileName(propertyFileName);
		reloadLog4jProperties();
	}
	
	public String getDefaultDatabaseDomain() {
		return databaseDomain;
	}
	
	public String getPropertyFileName() {
		return propertyFileName;
	}
	
	public void setPropertyFileName(String propertyFileName) {
		this.propertyFileName = propertyFileName;
	}
	
	public List<String> getBadGuyList() {
		return(new ArrayList<String>());
	}
	
	public void addQuittables(Quittable q){
		if(shuttingDown){
			q.setQuitting(true);
		}
		else{
			synchronized(quittables){
				synchronized(q){
					this.quittables.add(q);
					ArrayList<Quittable> newq = new ArrayList<Quittable>();
					MyShutdown m = new MyShutdown(newq);
					Runtime.getRuntime().addShutdownHook(m);
				}
			}
		}
	}
	
	
	/**
	 * Initiate a global shutdown
	 */
	public void setQuitting(boolean quitting){
		if(shuttingDown == false){
			if(quitting == true){
				shuttingDown = true;
				for(Quittable q: quittables){
					q.setQuitting(true);
				}
			}
		}
		else{
			if(quitting == false){
				getLog().fatal("Trying to undo a shutdown! Can't do that");
			}
			else{
				getLog().fatal("Trying to shutdown twice! Can't do that");
			}
		}
	}
}
