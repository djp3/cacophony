package edu.uci.ics.luci.cacophony.server;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Map.Entry;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;
import net.minidev.json.JSONValue;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

import edu.uci.ics.luci.cacophony.node.CNode;
import edu.uci.ics.luci.cacophony.server.responder.CNodeServerResponder;
import edu.uci.ics.luci.cacophony.server.responder.ResponderGeneric;
import edu.uci.ics.luci.p2pinterface.P2PInterface;
import edu.uci.ics.luci.p2pinterface.P2PSink;

/**
 * Hello There! What can I do for you?
 * @author djp3
 *
 */
public class CustomerService implements P2PSink{
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = LogManager.getLogger(CustomerService.class);
		}
		return log;
	}
	
	private P2PInterface p2p = null;
	private Map<String,CNodeServerResponder> handlers;
	private Map<String, CNode> cNodes;
	
	/**
	 * 
	 * @param configurations 
	 * @param handlers, It is a HashMap so that a null entry can be explicitly made for incoming
	 * packets that don't have a valid "request"
	 */
	public CustomerService(Map<String,CNodeServerResponder> handlers, Map<String, CNode> cNodes){
		if(handlers == null){
			throw new IllegalArgumentException("You can't initialize a CustomerService object with handlers equal to null");
		}
		else{
			this.handlers = handlers;
		}
		
		if(cNodes == null){
			throw new IllegalArgumentException("You can't initialize a CustomerService object with cNodes equal to null");
		}
		else{
			this.cNodes = cNodes;
		}
	}
	
	
	/**
	 * This is a generic handler which dispatches incoming requests to the appropriate
	 * handler
	 */
	public void incoming(Map<String, byte[]> map) {

		for(Entry<String, byte[]> e :map.entrySet()){
			String key = e.getKey();
			if((key == null) || (key.equals(""))){
				String incomingString = null;
				try{
					incomingString = new String(e.getValue(),"UTF-8");
				} catch (UnsupportedEncodingException e1) {
					getLog().error("Unable to reconstruct UTF-8 string from incoming byte packet:\n"+e.getValue());
					continue;
				}
				
				JSONObject jo = null;
				try{
					jo = (JSONObject) JSONValue.parse(incomingString);
				} catch (ClassCastException e1) {
					getLog().error("Unable to construct JSONObject from incoming string, perhaps it isn't a JSONObject, but some other kind of JSON thing?:\n"+incomingString);
					continue;
				}
				catch(RuntimeException e1){
					getLog().error("Unable to parse incoming string as JSON:\n"+incomingString);
					continue;
				}
				
				/* "from" isn't strictly necessary.  Not having a from means no response is returned */
				String from = null;
				try{
					from = (String) jo.get("from");
				} catch (ClassCastException e1) {
					getLog().error("Unable to make the \"from\" in the incoming JSON into a String\n"+jo.toJSONString(JSONStyle.NO_COMPRESS));
					continue;
				}
				catch(RuntimeException e1){
					getLog().error("Unable to find the \"from\" in the incoming JSON\n"+jo.toJSONString(JSONStyle.NO_COMPRESS));
					continue;
				}
				
				String request = null;
				try{
					request = (String) jo.get("request");
				} catch (ClassCastException e1) {
					getLog().error("Unable to make the \"request\" in the incoming JSON into a String\n"+jo.toJSONString(JSONStyle.NO_COMPRESS));
					continue;
				}
				catch(RuntimeException e1){
					getLog().error("Unable to find the \"request\" in the incoming JSON\n"+jo.toJSONString(JSONStyle.NO_COMPRESS));
					continue;
				}
					
				final CNodeServerResponder cnr = handlers.get(request);
				JSONObject response = null;
				if(cnr == null){
					CNodeServerResponder placeholderResponse = new ResponderGeneric("The handler for the request, \""+request+"\", is explicitly null.  This indicates a configuration problem in the CNode",null);
					response = placeholderResponse.constructResponse();
				}
				else{
					JSONObject data = null;
					try{
						data = (JSONObject) jo.get("data");
					} catch (ClassCastException e1) {
						getLog().error("Unable to make the \"data\" in the incoming JSON into a JSONObject\n"+jo.toJSONString(JSONStyle.NO_COMPRESS));
					}
					catch(RuntimeException e1){
						getLog().error("Unable to find the \"data\" in the incoming JSON\n"+jo.toJSONString(JSONStyle.NO_COMPRESS));
					}
					
					synchronized(cnr.getLock()){
						cnr.initialize();
						cnr.handle(data,cNodes);
						response = cnr.constructResponse();
					}
				}
					
				if(from == null){
					/* No response necessary */
					getLog().info("Incoming request,\""+request+"\", required no response");
					continue;
				}
				else{
					if(p2p == null){
						getLog().error("Unable to responsd to request,\""+request+"\", because p2p interface is not initialized");
					}
					else{
						if(response == null){
							CNodeServerResponder placeholderResponse = new ResponderGeneric("The handler for the request, \""+request+"\", explicitly return null for a response. The handler was: "+cnr.getClass().getCanonicalName(),null);
							response = placeholderResponse.constructResponse();
						}
						p2p.sendMessage(from, response.toJSONString(JSONStyle.LT_COMPRESS));
					}
				}
			}
			else{
				getLog().error("Unknown incoming message type: "+e.getKey());
			}
		}
	}
	

	public void setP2P(P2PInterface p2p){
		this.p2p = p2p;
	}
	

}
