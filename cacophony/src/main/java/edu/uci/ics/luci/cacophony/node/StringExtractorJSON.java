package edu.uci.ics.luci.cacophony.node;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jayway.jsonpath.*;

import edu.uci.ics.luci.utility.webserver.WebUtil;

public class StringExtractorJSON {
	
	public static String extract(String jsonPath, String regEx, String jsonContent) {
		String ret = null;
		if (jsonContent == null) {
			ret = null;
		}
		else {
			Object jsonValueRaw = JsonPath.read(jsonContent, jsonPath);
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

	public static String fetchAndExtract(String url, String jsonPath, String regEx) throws MalformedURLException, IOException {
		String jsonContent = WebUtil.fetchWebPage(url, false, null, 10000);
		return extract(jsonPath, regEx, jsonContent);
	}

// TODO: Decide if we really want to have an overload of fetchAndExtractData that takes a FailoverFetch object.
//	public static String fetchAndExtractData(FailoverFetch failoverFetch, String url, String jsonPath, String regEx, Translator<?> translator) throws MalformedURLException, IOException, JSONException {
//		JSONObject json = failoverFetch.fetchJSONObject(url, false, null, 10000);
//		return extractData(jsonPath, regEx, json, translator);
//	}
}
