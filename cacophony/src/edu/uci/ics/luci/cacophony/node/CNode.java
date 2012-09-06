package edu.uci.ics.luci.cacophony.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
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
import weka.filters.Filter;

import com.quub.util.Pair;
import com.quub.util.Quittable;

import edu.uci.ics.luci.cacophony.CacophonyRequestHandlerHelper;
import edu.uci.ics.luci.cacophony.directory.nodelist.CNodeReference;
import edu.uci.ics.luci.util.FailoverFetch;

public class CNode implements Quittable{
	

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
	public static final Integer waitTimeIndex = 10;
	
	private Map<String,Double> maxWait = null;
	
	private Instances testSet = null;
	private Canonicalize filter = null;
	private RandomizableIteratedSingleClassifierEnhancer model = null;

	private FailoverFetch failoverFetch;
	private Object heartBeatLock = null;
	private String nodeId="undefined";
	private String nodeName="undefined";
	private CNodePool parentPool;
	private List<Pair<Long, String>> baseUrls;
	
	
	public String cNodeGuid = null;
	public Timer heartbeat;
	private Random random = null;
	TreeSet<String> heartbeatList = null;
	private boolean shuttingDown = false;

	public CNode(FailoverFetch failoverFetch, CNodePool parent, List<Pair<Long,String>> baseUrls){
		
		this.random = new Random();
		this.cNodeGuid = "Batch CNode bagged IbK "+random.nextInt(Integer.MAX_VALUE);
		this.failoverFetch = failoverFetch;
		if(parent == null){
			throw new IllegalArgumentException("Can't handle null parent");
		}
		else{
			this.parentPool = parent;
		}
		if(baseUrls == null){
			throw new IllegalArgumentException("Can't handle null urls");
		}
		else{
			this.baseUrls = baseUrls;
		}
		this.heartBeatLock = new Object();
	}

	public void getANewConfiguration() {
		
		String responseString = null;
		try {
			
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version", CacophonyRequestHandlerHelper.getAPIVersion());
			params.put("namespace", parentPool.getNamespace());

			responseString = failoverFetch.fetchWebPage("/node_assignment", false, params, 30 * 1000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Bad URL:"+e);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Exception:"+e);
		}
		
		//System.out.println(responseString);

		JSONObject response = null;
		try {
			response = new JSONObject(responseString);
			try {
				if(response.getString("error").equals("false")){
					this.nodeId = response.getString("node_id");
					this.nodeName = response.getString("name");
				}
			} catch (JSONException e) {
				getLog().error("Problem getting configuration:"+e);
			}
		} catch (JSONException e) {
			getLog().error("Problem getting configuration:"+e);
		}
	}
	
