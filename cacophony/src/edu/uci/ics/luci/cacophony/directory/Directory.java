package edu.uci.ics.luci.cacophony.directory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.KeyIterator;
import me.prettyprint.cassandra.service.OperationType;
import me.prettyprint.cassandra.service.template.ColumnFamilyResult;
import me.prettyprint.cassandra.service.template.ColumnFamilyUpdater;
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ConsistencyLevelPolicy;
import me.prettyprint.hector.api.HConsistencyLevel;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLPropertiesConfiguration;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;

import edu.uci.ics.luci.cacophony.CacophonyGlobals;
import edu.uci.ics.luci.cacophony.api.HandlerShutdown;
import edu.uci.ics.luci.cacophony.api.HandlerVersion;
import edu.uci.ics.luci.cacophony.api.directory.HandlerDirectoryNamespace;
import edu.uci.ics.luci.cacophony.api.directory.HandlerDirectoryServers;
import edu.uci.ics.luci.cacophony.api.directory.HandlerNodeAssignment;
import edu.uci.ics.luci.cacophony.api.directory.HandlerNodeCheckin;
import edu.uci.ics.luci.cacophony.api.directory.HandlerNodeList;
import edu.uci.ics.luci.cacophony.api.directory.WebServerWarmUp;
import edu.uci.ics.luci.cacophony.directory.nodelist.CNodeReference;
import edu.uci.ics.luci.cacophony.directory.nodelist.MetaCNode;
import edu.uci.ics.luci.cacophony.directory.nodelist.NodeListLoader;
import edu.uci.ics.luci.utility.Globals;
import edu.uci.ics.luci.utility.Quittable;
import edu.uci.ics.luci.utility.datastructure.Pair;
import edu.uci.ics.luci.utility.webserver.AccessControl;
import edu.uci.ics.luci.utility.webserver.HandlerAbstract;
import edu.uci.ics.luci.utility.webserver.RequestDispatcher;
import edu.uci.ics.luci.utility.webserver.WebServer;
import edu.uci.ics.luci.utility.webserver.handlers.HandlerFileServer;


public class Directory implements Quittable{
	
