package edu.uci.ics.luci.cacophony.node;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLPropertiesConfiguration;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

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
import edu.uci.ics.luci.cacophony.api.HandlerVersion;
import edu.uci.ics.luci.cacophony.api.directory.WebServerWarmUp;
import edu.uci.ics.luci.cacophony.api.node.HandlerCNodePrediction;
import edu.uci.ics.luci.cacophony.model.KyotoCabinet;
import edu.uci.ics.luci.cacophony.model.KyotoCabinetVisitor;
import edu.uci.ics.luci.cacophony.model.ModelStorage;
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
	private List<String> directoryList = null;

	private Object shuttingDownLock = new Object();
	private boolean shuttingDown = false;
	private boolean updatingActive = false;
	private ModelStorage<String,CNode> db = null;
	private Thread updateThread = null;
	
	public CNodePool(ModelStorage<String,CNode> db){
		directoryList = new ArrayList<String>();
		this.db = db;
		db.open(Globals.getGlobals().isTesting(),Globals.getGlobals().isTesting());
	}
	
	public void setUpdatingActive(boolean active){
		this.updatingActive = active;
	}
	
	public boolean isUpdatingActive(){
		return(this.updatingActive);
	}
	
	

	public void setQuitting(boolean quitting) {
		synchronized(shuttingDownLock){
			if(getQuitting() == false){
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
		if(getQuitting()){
			while(updateThread.isAlive()){
				synchronized(updateThread){
					updateThread.notifyAll();
					try {
						updateThread.join();
					} catch (InterruptedException e) {
					}
				}
			}
			if(db != null){
				db.setQuitting(true);
			}
		}
	}


	
	public boolean getQuitting(){
		synchronized(shuttingDownLock){
			return shuttingDown;
		}
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
	
	

	public void addToPool(String id,CNode cnode){
		if(!db.set(id, cnode)){
			throw new RuntimeException("Unable to set ("+id+","+cnode.getMetaCNodeGUID()+") in kyotocabinet:"+db.error());
		}
	}
	
	public CNode getFromPool(String id){
		CNode c = (CNode) (db.get(id));
		c.setParentPool(this); // The parent is transient 
		return(c);
	}
	
	public void removeFromPool(String id, CNode cnode){
		db.remove(id);
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
	
	private void launchCNodePoolUpdateThread() {
		
		final CNodePool me = this;
		
		updateThread = new Thread(new Runnable(){
			@Override
			public void run() {
				while(!getQuitting()){
					me.setUpdatingActive(true);
					Long now = System.currentTimeMillis();
					
					/* We don't use db.iterate here because it locks the db for any incoming REST request */
					HashSet<String> poolKeySet = me.getPoolKeySet();
					for(String key:poolKeySet){
						CNode fromPool = me.getFromPool(key);
						if(fromPool != null){
							fromPool.synchronizeWithNetwork();
							me.addToPool(key, fromPool);
						}
					}
					me.setUpdatingActive(false);
					
					
					Long elapsed = System.currentTimeMillis() - now;
					synchronized(updateThread){
						while ((elapsed < CNode.ONE_HOUR) && (!getQuitting())){
							try {
								updateThread.wait(CNode.ONE_HOUR - elapsed);
							} catch (InterruptedException e) {
							}
							elapsed = System.currentTimeMillis() - now;
						}
					}
				}
			}
		});
		updateThread.setDaemon(false); //Force the thread to shut down cleanly
		updateThread.setName("CNodePoolUpdater");
		updateThread.start();
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
		
		if(directorySeed == null){
			directorySeed = config.getString("directory_seed");
		}
		failoverFetch = new FailoverFetch(directorySeed,getNamespace());
		
		if((config.getString("poll_for_config") != null) && (config.getString("poll_for_config").equals("true"))){
			if(poolSize == null){
				setPoolSize(config.getLong("pool_size"));
			}
			else{
				setPoolSize(poolSize);
			}
				
			for(int i = 0; i< getPoolSize(); i++){
				CNode c = new CNode();
				c.setFailoverFetch(failoverFetch);
				c.setParentPool(this);
				c.setBaseUrls(accessRoutes);
				c.getANewConfiguration();
				c.synchronizeWithNetwork(true,true,true,false);
				addToPool(c.getMetaCNodeGUID(),c);
			}
		}
		else{
			String cNodeLoader = config.getString("cnode.loader.class");
			Class<? extends CNodeLoader> cnhl = null;
			try {
				cnhl = (Class<? extends CNodeLoader>) Class.forName(cNodeLoader);
			} catch(ClassCastException e){
				getLog().error("Class does not extend CNodeHistoryLoader "+cNodeLoader);
			} catch (ClassNotFoundException e) {
				getLog().error("Unable to locate class to load nodes with "+cNodeLoader+"\n"+e);
			}
		
			List<CNode> cNodes = null;
			if( cnhl != null){
				String cNodeLoaderClassOptions = config.getString("cnode.loader.class.options");
				try {
					JSONObject hlcOptions = new JSONObject(cNodeLoaderClassOptions);
					CNodeLoader i = null;
					try {
						i = cnhl.newInstance();
						i.init(hlcOptions);
						cNodes = i.loadCNodes(this,failoverFetch,accessRoutes);
					} catch (InstantiationException e) {
						getLog().error("Unable to instantiate class to load nodes with "+cNodeLoader+"\n"+e);
					} catch (IllegalAccessException e) {
						getLog().error("Unable to instantiate class to load nodes with "+cNodeLoader+"\n"+e);
					}
				} catch (JSONException e) {
					getLog().error("Property file does not contain valid json\n"+cNodeLoaderClassOptions+"\n"+e);
				}
			}
			
			if(cNodes != null){
				for(CNode c: cNodes){
					c.synchronizeWithNetwork(true,true,true,false);
					addToPool(c.getMetaCNodeGUID(),c);
				}
			}
			
			if(poolSize != null){
				getLog().error("PoolSize should be null if you are using local config to load the pool");
			}
			else{
				setPoolSize((long) cNodes.size());
			}
			
		}
		
		launchCNodePoolUpdateThread();
		
		return(this);
	}
	
	
	static class CNodePoolKeySetVisitor extends KyotoCabinetVisitor<String, CNode>{
		
		public HashSet<String> keySet = new HashSet<String>();
		
		@Override
		public Pair<Response, CNode> visit_full(String key, CNode c) {
			keySet.add(key);
			return new Pair<Response,CNode>(KyotoCabinetVisitor.Response.NOP,null);
		}
	 }
	
	public HashSet<String> getPoolKeySet(){
		CNodePoolKeySetVisitor v = new CNodePoolKeySetVisitor();
		db.iterate(v,true);
		return(v.keySet);
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

		CNodePool cNPool = new CNodePool(new KyotoCabinet<String, CNode>());
		cNPool.launchCNodePool(config,1L,directorySeed,urls);
		Globals.getGlobals().addQuittables(cNPool);
		
		/* Create the webserver to catch rest action*/
		WebServer ws = null;
		try {
			HashMap<String, HandlerAbstract> requestHandlerRegistry = new HashMap<String,HandlerAbstract>();
			HandlerAbstract handler =  new HandlerVersion();
			requestHandlerRegistry.put("",handler);
			requestHandlerRegistry.put("version",handler);
			requestHandlerRegistry.put("predict",new HandlerCNodePrediction(cNPool));
			requestHandlerRegistry.put(null, new HandlerFileServer(edu.uci.ics.luci.cacophony.CacophonyGlobals.class,"/wwwNode/"));
			
			RequestDispatcher requestDispatcher = new RequestDispatcher(requestHandlerRegistry);
			ws = new WebServer(requestDispatcher, Integer.valueOf((String)getConfig(clo,g.getConfig(),"port")), false, new AccessControl());
			ws.start();
			Globals.getGlobals().addQuittables(ws);
		} catch (RuntimeException e) {
			getLog().fatal("Couldn't start webserver"+e);
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
