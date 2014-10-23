package edu.uci.ics.luci.cacophony.web;

import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;

import net.minidev.json.JSONObject;
import edu.uci.ics.luci.cacophony.node.CNode;
import edu.uci.ics.luci.cacophony.node.CNodeConfiguration;
import edu.uci.ics.luci.cacophony.node.SensorConfig;
import edu.uci.ics.luci.cacophony.node.StorageException;
import edu.uci.ics.luci.cacophony.node.TranslatorString;
import edu.uci.ics.luci.cacophony.server.CNodeServer;
import edu.uci.ics.luci.utility.datastructure.Pair;
import edu.uci.ics.luci.utility.webserver.HandlerAbstract;
import edu.uci.ics.luci.utility.webserver.RequestDispatcher.HTTPRequest;

public class HandlerCNodeLauncher extends HandlerAbstract {
	private CNodeServer cNodeServer;
	
	public HandlerCNodeLauncher(CNodeServer cNodeServer) {
		this.cNodeServer = cNodeServer;
	}
	
	@Override
	public Pair<byte[], byte[]> handle(InetAddress ip, HTTPRequest httpRequestType, Map<String, String> headers, String restFunction, Map<String, String> parameters) {
		String name = parameters.get("name");
		String url = parameters.get("url");
		String format = parameters.get("format");
		String path = parameters.get("path");
		String regex = parameters.get("regex");
		// TODO: add support for translator
		
		TranslatorString translator = new TranslatorString();
		String sensorID = UUID.randomUUID().toString();
		SensorConfig target = new SensorConfig(sensorID, name, url, format, regex, path, translator, null); 
		
		CNodeConfiguration config = new CNodeConfiguration("some_path", target); //TODO: figure out what path should be
		CNode cNode;
		String cnodeID = UUID.randomUUID().toString();
		try {
			cNode = new CNode(config, cnodeID);
		} catch (StorageException e) {
			e.printStackTrace();
			JSONObject ret = new JSONObject();
			ret.put("status", "Error");
			return new Pair<byte[],byte[]>(HandlerAbstract.getContentTypeHeader_JSON(), ret.toString().getBytes());
		}
		cNodeServer.getCNodes().put(cnodeID, cNode);
		cNodeServer.launch(cnodeID);
		
		JSONObject ret = new JSONObject();
		ret.put("status", "Launched CNode with ID " + cnodeID);
		
		return new Pair<byte[],byte[]>(HandlerAbstract.getContentTypeHeader_JSON(), ret.toString().getBytes());
	}

	@Override
	public HandlerAbstract copy() {
		return new HandlerCNodeLauncher(cNodeServer);
	}

}
