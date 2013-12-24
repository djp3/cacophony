package edu.uci.ics.luci.cacophony.server;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.uci.ics.luci.p2pinterface.P2PSink;

/* This is a sink class that checks to make sure that responses are correct for testing P2P actions with a 
 * CNodeServer*/
public class P2PSinkTest implements P2PSink {

	private CNodeServer cns;
	private String fail = null;
	private List<String> pass = new ArrayList<String>();
		
	public String getFail() {
		return fail;
	}
		

	public P2PSinkTest(CNodeServer cNodeServer) {
		this.cns = cNodeServer;
	}
		
	public void addPassPhrase(String phrase){
		pass.add(phrase);
	}
	
	public int getNumberOfPassPhrases(){
		return pass.size();
	}
		

	/**
	 *  This function decodes the incoming messages based on knowing the conventions of the sender.
	 *  Namely that a "null" message element type means the element is UTF-8 encoded bytes and
	 *  there are no other payloads.
	 * @return
	 */
	private String incomingHelper(Map<String, byte []> map){
		
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
		String deleteUs = null;
		String incomingMessage = incomingHelper(map);
		boolean ok = false;
		for(String s:pass){
			if(s.equals(incomingMessage)){
				deleteUs = s;
				ok = true;
			}
		}
		if(deleteUs != null){
			pass.remove(deleteUs);
		}
		if(!ok){
			this.cns.stop();
			fail = "Received an unexpected response:\n"+incomingMessage;
		}
	}
}
