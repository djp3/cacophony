package edu.uci.ics.luci.cacophony.server.responder;

import java.util.Map;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

import edu.uci.ics.luci.cacophony.node.CNode;

/**
 * Generally a CNodeReponse looks like a JSON Object with either an "errors" key or 
 * a "responses" key.  If there is no "errors" key then there were no errors.
 * 
 * This class should be subclassed and "handle" should be overridden to provide 
 * functionality.
 * 
 * @author djp3
 *
 */
public abstract class CNodeServerResponder {
	
	private static transient volatile Logger log = null;
	protected static Logger getLog(){
		if(log == null){
			log = LogManager.getLogger(CNodeServerResponder.class);
		}
		return log;
	}
	
	private JSONArray responses = new JSONArray();
	private JSONArray errors = new JSONArray();
	private Object lock = new Object();
	
	
	
	public Object getLock(){
		return lock;
	}
	
	/**
	 * Subclasses should use this to add an error to the existing collection of errors
	 * when handling a request
	 * @param error
	 */
	protected void appendError(Object error){
		synchronized(lock){
			this.errors.add(error);
		}
	}
	
	
	
	/**
	 * Subclasses can use this to overwrite all the errors with an array of their own
	 * construction.
	 * @param errors
	 */
	protected void replaceErrors(JSONArray errors){
		synchronized(lock){
			if(errors == null){
				getLog().error("Setting errors to null is unexpected.  Won't do it.");
			}
			else{
				this.errors = errors;
			}
		}
	}
	
	
	
	/**
	 * Subclasses should use this to add a response to the existing collection of errors
	 * when handling a request
	 * @param error
	 */
	protected void appendResponse(Object response){
		synchronized(lock){
			this.responses.add(response);
		}
	}
	
	
	
	/**
	 * Subclasses can use this to overwrite all the responses with an array of their own
	 * construction.
	 * @param responses
	 */
	protected void replaceResponses(JSONArray responses){
		synchronized(lock){
			if(responses == null){
				getLog().error("Setting responses to null is unexpected.  Won't do it.");
			}
			else{
				this.responses = responses;
			}
		}
	}
	
	
	
	/**
	 * This is used by the CustomerService class to construct a response based on the errors
	 * reponses generated during handling.  If the handler doesn't provide errors or responses
	 * then this returns an empty array of responses.
	 * @return
	 */
	public JSONObject constructResponse(){
		
		synchronized(lock){
			JSONObject ret = new JSONObject();
		
			if(errors.size() != 0){
				ret.put("errors", errors);
			}
			else{
				ret.put("responses",responses);
			}
		
			return ret;
		}
	}
	
	/**
	 * Called right before a handle to clear any data structures necessary
	 */
	public void initialize(){
		synchronized(lock){
			errors.clear();
			responses.clear();
		}
	}
		
	
	/**
	 * See also initialize
	 * @param jo
	 * @param cnodes
	 */
	public abstract void handle(JSONObject jo,Map<String,CNode> cnodes);
	

}
