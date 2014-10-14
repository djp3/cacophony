package edu.uci.ics.luci.cacophony.web;

import java.net.InetAddress;
import java.util.Map;

import edu.uci.ics.luci.utility.datastructure.Pair;
import edu.uci.ics.luci.utility.webserver.HandlerAbstract;
import edu.uci.ics.luci.utility.webserver.RequestDispatcher.HTTPRequest;

public class HandlerCNodeLauncher extends HandlerAbstract {

	@Override
	public Pair<byte[], byte[]> handle(InetAddress ip, HTTPRequest httpRequestType, Map<String, String> headers, String restFunction, Map<String, String> parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HandlerAbstract copy() {
		// TODO Auto-generated method stub
		return null;
	}

}
