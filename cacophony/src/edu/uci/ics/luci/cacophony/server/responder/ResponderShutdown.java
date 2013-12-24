package edu.uci.ics.luci.cacophony.server.responder;

import java.util.HashMap;

import edu.uci.ics.luci.utility.Quittable;

import net.minidev.json.JSONObject;


public class ResponderShutdown extends CNodeServerResponder {
	
	private Quittable cNodeServer;

	public ResponderShutdown(Quittable cNodeServer){
		this.cNodeServer = cNodeServer;
	}

	@Override
	public void handle(JSONObject jo,HashMap<String,JSONObject> configurations) {
		cNodeServer.setQuitting(true);
	}

}