	/**
	 * 
	 * @param delay How long to wait before first heartbeat goes out in milliseconds. Default is 0
	 * @param period How often to send a heartbeat in milliseconds. Default is 5 minutes
	 * @param metaCNodeID Some canonical name for the CNode configuration, like a restaurant id
	 * @param urls A list of URLs with which to reference this directory with a number indicating preference order. Lower is more preferred.
	 *  Default is just what java thinks the host address is.
	 */
	public void startHeartbeatAfterRequestingCNode(Long delay,Long period,String metaCNodeID, List<Pair<Long,String>> urls){
		
		if(delay == null){
			delay = 0L;
		}
		
		if(period == null){
			period = FIVE_MINUTES;
		}
		
		if(metaCNodeID == null){
			metaCNodeID = "Unknown CNode";
		}
		
		if(urls == null){
			urls = new ArrayList<Pair<Long,String>>();
		}
		
		final String localNamespace = parentPool.getNamespace();
		if(localNamespace == null){
			getLog().error("Set the namespace before starting the heartbeat");
		}
		
		if(urls.size() == 0){
			String url = null;
			try {
				url = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e1) {
				url ="127.0.0.1";
			}
			urls.add(new Pair<Long,String>(0L,url));
		}
		
		
	   	final JSONObject localData = new JSONObject();
	   	try {
			localData.put("guid",metaCNodeID);
			localData.put("node_id",this.nodeId);
			JSONArray servers = new JSONArray();
			for(Pair<Long,String> x:urls){
				JSONObject bar = new JSONObject();
				bar.put("priority_order",x.getFirst());
				bar.put("url",x.getSecond());
				servers.put(bar);
			}
			localData.put("access_routes_for_ui", servers);
	   	} catch (JSONException e) {
	   		getLog().fatal("Something is wrong with JSON:"+localNamespace+"\n"+e);
	   	}
		
		/* If we already started a heartbeat cancel it and restart */
		if(heartbeat != null){
			heartbeat.cancel();
		}
		
		try {
			getLog().error("Starting a CNode -> Directory heartbeat for: "+localData.toString(1));
		} catch (JSONException e1) {
		}
		
		/*Set up the heartbeat to go every 5 minutes;*/
		 heartbeat = new Timer(true);
		 heartbeat.scheduleAtFixedRate(
			    new TimerTask(){
			    	
					@Override
			    	public void run(){
						synchronized(heartBeatLock){
							String responseString = null;
							/* Update this nodes heartbeat */
							String now = Long.toString(System.currentTimeMillis());
							try {
								localData.put("heartbeat",now);
							} catch (JSONException e) {
								getLog().fatal("Something is wrong with JSON:"+now+"\n"+e);
							}
						
							try {
								HashMap<String, String> params = new HashMap<String, String>();
								params.put("version", CacophonyRequestHandlerHelper.getAPIVersion());
								params.put("namespace", localNamespace);
								params.put("json_data", localData.toString());
								responseString = failoverFetch.fetchWebPage("/node_checkin", false, params, 30 * 1000);
							} catch (MalformedURLException e) {
								getLog().warn("Bad URL when the CNode tried to check in:"+e);
							} catch (IOException e) {
								getLog().warn("IO Exception when the CNode tried to check in:"+e);
							}
							
							if(responseString == null){
							}
							else{
								JSONObject response = null;
								try {
									response = new JSONObject(responseString);
									try {
										assertEquals("false",response.getString("error"));
									} catch (JSONException e) {
										getLog().fatal("Something is wrong with JSON:"+responseString+"\n"+e);
									}
								} catch (JSONException e) {
									getLog().fatal("Something is wrong with JSON:"+responseString+"\n"+e);
								}
							}
						
							if(shuttingDown){
								if(heartbeat != null){
									heartbeat.cancel();
								}
							}
							heartBeatLock.notifyAll();
						}
					}
					}, delay, period);
		 
	}
	
	
	public void startHeartbeatAfterModeling(){
		startHeartbeatAfterModeling(null,null);
	}
	
	
	
