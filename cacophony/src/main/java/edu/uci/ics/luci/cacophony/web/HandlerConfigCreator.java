package edu.uci.ics.luci.cacophony.web;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import edu.uci.ics.luci.utility.datastructure.Pair;
import edu.uci.ics.luci.utility.webserver.HandlerAbstract;
import edu.uci.ics.luci.utility.webserver.RequestDispatcher.HTTPRequest;

public class HandlerConfigCreator extends HandlerAbstract {

	@Override
	public Pair<byte[], byte[]> handle(InetAddress ip, HTTPRequest httpRequestType, Map<String, String> headers, String restFunction, Map<String, String> parameters) {
		String url = parameters.get("url");
		Document doc;
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		Element base = doc.head().prependElement("base");
		base.attr("href", url);
		
		Element jquery = doc.head().appendElement("script");
		jquery.attr("type", "text/javascript");
		jquery.attr("src", "//ajax.googleapis.com/ajax/libs/jquery/1.8.2/jquery.min.js");
		
		Element jqueryUI = doc.head().appendElement("script");
		jqueryUI.attr("type", "text/javascript");
		jqueryUI.attr("src", "//ajax.googleapis.com/ajax/libs/jqueryui/1.9.0/jquery-ui.min.js");
		
		Element clickTrackJS = doc.head().appendElement("script");
		clickTrackJS.attr("type", "text/javascript");
		clickTrackJS.attr("src", "//localhost/cacophony.js");
		
		Element css = doc.head().appendElement("link");
		css.attr("rel", "stylesheet");
		css.attr("type", "text/css");
		css.attr("href", "//localhost/cacophony.css");
		
		Element div = doc.body().appendElement("div");
		div.attr("id", "cacophony_config");
		Element button = div.appendElement("button");
		button.attr("id", "show_cacophony_config");
		button.text("Click on a target and then click the button to launch a CNode.");
		
		Pair<byte[],byte[]> pair = new Pair<byte[],byte[]>(HandlerAbstract.getContentTypeHeader_HTML(), doc.toString().getBytes());
		return pair;
	}

	@Override
	public HandlerAbstract copy() {
		return new HandlerConfigCreator();
	}

}
