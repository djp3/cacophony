package edu.uci.ics.luci.cacophony.api.node;
import java.net.InetAddress;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.quub.util.Pair;
import com.quub.webserver.HandlerAbstract;
import com.quub.webserver.RequestDispatcher.HTTPRequest;

import edu.uci.ics.luci.cacophony.node.CNode;


public class HandlerCNodePrediction extends NodeRequestHandlerHelper {
	
	private static transient volatile Logger log = null;

	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(HandlerCNodePrediction.class);
		}
		return log;
	}

	private CNode cNode = null;
	
	public HandlerCNodePrediction(CNode cNode) {
		this.cNode = cNode;
	}
	
	@Override
	public HandlerCNodePrediction copy() {
		return new HandlerCNodePrediction(this.cNode);
	}

	@Override
	public Pair<byte[], byte[]> handle(InetAddress ip, HTTPRequest httpRequestType, Map<String, String> headers, String restFunction, Map<String, String> parameters) {
		
		Pair<byte[], byte[]> pair = null;
		
		String release;
		if((release = versionOK(parameters)) != null){
			return failResponse(release,restFunction,parameters);
		}
		
		JSONObject ret = new JSONObject();
		JSONArray errors = new JSONArray();
		
		String nodeToPredict = parameters.get("node");
		if(nodeToPredict != null){
			/* Day of the week -> <time since 4am, wait time>*/
			Map<String,TreeSet<Pair<Integer,Double>>> prediction = cNode.predict(nodeToPredict);
			
			if(prediction != null){
				for(Entry<String, TreeSet<Pair<Integer, Double>>> foo:prediction.entrySet()){
					try {
						JSONArray dayData = new JSONArray();
						for(Pair<Integer, Double> guess:foo.getValue()){
							JSONObject element = new JSONObject();
							element.put("x", guess.getFirst().toString());
							element.put("y", guess.getSecond().toString());
							dayData.put(element);
						}
						ret.put(foo.getKey(),dayData);
					} catch (JSONException e) {
						getLog().error("Unable to construct JSON :"+e);
						errors.put("I had a problem putting together my prediction");
					}
				}
			}
			else{
				errors.put("Prediction unavailable.  We may be recalculating model right now.");
			}
		}
		else{
			errors.put("No \"node\" parameter present to predict");
		}
		
		try{
			if(errors.length() == 0){
				ret.put("error", "false");
			}
			else{
				ret.put("error", "true");
				ret.put("errors",errors);
			}
		} catch (JSONException e) {
			getLog().error("Unable to respond with error:"+e);
			errors.put("I had a problem telling you about the error status");
		}
		
		pair = new Pair<byte[],byte[]>(HandlerAbstract.getContentTypeHeader_JSON(),wrapCallback(parameters,ret.toString()).getBytes());
		return pair;
	}


}


