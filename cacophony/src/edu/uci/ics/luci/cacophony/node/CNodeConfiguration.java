package edu.uci.ics.luci.cacophony.node;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;

public class CNodeConfiguration {
	private String myPath;
	private Set<CNodeAddress> predictors = new HashSet<CNodeAddress>();
	private String targetURL;
	private String targetFormat;
	private String targetRegEx;
	private String targetPathExpression;
	private Translator translator;
	private JSONObject translatorOptions;
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
		
		JSONObject target = new JSONObject();
		target.put("url",targetURL);
		target.put("format",targetFormat);
		target.put("path_expression", targetPathExpression);
		target.put("reg_ex", targetRegEx);
		
		JSONObject translatorJSON = new JSONObject();
		translatorJSON.put("classname",translator.getClass().getCanonicalName());
		translatorJSON.put("options",translatorOptions);
		target.put("translator", translatorJSON);
		
		configuration.put("target", target);
		
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
		
		JSONArray predictorsJSON = (JSONArray) jo.get("predictors");
		if(predictors != null){
			for(int i = 0; i < predictorsJSON.size(); i++){
				String predictor = (String) predictorsJSON.get(i);
				try{
					CNodeAddress cna = new CNodeAddress(predictor);
					predictors.add(cna);
				}
				catch(IllegalArgumentException e){
					throw new IllegalArgumentException("Unable to load configuration for "+myPath+" because the predictor didn't parse correctly: "+predictor);
				}
				
			}
		}
		
		JSONObject target = (JSONObject) jo.get("target");
		if(target == null){
			throw new IllegalArgumentException("Unable to load configuration for "+myPath+" because the target didn't exist.");
		}
		else{
			targetURL = (String) target.get("url");
			if(targetURL == null){
				throw new IllegalArgumentException("Unable to load configuration for "+myPath+" because the target url didn't exist.");
			}
			
			targetFormat = (String) target.get("format");
			if(targetFormat == null){
				throw new IllegalArgumentException("Unable to load configuration for "+myPath+" because the target format didn't exist.");
			}
			if(!targetFormat.equals("html") && !targetFormat.equals("json")){
				throw new IllegalArgumentException("Unable to load configuration for "+myPath+" because the target format must be \"json\" or \"html\" not "+targetFormat);
			}
			
			targetPathExpression = (String) target.get("path_expression");
			if(targetPathExpression == null){
				throw new IllegalArgumentException("Unable to load configuration for "+myPath+" because the target path_expression didn't exist.");
			}
			
			targetRegEx = (String) target.get("reg_ex");
			if(targetRegEx == null){
				throw new IllegalArgumentException("Unable to load configuration for "+myPath+" because the target reg_ex didn't exist.");
			}
			
			JSONObject translatorJSON = (JSONObject) target.get("translator");
			if(translatorJSON == null){
				throw new IllegalArgumentException("Unable to load configuration for "+myPath+" because the target translator didn't exist.");
			}
			
			String name = (String)translatorJSON.get("classname");
			if(name == null){
				throw new IllegalArgumentException("Unable to load configuration for "+myPath+" because the target translator classname didn't exist.");
			}
			
			translatorOptions = (JSONObject) translatorJSON.get("options");
			if(translatorOptions == null){
				throw new IllegalArgumentException("Unable to load configuration for "+myPath+" because the target translator options didn't exist.");
			}
			
			try {
				translator= (Translator) Class.forName(name).newInstance();
			} catch (InstantiationException e) {
				throw new IllegalArgumentException("Unable to load configuration for "+myPath+" because the target translator couldn't be instatiated :"+name+"\n"+e);
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException("Unable to load configuration for "+myPath+" because the target translator couldn't be instatiated :"+name+"\n"+e);
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("Unable to load configuration for "+myPath+" because the target translator couldn't be instatiated :"+name+"\n"+e);
			}
			
			try{
				translator.initialize(translatorOptions);
			} catch (RuntimeException e) {
				throw new IllegalArgumentException("Unable to load configuration for "+myPath+" because the target translator couldn't be initialized with :"+translatorOptions.toJSONString()+"\n"+e);
			}
			
			
			JSONObject polling = (JSONObject) jo.get("polling");
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

	public String getTargetURL() {
		return targetURL;
	}

	public void setTargetURL(String targetURL) {
		this.targetURL = targetURL;
	}

	public String getTargetFormat() {
		return targetFormat;
	}

	public void setTargetFormat(String targetFormat) {
		this.targetFormat = targetFormat;
	}

	public String getTargetRegEx() {
		return targetRegEx;
	}

	public void setTargetRegEx(String targetRegEx) {
		this.targetRegEx = targetRegEx;
	}

	public String getTargetPathExpression() {
		return targetPathExpression;
	}

	public void setTargetPathExpression(String targetPathExpression) {
		this.targetPathExpression = targetPathExpression;
	}

	public Translator getTranslator() {
		return translator;
	}

	public void setTranslator(Translator translator) {
		this.translator = translator;
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
