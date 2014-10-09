package edu.uci.ics.luci.cacophony.server.responder;


import java.util.Map;

import net.minidev.json.JSONObject;
import edu.uci.ics.luci.cacophony.node.CNode;
import edu.uci.ics.luci.utility.Quittable;


public class ResponderShutdown extends CNodeServerResponder {
	
	private Quittable cNodeServer;

	public ResponderShutdown(Quittable cNodeServer){
		if(cNodeServer == null){
			throw new IllegalArgumentException("Can't initialize with a null cNodeServer");
		}
		this.cNodeServer = cNodeServer;
	}

	@Override
	public void handle(JSONObject jo, Map<String, CNode> cnodes) {
		cNodeServer.setQuitting(true);
	}

}
