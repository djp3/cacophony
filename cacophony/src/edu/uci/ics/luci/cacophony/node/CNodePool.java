package edu.uci.ics.luci.cacophony.node;

import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLPropertiesConfiguration;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import weka.core.Instances;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.quub.Globals;
import com.quub.util.Pair;
import com.quub.util.Quittable;
import com.quub.webserver.AccessControl;
import com.quub.webserver.HandlerAbstract;
import com.quub.webserver.RequestDispatcher;
import com.quub.webserver.WebServer;
import com.quub.webserver.handlers.HandlerFileServer;

import edu.uci.ics.luci.cacophony.CacophonyGlobals;
import edu.uci.ics.luci.cacophony.api.HandlerShutdown;
import edu.uci.ics.luci.cacophony.api.HandlerVersion;
import edu.uci.ics.luci.cacophony.api.directory.WebServerWarmUp;
import edu.uci.ics.luci.cacophony.api.node.HandlerCNodePrediction;
import edu.uci.ics.luci.util.FailoverFetch;

public class CNodePool implements Quittable{
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(CNodePool.class);
		}
		return log;
	}
	
	private FailoverFetch failoverFetch = null;
	private String namespace = null;
	private Long poolSize = null;
	private List<CNode> pool = null;
	private List<String> directoryList = null;
	private Instances trainingSet = null;

	private boolean shuttingDown = false;
	
	public synchronized void setQuitting(boolean quitting) {
		if(shuttingDown == false){
			if(quitting == true){
				shuttingDown = true;
				if(pool != null){
					for(CNode c:pool){
						c.setQuitting(quitting);
					}
					pool.clear();
				}
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
	
	
	public CNodePool(){
		directoryList = new ArrayList<String>();
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


	
	public FailoverFetch getFailoverFetch() {
		return failoverFetch;
	}


	public void setFailoverFetch(FailoverFetch failoverFetch) {
		this.failoverFetch = failoverFetch;
	}


	public List<String> getDirectoryList(){
		return directoryList;
	}
	
	public Instances getTrainingSet() {
		return trainingSet;
	}


	public void setTrainingSet(Instances trainingSet) {
		this.trainingSet = trainingSet;
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
	
	public CNodePool launchCNodePool() {
		String propertiesLocation = "cacophony.c_node_pool.properties";
		
		XMLPropertiesConfiguration config = null;
		try {
			config = new XMLPropertiesConfiguration(propertiesLocation);
		} catch (ConfigurationException e2) {
			getLog().error("Unable to use default CNodePool configuration file:"+propertiesLocation);
		}
		
		return(launchCNodePool(config));
	}


	public CNodePool launchCNodePool(XMLPropertiesConfiguration propertiesLocation) {
		return(launchCNodePool(propertiesLocation,null,null,null));
	}


	public CNodePool launchCNodePool(XMLPropertiesConfiguration propertiesLocation,List<Pair<Long,String>> accessRoutes) {
		return(launchCNodePool(propertiesLocation,null,null,accessRoutes));
	}


	/**
	 * 
	 * @param config
	 * @param namespace
	 * @param poolSize
	 * @param directorySeed
	 * @param accessRoutes
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public CNodePool launchCNodePool(XMLPropertiesConfiguration config,Long poolSize,String directorySeed,List<Pair<Long,String>> accessRoutes) {
		
		setNamespace(config.getString("namespace"));
		
		if(poolSize == null){
			poolSize = config.getLong("pool_size");
		}
		setPoolSize(poolSize);
		
		if(directorySeed == null){
			directorySeed = config.getString("directory_seed");
		}
		failoverFetch = new FailoverFetch(directorySeed,getNamespace());
		
		String historyLoader = config.getString("history.loader.class");
		Class<? extends CNodeHistoryLoader> cnhl = null;
		try {
			 cnhl = (Class<? extends CNodeHistoryLoader>) Class.forName(historyLoader);
		} catch(ClassCastException e){
			getLog().error("Class does not extend CNodeHistoryLoader "+historyLoader);
		} catch (ClassNotFoundException e) {
			getLog().error("Unable to locate class to load nodes with "+historyLoader+"\n"+e);
		}
		
		if( cnhl != null){
			String historyLoaderClassOptions = config.getString("history.loader.class.options");
			try {
				JSONObject hlcOptions = new JSONObject(historyLoaderClassOptions);
				CNodeHistoryLoader i = null;
				try {
					i = cnhl.newInstance();
					i.init(hlcOptions);
					trainingSet = i.loadCNodeHistory();
				} catch (InstantiationException e) {
					getLog().error("Unable to instantiate class to load nodes with "+historyLoader+"\n"+e);
				} catch (IllegalAccessException e) {
					getLog().error("Unable to instantiate class to load nodes with "+historyLoader+"\n"+e);
				}
			} catch (JSONException e) {
				getLog().error("Property file does not contain valid json\n"+historyLoaderClassOptions+"\n"+e);
			}
			finally{}
		}
		
		
		setPool(new ArrayList<CNode>());
		for(int i = 0; i< poolSize; i++){
			CNode c = new CNode(failoverFetch,this,accessRoutes);
			if(config.getString("poll_for_config") != null){
				if(config.getString("poll_for_config").equals("true")){
					c.getANewConfiguration();
				}
			}
			c.launch(trainingSet);
			getPool().add(c);
		}
		return(this);
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
		CacophonyGlobals g = new CacophonyGlobals();
		Globals.setGlobals(g);
		
		XMLPropertiesConfiguration config = null;
		try {
			config = new XMLPropertiesConfiguration(clo.getString("config"));
			g.setConfig(config);
		} catch (ConfigurationException e1) {
			getLog().error("Problem loading configuration from:"+clo.getString("config")+"\n"+e1);
		}
		
		Globals.getGlobals().setTesting((Boolean)getConfig(clo,g.getConfig(),"testing"));
		
		
		/* Set up the urls to access the directory from */
		List<Pair<Long, String>> urls = new ArrayList<Pair<Long,String>>();
		
		/* Get the url for external access to the directory */
		String externalURL = getConfig(clo,g.getConfig(),"url.external");
		if((externalURL == null) || (externalURL.equals(""))){
			try {
				externalURL = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				try {
					externalURL = InetAddress.getLocalHost().getHostAddress();
				} catch (UnknownHostException e1) {
					externalURL = null;
				}
			}
		}
		
		if(externalURL != null){
			Pair<Long, String> p = new Pair<Long,String>(1L,externalURL+":"+getConfig(clo,g.getConfig(),"port"));
			urls.add(p);
		}
		
		String internalURL = getConfig(clo,g.getConfig(),"url.internal");
		try {
			if((internalURL == null) || (internalURL.equals(""))){
				internalURL = InetAddress.getLocalHost().getHostName();
			}
		} catch (UnknownHostException e) {
			try {
				internalURL = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e1) {
				internalURL = null;
			}
		}
		if((internalURL != null) && (!internalURL.equals(externalURL))){
			Pair<Long, String> p = new Pair<Long,String>(0L,internalURL+":"+getConfig(clo,g.getConfig(),"port"));
			urls.add(p);
		}
		
		String directorySeed = getConfig(clo,g.getConfig(),"directory_seed");

		CNodePool cNPool = new CNodePool();
		cNPool.launchCNodePool(config,1L,directorySeed,urls);
		Globals.getGlobals().addQuittables(cNPool);
		
		/* Create the webserver to catch rest action*/
		WebServer ws = null;
		try {
			HashMap<String, HandlerAbstract> requestHandlerRegistry = new HashMap<String,HandlerAbstract>();
			HandlerAbstract handler =  new HandlerVersion();
			requestHandlerRegistry.put("",handler);
			requestHandlerRegistry.put("version",handler);
			requestHandlerRegistry.put("predict",new HandlerCNodePrediction(cNPool.getPool().get(0)));
			requestHandlerRegistry.put(null, new HandlerFileServer(edu.uci.ics.luci.cacophony.CacophonyGlobals.class,"/wwwNode/"));
			
			RequestDispatcher requestDispatcher = new RequestDispatcher(requestHandlerRegistry);
			ws = new WebServer(requestDispatcher, Integer.valueOf((String)getConfig(clo,g.getConfig(),"port")), false, new AccessControl());
			ws.start();
			Globals.getGlobals().addQuittables(ws);
		} catch (RuntimeException e) {
			fail("Couldn't start webserver"+e);
		}
		
		
		/* Warm up web server */
		if(ws != null){
			WebServerWarmUp.go(ws, Integer.valueOf((String) getConfig(clo,g.getConfig(),"port")),"http://localhost");
			if(ws.getQuitting()){
				getLog().info("Warm-up failed, shutting down");
				g.setQuitting(true);
			}
		}

		getLog().info("\nDone in "+CNodePool.class.getCanonicalName()+" main()\n");
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
