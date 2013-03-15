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

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.tidy.Tidy;

import edu.uci.ics.luci.utility.webserver.WebUtil;

public class ExtractDataFromHTML {
	
	public static String extractData(String xPathString, String regEx, String htmlContent) throws XPathExpressionException {
		Tidy tidy = new Tidy();
		tidy.setXHTML(true);
		tidy.setQuiet(true);
		tidy.setXmlOut(true);
		tidy.setShowWarnings(false);
		tidy.setTrimEmptyElements(false);
		
		StringReader sr = new StringReader(htmlContent);
		Document doc = tidy.parseDOM(sr, null);
		XPath xPath = XPathFactory.newInstance().newXPath();
		Node node = (Node)xPath.evaluate(xPathString, doc, XPathConstants.NODE);
		if (node != null) {
			// Text only appears at leaves in the DOM tree, so check if the node specified by the XPath is a leaf.
			// If it's not a leaf, try getting text from its first child.
			String nodeValue = (node.getFirstChild() == null ? node.getNodeValue() : node.getFirstChild().getNodeValue());
			if (regEx == null || regEx.trim().equals("")) {
				return nodeValue;
			}
			Matcher matcher = Pattern.compile(regEx).matcher(nodeValue);
			if (matcher.find()) {
				return matcher.group(1);
			}
		}
		return null; //TODO: Instead of returning null, should we throw an exception?
	}

	public static String fetchAndExtractData(String url, String xPathString, String regEx) throws MalformedURLException, IOException, XPathExpressionException {
		String html = WebUtil.fetchWebPage(url, false, null, 10000);
		return extractData(xPathString, regEx, html);
	}

}
