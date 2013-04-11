package edu.uci.ics.luci.util;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.tidy.Tidy;

import edu.uci.ics.luci.cacophony.node.Translator;
import edu.uci.ics.luci.utility.webserver.WebUtil;

	//TODO: John to implement and clean this up
public class ExtractDataFromJSON {
	
	//TODO: John to implement and clean this up
	public static String extractData(String jsonPathString, String regEx, Translator<?> t,String htmlContent) throws XPathExpressionException {
		
		String ret = null;
		if(htmlContent == null){
			ret = null;
		}
		else{
			Tidy tidy = new Tidy();
			tidy.setXHTML(true);
			tidy.setQuiet(true);
			tidy.setXmlOut(true);
			tidy.setShowWarnings(false);
			tidy.setTrimEmptyElements(false);
		
			StringReader sr = new StringReader(htmlContent);
			Document doc = tidy.parseDOM(sr, null);
			XPath xPath = XPathFactory.newInstance().newXPath();
			Node node = (Node)xPath.evaluate(jsonPathString, doc, XPathConstants.NODE);
			if (node != null) {
				// Text only appears at leaves in the DOM tree, so check if the node specified by the XPath is a leaf.
				// If it's not a leaf, try getting text from its first child.
				String nodeValue = (node.getFirstChild() == null ? node.getNodeValue() : node.getFirstChild().getNodeValue());
				if (regEx == null || regEx.trim().equals("")) {
					ret = nodeValue;
				}
				else{
					Matcher matcher = Pattern.compile(regEx).matcher(nodeValue);
					if (matcher.find()) {
						ret = matcher.group(1);
					}
				}
			}
		}
		return ret;
	}

	//TODO: John to implement and clean this up
	public static String fetchAndExtractData(String url, String xPathString, String regEx) throws MalformedURLException, IOException, XPathExpressionException {
		String json = WebUtil.fetchWebPage(url, false, null, 10000);
		return extractData(xPathString, regEx, html);
	}
	
	//TODO: John to implement and clean this up
	public static String fetchAndExtractData(FailoverFetch failoverFetch,String url, String jsonPathString, String regEx,Translator<?> translator) throws MalformedURLException, IOException, XPathExpressionException {
		
		JSONObject json = failoverFetch.fetchJSONObject(url, false, null, 10000);
		return extractData(jsonPathString, regEx, json);
	}

}