	/**
	 * 
	 * @param delay How long to wait before first heartbeat goes out in milliseconds. Default is 0
	 * @param period How often to send a heartbeat in milliseconds. Default is 5 minutes
	 * @param baseUrls A list of URLs with which to reference this directory with a number indicating preference order. Lower is more preferred.
	 *  Default is just what java thinks the host address is.
	 */
	public void startHeartbeatAfterModeling(Long delay,Long period){
		
		if(delay == null){
			delay = 0L;
		}
		
		if(period == null){
			/* Minimum acceptable */
			period = 1000L;
			
			int num = maxWait.keySet().size();
			if(num == 0){
				/* Check every five minutes to see if we've modeled anything */
				period = FIVE_MINUTES;
			}
			else {
				long proposed = FIVE_MINUTES/num;
				if(proposed > period){
					period = proposed;
				}
			}
		}
		
		final String localCNodeGuid = this.cNodeGuid;
		
		if(baseUrls == null){
			baseUrls = new ArrayList<Pair<Long,String>>();
		}
		
		final String localName = nodeName;
		
		final String localNamespace = parentPool.getNamespace();
		if(localNamespace == null){
			getLog().error("Set the namespace of the pool before starting the heartbeat");
		}
		
		if(baseUrls.size() == 0){
			String url = null;
			try {
				url = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e1) {
				url ="127.0.0.1";
			}
			baseUrls.add(new Pair<Long,String>(0L,url));
		}
		final List<Pair<Long, String>> localBaseUrls = baseUrls;
		
		/* If we already started a heartbeat cancel it and restart */
		if(heartbeat != null){
			heartbeat.cancel();
		}
		
		getLog().info("Starting a CNode -> Directory heartbeat for initial list of "+maxWait.size()+" nodes");
		
		/*Set up the heartbeat*/
		 heartbeat = new Timer(true);
		 heartbeat.scheduleAtFixedRate(
			    new TimerTask(){
			    	
					@Override
			    	public void run(){
						
						synchronized(heartBeatLock){
							if(heartbeatList == null){
								heartbeatList = new TreeSet<String>();
							}
							if(heartbeatList.size() <= 0){
								heartbeatList.addAll(maxWait.keySet());
							}
							
							
							/* Pick the heartbeat we are reporting */
							if(heartbeatList.size() > 0){
								CNodeReference cnr = new CNodeReference();
								
								/* Set the identity */
								String metaCNodeGuid = heartbeatList.pollFirst();
								cnr.setMetaCNodeGuid(metaCNodeGuid);
								
								cnr.setCNodeGuid(localCNodeGuid);
								
								/* Update this node's heartbeat */
								cnr.setLastHeartbeat(System.currentTimeMillis());
								
								Set<Pair<Long, String>> accessRoutes = new TreeSet<Pair<Long, String>>();
								
								for(Pair<Long,String> x:localBaseUrls){
									try{
										Pair<Long,String> p = new Pair<Long,String>(x.getFirst(),x.getSecond()+"/index.html?node="+URLEncoder.encode(metaCNodeGuid,"UTF-8")+"&name="+URLEncoder.encode(localName,"UTF-8")+"&namespace="+URLEncoder.encode(localNamespace,"UTF-8"));
										accessRoutes.add(p);
									} catch (UnsupportedEncodingException e) {
										getLog().fatal("Something is wrong with URL Making\n"+e);
									}
								}
								cnr.setAccessRoutesForUI(accessRoutes);
								
								accessRoutes = new TreeSet<Pair<Long, String>>();
								
								for(Pair<Long,String> x:localBaseUrls){
									try{
										Pair<Long,String> p = new Pair<Long,String>(x.getFirst(),x.getSecond()+"/predict?version="+URLEncoder.encode(CacophonyRequestHandlerHelper.getAPIVersion(),"UTF-8")+"&namespace="+URLEncoder.encode(localNamespace,"UTF-8")+"&node="+URLEncoder.encode(metaCNodeGuid,"UTF-8"));
										accessRoutes.add(p);
									} catch (UnsupportedEncodingException e) {
										getLog().fatal("Something is wrong with URL Making\n"+e);
									}
								}
								cnr.setAccessRoutesForAPI(accessRoutes);
								
								/* Send heartbeat */
								String responseString = null;
								try {
									HashMap<String, String> params = new HashMap<String, String>();
									params.put("version", CacophonyRequestHandlerHelper.getAPIVersion());
									params.put("namespace", localNamespace);
									params.put("json_data", cnr.toJSONObject().toString());
									responseString = failoverFetch.fetchWebPage("/node_checkin", false, params, 30 * 1000);
									
									if(responseString != null){
										JSONObject response = null;
										try {
											response = new JSONObject(responseString);
											if(response.getString("error").equals("true")){
												getLog().error("Something is wrong with JSON:"+responseString);
											}
										} catch (JSONException e) {
											getLog().error("Something is wrong with JSON:"+responseString+"\n"+e);
										}
									}
									
								} catch (MalformedURLException e) {
									getLog().warn("Bad URL when the CNode tried to check in:"+e);
								} catch (IOException e) {
									getLog().warn("IO Exception when the CNode tried to check in:"+e);
								}
							}
							
							if(shuttingDown){
								if(heartbeat != null){
									heartbeat.cancel();
								}
							}
						
							heartBeatLock.notifyAll();
						}
					}
					}, delay, period);
		 
	}
	

