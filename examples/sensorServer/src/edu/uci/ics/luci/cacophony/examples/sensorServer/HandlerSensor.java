/*
	Copyright 2007-2013
		University of California, Irvine (c/o Donald J. Patterson)
*/
/*
	This file is part of the Laboratory for Ubiquitous Computing java Utility package, i.e. "Utilities"

    Utilities is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Utilities is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Utilities.  If not, see <http://www.gnu.org/licenses/>.
*/

package edu.uci.ics.luci.cacophony.examples.sensorServer;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.uci.ics.luci.cacophony.sensors.Abstract;
import edu.uci.ics.luci.cacophony.sensors.Accelerometer;
import edu.uci.ics.luci.cacophony.sensors.IPAddress;
import edu.uci.ics.luci.cacophony.sensors.Idle;
import edu.uci.ics.luci.cacophony.sensors.Light;
import edu.uci.ics.luci.cacophony.sensors.PowerSource;
import edu.uci.ics.luci.cacophony.sensors.Process;
import edu.uci.ics.luci.cacophony.sensors.UIActivity;
import edu.uci.ics.luci.cacophony.sensors.Volume;
import edu.uci.ics.luci.cacophony.sensors.WiFi;
import edu.uci.ics.luci.utility.datastructure.Pair;
import edu.uci.ics.luci.utility.webserver.HandlerAbstract;
import edu.uci.ics.luci.utility.webserver.RequestDispatcher.HTTPRequest;


public class HandlerSensor extends HandlerAbstract {
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(HandlerSensor.class);
		}
		return log;
	}


	public HandlerSensor() {
		super();
	}
	
	@Override
	public HandlerSensor copy() {
		return new HandlerSensor();
	}
	
	

	/**
	 * @return the version of the API are we implementing
	 */
	public static String getAPIVersion() {
		return Server.API_VERSION;
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
	
	
	/**
	 * This returns the version number.
	 * @param parameters a map of key and value that was passed through the REST request
	 * @return a pair where the first element is the content type and the bytes are the output bytes to send back
	 */
	@Override
	public Pair<byte[], byte[]> handle(InetAddress ip, HTTPRequest httpRequestType, Map<String, String> headers, String restFunction, Map<String, String> parameters) {
		
		String release;
		if((release = versionOK(parameters)) != null){
			return failResponse(release,restFunction,parameters);
		}
		
		String sensorString = parameters.get("sensor");
		
		Abstract sensor = null;
		
		if(sensorString.toLowerCase().equals("accelerometer")){
			sensor = Accelerometer.getSensor();
		}else if(sensorString.toLowerCase().equals("idle")){
			sensor = Idle.getSensor();
		}else if(sensorString.toLowerCase().equals("ipaddress")){
			sensor = IPAddress.getSensor();
		}else if(sensorString.toLowerCase().equals("light")){
			sensor = Light.getSensor();
		}else if(sensorString.toLowerCase().equals("powersource")){
			sensor = PowerSource.getSensor();
		}else if(sensorString.toLowerCase().equals("process")){
			sensor = Process.getSensor();
		}else if(sensorString.toLowerCase().equals("uiactivity")){
			sensor = UIActivity.getSensor();
		}else if(sensorString.toLowerCase().equals("volume")){
			sensor = Volume.getSensor();
		}else if(sensorString.toLowerCase().equals("wifi")){
			sensor = WiFi.getSensor();
		}
		
		String format = parameters.get("format");
		Pair<byte[],byte[]> response;
		if((format!= null) && format.equals("html")){
			StringBuffer sb = new StringBuffer();
			sb.append("<html>");
			sb.append("<body>");
			sb.append(escapeHtml(sensor.sense().toString()));
			sb.append("</body>");
			sb.append("</html>");
			response = new Pair<byte[],byte[]>(HandlerAbstract.getContentTypeHeader_HTML(),sb.toString().getBytes());
		}
		if((format!= null) && format.equals("jsonp")){
			JSONObject j = new JSONObject();
			try {
				j.put("error", "false");
				j.put("value", sensor.sense().toString());
			} catch (JSONException e) {
				try {
					j.put("value", "Unable to parse:1");
				} catch (JSONException e1) {
					response = new Pair<byte[],byte[]>(HandlerAbstract.getContentTypeHeader_JSON(),wrapCallback(parameters,"{\"value\",\"Unable to parse:2\"}").getBytes());
				}
			}
			response = new Pair<byte[],byte[]>(HandlerAbstract.getContentTypeHeader_JSON(),wrapCallback(parameters,j.toString()).getBytes());
		}
		else{
			JSONObject j = new JSONObject();
			try {
				j.put("error", "false");
				j.put("value", sensor.sense().toString());
			} catch (JSONException e) {
				try {
					j.put("value", "Unable to parse:1");
				} catch (JSONException e1) {
					response = new Pair<byte[],byte[]>(HandlerAbstract.getContentTypeHeader_JSON(),"{\"value\",\"Unable to parse:2\"}".getBytes());
				}
			}
			response = new Pair<byte[],byte[]>(HandlerAbstract.getContentTypeHeader_JSON(),j.toString().getBytes());
		}
		
		return response;
	}

}


