package edu.uci.ics.luci.cacophony.web;

import java.net.InetAddress;
import java.util.Map;

import edu.uci.ics.luci.cacophony.node.CNode;
import edu.uci.ics.luci.cacophony.node.CNodeConfiguration;
import edu.uci.ics.luci.cacophony.node.SensorConfig;
import edu.uci.ics.luci.cacophony.node.StorageException;
import edu.uci.ics.luci.cacophony.node.Translator;
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
		String ID = name + "_" + url + "_" + path; // TODO: should ID parameter be something guaranteed to be unique? Should the name and ID be the same or different?
		SensorConfig target = new SensorConfig(ID, name, url, format, regex, path, translator, null); 
		
		CNodeConfiguration config = new CNodeConfiguration(ID, target);
		CNode cNode;
		try {
			cNode = new CNode(config);
		} catch (StorageException e) {
			e.printStackTrace();
			return new Pair<byte[],byte[]>(HandlerAbstract.getContentTypeHeader_HTML(), "Error".getBytes());
		}
		cNodeServer.getCNodes().put(ID, cNode);
		cNodeServer.launch(ID);
		
		Pair<byte[],byte[]> pair = new Pair<byte[],byte[]>(HandlerAbstract.getContentTypeHeader_HTML(), "CNode Launched".getBytes());
		return pair;
	}

	@Override
	public HandlerAbstract copy() {
		return new HandlerCNodeLauncher(cNodeServer);
	}

}
