package edu.uci.ics.luci.cacophony.node;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLPropertiesConfiguration;
import org.apache.log4j.Logger;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.quub.Globals;
import com.quub.util.Quittable;
import com.quub.webserver.AccessControl;
import com.quub.webserver.HandlerAbstract;
import com.quub.webserver.RequestHandlerFactory;
import com.quub.webserver.WebServer;
import com.quub.webserver.handlers.HandlerFileServer;
import com.quub.webserver.handlers.HandlerVersion;

import edu.uci.ics.luci.cacophony.CacophonyGlobals;
import edu.uci.ics.luci.cacophony.directory.api.HandlerShutdown;
import edu.uci.ics.luci.cacophony.directory.api.WebServerWarmUp;
import edu.uci.ics.luci.util.FailoverFetch;

public class CNodePool implements Quittable{
	
	private static transient volatile Logger log = null;
	private static CNodePool theOne = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(CNodePool.class);
		}
		return log;
	}
	
	private String namespace = null;
	private Long poolSize = null;
	private List<CNode> pool = null;
	private List<String> directoryList = null;

	private boolean shuttingDown = false;
	
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
				getLog().fatal("Trying to shutdown twice! Don't do that");
			}
		}
	}	
	
	
	private CNodePool(){
		Globals.getGlobals();
		directoryList = new ArrayList<String>();
	}
	
	public static synchronized CNodePool getInstance(){
		if(theOne == null){
			theOne = new CNodePool();
		}
		return theOne;
	}
	
	public String getNamespace() {
		return namespace;
	}


	protected void setNamespace(String namespace) {
		if(this.namespace == null){
			this.namespace = namespace;
		}
		else{
			getLog().error("Doublesetting  namespace: "+this.namespace+" ->"+namespace);
			this.namespace = namespace;
		}
	}
	
	public Long getPoolSize() {
		return poolSize;
	}


	protected void setPoolSize(Long poolSize) {
		this.poolSize = poolSize;
	}
	
	

	public List<CNode> getPool() {
		return pool;
	}


	protected void setPool(List<CNode> pool) {
		this.pool = pool;
	}


	
	public List<String> getDirectoryList(){
		return directoryList;
	}
	
	protected static JSAPResult parseCommandLine(String[] args) throws JSAPException {
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
			
			fl = new FlaggedOption("url.external")
					.setStringParser(JSAP.STRING_PARSER)
					.setRequired(false) 
					.setShortFlag('u') 
					.setLongFlag("url.external");
  
			fl.setHelp("What is the url that external web browsers can find you? (Maybe different than what the server thinks it is)");
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
            System.err.println("Usage: java " + CNodePool.class.getName());
            System.err.println("                " + jsap.getUsage());
            System.err.println();
            System.err.println(jsap.getHelp());
            System.err.println();
            throw new InvalidParameterException("Unable to parse command line");
        }

		return config;
	}
	
	public static void main(String[] args) {
		
		/*Set the thread name for error reporting */
		Thread.currentThread().setName(CNodePool.class.getName());
			
		/*Get command line options */
		JSAPResult clo = null;
		try {
			clo = parseCommandLine(args);
		} catch (JSAPException e) {
			throw new IllegalArgumentException(e);
		}  
		
		/* Get Globals and local properties */
		CacophonyGlobals g = CacophonyGlobals.getGlobals();
		try {
			PropertiesConfiguration config;
			config = new PropertiesConfiguration(clo.getString("config"));
			g.setConfig(config);
		} catch (ConfigurationException e1) {
			getLog().error("Problem loading configuration from:"+clo.getString("config")+"\n"+e1);
		}
		
		Globals.getGlobals().setTesting((Boolean)getConfig(clo,g.getConfig(),"testing"));
				
		CNodePool cNPool = launchCNodePool();
		
		/* Create the webserver to catch rest action*/
		WebServer ws = null;
		try{
			Map<String, Class<? extends HandlerAbstract>> requestHandlerRegistry = new HashMap<String, Class<? extends HandlerAbstract>>();
			requestHandlerRegistry.put(null, HandlerFileServer.class); //Default response
			requestHandlerRegistry.put("version",HandlerVersion.class);
			requestHandlerRegistry.put("shutdown",HandlerShutdown.class);

			RequestHandlerFactory requestHandlerFactory = new RequestHandlerFactory(g,requestHandlerRegistry);
			AccessControl accessControl = new AccessControl();
			
			Integer port = getConfig(clo,g.getConfig(),"port");
			
			ws = new WebServer(g, requestHandlerFactory, null/*odbcp*/, port, false,accessControl);
		} catch (RuntimeException e) {
			getLog().fatal("Couldn't start webserver:"+e);
			if(ws != null){
				ws.setQuitting(true);
			}
			g.setQuitting(true);
		}
		
		/* Warm up web server */
		try {
			if(ws != null){
				ws.start();
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
		}
		
		if(ws != null){
			WebServerWarmUp.go(clo, ws, "http://localhost");
			if(ws.getQuitting()){
				g.setQuitting(true);
			}
		}
		
		String url = getConfig(clo,g.getConfig(),"url.external");
		try {
			if((url == null) || (url.equals(""))){
				url = InetAddress.getLocalHost().getHostAddress();
			}
		} catch (UnknownHostException e) {
			url = "127.0.0.1";
		}
		url = url+":"+getConfig(clo,g.getConfig(),"port");
		
		/*Set up clean shutdown hooks*/
		g.addQuittables(ws);
		g.addQuittables(cNPool);
		
		getLog().info("\nDone in "+CNodePool.class.getCanonicalName()+" main()\n");
	}
	
	public static CNodePool launchCNodePool() {
		String propertiesLocation = "cacophony.c_node_pool.properties";
		return(launchCNodePool(propertiesLocation));
	}


	public static CNodePool launchCNodePool(String propertiesLocation) {
		CNodePool cNPool = CNodePool.getInstance();
		
		/* Get Directory properties and initialize */
		try {
			XMLPropertiesConfiguration config;
			config = new XMLPropertiesConfiguration(propertiesLocation);
			
			String namespace = config.getString("namespace");
			cNPool.setNamespace(namespace);
			
			Long poolSize = config.getLong("pool_size");
			cNPool.setPoolSize(poolSize);
			
			String directorySeed = config.getString("directory_seed");
			FailoverFetch failoverFetch = new FailoverFetch(directorySeed);
			
			cNPool.setPool(new ArrayList<CNode>());
			
			for(int i = 0; i< poolSize; i++){
				CNode c = new CNode(failoverFetch,cNPool);
				c.getANewConfiguration();
				c.launch();
				cNPool.getPool().add(c);
			}
			
		} catch (ConfigurationException e1) {
			getLog().error("Problem loading configuration from:"+propertiesLocation+"\n"+e1);
			return null;
		}
		return cNPool;
	}



	@SuppressWarnings("unchecked")
	private static <T> T getConfig(JSAPResult clo, PropertiesConfiguration config, String string) {
		T ret = null;
		if(config.containsKey(string)){
			ret = (T) config.getProperty(string);
		}
		T _ret = (T) clo.getObject(string);
		if(_ret != null){
			ret = _ret;
		}
		return (ret);
	}

}
