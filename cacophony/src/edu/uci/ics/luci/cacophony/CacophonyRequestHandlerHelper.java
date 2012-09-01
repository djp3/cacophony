package edu.uci.ics.luci.cacophony;

import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.quub.util.Pair;
import com.quub.webserver.HandlerAbstract;


public abstract class CacophonyRequestHandlerHelper extends HandlerAbstract{
	
	public static final long ONE_SEC = 1000L;
	public static final long ONE_MIN = 60 * ONE_SEC;
	public static final long ONE_HOUR = 60 * ONE_MIN;
	public static final long ONE_DAY = 24 * ONE_HOUR;
	public static final long ONE_WEEK = 7 * ONE_DAY;
	public static final long ONE_YEAR = 365 * ONE_DAY;
	public static final long SIX_MONTHS = ONE_YEAR / 2;
	protected static final long DEFAULT_EXPIRATION = SIX_MONTHS;
	private static final String API_VERSION = "1.1";
	protected static transient volatile Random random = new Random();

	/**
	 * @return the version of the API are we implementing
	 */
	public static String getAPIVersion() {
		return API_VERSION;
	}
	
	static protected Random getRandom(){
		return random;
	}
	
	protected String versionOK(Map<String, String> parameters) {
		String trueVersion = getAPIVersion();
		String version = parameters.get("version");
		String noerror = null;
		String error = "API is version: "+trueVersion+". REST call requested: "+version;
		if(version != null){
			if(!trueVersion.equals(version)){
				return error;
			}
			else{
				return noerror;
			}
		}
		else{
			return error;
		}
	}

	
	public Pair<byte[], byte[]> failResponse(String reason,String restFunction,Map<String, String> parameters){
		Pair<byte[], byte[]> pair = null;

		JSONObject ret = new JSONObject();

		try {
			ret.put("error","true");
			JSONArray errors = new JSONArray();
			errors.put("REST call to "+restFunction+" failed");  
			errors.put(reason);
			ret.put("errors",errors);
			
			pair = new Pair<byte[],byte[]>(HandlerAbstract.getContentTypeHeader_JSON(),ret.toString().getBytes());
		} catch (JSONException e) {
			getLog().error("Unable to respond with version:"+e);
		}
		return pair;
	}
}