	private static final Integer CASSANDRA_PORT=9160;
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(WebServer.class);
		}
		return log;
	}
	
	private String KEYSPACE = "CacophonyKeyspaceV1_0s";
	private StringSerializer stringSerializer = null;
	private LongSerializer longSerializer = null;
	
	private Cluster cluster = null;
	private Keyspace ksp = null;
	
	private String directoryNamespace = null;
	
	private ThriftColumnFamilyTemplate<String, String> directoryServerTemplate = null;
	private final static String DIRECTORY_SERVER_CF="directory_server";
	private ThriftColumnFamilyTemplate<String, String> cacophonyNodeTemplate = null;
	private final static String C_NODE_LIST_CF="cnode_list";
	
	public final static long ONE_SECOND = 1000L;
	public final static long ONE_MINUTE = 60 * ONE_SECOND;
	public final static long FIVE_MINUTES  = 5 * ONE_MINUTE;
	public final static long ONE_HOUR = 60 * ONE_MINUTE;
	public final static long ONE_DAY = 24 * ONE_HOUR;
	public Timer heartbeat;
	public Timer metaCNodeListCleaner;
	
	private boolean shuttingDown = false;
	
	public synchronized void setQuitting(boolean quitting) {
		if(shuttingDown == false){
			if(quitting == true){
				shuttingDown = true;
				if(heartbeat!= null){
					heartbeat.cancel();
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
	
	
	static final class MyConsistencyLevel implements ConsistencyLevelPolicy {

		@Override
		public HConsistencyLevel get(OperationType op) {
		   switch (op){
		      case READ:return HConsistencyLevel.QUORUM;
		      case WRITE: return HConsistencyLevel.QUORUM;
		      default: return HConsistencyLevel.QUORUM; //Just in Case
		   }
		}
		@Override
		public HConsistencyLevel get(OperationType op, String cfName) {
		   switch (op){
		      case READ:return HConsistencyLevel.QUORUM;
		      case WRITE: return HConsistencyLevel.QUORUM;
		      default: return HConsistencyLevel.QUORUM; //Just in Case
		   }
		}
		
	}

	
	
	public Directory(){
		Globals g = Globals.getGlobals();
		
		String url = null;
		try {
			if(System.getProperty("os.name").contains("Windows")){
				url = "127.0.0.1";
			}
			else{
				url = InetAddress.getLocalHost().getHostAddress();
				getLog().info("Connecting to Cassandra ring on: "+url);
			}
		} catch (UnknownHostException e1) {
			url ="127.0.0.1";
		}
		
		try{
			cluster = HFactory.getOrCreateCluster("CacophonyClusterV1_0",url+":"+Integer.toString(Directory.CASSANDRA_PORT));
		}
		catch(NoClassDefFoundError e){
			log.error("Make sure you are including libthrift.jar in theproject");
			throw e;
		}
		
		if((g == null) || (g.isTesting())){
			KEYSPACE = "CacophonyKeyspaceV1_0s";
		}
		else{
			KEYSPACE = "CacophonyKeyspaceV1_0r";
		}
		
		ksp = HFactory.createKeyspace(KEYSPACE, cluster);
		if (ksp == null) {
			throw new RuntimeException("Unable to find keyspace");
		}
		ksp.setConsistencyLevelPolicy(new MyConsistencyLevel());
		
		stringSerializer = StringSerializer.get();
		if (stringSerializer == null) {
			throw new RuntimeException("Unable to get StringSerializer");
		}
		
		longSerializer = LongSerializer.get();
		if (longSerializer == null) {
			throw new RuntimeException("Unable to get LongSerializer");
		}
		
		directoryServerTemplate = new ThriftColumnFamilyTemplate<String,String>(ksp,
				DIRECTORY_SERVER_CF,
				stringSerializer,
				stringSerializer);
		
		cacophonyNodeTemplate = new ThriftColumnFamilyTemplate<String,String>(ksp,
				C_NODE_LIST_CF,
				stringSerializer,
				stringSerializer);
		
	}
	
	public String getDirectoryNamespace() {
		return directoryNamespace;
	}


	public void setDirectoryNamespace(String directoryNamespace) {
		if(this.directoryNamespace == null){
			this.directoryNamespace = directoryNamespace;
		}
		else{
			getLog().info("Doublesetting directory namespace: "+this.directoryNamespace+" -> "+directoryNamespace);
			this.directoryNamespace = directoryNamespace;
		}
	}


	public void startHeartbeat(){
		startHeartbeat(null,null,null,null);
	}
	
	public void startHeartbeat(Long delay, Long period){
		startHeartbeat(delay,period,null,null);
	}
	
	public void startHeartbeat(String guid){
		startHeartbeat(null,null,guid,null);
	}
	
	public void startHeartbeat(String guid,List<Pair<Long,String>> urls){
		startHeartbeat(null,null,guid,urls);
	}
	
	/**
	 * 
	 * @param delay How long to wait before first heartbeat goes out in milliseconds. Default is 0
	 * @param period How often to send a heartbeat in milliseconds. Default is 5 minutes
	 * @param guid Some canonical name for the directory, like "cloud2". Default is "Unknown Directory"
	 * @param urls A list of URLs with which to reference this directory with a number indicating preference order. Lower is more preferred.
	 *  Default is just what java thinks the host address is.
	 */
	public void startHeartbeat(Long delay,Long period,String guid, List<Pair<Long,String>> urls){
		
		if(delay == null){
			delay = 0L;
		}
		
		if(period == null){
			period = FIVE_MINUTES;
		}
		
		if(guid == null){
			guid = "Unknown Directory";
		}
		
		if(urls == null){
			urls = new ArrayList<Pair<Long,String>>();
		}
		
		final String localNamespace = getDirectoryNamespace();
		if(localNamespace == null){
			getLog().error("Set the namespace before starting the heartbeat");
		}
		
		final String localGUID = guid;
		
		if(urls.size() == 0){
			String url = null;
			try {
				if(System.getProperty("os.name").contains("Windows")){
					url = "127.0.0.1";
				}
				else{
					try {
						url = InetAddress.getLocalHost().getHostName();
					} catch (UnknownHostException e) {
						url = InetAddress.getLocalHost().getHostAddress();
					}
				}
			} catch (UnknownHostException e1) {
				url ="127.0.0.1";
			}
			urls.add(new Pair<Long,String>(0L,url));
		}
		
		
	   	final JSONObject localData = new JSONObject();
	   	try {
			localData.put("namespace", localNamespace);
			JSONArray servers = new JSONArray();
			for(Pair<Long,String> x:urls){
				JSONObject bar = new JSONObject();
				bar.put("priority_order",x.getFirst());
				bar.put("url",x.getSecond());
				servers.put(bar);
			}
			localData.put("access_routes", servers);
	   	} catch (JSONException e) {
	   		getLog().fatal("Something is wrong with JSON:"+localNamespace+"\n"+e);
	   	}
		
		/* If we already started a heartbeat cancel it and restart */
		if(heartbeat != null){
			heartbeat.cancel();
		}
		
		try {
			getLog().info("Starting a Directory -> Cassandra heartbeat for: "+localData.toString(1));
		} catch (JSONException e1) {
		}
		
		/*Set up the heartbeat to go every 5 minutes;*/
		 heartbeat = new Timer(true);
		 heartbeat.scheduleAtFixedRate(
			    new TimerTask(){
			    	
					@Override
			    	public void run(){
						if(shuttingDown){
							heartbeat.cancel();
							return;
						}
						/* Update this nodes heartbeat */
			    		ColumnFamilyUpdater<String, String> updater = directoryServerTemplate.createUpdater(localGUID);
			    		String now = Long.toString(System.currentTimeMillis());
			    		try {
							localData.put("heartbeat",now);
						} catch (JSONException e) {
							getLog().fatal("Something is wrong with JSON:"+now+"\n"+e);
						}
						updater.setString("json_data", localData.toString());
						directoryServerTemplate.update(updater);
						
						
						/* Clean up old heartbeats */
						KeyIterator<String> keyIterator = new KeyIterator<String>(ksp, DIRECTORY_SERVER_CF,stringSerializer);
						
						for(String keyI: keyIterator){
							String jsonData = null;
							try {
						    	ColumnFamilyResult<String, String> res = directoryServerTemplate.queryColumns(keyI);
								jsonData = res.getString("json_data");
								if(jsonData != null){
									JSONObject jsonObject = new JSONObject(jsonData);
									Long heartbeat  = jsonObject.getLong("heartbeat");
									if((heartbeat == null)||(heartbeat < System.currentTimeMillis() - ONE_DAY)){
										directoryServerTemplate.deleteRow(keyI);
									}
								}
							} catch (HectorException e) {
								getLog().error("Problem getting a Directory Server List:\n"+e);
							} catch (JSONException e) {
								getLog().error("Bad JSON Data in Cassandra ring:\n"+jsonData+"\n"+e);
							}
						}
					}
					}, delay, period);
	}
	
	public Long getHeartbeat(String key){
		Long ret = null;
		
		KeyIterator<String> keyIterator = new KeyIterator<String>(ksp, DIRECTORY_SERVER_CF,stringSerializer);
		
		for(String keyI: keyIterator){
			try {
			    if(keyI.equals(key)){
			    	ColumnFamilyResult<String, String> res = directoryServerTemplate.queryColumns(keyI);
			    	String jsonString = res.getString("json_data");
			    	JSONObject jsonObject = new JSONObject(jsonString);
			    	ret = jsonObject.getLong("heartbeat");
			    }
			} catch (HectorException e) {
				getLog().error("Problem getting a Heartbeat:\n"+e);
			} catch (JSONException e) {
				getLog().error("Problem with JSON getting a Heartbeat:\n"+e);
			}
		}
		return ret;
	}
	
	
	public void startMetaCNodeListCleaner(){
		startMetaCNodeListCleaner(null,null);
	}
	
	
	public void startMetaCNodeListCleaner(Long delay,Long period){
		
		if(delay == null){
			delay = 0L;
		}
		
		if(period == null){
			period = ONE_DAY;
		}
		
		/* If we already started a cleaner cancel it and restart */
		if(metaCNodeListCleaner != null){
			metaCNodeListCleaner.cancel();
		}
		
		getLog().info("Starting a MetaCNodeList Cleaner");
		
		 metaCNodeListCleaner = new Timer(true);
		 metaCNodeListCleaner.scheduleAtFixedRate(
			    new TimerTask(){
			    	
					@Override
			    	public void run(){
						
						synchronized(cacheLock){
							
							refreshAllDataInCache();
							Collection<MetaCNode> mcs = new ArrayList<MetaCNode>();
							mcs.addAll(cache.values());
							
							for(MetaCNode mc:mcs){
							
								boolean deleteMC = true;
							
								List<CNodeReference> deleteCNR = new ArrayList<CNodeReference>();
								if(mc.getCNodeReferences() != null){
									for(CNodeReference cnr:mc.getCNodeReferences().values()){
										if(cnr != null){
											if(cnr.getLastHeartbeat() != null){
												/* If one of the cnodes has checked in in the last 24 hours then keep
												 * the meta node around
												 */
												if(cnr.getLastHeartbeat() > (System.currentTimeMillis()- ONE_DAY)){
													deleteMC = false;
												}
												else{
													deleteCNR.add(cnr);
												}
											}
											else{
												deleteCNR.add(cnr);
											}
										}
									}
								}
							
								/* Delete the cnodes with no heartbeat from the local copy */
								for(CNodeReference cnr:deleteCNR){
									mc.getCNodeReferences().remove(cnr.getCNodeGuid());
								}
							
								/* If there were no recent CNode checkins and the MetaNode was created more than
								 * one day ago, then delete it from the backing store, otherwise consider updating it.
								 */
								if( (deleteMC && (mc.getCreationTime() == null)) ||
									(deleteMC && (mc.getCreationTime() < (System.currentTimeMillis() - ONE_DAY)))){
									removeDatumThroughCache(mc.getGuid());
								}
								else{
									/* If there was a change then update the row */
									if(deleteCNR.size() != 0){
										writeDatumThroughCache(mc.getGuid(),mc);
									}
								}
							}
						}
					}
					}, delay, period);
	}
	
	/**
	 * This will remove all directory servers from the backing store.
	 * This is an immediate removal unlike the normal scrubbing that a directory does based on heartbeats that aren't refreshed.
	 * This is really only intended to be used when doing tests
	 */
	public void removeAllServers(){
		//while(getServers().size() > 0){
			KeyIterator<String> keyIterator = new KeyIterator<String>(ksp, DIRECTORY_SERVER_CF,stringSerializer);
		
			for(String keyI: keyIterator){
				try {
					System.out.println("Removing: "+keyI);
					directoryServerTemplate.deleteRow(keyI);
				} catch (HectorException e) {
					getLog().error("Problem getting a Directory Server List:\n"+e);
				}
			}
		
		//	try {
		//		Thread.sleep(1000);
		//	} catch (InterruptedException e) {
		//	}
		//}
		
	}
	
	
	
	
	public Map<String, JSONObject> getServers(){
		Map<String,JSONObject> ret = new HashMap<String,JSONObject>();
		
		KeyIterator<String> keyIterator = new KeyIterator<String>(ksp, DIRECTORY_SERVER_CF,stringSerializer);
		
		for(String keyI: keyIterator){
			String jsonData = null;
			try {
		    	ColumnFamilyResult<String, String> res = directoryServerTemplate.queryColumns(keyI);
				jsonData = res.getString("json_data");
				if(jsonData != null){
					JSONObject jsonObject = new JSONObject(jsonData);
					ret.put(keyI, jsonObject);
				}
			} catch (HectorException e) {
				getLog().error("Problem getting a Directory Server List:\n"+e);
			} catch (JSONException e) {
				getLog().error("Bad JSON Data in Cassandra ring:\n"+jsonData+"\n"+e);
			}
		}
		return ret;
	}
	
	
	
	public List<MetaCNode> getNodeList(){
		return(getNodeList(null));
	}
	
	
	private void removeDatumThroughCache(String metaCNodeID) {

		synchronized(cacheLock){
			/* Remove it in from the backing store */
			cacophonyNodeTemplate.deleteRow(metaCNodeID);
			
			/* Remove it in from the cache */
			cache.remove(metaCNodeID);
		}
		
	}
	
	private void writeDatumThroughCache(String metaCNodeID, MetaCNode mc) {

		synchronized(cacheLock){
			/* Put it in the backing store */
			ColumnFamilyUpdater<String, String> updater = this.cacophonyNodeTemplate.createUpdater(metaCNodeID);
			updater.setString("json_data", mc.toJSONObject().toString());
			this.cacophonyNodeTemplate.update(updater);
			
			/* Put it in the cache */
			cache.put(metaCNodeID, mc);
		}
		
	}



	private void refreshDatumInCache(String id){
		String jsonData = null;
		try {
	    	ColumnFamilyResult<String, String> res = this.cacophonyNodeTemplate.queryColumns(id);
			jsonData = res.getString("json_data");
			JSONObject jsonObject = null;
			if(jsonData != null){
				jsonObject = new JSONObject(jsonData);
			}
			/* Put the updated info in the cache */
			synchronized(cacheLock){
				cache.put(id,MetaCNode.fromJSONObject(jsonObject));
			}
		} catch (HectorException e) {
			getLog().error("Problem getting a c node list:\n"+e);
		} catch (JSONException e) {
			getLog().error("Bad JSON Data in Cassandra ring:\n"+jsonData+"\n"+e);
		}
	}	
	
	
	
	private void refreshAllDataInCache(){
		KeyIterator<String> keyIterator = new KeyIterator<String>(ksp, C_NODE_LIST_CF,stringSerializer);
		
		for(String keyI: keyIterator){
			refreshDatumInCache(keyI);
		}
		
	}
	
	Long cacheTimeout = 0L;
	Map<String,MetaCNode> cache = new TreeMap<String,MetaCNode>();
	Object cacheLock = new Object();
	public List<MetaCNode> getNodeList(Set<String> guids){
		List<MetaCNode> ret = new ArrayList<MetaCNode>();
		
		synchronized(cacheLock){
		/* If we are asking for everything then use cached copy */
			if(guids == null){
				if(cacheTimeout < (System.currentTimeMillis() - FIVE_MINUTES)){
					cache.clear();
					refreshAllDataInCache();
					cacheTimeout = System.currentTimeMillis();
				}
		
				ret.addAll(cache.values());
			}
			else{
				for(String id:guids){
					refreshDatumInCache(id);
					ret.add(cache.get(id));
				}
			}
		}
		
		return ret;
	}
	
	protected void setNodeList(NodeListLoader i) {
		List<MetaCNode> map = i.loadNodeList();
		
		for( MetaCNode e : map){
			ColumnFamilyUpdater<String, String> updater = this.cacophonyNodeTemplate.createUpdater(e.getGuid());
			
			updater.setString("json_data", e.toJSONObject().toString());
			this.cacophonyNodeTemplate.update(updater);
		}
		
		getLog().info("Loaded "+map.size()+" CNodes");
	}
	
	
	public void updateMetaCNode(String metaCNodeID, String cNodeGUID, Long heartbeat){
		CNodeReference cnr = new CNodeReference();
		cnr.setMetaCNodeGuid(metaCNodeID);
		cnr.setCNodeGuid(cNodeGUID);
		cnr.setLastHeartbeat(heartbeat);
		updateMetaCNode(cnr);
	}
		
	public void updateMetaCNode(CNodeReference cnr){
		
		if(cnr.getMetaCNodeGuid() == null){
			return;
		}
		if(cnr.getCNodeGuid() == null){
			return;
		}
		
		synchronized(cacheLock){
			refreshDatumInCache(cnr.getMetaCNodeGuid());
			MetaCNode mc = cache.get(cnr.getMetaCNodeGuid());
		
			try {
				Map<String, CNodeReference> cnrs = mc.getCNodeReferences();
				if(cnrs == null){
					cnrs = new TreeMap<String,CNodeReference>();
				}
				
				CNodeReference cr = new CNodeReference(cnr);
				cnrs.put(cnr.getCNodeGuid(), cr);
				mc.setCNodeReferences(cnrs);
				
				writeDatumThroughCache(cnr.getMetaCNodeGuid(), mc);
			} catch (HectorException e) {
				getLog().error("Problem updating a c node in cassandra:"+cnr.getMetaCNodeGuid()+"\n"+e);
			}
		}
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
			
			fl = new FlaggedOption("server.guid")
					.setStringParser(JSAP.STRING_PARSER)
					.setRequired(false) 
					.setShortFlag('g') 
					.setLongFlag("server.guid");
  
			fl.setHelp("What is a guid to identify this server by?");
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
            System.err.println("Usage: java " + Directory.class.getName());
            System.err.println("                " + jsap.getUsage());
            System.err.println();
            System.err.println(jsap.getHelp());
            System.err.println();
            throw new InvalidParameterException("Unable to parse command line");
        }

		return config;
	}
	
	public void initializeDirectory() {
		final String defaultFileName = "cacophony.directory.properties";
		XMLPropertiesConfiguration config = null;
		try {
			config = new XMLPropertiesConfiguration(defaultFileName);
			initializeDirectory(config);
		} catch (ConfigurationException e) {
			getLog().error("Unable to initialize directory with "+defaultFileName);
		} catch (ClassNotFoundException e) {
			getLog().error("Unable to initialize directory with "+defaultFileName);
		}
	}



	@SuppressWarnings("unchecked")
	public void initializeDirectory(XMLPropertiesConfiguration config) throws ClassNotFoundException {
		if(config == null){
			getLog().warn("Called with null config, using default");
			initializeDirectory();
			return;
		}
		
		/* Launch Directory Node */
		String namespace = config.getString("namespace");
		setDirectoryNamespace(namespace);
		
		String nodeListLoader = config.getString("nodelist.loader.class");
		if(nodeListLoader == null){
			throw new ClassNotFoundException("You need to specify a nodelist.loader.class in the properties file: "+config.getBasePath());
		}
		Class<? extends NodeListLoader> c = null;
		try {
			 c = (Class<? extends NodeListLoader>) Class.forName(nodeListLoader);
		} catch(ClassCastException e){
			getLog().error("Class does not extend NodeListLoader "+nodeListLoader);
		} catch (ClassNotFoundException e) {
			getLog().error("Unable to locate class to load nodes with "+nodeListLoader+"\n"+e);
		}
		
		if( c != null){
			String nodeListLoaderOptions = config.getString("nodelist.loader.class.options");
			try {
				JSONObject nllOptions = new JSONObject(nodeListLoaderOptions);
				NodeListLoader i = null;;
				try {
					i = c.newInstance();
					i.init(nllOptions);
					setNodeList(i);
				} catch (InstantiationException e) {
					getLog().error("Unable to instantiate class to load nodes with "+nodeListLoader+"\n"+e);
				} catch (IllegalAccessException e) {
					getLog().error("Unable to instantiate class to load nodes with "+nodeListLoader+"\n"+e);
				}
			} catch (JSONException e) {
				getLog().error("Property file does not contain valid json\n"+nodeListLoaderOptions+"\n"+e);
			}
			finally{}
		}
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



	public static void main(String[] args) {
		
		/*Set the thread name for error reporting */
		Thread.currentThread().setName(Directory.class.getName());
			
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
		
		String guid = (String)getConfig(clo,g.getConfig(),"server.guid");
		
		Directory directory = new Directory();
		Globals.getGlobals().addQuittables(directory);
		
		try {
			directory.initializeDirectory(config);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			log.warn("Could not initialize directory");
		}
		
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
		
		/*Start the heart beats */
		directory.startHeartbeat(guid,urls);
		
		/*Start the housecleaning*/
		directory.startMetaCNodeListCleaner();
		
		/* Set up webserver to access the directory */
		
		WebServer ws = null;
		try {
			HashMap<String, HandlerAbstract> requestHandlerRegistry = new HashMap<String,HandlerAbstract>();
			
			requestHandlerRegistry.put(null, new HandlerFileServer(edu.uci.ics.luci.cacophony.CacophonyGlobals.class,"/www/"));
			
			HandlerAbstract handler =  new HandlerVersion();
			requestHandlerRegistry.put("",handler);
			requestHandlerRegistry.put("version",handler);
			
			requestHandlerRegistry.put("servers",new HandlerDirectoryServers(directory));
			requestHandlerRegistry.put("namespace",new HandlerDirectoryNamespace(directory));
			requestHandlerRegistry.put("nodes",new HandlerNodeList(directory));
			requestHandlerRegistry.put("node_assignment",new HandlerNodeAssignment(directory));
			requestHandlerRegistry.put("node_checkin",new HandlerNodeCheckin(directory));
			
			requestHandlerRegistry.put("shutdown",new HandlerShutdown());
			
			RequestDispatcher requestDispatcher = new RequestDispatcher(requestHandlerRegistry);
			ws = new WebServer(requestDispatcher, Integer.valueOf((String) getConfig(clo,g.getConfig(),"port")), false, new AccessControl());
			ws.start();
			Globals.getGlobals().addQuittables(ws);
		} catch (RuntimeException e) {
			getLog().error("We couldn't get a webserver up and running!\n"+e);
			if(ws != null){
				ws.setQuitting(true);
			}
			g.setQuitting(true);
		}
		
		if(ws != null){
			WebServerWarmUp.go(ws, Integer.valueOf((String) getConfig(clo,g.getConfig(),"port")),"http://localhost");
			if(ws.getQuitting()){
				getLog().info("Warm-up failed, shutting down");
				g.setQuitting(true);
			}
		}
		
		getLog().info("\nDone in "+Directory.class.getCanonicalName()+" main()\n");
		
	}

}
