package com.quub.webserver;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.quub.Globals;
import com.quub.database.QuubDBConnectionPool;
import com.quub.util.CalendarCache;
import com.quub.util.Quittable;

/**
 * 
 * This Web server class listens for any incoming client request at a specified
 * port. Every incoming request spawns a new thread to process it.
 * 
 * Usage: java MyWebServer [port] where port is any unused port in the system
 * value between 1024 and 65535. Ports less than 1024 needs admin access. if
 * port is 0 or not specified, then any unused port is used
 * 
 */

public class WebServer implements Runnable,Quittable{
	static public final int DEFAULT_PORT = 80;
	static public final int threadPoolSize = 100;
	
	static final int databaseConnectionPoolSize = threadPoolSize;
	
	static private CalendarCache calendarCache = new CalendarCache(CalendarCache.TZ_GMT);
	
	private long startTime = System.currentTimeMillis();
	private Date startDate = new Date();
	private long count = 0;

	int port = DEFAULT_PORT;
	Boolean testing = null;
	Thread webServer = null;

	ExecutorService threadExecutor = null;
	
	QuubDBConnectionPool dBConnectionPool = null;
	
	private boolean quitting = false;

	private RequestHandlerFactory requestHandlerFactory = null;
	private AccessControl accessControl;
	
	private static Globals _globals = null;
	public Globals getGlobals() {
		return _globals;
	}
	
	public static void setGlobals(Globals g) {
		_globals = g;
	}

	private static transient volatile Logger log = null;
	public Logger getLog(){
		if(log == null){
			log = Logger.getLogger(WebServer.class);
		}
		return log;
	}
	
	
	public void setQuitting(boolean q){
		getLog().debug("Setting quitting to :"+q);
		StackTraceElement[] foo = Thread.currentThread().getStackTrace();
		for(StackTraceElement ste: foo){
			getLog().debug(ste.toString());
		}
		quitting = q;
	}
	
	public boolean getQuitting() {
		return quitting;
	}

	public static CalendarCache getCalendarCache() {
		return calendarCache;
	}

	public static void setCalendarCache(CalendarCache calendarCache) {
		WebServer.calendarCache = calendarCache;
	}

	public long getLaunchTime() {
		return startTime;
	}
	
	public String getLaunchDate(){
		return startDate.toString();
	}

	public long getTotalRequests() {
		return count;
	}
	
	
	public Boolean getTesting() {
		return testing;
	}

	public void setTesting(Boolean testing) {
		this.testing = testing;
	}
	
	public Thread getWebServer() {
		return webServer;
	}

	private void setWebServer(Thread webServer) {
		this.webServer = webServer;
	}

	public WebServer(Globals globals,RequestHandlerFactory requestHandlerFactory,QuubDBConnectionPool odbcp,int port,boolean testing,AccessControl accessControl){
		
		setGlobals(globals);
		this.requestHandlerFactory = requestHandlerFactory;
		this.dBConnectionPool = odbcp;
		this.port = port;
		
		threadExecutor = Executors.newFixedThreadPool(threadPoolSize);
		
		this.accessControl = accessControl;
		this.accessControl.setTesting(testing);
		this.accessControl.setDefaultFilename(null);
		
		this.setTesting(testing);
		if(getTesting()){
			/*Clear access controls for testing*/
			this.accessControl.setBadGuyTest(new ArrayList<String>());
		}
		
		setWebServer(new Thread(this));
		getWebServer().setName("WebServer:"+((testing)?"testing":"not testing"));
		getWebServer().setDaemon(false); /* Force a clean shutdown */
		
		if(testing){
			getLog().info("Sleeping 1 seconds so everything can stabilize");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			getLog().info("Done Sleeping");
		}
		
	}
	
	public void start(){
		getWebServer().start();
	}
	
	
	public void run(){
		/* Set up an infinite loop to field requests */
		
		ServerSocket serverSoc = null;
		
		try {
			/*Counting requests for stats */
			count = 0;
			
			serverSoc = new ServerSocket(port);
			serverSoc.setSoTimeout(1000);
			
			getLog().info("Time:"+System.currentTimeMillis()+",Server is listening on port " + serverSoc.getLocalPort());
			
			while (!quitting) {
				/* Blocks until connection arrives */
				Socket soc;
				try{
					soc = serverSoc.accept();
				}
				catch(java.net.SocketTimeoutException e){
					soc = null;
				}
				
				if(!quitting && (soc != null) ){
					/* When we get a connection handle it and wait for the next one */
					threadExecutor.execute(this.requestHandlerFactory.makeHandler(this,accessControl,threadExecutor,dBConnectionPool,soc,testing));
					count++;
				}
			}
		} catch (BindException e) {
			getLog().fatal(e.toString());
		} catch (RuntimeException e) {
			getLog().fatal(e.toString());
		} catch (IOException e) {
			getLog().fatal(e.toString());
		} finally {
			try {
				serverSoc.close();
				serverSoc=null;
			} catch (Exception e) {
				getLog().error(e.toString());
			}
			finally{
				try{
					threadExecutor.shutdown();
					threadExecutor=null;
				} catch (Exception e) {
					getLog().error(e.toString());
				}
			}
		}
		getLog().info("WebServer shutdown");
	}


}
