package edu.uci.ics.luci.cacophony.directory.api;

import java.util.Map;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.quub.util.Pair;
import com.quub.webserver.RequestHandlerHelper;

import edu.uci.ics.luci.cacophony.CacophonyGlobals;

public class CacophonyRequestHandlerHelper extends RequestHandlerHelper{
	
	public static final long ONE_SEC = 1000L;
	public static final long ONE_MIN = 60 * ONE_SEC;
	public static final long ONE_HOUR = 60 * ONE_MIN;
	public static final long ONE_DAY = 24 * ONE_HOUR;
	public static final long ONE_WEEK = 7 * ONE_DAY;
	public static final long ONE_YEAR = 365 * ONE_DAY;
	public static final long SIX_MONTHS = ONE_YEAR / 2;
	protected static final long DEFAULT_EXPIRATION = SIX_MONTHS;
	private static transient volatile Base64 b64 = new Base64(true);
	protected static transient volatile Random random = new Random();

	protected boolean versionOK(Map<String, String> parameters)
	{
		String trueVersion = CacophonyGlobals.getVersion();
		String version = parameters.get("version");
		if(trueVersion != null){
			if(version != null){
				return trueVersion.equals(version);
			}
			else{
				return false;
			}
		}
		else{
			return false;
		}
	}
	
	
	
	static protected Random getRandom(){
		return random;
	}

	public Pair<byte[], String> versionFailResponse(String restFunction,Map<String, String> parameters){
		Pair<byte[], String> pair = null;

		JSONObject ret = new JSONObject();

		try {
			ret.put("error","true");
			JSONArray errors = new JSONArray();
			errors.put("REST call to "+restFunction+" requested unsupported version:"+parameters.get("version")+". Valid version is "+CacophonyGlobals.getVersion());
			ret.put("errors",errors);
			
			pair = new Pair<byte[],String>(RequestHandlerHelper.contentTypeHeader_JSON,ret.toString());
		} catch (JSONException e) {
			getLog().error("Unable to respond with version:"+e);
		}
		return pair;
	}

	protected CacophonyGlobals getGlobals() {
		return (CacophonyGlobals) super.getGlobals();
	}

}

