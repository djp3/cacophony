package edu.uci.ics.luci.cacophony.node;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.xpath.XPathExpressionException;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;
import net.minidev.json.JSONValue;

import org.apache.log4j.Logger;

import weka.classifiers.Evaluation;
import weka.classifiers.RandomizableIteratedSingleClassifierEnhancer;
import weka.classifiers.meta.Bagging;
import weka.core.Instance;
import weka.core.Instances;
import weka.experiment.InstanceQuery;
import weka.filters.Filter;
import edu.uci.ics.luci.cacophony.CacophonyGlobals;
import edu.uci.ics.luci.cacophony.api.CacophonyRequestHandlerHelper;
import edu.uci.ics.luci.cacophony.directory.nodelist.CNodeReference;
import edu.uci.ics.luci.util.ExtractDataFromHTML;
import edu.uci.ics.luci.util.ExtractDataFromJSON;
import edu.uci.ics.luci.util.FailoverFetch;
import edu.uci.ics.luci.utility.CalendarCache;
import edu.uci.ics.luci.utility.Quittable;
import edu.uci.ics.luci.utility.datastructure.Pair;

public class CNode implements Quittable,Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4246341487110473130L;
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(CNode.class);
		}
		return log;
	}
	public final static long ONE_SECOND = 1000L;
	public final static long ONE_MINUTE = 60 * ONE_SECOND;
	public final static long FIVE_MINUTES  = 5 * ONE_MINUTE;
	public final static long ONE_HOUR = 60 * ONE_MINUTE;
	public final static long ONE_DAY = 24 * ONE_HOUR;
	
	//TODO:These are algorithm specific and should somehow be moved into configuration
	public static final Integer zidIndex = 0;
	public static final Integer sundayIndex = 1;
	public static final Integer minutesSinceFourIndex = 8; 
	public static final Integer epochTimeIndex = 9;
	public static final Integer timeZoneIndex = 10;
	public static final Integer waitTimeIndex = 11;
	
	private int timeZoneOffset;
	private double maxWait;
	
	private Instance typicalInstance = null;
	private Instances typicalInstances = null;
	private Instances graphingTestSet = null;    
	private Canonicalize filter = null;
	private Evaluation evaluation = null;
	private Integer lastTrainingSetHash = null;
	private Long lastModelBuildTime = null;
	private RandomizableIteratedSingleClassifierEnhancer model = null;

	private FailoverFetch failoverFetch;
	private String metaCNodeGUID = null;
	private String nodeName = null;
	private InstanceQuery trainingQuery = null;
	/*Warning this needs to be set when deserialized! */
	private transient CNodePool parentPool = null;  
	private List<Pair<Long, String>> baseUrls = null;
	
	private String configurationString = null;
	transient private JSONObject configuration = null;
	
	public String cNodeGuid = null;
	private Random random = null;
	
	/* Does the node manage synchronizing with the world itself? */
	private boolean selfSynchronize = false;
	TreeSet<String> heartbeatList = null;
	private boolean shuttingDown = false;
	
	/* The last data that was retrieved by this CNode*/
	private transient Object lastData = null;
	private transient Long lastDataUpdateTime = null;
	
	/* A list of times between updates for calculating statistics */
	private List<Long> durationTimes = null;
	private int durationTimesMaxSize = 1000;
	
	/** Calculate the features that don't need a CNode to resolve them
	 * 
	 * @param feature
	 * @return "time": milliseconds since epoch 
	 * @return "weekend": boolean which is true if it is currently the weekend in Los Angeles 
	 */
	public static Object calculateFeature(String feature) {
		if(feature.equals("time")){
			return System.currentTimeMillis();
		}
		else if(feature.equals("weekend")){
			return ((CalendarCache.C_LosAngeles.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) || (CalendarCache.C_LosAngeles.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY));
		}
		else{
			getLog().error("Unknown general feature:"+feature);
		}
		return null;
	}

	/**
	 * setFailoverFetch,setParentPool, setBaseUrls, setNodeId, and setNodeName should be called before launching
	 */
	public CNode(){
		this.random = new Random();
		durationTimes = new ArrayList<Long>();
	}
	
	public synchronized void setCNodeGuid(){
		this.cNodeGuid = "Bagged IbK, "+getMetaCNodeGUID()+","+getNodeName()+","+random.nextInt(Integer.MAX_VALUE);
	}
	
	public synchronized String getCNodeGuid() {
		return cNodeGuid;
	}
	
	public synchronized void setSelfSynchronize(boolean selfSynchronize){
		this.selfSynchronize = selfSynchronize;
	}
	
	public synchronized boolean getSelfSynchronize() {
		return this.selfSynchronize;
	}

	public synchronized String getMetaCNodeGUID() {
		return metaCNodeGUID;
	}


	public synchronized void setMetaCNodeGUID(String guid) {
		this.metaCNodeGUID = guid;
		setCNodeGuid();
	}


	public synchronized String getNodeName() {
		return nodeName;
	}


	public synchronized void setNodeName(String nodeName) {
		this.nodeName = nodeName;
		setCNodeGuid();
	}


	public synchronized InstanceQuery getTrainingQuery() {
		return trainingQuery;
	}


	public synchronized void setTrainingQuery(InstanceQuery trainingQuery) {
		this.trainingQuery = trainingQuery;
	}


	public synchronized FailoverFetch getFailoverFetch() {
		return failoverFetch;
	}


	public synchronized void setFailoverFetch(FailoverFetch failoverFetch) {
		this.failoverFetch = failoverFetch;
	}


	public synchronized CNodePool getParentPool() {
		return parentPool;
	}


	public synchronized void setParentPool(CNodePool parentPool) {
		this.parentPool = parentPool;
	}


	public synchronized List<Pair<Long, String>> getBaseUrls() {
		return baseUrls;
	}


	public synchronized void setBaseUrls(List<Pair<Long, String>> baseUrls) {
		this.baseUrls = baseUrls;
	}


	public synchronized JSONObject getConfiguration() {
		if(configuration == null){
			if(configurationString != null){
				try {
					configuration = (JSONObject) JSONValue.parse(configurationString);
				} catch (ClassCastException e) {
				}
			}
		}
		return configuration;
	}

	public synchronized void setConfiguration(String configurationString) {
		this.configurationString = configurationString;
		try {
			configuration = (JSONObject) JSONValue.parse(configurationString);
		} catch (ClassCastException e) {
		}
	}

	private void setLastTrainingSetHash(int hashCode) {
		this.lastTrainingSetHash = hashCode;
	}

	public synchronized Integer getLastTrainingSetHash() {
		return lastTrainingSetHash;
	}
	
	private void setLastModelBuildTime(long time) {
		this.lastModelBuildTime = time;
	}

	public synchronized Long getLastModelBuildTime() {
		return lastModelBuildTime;
	}

	public synchronized void getANewConfiguration() {
		
		try {

			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version", CacophonyRequestHandlerHelper.getAPIVersion());
			params.put("namespace", parentPool.getNamespace());

			JSONObject response = failoverFetch.fetchJSONObject("/node_assignment", false, params, 30 * 1000);
			if(response == null){
				getLog().error("Unable to get a node assignment!");
			}
			else if(response.get("error") == null){
				getLog().error("Bad node assignment format received!");
			}
			else{
				try {
					String error = null;
					try{
						error = (String)response.get("error");
					}
					catch(ClassCastException e){
						getLog().error("Unable to find error report");
					}
					if((error != null) && (error.equals("false"))){
						String node_id = null;
						try{
							node_id = (String)response.get("node_id");
						}
						catch(ClassCastException e){
						}
						
						this.setMetaCNodeGUID(node_id);
						
						String name = null;
						try{
							name = (String)response.get("name");
						}
						catch(ClassCastException e){
						}
						this.setNodeName(name);
							
						InstanceQuery trainingQuery;
						JSONObject configuration = null;
						try{
							configuration = (JSONObject)response.get("node_configuration");
						}
						catch(ClassCastException e){
						}
						
						if(configuration != null){
							this.setConfiguration(configuration.toString());
							String doTraining = null;
							try{
								doTraining = (String)configuration.get("doTraining");
							}
							catch(ClassCastException e){
							}
							if((doTraining != null) && (doTraining.equals("true"))){
								String zid = null;
								try {
									trainingQuery = new InstanceQuery();
									trainingQuery.setUsername((String)configuration.get("username"));
									trainingQuery.setPassword((String)configuration.get("password"));
									trainingQuery.setDatabaseURL("jdbc:mysql://"+((String)configuration.get("databaseDomain"))+"/"+((String)configuration.get("database")));
									String trainingQueryString = (String)configuration.get("trainingQuery");
									zid = (String)configuration.get("node_id");
									trainingQuery.setQuery(trainingQueryString.replaceAll("_NODE_ID_", zid));
									this.setTrainingQuery(trainingQuery);
								} catch (ClassCastException e) {
									getLog().error("Unable to parse cnode configureation: "+configuration);
								} catch (Exception e) {
									getLog().error("Unable to load cnode training using: "+zid);
								}
							}
							else{
								this.setTrainingQuery(null);
							}
						}
					}
				} catch (ClassCastException e) {
					getLog().error("Problem getting configuration:"+e);
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public synchronized void sendHeartbeat(){
		
		/* Sanity check the values */
		if(this.getMetaCNodeGUID() == null){
			getLog().error("Set the MetaCNodeGUID of the node before starting the heartbeat");
		}
		
		if(this.getCNodeGuid() == null){
			getLog().error("Set the CNodeGUID of the node before starting the heartbeat");
		}
		
		if(this.getNodeName() == null){
			getLog().error("Set the CNode nodeName of the node before starting the heartbeat");
		}
		
		if(this.getParentPool().getNamespace() == null){
			getLog().error("Set the namespace of the pool before starting the heartbeat");
		}
		
		if(this.getBaseUrls() == null){
			this.setBaseUrls(new ArrayList<Pair<Long,String>>());
		}
		
		if(this.getBaseUrls().size() == 0){
			String url = null;
			try {
				url = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e1) {
				url ="127.0.0.1";
			}
			this.getBaseUrls().add(new Pair<Long,String>(0L,url));
		}
		
		
		/* Build the reference to send */
		
		CNodeReference cnr = new CNodeReference();
					
		/* Set the identity */
		cnr.setMetaCNodeGuid(this.getMetaCNodeGUID());
		cnr.setCNodeGuid(this.getCNodeGuid());

		/* Update this node's heartbeat */
		cnr.setLastHeartbeat(System.currentTimeMillis());
					
		Set<Pair<Long, String>> accessRoutes = new TreeSet<Pair<Long, String>>();
					
		for(Pair<Long,String> x:this.getBaseUrls()){
			try{
				Pair<Long,String> p = new Pair<Long,String>(x.getFirst(),x.getSecond()+"/index.html?node="+URLEncoder.encode(this.getMetaCNodeGUID(),"UTF-8")+"&name="+URLEncoder.encode(this.getNodeName(),"UTF-8")+"&namespace="+URLEncoder.encode(this.getParentPool().getNamespace(),"UTF-8"));
				accessRoutes.add(p);
			} catch (UnsupportedEncodingException e) {
				getLog().fatal("Something is wrong with URL Making\n"+e);
			}
		}
		cnr.setAccessRoutesForUI(accessRoutes);
					
		accessRoutes = new TreeSet<Pair<Long, String>>();
					
		for(Pair<Long,String> x:this.getBaseUrls()){
			try{
				Pair<Long,String> p = new Pair<Long,String>(x.getFirst(),x.getSecond()+"/predict?version="+URLEncoder.encode(CacophonyRequestHandlerHelper.getAPIVersion(),"UTF-8")+"&namespace="+URLEncoder.encode(this.getParentPool().getNamespace(),"UTF-8")+"&node="+URLEncoder.encode(this.getMetaCNodeGUID(),"UTF-8"));
				accessRoutes.add(p);
				} catch (UnsupportedEncodingException e) {
					getLog().fatal("Something is wrong with URL Making\n"+e);
				}
		}
		cnr.setAccessRoutesForAPI(accessRoutes);
					
		/* Send heartbeat */
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version", CacophonyRequestHandlerHelper.getAPIVersion());
			params.put("namespace", this.getParentPool().getNamespace());
			params.put("json_data", cnr.toJSONObject().toString());
			JSONObject response = failoverFetch.fetchJSONObject("/node_checkin", false, params, 30 * 1000);
						
			if(response != null){
				try {
					if(((String)response.get("error")).equals("true")){
						getLog().error("Something is wrong with JSON:"+response.toString(JSONStyle.NO_COMPRESS));
					}
				} catch (ClassCastException e) {
					getLog().error("Something is wrong with JSON:"+response.toString(JSONStyle.NO_COMPRESS)+"\n"+e);
				}
			}
		} catch (MalformedURLException e) {
			getLog().warn("Bad URL when the CNode tried to check in:"+e);
		} catch (IOException e) {
			getLog().warn("IO Exception when the CNode tried to check in:"+e);
		}
		catch (ClassCastException e) {
			getLog().warn("JSON Exception when the CNode tried to check in:"+e);
		}
	}
	

	/**
	 * This makes a test set specifically for a detailed prediction REST call
	 * @param timesToPredict
	 * @return
	 */
	protected synchronized Instances makeTestSet(JSONArray timesToPredict) {
		
		if(typicalInstance == null){
			getLog().error("typicalInstance is null. (Trying to predict without training first?)");
			return null;
		}
		
		if(typicalInstances == null){
			getLog().error("typicalInstances is null. (Trying to predict without training first?)");
			return null;
		}
		
	   	/* Get the conversion filter */
	   	Canonicalize canonicalizeFilter = this.getCanonicalizeFilter();
		if(canonicalizeFilter == null){
			getLog().error("canonicalizeFilter is null. (Trying to predict without training first?)");
			return null;
		}
		
		/* Create test data set */
	   	Instances proposedTestSet = new Instances(typicalInstances,0);
		
	   	try{
	   		for(int i = 0; i < timesToPredict.size(); i++){
	   			
	   			/* Get the first time we are predicting */
	   			Long originalTime = Long.parseLong(((String)timesToPredict.get(i)));
	   			
	   			/* Convert it to the standard and figure out the day of the week */
	   			Long t = transformTimeForCalendar(getTimeZoneOffset(), originalTime);
	   			
	   			Calendar calendar = CacophonyGlobals.getGlobals().getCalendar(CalendarCache.TZ_GMT);
	   			calendar.setTimeInMillis(t);
	   			
	   			/* Translate the Calendar Day Of the Week (SUNDAY = 1) to SQL query (SUNDAY = 0) */
	   			int dayOfTheWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
	   		
	   			/* Set the fields */
	   			Instance c = (Instance) typicalInstance.copy();
	   			
	   			c.setValue(zidIndex, this.getMetaCNodeGUID());
	   			
   				for(int k = 0;k < 7;k++){
   					if(dayOfTheWeek == k){
   						c.setValue(sundayIndex+k, canonicalizeFilter.toCanonical(sundayIndex+k,1.0));
   					}
   					else{
   						c.setValue(sundayIndex+k, canonicalizeFilter.toCanonical(sundayIndex+k,0.0));
   					}
   				}
   				
   				int minutesSinceFour = calculateMinutesSinceMidnight(calendar);
   				c.setValue(minutesSinceFourIndex, canonicalizeFilter.toCanonical(minutesSinceFourIndex,minutesSinceFour));
   				
   				c.setValue(epochTimeIndex,canonicalizeFilter.toCanonical(epochTimeIndex,originalTime/1000.0));
   				//c.setValue(epochTimeIndex,canonicalizeFilter.toCanonical(epochTimeIndex,System.currentTimeMillis()/1000.0));
   				
   				c.setValue(timeZoneIndex,getTimeZoneOffset());
   				
   				c.setValue(waitTimeIndex,canonicalizeFilter.toCanonical(waitTimeIndex,0));
   				proposedTestSet.add(c);
	   		}
	   	}
	   	catch(ClassCastException e){
			getLog().error("Unable to parse JSON:"+timesToPredict.toString(JSONStyle.NO_COMPRESS)+"\n"+e);
	   	}
	   	catch(Exception e){
			getLog().error("Unable to recreate a test set\n"+e);
			return null;
	   	}
	    	
	   	// set class attribute
	   	proposedTestSet.setClassIndex(waitTimeIndex);
	   	
	   	return(proposedTestSet);
	}

	protected static int calculateMinutesSinceMidnight(Calendar calendar) {
		int minutesSinceFour;
		{
			int h = calendar.get(Calendar.HOUR_OF_DAY);
			int m = calendar.get(Calendar.MINUTE);
			minutesSinceFour =  (h*60+m);
		}
		return minutesSinceFour;
	}

	protected static Long transformTimeForCalendar(Integer timeZoneOffset, Long t) {
		t = t + timeZoneOffset * ONE_HOUR;
		t = t - 4 * ONE_HOUR;
		return t;
	}
	
	protected static Long untransformTimeForCalendar(Integer timeZoneOffset, Long t) {
		t = t + 4 * ONE_HOUR;
		t = t - timeZoneOffset * ONE_HOUR;
		return t;
	}

	public synchronized Instances fetchTrainingSet() {
		Instances data = null;
		try {
			if(trainingQuery != null){
				data = trainingQuery.retrieveInstances();
			}
		} catch (Exception e) {
			getLog().error("Unable to get training instances from database\n"+e);
		}
		finally{
			if(trainingQuery != null){
				trainingQuery.close();
			}
		}
		return data;
	}


	private synchronized void setMaxWait(double maxWait) {
		this.maxWait = maxWait;
	}
	
	public synchronized int getTimeZoneOffset() {
		return timeZoneOffset;
	}

	public synchronized void setTimeZoneOffset(int timeZoneOffset) {
		this.timeZoneOffset = timeZoneOffset;
	}
	
	private synchronized void setGraphingTestSet(Instances localTestSet) {
		this.graphingTestSet = new Instances(localTestSet);
	}
	
	public synchronized Instances getGraphingTestSet() {
		return (this.graphingTestSet);
	}
	
	private synchronized Canonicalize getCanonicalizeFilter() {
		return(this.filter) ;
	}
	
	private synchronized void setCanonicalizeFilter(Canonicalize filter) {
		this.filter = filter;
	}
	
	private synchronized Evaluation getEvaluation() {
		return(this.evaluation) ;
	}
	
	private synchronized void setEvaluation(Evaluation evaluation) {
		this.evaluation = evaluation;
	}
	
	private synchronized void setModel(RandomizableIteratedSingleClassifierEnhancer bagging) {
		this.model = bagging;
		
	}
	
	
	public JSONObject getAccuracy() {
		JSONObject ret = new JSONObject();
		Evaluation eval = getEvaluation();
		if(eval != null){
			try {
				Long lmbt = getLastModelBuildTime(); 
				if((lmbt == null) || (lmbt == 0)){
					ret.put("last_model_build_time", "never");
				}
				else{
					Date build = new Date(lmbt);
					ret.put("last_model_build_time", DateFormat.getInstance().format(build));
				}
				ret.put("instances",eval.numInstances());
				ret.put("mean_absolute_error", eval.meanAbsoluteError());
				ret.put("root_mean_squared_error", eval.rootMeanSquaredError());
				ret.put("relative_absolute_error", eval.relativeAbsoluteError());
			} catch (Exception e) {
				getLog().error("Problem:"+e+"\n"+eval.toSummaryString());
			}
		}
		return(ret);
	}
	
	
	public synchronized Map<String, TreeSet<Pair<Long, Double>>> predict(JSONArray timesToPredict) {
		
		/* Check if we've got a filter */
		if(this.getCanonicalizeFilter() == null){
			return null;
		}
		Canonicalize canonicalizeFilter = getCanonicalizeFilter();
		
		/* Assign the set we are testing with */
		Instances localTestSet = null;
		if(timesToPredict != null){
			Instances foo = makeTestSet(timesToPredict);
			if(foo == null){
				return null;
			}
			localTestSet = foo;
		}
		else{
			localTestSet = getGraphingTestSet();
			
			/* Label instances */
			for(int i=0; i < localTestSet.numInstances(); i++){
				/* Set restaurant to predict */
				localTestSet.instance(i).setValue(zidIndex, this.getMetaCNodeGUID());
   				localTestSet.instance(i).setValue(timeZoneIndex,getTimeZoneOffset());
			}
		}
			
		
		HashMap<String, TreeSet<Pair<Long, Double>>> ret = new HashMap<String, TreeSet<Pair<Long, Double>>>();
		if(timesToPredict != null){
			ret.put("predictions",new TreeSet<Pair<Long,Double>>());
		}
		else{
			ret.put("sunday",new TreeSet<Pair<Long,Double>>());
			ret.put("monday",new TreeSet<Pair<Long,Double>>());
			ret.put("tuesday",new TreeSet<Pair<Long,Double>>());
			ret.put("wednesday",new TreeSet<Pair<Long,Double>>());
			ret.put("thursday",new TreeSet<Pair<Long,Double>>());
			ret.put("friday",new TreeSet<Pair<Long,Double>>());
			ret.put("saturday",new TreeSet<Pair<Long,Double>>());
		}
		
		/* Label instances */
		for(int i=0; i < localTestSet.numInstances(); i++){
			double clsLabel;
			try {
				/* Predict and unscale*/
				clsLabel = canonicalizeFilter.fromCanonical(waitTimeIndex, model.classifyInstance(localTestSet.instance(i)));
				clsLabel *= maxWait;
				
				/* Figure out what time that was for */
				Long time = null;
				if(timesToPredict == null){
					time = (long) canonicalizeFilter.fromCanonical(minutesSinceFourIndex, localTestSet.instance(i).value(minutesSinceFourIndex));
				}
				else{
					double foo = localTestSet.instance(i).value(epochTimeIndex);
					double baseTime = canonicalizeFilter.fromCanonical(epochTimeIndex, foo);
					time = Math.round(baseTime*1000.0);
				}
				
				
				/* Log it */
				Pair<Long,Double> foo = new Pair<Long,Double>(time,clsLabel);
				
				if(timesToPredict != null){
					ret.get("predictions").add(foo);
				}
				else{
					/* Put it in the right day */
					if(localTestSet.instance(i).value(1) == 1){
						ret.get("sunday").add(foo);
					}
					else if(localTestSet.instance(i).value(2) == 1){
						ret.get("monday").add(foo);
					}
					else if(localTestSet.instance(i).value(3) == 1){
						ret.get("tuesday").add(foo);
					}
					else if(localTestSet.instance(i).value(4) == 1){
						ret.get("wednesday").add(foo);
					}
					else if(localTestSet.instance(i).value(5) == 1){
						ret.get("thursday").add(foo);
					}
					else if(localTestSet.instance(i).value(6) == 1){
						ret.get("friday").add(foo);
					}
					else if(localTestSet.instance(i).value(7) == 1){
						ret.get("saturday").add(foo);
					}
				}
			} catch (Exception e) {
				getLog().error("Unable to make prediction: ("+i+")");
			}
		}
		return(ret);
	}
	
	
	@Override
	public synchronized boolean isQuitting(){
		return shuttingDown;
	}

	@Override
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
	
	public synchronized boolean modelOutdated(Instances trainingSet){
		if((trainingSet == null) || (getLastTrainingSetHash() == null) || (getLastTrainingSetHash() != trainingSet.hashCode())){
			return(true);
		}
		else{
			return(false);
		}
	}
	
	
	public synchronized void synchronizeWithNetwork(){
		synchronizeWithNetwork(true,true,true,true);
	}
	
	public synchronized void synchronizeWithNetwork(boolean checkForNewData, boolean rebuildModelIfNecessary, boolean testAccuracyOnSelf, boolean sendHeartbeat){
		
		Instances trainingSet = null;
		if(checkForNewData){
			getLog().info("Synchronizing checking for new data: "+this.getMetaCNodeGUID());
			
			//Build feature vector
			
			JSONObject targetInfo = (JSONObject) this.configuration.get("target");
			if(targetInfo != null){
				String format = "";
				String url = "";
				String dataPath = "";
				String regularExpression = "";
				String translatorClass = "";
				
				try{
					format = (String)targetInfo.get("format");
				}
				catch(ClassCastException e){
				}
				
				try{
					url = ((String)targetInfo.get("url"));
				}
				catch(ClassCastException e){
				}
				
				try{
					dataPath = ((String)targetInfo.get("data_path"));
				}
				catch(ClassCastException e){
				}
				
				try{
					regularExpression = ((String)targetInfo.get("regular_expression"));
				}
				catch(ClassCastException e){
				}
				
				try{
					translatorClass = ((String)targetInfo.get("translator_class"));
				}
				catch(ClassCastException e){
				}
				
				format = format.toLowerCase().trim();
				url = url.trim();
				dataPath = dataPath.trim();
				regularExpression = regularExpression.trim();
				translatorClass = translatorClass.trim();
				
				Translator<?> translator;
				try {
					translator = (Translator<?>) Class.forName(translatorClass).newInstance();
				}
				catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
					getLog().error("Unable to synchronize. There was a problem creating a new instance of the class " + translatorClass + "\n" + e);
					return;
				}
				
				try {
					Object targetData = null;
					if(format.equals("html")){
						targetData = ExtractDataFromHTML.fetchAndExtractData(url,dataPath,regularExpression,translator);
					}
					else if(format.equals("json")){
						targetData = ExtractDataFromJSON.fetchAndExtractData(url,dataPath,regularExpression,translator);
					}
					else{
						getLog().warn("Unrecognized CNode format: "+targetInfo);
					}
					
					// Collect durations between changes
					if(targetData != null){
						if((lastData == null) || (!targetData.equals(lastData))){
							Long now = System.currentTimeMillis();
							if(lastDataUpdateTime != null){
								Long duration = now - lastDataUpdateTime;
								if(duration > 0){
									durationTimes.add(duration);
									if(durationTimes.size() > this.durationTimesMaxSize){
										durationTimes.remove(0);
									}
								}
							}
							lastDataUpdateTime = now;
						}
						lastData = targetData;
					}
					
					
					/* Fetch features in parallel */
					int poolSize = 20;
					ExecutorService pool = Executors.newFixedThreadPool(poolSize);
					/* maps from namespace,feature to future that fetched the value */
					Map<Pair<String,String>,Future<?>> featureFutures = new HashMap<Pair<String,String>,Future<?>>();
					Map<Pair<String,String>,Object> features = new HashMap<Pair<String,String>,Object>();
					
					JSONArray featureSets = (JSONArray) this.configuration.get("features");
					if(featureSets != null){
						for(int i = 0 ; i < featureSets.size(); i++){
							String namespace = (String) ((JSONObject)featureSets.get(i)).get("namespace");
							if(namespace != null){
								namespace = (String) ((JSONObject)featureSets.get(i)).get("namespace");
								/* Check to make sure we can handle namespace of feature */
								if((namespace != null) && (!namespace.equals(this.getParentPool().getNamespace()))){
									getLog().error("We can only handle null namespace and namespaces that are the same as the CNode for features");
								}
								else{
									JSONArray names = (JSONArray) ((JSONObject)featureSets.get(i)).get("names");
									
									for(int j = 0; j< names.size(); j++){
										final String feature = (String) names.get(j);
										Pair<String, String> key = new Pair<String,String>(namespace,feature);
										Future<?> value = null;
										if(namespace == null){ //Internal functions
											value = pool.submit(new Callable<Object>(){
												@Override
												public Object call() throws Exception {
													return CNode.calculateFeature(feature);
												}
											});
										}
										else{ //fetch the feature
											value = pool.submit(new Callable<Object>(){
												@Override
												public Object call() throws Exception {
													// TODO: For Don: Not sure if failoverFetch should be passed to fetchAndExtractData or not. For now I'm commenting out the overload. -John
													//return ExtractDataFromJSON.fetchAndExtractData(failoverFetch,"/api/cnode_data","$.data","(.*)",new TranslatorIdentity());
													return ExtractDataFromJSON.fetchAndExtractData("/api/cnode_data","$.data","(.*)",new TranslatorIdentity());
												}
											});
										}
										featureFutures.put(key,value);
									}
								}
							}
						}
					}
					/* Wait for features to arrive */
					for(Entry<Pair<String, String>, Future<?>> pair:featureFutures.entrySet()){
						/* Block waiting for completion */
						Pair<String, String> key = pair.getKey();
						Future<?> future = pair.getValue();
						try {
							features.put(key, future.get());
						} catch (InterruptedException | ExecutionException e) {
							getLog().error("There was a problem retrieving the feature value for the future '" + future + "'\n" + e);
						}
					}

					//TODO: Store the features and the data in a tokyocabinet
					storeFeatures(features,targetData);
				} catch (MalformedURLException e) {
					getLog().error("The URL '" + url + "' is invalid\n" + e);
					return;
				} catch (XPathExpressionException e) {
					getLog().error("There was a problem with the XPath expression '" + dataPath + "'\n" + e);
				} catch (IOException e) {
					getLog().error("There was a problem fetching and extracting data for the URL '" + url + "'\n" + e);
				} catch (ClassCastException e) {
					getLog().error("There was a problem parsing the JSON for the URL '" + url + "'\n" + e);
				}
			}
			
			if(rebuildModelIfNecessary){
				if(modelOutdated(trainingSet)){
					//TODO: Clean this up eventually
					buildModel(testAccuracyOnSelf,trainingSet);
				}
			}
		}
		if((trainingSet == null) || (getLastModelBuildTime() != null)){
			if(sendHeartbeat){
				sendHeartbeat();
			}
		}
	}

	

	public synchronized void buildModel(boolean selfEval,Instances trainingData) {
		
		if(trainingData == null){
			getLog().warn("Can't build a model with no training data");
			return;
		}
		
		if(trainingData.numInstances() < 3){
			getLog().warn("It is beneath me to build a model with "+trainingData.numInstances()+" datapoints");
			return;
		}
		
		getLog().info("Training on "+trainingData.numInstances()+" instances");
		
		/*Keep track of the timezones for the restaurants*/
		Map<String, Integer> localTimeZoneOffsets = new HashMap<String,Integer>();
			
		/* Make all the wait times relative to the restaurant */
		Map<String, Double> localMaxWait = new HashMap<String,Double>();
	    for (int index = 0; index < trainingData.numInstances(); index++){
	    	Instance nextElement = trainingData.instance(index);
	    	String zid = nextElement.attributeSparse(zidIndex).value((int) Math.round(nextElement.value(zidIndex)));
	    	
	    	Integer timezoneOffset = (int)Math.round(nextElement.value(timeZoneIndex));
	    	if((timezoneOffset < -12)||(timezoneOffset > 12)){
	    		timezoneOffset = 0;
	    	}
	    	localTimeZoneOffsets.put(zid,timezoneOffset);
	    	
	    	
	    	Double wait = Double.valueOf(nextElement.value(waitTimeIndex));
	    	if(localMaxWait.containsKey(zid)){
	    		if(localMaxWait.get(zid) < wait){
	    			localMaxWait.put(zid,wait);
	    		}
	    	}
	    	else{
	    		localMaxWait.put(zid,wait);
	    	}
	    }
		    
	    for (int index = 0; index < trainingData.numInstances(); index++){
		   	Instance nextElement = trainingData.instance(index);
	    	String zid = nextElement.attributeSparse(zidIndex).value((int) Math.round(nextElement.value(zidIndex)));
		   	Double wait = Double.valueOf(nextElement.value(waitTimeIndex));
		   	Double oldMaxWait = localMaxWait.get(zid);
		   	if((wait > 0) && (oldMaxWait > 0)){
		   		trainingData.instance(index).setValue(waitTimeIndex,(wait/oldMaxWait));
		   	}
	    }
	    
	    if(localTimeZoneOffsets.keySet().size() > 1){
	    	getLog().fatal("Something unexpected happened.  I was only expecting training data from one zid"+localTimeZoneOffsets.keySet().toString());
	    }
	    
	    if(localMaxWait.keySet().size() > 1){
	    	getLog().fatal("Something unexpected happened.  I was only expecting training data from one zid"+localMaxWait.keySet().toString());
	    }
	    
	   	setTimeZoneOffset(localTimeZoneOffsets.get(this.getMetaCNodeGUID()));
	   	setMaxWait(localMaxWait.get(this.getMetaCNodeGUID()));
	    
	    /* Set the class index */
	    trainingData.setClassIndex(waitTimeIndex);
		    
	    /* Canonicalized the fields*/
		Canonicalize canonicalizeFilter = new Canonicalize();
	    Instances canonicalizeData;
		try {
			HashSet<Integer> rescale = new HashSet<Integer>();
			rescale.add(minutesSinceFourIndex);
			rescale.add(epochTimeIndex);
			canonicalizeFilter.addScaledAttributed(rescale);
	    
			HashSet<Integer> standardize = new HashSet<Integer>();
			standardize.add(waitTimeIndex);
			canonicalizeFilter.addStandardizedAttributed(standardize);
			
			canonicalizeFilter.setInputFormat(trainingData);
			canonicalizeData = Filter.useFilter(trainingData, canonicalizeFilter);
		} catch (Exception e) {
			getLog().error("Unable to get filter training instances from database\n"+e);
			return;
		}
		
		    
		/* Train */
		/* For nearest neighbor we use 1/10 the instances up to a max of 20 */
		long nnConstant = Math.round(Math.floor(trainingData.numInstances()/10.0)+1.0);
		if(nnConstant > 20L){
			nnConstant = 20L;
		}
		
		Bagging bagger = new Bagging();
		try{
			bagger.setOptions(weka.core.Utils.splitOptions("-P 50 -S 1 -I 10 -W weka.classifiers.lazy.IBk -- -K "+nnConstant+" -W 0 -I -A \"weka.core.neighboursearch.KDTree -A \\\"weka.core.EuclideanDistance -D -R 2-10\\\" -S weka.core.neighboursearch.kdtrees.SlidingMidPointOfWidestSide -W 0.01 -L 40 -N\""));
		} catch (Exception e) {
			getLog().error("Unable to build classifier\n"+e);
			return;
		}
		
		/* Evaluation must be done on an untrained classifer */
		/* For number of folds we use 2 instances per fold up to 10 folds*/
		int numFolds = (int) Math.round(Math.floor(trainingData.numInstances()/2.0)+1.0);
		if(numFolds > 10){
			numFolds = 10;
		}
		if(selfEval){
			try{
				Evaluation eval = new Evaluation(canonicalizeData);
				eval.crossValidateModel(bagger, canonicalizeData,numFolds, this.random);
				setEvaluation(eval);
				getLog().info(eval.toSummaryString("\nResults\n======\n", false));
			} catch (Exception e) {
				getLog().error("Unable to build and evaluate classifier\n"+e);
				return;
			}
		}
		
		try{
			bagger.buildClassifier(canonicalizeData);   // build classifier		    
		} catch (Exception e) {
			getLog().error("Unable to build classifier\n"+e);
			return;
		}
		    
		/* Dump a mid-pipeline copy */
		/*
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter( new FileWriter("/Users/djp3/djp3.train.arff"));
			writer.write(canonicalizeData.toString());
			writer.newLine();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		finally{
			try {
				if(writer != null){
					writer.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			finally{
				try{
					if(writer != null){
						writer.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				finally{
					writer = null;
				}
			}
		}
		*/
		
		/* Create test data set */
	   	Instances proposedTestSet = new Instances(canonicalizeData,0);
		typicalInstance = canonicalizeData.instance(0);
		typicalInstances = new Instances(canonicalizeData,0);
		
	   	// Make space
	   	canonicalizeData = null;
	   	
	   	try{
	   		for(int i = 0; i < 7 ; i++){
	   			for(double j = 780.0; j < 1250.0 ; j += 5.0){
	   				Instance c = (Instance) typicalInstance.copy();
	   				c.setValue(zidIndex, canonicalizeFilter.toCanonical(zidIndex, typicalInstance.value(zidIndex)));
	   				for(int k = 0;k < 7;k++){
	   					if(i == k){
	   						c.setValue(sundayIndex+k, canonicalizeFilter.toCanonical(sundayIndex+k,1.0));
	   					}
	   					else{
	   						c.setValue(sundayIndex+k, canonicalizeFilter.toCanonical(sundayIndex+k,0.0));
	   					}
	   				}
	   				c.setValue(minutesSinceFourIndex, canonicalizeFilter.toCanonical(minutesSinceFourIndex,j));
	   				c.setValue(epochTimeIndex,canonicalizeFilter.toCanonical(epochTimeIndex,System.currentTimeMillis()/1000.0));
	   				Integer tzo = getTimeZoneOffset();
	   				c.setValue(timeZoneIndex,tzo);
	   				c.setValue(waitTimeIndex,canonicalizeFilter.toCanonical(waitTimeIndex,0));
	   				proposedTestSet.add(c);
	   			}
	   		}
	   	}
	   	catch(Exception e){
			getLog().error("Unable to recreate a test set\n"+e);
			return;
	   	}
	    	
	   	// set class attribute
	   	proposedTestSet.setClassIndex(waitTimeIndex);
	   	
	   	/*
		writer = null;
		try {
			writer = new BufferedWriter( new FileWriter("/Users/djp3/djp3.test.arff"));
			writer.write(proposedTestSet.toString());
			writer.newLine();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		finally{
			try {
				if(writer != null){
					writer.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			finally{
				try{
					if(writer != null){
						writer.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				finally{
					writer = null;
				}
			}
		}
		*/
	   	
	   	setGraphingTestSet(proposedTestSet);
	   	setCanonicalizeFilter(canonicalizeFilter);
	   	setModel(bagger);
   		setLastTrainingSetHash(trainingData.hashCode());
   		setLastModelBuildTime(System.currentTimeMillis());
	}	
	
	Long getNextUpdateTime(){
		// TODO: Jeff: Make this dynamic based on the 95% confidence interval of when changes are observed
		// You will need this data structure -> durationTimes
		return (System.currentTimeMillis() + ONE_MINUTE);
	}
	
	private void storeFeatures(Map<Pair<String, String>, Object> features, Object targetData) {
		// TODO Auto-generated method stub
	}
	
	public void launch(){
		if(getSelfSynchronize()){
			/* TODO:Start a thread to self synchronize */
		}
		else{
			/* The CNode Pool is causing the synchronization to happen */
		}
	}

}
