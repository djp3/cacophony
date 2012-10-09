package edu.uci.ics.luci.cacophony.node;

import static org.junit.Assert.fail;

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
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import weka.classifiers.Evaluation;
import weka.classifiers.RandomizableIteratedSingleClassifierEnhancer;
import weka.classifiers.meta.Bagging;
import weka.core.Instance;
import weka.core.Instances;
import weka.experiment.InstanceQuery;
import weka.filters.Filter;

import com.quub.util.CalendarCache;
import com.quub.util.Pair;
import com.quub.util.Quittable;

import edu.uci.ics.luci.cacophony.CacophonyGlobals;
import edu.uci.ics.luci.cacophony.api.CacophonyRequestHandlerHelper;
import edu.uci.ics.luci.cacophony.directory.nodelist.CNodeReference;
import edu.uci.ics.luci.util.FailoverFetch;

public class CNode implements Quittable,Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6251847680422827405L;
	
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
	private Long lastModelBuildTime = 0L;
	private RandomizableIteratedSingleClassifierEnhancer model = null;

	private FailoverFetch failoverFetch;
	private String metaCNodeGUID = null;
	private String nodeName = null;
	private InstanceQuery trainingQuery = null;
	/*Warning this needs to be set when deserialized! */
	private transient CNodePool parentPool = null;  
	private List<Pair<Long, String>> baseUrls = null;
	//private String config;
	
	public String cNodeGuid = null;
	private Random random = null;
	TreeSet<String> heartbeatList = null;
	private boolean shuttingDown = false;

	/**
	 * setFailoverFetch,setParentPool, setBaseUrls, setNodeId, and setNodeName should be called before launching
	 */
	public CNode(){
		this.random = new Random();
	}
	
	public synchronized void setCNodeGuid(){
		this.cNodeGuid = "Bagged IbK, "+getMetaCNodeGUID()+","+getNodeName()+","+random.nextInt(Integer.MAX_VALUE);
	}
	
	public synchronized String getCNodeGuid() {
		return cNodeGuid;
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
			try {
				if(response.getString("error").equals("false")){
					this.setMetaCNodeGUID(response.getString("node_id"));
					this.setNodeName(response.getString("name"));
			    	InstanceQuery trainingQuery;
			    	JSONObject configuration = response.getJSONObject("node_configuration");
					String zid = null;
					try {
			    		trainingQuery = new InstanceQuery();
			    		trainingQuery.setUsername(configuration.getString("username"));
			    		trainingQuery.setPassword(configuration.getString("password"));
			    		trainingQuery.setDatabaseURL("jdbc:mysql://"+configuration.getString("databaseDomain")+"/"+configuration.getString("database"));
			    		String trainingQueryString = configuration.getString("trainingQuery");
			    		zid = configuration.getString("node_id");
			    		trainingQuery.setQuery(trainingQueryString.replaceAll("_NODE_ID_", zid));
			    		this.setTrainingQuery(trainingQuery);
					} catch (Exception e) {
						getLog().error("Unable to load cnode training using: "+zid);
					}
				}
			} catch (JSONException e) {
				getLog().error("Problem getting configuration:"+e);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Bad URL:"+e);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Exception:"+e);
		} catch (JSONException e) {
			getLog().error("Problem getting configuration:"+e);
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
					if(response.getString("error").equals("true")){
						getLog().error("Something is wrong with JSON:"+response.toString(1));
					}
				} catch (JSONException e) {
					getLog().error("Something is wrong with JSON:"+response.toString(1)+"\n"+e);
				}
			}
		} catch (MalformedURLException e) {
			getLog().warn("Bad URL when the CNode tried to check in:"+e);
		} catch (IOException e) {
			getLog().warn("IO Exception when the CNode tried to check in:"+e);
		}
		catch (JSONException e) {
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
	   		for(int i = 0; i < timesToPredict.length(); i++){
	   			
	   			/* Get the first time we are predicting */
	   			Long originalTime = timesToPredict.getLong(i);
	   			
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
		try {
			Date build = new Date(getLastModelBuildTime());
			ret.put("last_model_build_time", DateFormat.getInstance().format(build));
			ret.put("instances",eval.numInstances());
			ret.put("mean_absolute_error", eval.meanAbsoluteError());
			ret.put("root_mean_squared_error", eval.rootMeanSquaredError());
			ret.put("relative_absolute_error", eval.relativeAbsoluteError());
		} catch (JSONException e) {
			getLog().error("Probably building JSON"+e+"\n"+eval.toSummaryString());
		} catch (Exception e) {
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
		if(checkForNewData){
			Instances trainingSet = fetchTrainingSet();
			if(modelOutdated(trainingSet)){
				if(rebuildModelIfNecessary){
					buildModel(testAccuracyOnSelf,trainingSet);
				}
			}
		}
		if(sendHeartbeat){
			sendHeartbeat();
		}
	}

	public synchronized void buildModel(boolean selfEval,Instances trainingData) {
		
		if(trainingData == null){
			getLog().warn("Can't build a model with no training data");
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

}
