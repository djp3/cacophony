package edu.uci.ics.luci.cacophony.api.node;
import java.net.InetAddress;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import org.apache.log4j.Logger;

import edu.uci.ics.luci.cacophony.node.CNode;
import edu.uci.ics.luci.cacophony.node.CNodePool;
import edu.uci.ics.luci.utility.datastructure.Pair;
import edu.uci.ics.luci.utility.webserver.HandlerAbstract;
import edu.uci.ics.luci.utility.webserver.RequestDispatcher.HTTPRequest;


public class HandlerCNodePrediction extends NodeRequestHandlerHelper {
	
	private static transient volatile Logger log = null;

	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(HandlerCNodePrediction.class);
		}
		return log;
	}

	private CNodePool cnp = null;
	
	public HandlerCNodePrediction(CNodePool cnp) {
		this.cnp = cnp;
	}
	
	@Override
	public HandlerCNodePrediction copy() {
		return new HandlerCNodePrediction(this.cnp);
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
		
		Map<String,TreeSet<Pair<Long,Double>>> prediction = null;
		
		String nodeToPredict = parameters.get("node");
		if(nodeToPredict != null){
			
			String _timesToPredict = parameters.get("times");
			if(_timesToPredict != null){
				try {
					JSONArray timesToPredict = (JSONArray) JSONValue.parse(_timesToPredict);
					CNode fromPool = cnp.getFromPool(nodeToPredict);
					if(fromPool != null){
						prediction = fromPool.predict(timesToPredict);
					}
					else{
						errors.add("No model currently exists for "+nodeToPredict);
					}
				} catch (ClassCastException e) {
					getLog().error("Unable to parse JSON for times:"+_timesToPredict);
					errors.add("Unable to parse JSON for times:"+_timesToPredict);
				}
			}
			else{
				/* Day of the week -> <time since 4am, wait time>*/
				CNode fromPool = cnp.getFromPool(nodeToPredict);
				if(fromPool != null){
					prediction = fromPool.predict(null);
				}
				else{
					errors.add("No model currently exists for "+nodeToPredict);
				}
			}
			
			if(prediction != null){
				for(Entry<String, TreeSet<Pair<Long, Double>>> foo:prediction.entrySet()){
					JSONArray dayData = new JSONArray();
					for(Pair<Long, Double> guess:foo.getValue()){
						JSONObject element = new JSONObject();
						element.put("x", guess.getFirst().toString());
						element.put("y", guess.getSecond().toString());
						dayData.add(element);
					}
					ret.put(foo.getKey(),dayData);
				}
			}
			else{
				errors.add("Prediction unavailable.  We may be recalculating model right now.");
			}
		}
		else{
			errors.add("No \"node\" parameter present to predict");
		}
		
		CNode fromPool = cnp.getFromPool(nodeToPredict);
		if(fromPool != null){
			ret.put("stats", fromPool.getAccuracy());
		}
		
		if(errors.size() == 0){
			ret.put("error", "false");
		}
		else{
			ret.put("error", "true");
			ret.put("errors",errors);
		}
		
		pair = new Pair<byte[],byte[]>(HandlerAbstract.getContentTypeHeader_JSON(),wrapCallback(parameters,ret.toString()).getBytes());
		return pair;
	}


}


