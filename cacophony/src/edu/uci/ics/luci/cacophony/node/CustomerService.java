package edu.uci.ics.luci.cacophony.node;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Map.Entry;

import net.minidev.json.JSONObject;

import edu.uci.ics.luci.p2pinterface.P2PSink;

/**
 * Hello There! What can I do for you?
 * @author djp3
 *
 */
public class CustomerService implements P2PSink{
	
	/**
	 *  This function decodes the incoming messages based on knowing the conventions of the sender.
	 *  Namely that a "null" message element type means the element is UTF-8 encoded bytes and
	 *  there are no other payloads.
	 * @return
	 */
	private JSONObject incomingHelper(Map<String, byte []> map){
		
		for(Entry<String, byte[]> e :map.entrySet()){
			String key = e.getKey();
			if((key == null) || (key.equals(""))){
				try {
					return new String(e.getValue(),"UTF-8");
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
			}
		}
		return null;
	}
	
	public void incoming(Map<String, byte[]> map) {
		String incomingMessage = incomingHelper(map);
		System.out.println(incomingMessage);
		
		synchronized(lock){
			messageReceived = true;
			lock.notifyAll();
		}
	}
	

	public void incoming(Map<String, byte[]> arg0) {
		System.out.println("Just got a message");
	}
	

}
