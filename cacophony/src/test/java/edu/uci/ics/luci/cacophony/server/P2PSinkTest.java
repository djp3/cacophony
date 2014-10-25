package edu.uci.ics.luci.cacophony.server;

import static org.junit.Assert.fail;

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
	private String incomingMessage = null;
		
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
	
	public String getPassPhrases(){
		StringBuilder sb = new StringBuilder();
		for(String s:pass){
			sb.append("\n");
			sb.append(s);
		}
		sb.append("\n");
		return sb.toString();
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
			if(incomingMessage.matches(s)){
				deleteUs = s;
				ok = true;
			}
		}
		if(deleteUs != null){
			pass.remove(deleteUs);
		}
		if(!ok){
			fail = "Received an unexpected response:\n"+incomingMessage;
			this.cns.stop();
		}
		this.incomingMessage = incomingMessage;
	}
	
	public String getIncomingMessage() {
		return incomingMessage;
	}


	/**
	 * Wait 10 seconds for a response
	 * @param p2pSinkTest
	 */
	public static String waitForResponse(P2PSinkTest p2pSinkTest) {
		int count = 0;
		while((p2pSinkTest.getNumberOfPassPhrases() > 0) && (p2pSinkTest.getFail() == null)){
			try {
				Thread.sleep(100);
				count++;
			} catch (InterruptedException e) {}
			if(count > 100){
				fail("Message did not arrive as expected:"+p2pSinkTest.getPassPhrases()+"\n"+p2pSinkTest.getFail());
			}
		}
		if(p2pSinkTest.getFail() != null){
			fail(p2pSinkTest.getFail());
		}
		return p2pSinkTest.getIncomingMessage();
	}
}
