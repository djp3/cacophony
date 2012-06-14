package edu.uci.ics.luci.cacophony.directory.api;

import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.quub.Globals;
import com.quub.util.Pair;
import com.quub.webserver.HandlerAbstract;

import edu.uci.ics.luci.cacophony.CacophonyGlobals;

public abstract class CacophonyRequestHandlerHelper extends HandlerAbstract{
	
	public static final long ONE_SEC = 1000L;
	public static final long ONE_MIN = 60 * ONE_SEC;
	public static final long ONE_HOUR = 60 * ONE_MIN;
	public static final long ONE_DAY = 24 * ONE_HOUR;
	public static final long ONE_WEEK = 7 * ONE_DAY;
	public static final long ONE_YEAR = 365 * ONE_DAY;
	public static final long SIX_MONTHS = ONE_YEAR / 2;
	protected static final long DEFAULT_EXPIRATION = SIX_MONTHS;
	protected static transient volatile Random random = new Random();

	protected boolean versionOK(Map<String, String> parameters)
	{
		String trueVersion = Globals.getGlobals().getVersion();
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

	public Pair<byte[], byte[]> versionFailResponse(String restFunction,Map<String, String> parameters){
		Pair<byte[], byte[]> pair = null;

		JSONObject ret = new JSONObject();

		try {
			ret.put("error","true");
			JSONArray errors = new JSONArray();
			errors.put("REST call to "+restFunction+" requested unsupported version:"+parameters.get("version")+". Valid version is "+Globals.getGlobals().getVersion());
			ret.put("errors",errors);
			
			pair = new Pair<byte[],byte[]>(HandlerAbstract.contentTypeHeader_JSON,ret.toString().getBytes());
		} catch (JSONException e) {
			getLog().error("Unable to respond with version:"+e);
		}
		return pair;
	}

	protected CacophonyGlobals getGlobals() {
		return (CacophonyGlobals) super.getGlobals();
	}



}

