package edu.uci.ics.luci.cacophony.node;

import edu.uci.ics.luci.utility.datastructure.HashCodeUtil;
import net.minidev.json.JSONObject;

public class SensorConfig{
	private final String ID;
	private final String name;
	private final String URL;
	private final String format;
	private final String regEx;
	private final String pathExpression;
	private final Translator<?> translator;
	private final JSONObject translatorOptions;
	
	public SensorConfig(String ID, String name, String URL, String format, String regEx, String pathExpression, Translator<?> translator, JSONObject translatorOptions){
		this.ID = ID;
		this.name = name;
		this.URL = URL;
		this.format = format;
		this.regEx = regEx;
		this.pathExpression = pathExpression;
		this.translator = translator;
		this.translatorOptions = translatorOptions;
	}
	
	public SensorConfig(JSONObject jo){
		if(jo == null){
			throw new IllegalArgumentException("JSONObject passed to SensorConfig constructor is null.");
		}

		ID = (String)jo.get("ID");
		if(ID == null){
			throw new IllegalArgumentException("Unable to deserialize SensorConfig JSON because the sensor's ID is null.");
		}
		
		name = (String)jo.get("name");
		if(name == null){
			throw new IllegalArgumentException("Unable to deserialize SensorConfig JSON because the sensor's name is null.");
		}
		
		URL = (String)jo.get("url");
		if(URL == null){
			throw new IllegalArgumentException("Unable to deserialize SensorConfig JSON because the sensor's URL is null.");
		}
		
		format = (String)jo.get("format");
		if(format == null){
			throw new IllegalArgumentException("Unable to deserialize SensorConfig JSON because the sensor's format is null.");
		}
		if(!format.equals("html") && !format.equals("json")){
			throw new IllegalArgumentException("Unable to deserialize SensorConfig JSON because the sensor's format must be \"json\" or \"html\" not "+format);
		}
		
		pathExpression = (String)jo.get("path_expression");
		if(pathExpression == null){
			throw new IllegalArgumentException("Unable to deserialize SensorConfig JSON because the sensor's path_expression is null.");
		}
		
		regEx = (String)jo.get("reg_ex");
		if(regEx == null){
			throw new IllegalArgumentException("Unable to deserialize SensorConfig JSON because the sensor's reg_ex is null.");
		}
		
		JSONObject translatorJSON = (JSONObject)jo.get("translator");
		if(translatorJSON == null){
			// TODO: Is it actually a problem if the translator is null? Does it makes sense for all sensors to need a translator?
			throw new IllegalArgumentException("Unable to deserialize SensorConfig JSON because the sensor's translator is null.");
		}
		
		String classname = (String)translatorJSON.get("classname");
		if(classname == null){
			throw new IllegalArgumentException("Unable to deserialize SensorConfig JSON because the sensor's translator classname is null.");
		}
		
		try {
			translator = (Translator<?>)Class.forName(classname).newInstance();
		} catch (InstantiationException e) {
			throw new IllegalArgumentException("Unable to deserialize SensorConfig JSON because the sensor's translator couldn't be instatiated :"+classname+"\n"+e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Unable to deserialize SensorConfig JSON because the sensor's translator couldn't be instatiated :"+classname+"\n"+e);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Unable to deserialize SensorConfig JSON because the sensor's translator couldn't be instatiated :"+classname+"\n"+e);
		}
		
		translatorOptions = (JSONObject)translatorJSON.get("options");
		if(translatorOptions == null){
			throw new IllegalArgumentException("Unable to deserialize SensorConfig JSON because the sensor's translator options are null.");
		}
		
	// TODO: leaving this commented out until we need to add support for it.
	//	try{
	//		translator.initialize(translatorOptions);
	//	} catch (RuntimeException e) {
	//		throw new IllegalArgumentException("Unable to load configuration for "+myPath+" because the target translator couldn't be initialized with :"+translatorOptions.toJSONString()+"\n"+e);
	//	}
	}
	
	public JSONObject serializeToJSON() {
		JSONObject sensorJSON = new JSONObject();
		sensorJSON.put("ID", ID);
		sensorJSON.put("name", name);
		sensorJSON.put("url", URL);
		sensorJSON.put("format", format);
		sensorJSON.put("path_expression", pathExpression);
		sensorJSON.put("reg_ex", regEx);
		
		JSONObject translatorJSON = new JSONObject();
		translatorJSON.put("classname", translator.getClass().getCanonicalName());
		translatorJSON.put("options", translatorOptions);
		sensorJSON.put("translator", translatorJSON);
		
		return sensorJSON;
	}
	
	public String getID() {
		return ID;
	}
	
	public String getName() {
		return name;
	}
	
	public String getURL() {
		return URL;
	}
	
	public String getFormat() {
		return format;
	}
	
	public String getRegEx() {
		return regEx;
	}
	
	public String getPathExpression() {
		return pathExpression;
	}
	
	public Translator<?> getTranslator() {
		return translator;
	}
	
	public JSONObject getTranslatorOptions() {
		return translatorOptions;
	}

	@Override
	public boolean equals(Object _that) {
		if(_that == null) return false;
		if(!(_that instanceof SensorConfig)) return false; 
		if(this == _that) return true; 

		SensorConfig that = (SensorConfig)_that;
		
		return this.ID.equals(that.ID)
				&& this.name.equals(that.name)
				&& this.URL.equals(that.URL)
				&& this.format.equals(that.format)
				&& this.regEx.equals(that.regEx)
				&& this.pathExpression.equals(that.pathExpression)
				&& this.translator.getClass().equals(that.translator.getClass())
				&& this.translatorOptions.equals(that.translatorOptions);
	}
	
	@Override
	public int hashCode(){
		int result = HashCodeUtil.SEED;

		result = HashCodeUtil.hash(result, this.ID);
		result = HashCodeUtil.hash(result, this.name);
		result = HashCodeUtil.hash(result, this.URL);
		result = HashCodeUtil.hash(result, this.format);
		result = HashCodeUtil.hash(result, this.regEx);
		result = HashCodeUtil.hash(result, this.pathExpression);
		result = HashCodeUtil.hash(result, this.translator.getClass());
		result = HashCodeUtil.hash(result, this.translatorOptions);
		
		return(result);
	}
}
