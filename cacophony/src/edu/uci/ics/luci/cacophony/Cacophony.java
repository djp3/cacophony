package edu.uci.ics.luci.cacophony;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.quub.util.Quittable;
import com.quub.webserver.AccessControl;
import com.quub.webserver.RequestHandlerFactory;
import com.quub.webserver.WebServer;
import com.quub.webserver.WebUtil;

public class Cacophony implements Quittable{
	
	private static transient volatile Logger log = null;
		
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(Cacophony.class);
		}
		return log;
	}


	private boolean shuttingDown = false;
	private boolean testing = false;

	public Cacophony(){
		this(false);
	}
	
	public Cacophony(boolean testing){
		super();
		this.testing = testing;
	}
	

	public synchronized void setQuitting(boolean quitting) {
		if(shuttingDown == false){
			if(quitting == true){
				shuttingDown = true;
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
	
	

	private static JSAPResult parseCommandLine(String[] args) throws JSAPException {
		JSAP jsap = new JSAP();
		JSAPResult config = null;
		Switch sw = null;
		FlaggedOption fl = null;
	        
		try{
			sw = new Switch("testing")
                      .setDefault("false") 
                      .setShortFlag('t') 
                      .setLongFlag("testing");
	        
			sw.setHelp("Run in testing configuration");
			jsap.registerParameter(sw);
			
			fl = new FlaggedOption("port")
        			  .setStringParser(JSAP.INTEGER_PARSER)
                      .setDefault(""+CacophonyGlobals.DEFAULT_PORT) 
                      .setRequired(false) 
                      .setShortFlag('p') 
                      .setLongFlag("port");
	        
			fl.setHelp("Which port should I listen for REST commands on?");
			jsap.registerParameter(fl);
			
			fl = new FlaggedOption("config")
					.setStringParser(JSAP.STRING_PARSER)
					.setDefault(""+CacophonyGlobals.CONFIG_FILENAME_DEFAULT) 
					.setRequired(false) 
					.setShortFlag('c') 
					.setLongFlag("config");
  
			fl.setHelp("What is the name of the file with the configuation properties?");
			jsap.registerParameter(fl);
        
			sw = new Switch("help")
        			.setDefault("false") 
        			.setShortFlag('h') 
        			.setLongFlag("help");

			sw.setHelp("Show this help message"); 
			jsap.registerParameter(sw);
        
			config = jsap.parse(args);
		}
		catch(Exception e){
			config=null;
			getLog().error(e.toString());
		}
        
        // check whether the command line was valid, and if it wasn't,
        // display usage information and exit.
        if ((config == null) || !config.success() || config.getBoolean("help")) {
        	// print out specific error messages describing the problems
            // with the command line, THEN print usage, THEN print full
            // help.  This is called "beating the user with a clue stick."
        	if(config != null){
        		for (Iterator<?> errs = config.getErrorMessageIterator(); errs.hasNext();) {
        			System.err.println("Error: " + errs.next());
        		}
        	}

            System.err.println();
            System.err.println("Usage: java " + Cacophony.class.getName());
            System.err.println("                " + jsap.getUsage());
            System.err.println();
            System.err.println(jsap.getHelp());
            System.err.println();
            throw new InvalidParameterException("Unable to parse command line");
        }

		return config;
	}
	
	private static void warmUpWebServer(JSAPResult config, WebServer ws) {
		
		/* Warm up the web server by asking for the version */
		try{
			String responseString =  null;
			HashMap<String, String> params = new HashMap<String, String>();
			try{
				boolean success = false;
				int count = 0;
				while((!success) && (count < 10)){
					try{
						responseString = WebUtil.fetchWebPage("http://localhost:" + config.getInt("port") + "/version", false, params, 30 * 1000);
						success = true;
					}
					catch(java.net.SocketTimeoutException e){
						getLog().error("Couldn't warm up web server.  Timed out. Trying again");
						count++;
					}
				}
			} catch (MalformedURLException e) {
				ws.getLog().fatal("Couldn't ping webserver, bad URL:"+e.toString());
				ws.setQuitting(true);
			} catch (IOException e) {
				ws.getLog().fatal("Couldn't ping webserver, IO problem:"+e.toString());
				ws.setQuitting(true);
			}
			
			if(responseString == null){
				ws.getLog().fatal("Couldn't ping webserver: responseString was null");
				ws.setQuitting(true);
			}
			else{
				JSONObject response = null;
				try {
					response = new JSONObject(responseString);
				} catch (JSONException e) {
					ws.getLog().fatal("Couldn't form JSON from responseString:"+e.toString()+":"+responseString);
					ws.setQuitting(true);
				}
			
				if(response == null){
					ws.getLog().fatal("Didnt get good data from webserver: response was null");
					ws.setQuitting(true);
				}
				else{
					JSONArray errors = null;
					try {
						errors = response.getJSONArray("errors");
					} catch (JSONException e) {
						ws.getLog().fatal("Couldn't find errors respons from webserver:"+e.toString()+"\n"+response);
						ws.setQuitting(true);
					}
					
					if(errors == null){
						ws.getLog().fatal("Couldn't ping webserver: errors was null");
						ws.setQuitting(true);
					}
					else{
						String thing = null;
						try {
							thing = errors.getString(0);
						} catch (JSONException e) {
							ws.getLog().fatal("Couldn't form JSON string from errors:"+e.toString()+"\n"+errors);
							ws.setQuitting(true);
						}
						
						if(thing == null){
							ws.getLog().fatal("Got errors while pinging webserver: thing is null");
							ws.setQuitting(true);
						}
						else if(!thing.equals("false")){
							ws.getLog().fatal("Got unexpected errors while pinging webserver:"+thing);
							ws.setQuitting(true);
						}
						else{
							String answer = null;
							try {
								answer = response.getString("version");
							} catch (JSONException e) {
								ws.getLog().fatal("Couldn't form JSON string from version:"+e.toString()+"\n"+response);
								ws.setQuitting(true);
							}
							
							if(answer == null){
								ws.getLog().fatal("Didn't get expected version number, wanted:"+RequestHandlerCacophony.VERSION+", got: null");
								ws.setQuitting(true);
							}
							else if(!answer.equals(RequestHandlerCacophony.VERSION)){
								ws.getLog().fatal("Didn't get expected version number, wanted:"+RequestHandlerCacophony.VERSION+", got:"+answer);
								ws.setQuitting(true);
							}
							else{
								ws.getLog().fatal("Started WhisperBase WebServer version "+answer);
							}
						}
					}
				}
			}
		} catch (RuntimeException e) {
			ws.getLog().fatal("Couldn't ping webserver");
			ws.setQuitting(true);
		}
	}


	
	public static void main(String[] args) {
		System.out.println("Cacophony team members:");
		System.out.println("	Don J. Patterson");
		System.out.println("	Raphael Chang Lee");
		System.out.println("	Aaron Pecson" );
		System.out.println("	Vatsal Shah" );	
		

		/*Set the thread name for error reporting */
		Thread.currentThread().setName(Cacophony.class.getName());
			
		/*Get command line options */
		JSAPResult commandLineOptions = null;
		try {
			commandLineOptions = parseCommandLine(args);
		} catch (JSAPException e) {
			throw new IllegalArgumentException(e);
		}    
		
		try {
			Configuration config;
			config = new PropertiesConfiguration(commandLineOptions.getString("config"));
			CacophonyGlobals.getCacophonyGlobals().setConfig(config);
		} catch (ConfigurationException e1) {
			getLog().error("Problem loading configuration from:"+commandLineOptions.getString("config")+"\n"+e1);
		}
		
		
		Cacophony cNode = new Cacophony();
		
		WebServer ws = null;
		try{
			RequestHandlerFactory requestHandlerFactory = new RequestHandlerFactory(CacophonyGlobals.getGlobals(),RequestHandlerCacophony.class);
			AccessControl accessControl = new AccessControl(CacophonyGlobals.getGlobals());
			ws = new WebServer(CacophonyGlobals.getGlobals(), requestHandlerFactory, null, commandLineOptions.getInt("port"), commandLineOptions.getBoolean("testing"), accessControl);
			
			ws.start();
		} catch (RuntimeException e) {
			getLog().fatal("Couldn't start webserver:"+e.toString());
			ws.setQuitting(true);
		}
			
		/*Set up clean shutdown hooks*/
		CacophonyGlobals.getGlobals().addQuittables(ws);
		
		/* Warm up web server */
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		if(ws != null){
			warmUpWebServer(commandLineOptions, ws);
			if(ws.getQuitting()){
				cNode.setQuitting(true);
			}
		}
			
		getLog().info("\nDone in Cacophony main()\n"+System.getProperty("user.dir"));
	}
}