	public void buildModel(boolean selfEval,Instances trainingData) {
		
		getLog().info("Training on "+trainingData.numInstances()+" instances");
			
		/* Make all the wait times relative to the restaurant */
		Map<String, Double> localMaxWait = new HashMap<String,Double>();
	    for (int index = 0; index < trainingData.numInstances(); index++){
	    	Instance nextElement = trainingData.instance(index);
	    	String zid = nextElement.attributeSparse(zidIndex).value((int) Math.round(nextElement.value(zidIndex)));
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
		finally{
			// Make space
			trainingData = null;
		}
		
		    
		/* Train */
		Bagging bagger = new Bagging();
		try{
			//bagger.setOptions(weka.core.Utils.splitOptions("-P 100 -S 1 -I 10 -W weka.classifiers.lazy.IBk -- -K 5 -W 0 -A \"weka.core.neighboursearch.KDTree -A \\\"weka.core.EuclideanDistance -R first-last\\\" -S weka.core.neighboursearch.kdtrees.SlidingMidPointOfWidestSide -W 0.01 -L 40 -N\""));
			bagger.setOptions(weka.core.Utils.splitOptions("-P 50 -S 1 -I 10 -W weka.classifiers.lazy.IBk -- -K 20 -W 0 -I -A \"weka.core.neighboursearch.KDTree -A \\\"weka.core.EuclideanDistance -D -R 2-10\\\" -S weka.core.neighboursearch.kdtrees.SlidingMidPointOfWidestSide -W 0.01 -L 40 -N\""));
			bagger.buildClassifier(canonicalizeData);   // build classifier		    
		} catch (Exception e) {
			getLog().error("Unable to build classifier\n"+e);
			return;
		}
		    
		if(selfEval){
			try{
				Evaluation eval = new Evaluation(canonicalizeData);
				eval.evaluateModel(bagger,canonicalizeData);
				getLog().info(eval.toSummaryString("\nResults\n======\n", false));
			} catch (Exception e) {
				getLog().error("Unable to self-test classifier\n"+e);
				return;
			}
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
		Instance typicalI = canonicalizeData.instance(0);
		
	   	// Make space
	   	canonicalizeData = null;
	   	
	   	try{
	   		for(int i = 0; i < 7 ; i++){
	   			for(double j = 780.0; j < 1250.0 ; j += 5.0){
	   				Instance c = new Instance(typicalI);
	   				c.setValue(zidIndex, canonicalizeFilter.toCanonical(zidIndex, typicalI.value(zidIndex)));
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
	   	
	   	setMaxWait(localMaxWait);
	   	setTestSet(proposedTestSet);
	   	setCanonicalizeFilter(canonicalizeFilter);
	   	setModel(bagger);
	}


	public void launch(Instances trainingSet) {
		buildModel(false,trainingSet);
		startHeartbeatAfterModeling();
	}
	
	private synchronized void setMaxWait(Map<String,Double> maxWait) {
		HashMap<String, Double> ret = new HashMap<String,Double>();
		ret.putAll(maxWait);
		this.maxWait = Collections.synchronizedMap(maxWait);
	}
	
	private synchronized void setTestSet(Instances localTestSet) {
		this.testSet = new Instances(localTestSet);
	}
	
	public synchronized Instances getTestSet() {
		return (this.testSet);
	}
	
	private synchronized void setCanonicalizeFilter(Canonicalize filter) {
		this.filter = filter;
	}
	
	private synchronized void setModel(RandomizableIteratedSingleClassifierEnhancer bagging) {
		this.model = bagging;
		
	}

	public synchronized Map<String, TreeSet<Pair<Integer, Double>>> predict(String nodeToPredict) {
		
		if(testSet == null){
			return null;
		}
		
		if(filter == null){
			return null;
		}
		
		if(testSet.instance(0) == null){
			return null;
		}
		
		if(testSet.instance(0).attribute(0) == null){
			return null;
		}
		
		int valIndex = testSet.instance(0).attribute(0).indexOfValue(nodeToPredict);
		if (valIndex == -1) {
			if (testSet.instance(0).attribute(0).isNominal()) {
				/* This node was never seen during training and we can't add it on the fly */
				return null;
			}
		}
		
		HashMap<String, TreeSet<Pair<Integer, Double>>> ret = new HashMap<String, TreeSet<Pair<Integer, Double>>>();
		ret.put("sunday",new TreeSet<Pair<Integer,Double>>());
		ret.put("monday",new TreeSet<Pair<Integer,Double>>());
		ret.put("tuesday",new TreeSet<Pair<Integer,Double>>());
		ret.put("wednesday",new TreeSet<Pair<Integer,Double>>());
		ret.put("thursday",new TreeSet<Pair<Integer,Double>>());
		ret.put("friday",new TreeSet<Pair<Integer,Double>>());
		ret.put("saturday",new TreeSet<Pair<Integer,Double>>());
		
		/* Label instances */
		for(int i=0; i < testSet.numInstances(); i++){
			
			/* Set restaurant to predict */
			testSet.instance(i).setValue(zidIndex, nodeToPredict);
			
			double clsLabel;
			try {
				/* Predict */
				//clsLabel = model.classifyInstance(staticTestSet.instance(i)) * maxWait.get(nodeToPredict);
				clsLabel = model.classifyInstance(testSet.instance(i));
				
				/* Figure out what time that was for */
				double baseTime = filter.fromCanonical(minutesSinceFourIndex, testSet.instance(i).value(minutesSinceFourIndex));
				
				Integer time = Integer.valueOf((int) Math.floor(baseTime));
				
				/* Log it */
				Pair<Integer,Double> foo = new Pair<Integer,Double>(time,clsLabel);
				
				/* Put it in the right day */
				if(testSet.instance(i).value(1) == 1){
					ret.get("sunday").add(foo);
				}
				else if(testSet.instance(i).value(2) == 1){
					ret.get("monday").add(foo);
				}
				else if(testSet.instance(i).value(3) == 1){
					ret.get("tuesday").add(foo);
				}
				else if(testSet.instance(i).value(4) == 1){
					ret.get("wednesday").add(foo);
				}
				else if(testSet.instance(i).value(5) == 1){
					ret.get("thursday").add(foo);
				}
				else if(testSet.instance(i).value(6) == 1){
					ret.get("friday").add(foo);
				}
				else if(testSet.instance(i).value(7) == 1){
					ret.get("saturday").add(foo);
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

}
