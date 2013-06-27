package edu.uci.ics.luci.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import org.apache.log4j.Logger;

import com.jayway.jsonpath.JsonPath;

import edu.uci.ics.luci.cacophony.node.Translator;
import edu.uci.ics.luci.utility.webserver.WebUtil;

public class ExtractDataFromJSON {
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(ExtractDataFromJSON.class);
		}
		return log;
	}
	
	static JSONParser jsonParser = new JSONParser(JSONParser.MODE_PERMISSIVE);
	
	//TODO: John to add translator support
	public static String extractData(String jsonPath, String regEx, JSONObject jsonObject, Translator<?> translator) {
		String ret = null;
		if (jsonObject == null) {
			ret = null;
		}
		else {
			Object jsonValueRaw = JsonPath.read(jsonObject, jsonPath);
			if (jsonValueRaw instanceof List<?> && ((List<?>)jsonValueRaw).size() > 0) {
				// If the jsonPath specified a list of values, just choose the first value.
				jsonValueRaw = ((List<?>)jsonValueRaw).get(0);
			}
			if (jsonValueRaw == null) {
				ret = null;
			}
			else {
				String jsonValue = jsonValueRaw.toString();
				
				if (regEx == null || regEx.trim().equals("")) {
					ret = jsonValue;
				}
				else {
					Matcher matcher = Pattern.compile(regEx).matcher(jsonValue);
					if (matcher.find()) {
						ret = matcher.group(1);
					}
				}
			}
		}
		return ret;
	}

	public static String fetchAndExtractData(String url, String jsonPath, String regEx, Translator<?> translator) throws MalformedURLException, IOException {
		String ret = null;
		String response = WebUtil.fetchWebPage(url, false, null, 10000);
		
		JSONObject jsonObject = null;
		try {
			jsonObject = (JSONObject) jsonParser.parse(response);
			ret = extractData(jsonPath, regEx, jsonObject, translator);
		} catch (ParseException e) {
			getLog().warn("Received bad json from:"+url+"\n"+response);
			ret = null;
		}
		return ret;
	}

	public static String fetchAndExtractData(FailoverFetch failoverFetch, String url, String jsonPath, String regEx, Translator<?> translator) throws MalformedURLException, IOException{
		
		JSONObject json = failoverFetch.fetchJSONObject(url, false, null, 10000);
		return extractData(jsonPath, regEx, json, translator);
	}
}
