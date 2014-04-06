package edu.uci.ics.luci.cacophony.node;

import java.util.HashSet;
import java.util.Set;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

public class CNodeConfiguration {
	private String myPath;
	private Set<CNodeAddress> predictors = new HashSet<CNodeAddress>();
	private SensorConfig target;
	private Set<SensorConfig> features = new HashSet<SensorConfig>();
	private Long pollingMinInterval;
	private PollingPolicy pollingPolicy;
	
	public CNodeConfiguration(JSONObject jo){
		this.fromJSONObject(jo);
	}
	
	public JSONObject toJSONObject(){
		JSONObject configuration = new JSONObject();
		configuration.put("c_node_name", myPath);
		
		JSONArray predictorsJSON = new JSONArray();
		for(CNodeAddress cna : predictors){
			predictorsJSON.add(cna.toString());
		}
		configuration.put("predictors", predictorsJSON);
		
		configuration.put("target", target.serializeToJSON());
		
		JSONArray featuresJSON = new JSONArray();
		for(SensorConfig feature : features){
			JSONObject featureJSON = feature.serializeToJSON();
			featuresJSON.add(featureJSON);
		}
		configuration.put("features", featuresJSON);
		
		JSONObject polling = new JSONObject();
		polling.put("policy", pollingPolicy.toString());
		polling.put("min_interval", pollingMinInterval.toString());
		configuration.put("polling", polling);
		
		return configuration;
	}
	
	public void fromJSONObject(JSONObject jo){
		if(jo == null){
			throw new IllegalArgumentException("Can't create a configuration from null");
		}
		
		String myPathString = (String) jo.get("c_node_name");
		if(myPathString == null){
			throw new IllegalArgumentException("Configuration does not have a name");
		}
		myPath = myPathString;
		
		JSONArray predictorsJSON = (JSONArray)jo.get("predictors");
		if(predictors != null){
			for(int i = 0; i < predictorsJSON.size(); i++){
				String predictor = (String)predictorsJSON.get(i);
				try{
					predictors.add(new CNodeAddress(predictor));
				}
				catch(IllegalArgumentException e){
					throw new IllegalArgumentException("Unable to load configuration for "+myPath+" because the predictor didn't parse correctly: "+predictor);
				}
			}
		}
		
		target = new SensorConfig((JSONObject)jo.get("target"));

		JSONArray featuresJSON = (JSONArray)jo.get("features"); 
		for(int i = 0; i < featuresJSON.size(); i++){
			JSONObject featureJSON = (JSONObject)featuresJSON.get(i);
			features.add(new SensorConfig(featureJSON));
		}
		
		JSONObject polling = (JSONObject)jo.get("polling");
		if(polling == null){
			throw new IllegalArgumentException("Unable to load configuration for "+myPath+" because the polling parameters didn't exist.");
		}
		else{
			String pollingMinIntervalString = (String) polling.get("min_interval");
			if(pollingMinIntervalString == null){
				throw new IllegalArgumentException("Unable to load configuration for "+myPath+" because the polling min interval didn't exist.");
			}
			
			try{
				pollingMinInterval = Long.valueOf(pollingMinIntervalString);
			}
			catch(NumberFormatException e){
				throw new IllegalArgumentException("Unable to load configuration for "+myPath+" because the polling min interval look like a long."+e);
			}
			
			String pollingPolicyString = (String)polling.get("policy");
			if(pollingPolicyString == null){
				throw new IllegalArgumentException("Unable to load configuration for "+myPath+" because the polling policy didn't exist.");
			}
			try{
				pollingPolicy = (PollingPolicy) PollingPolicy.fromString(pollingPolicyString);
			}
			catch(IllegalArgumentException e){
				throw new IllegalArgumentException("Unable to load configuration for "+myPath+" because the polling policy was unknown."+e);
			}
		}
	}

	public String getMyPath() {
		return myPath;
	}

	public void setMyPath(String myPath) {
		this.myPath = myPath;
	}

	public Set<CNodeAddress> getPredictors() {
		return predictors;
	}

	public void setPredictors(Set<CNodeAddress> predictors) {
		this.predictors = predictors;
	}

	public SensorConfig getTarget() {
		return target;
	}
	
	public void setTarget(SensorConfig target) {
		this.target = target; 
	}

	public Set<SensorConfig> getFeatures() {
		return features;
	}

	public void setFeatures(Set<SensorConfig> features) {
		this.features = features;
	}
	
	public Long getPollingMinInterval() {
		return pollingMinInterval;
	}

	public void setPollingMinInterval(Long pollingMinInterval) {
		this.pollingMinInterval = pollingMinInterval;
	}

	public PollingPolicy getPollingPolicy() {
		return pollingPolicy;
	}

	public void setPollingPolicy(PollingPolicy pollingPolicy) {
		this.pollingPolicy = pollingPolicy;
	}
}
