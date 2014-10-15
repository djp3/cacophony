package edu.uci.ics.luci.cacophony.node;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.type.UnknownTypeException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import edu.uci.ics.luci.utility.webserver.WebUtil;

public class StringExtractorHTML {
	
	public static String extract(String xPath, String regEx, String htmlContent) throws XPathExpressionException {
		String ret = null;
		if (htmlContent == null) {
			ret = null;
		}
		else {
			Tidy tidy = new Tidy();
			tidy.setXHTML(true);
			tidy.setQuiet(true);
			tidy.setXmlOut(true);
			tidy.setShowWarnings(false);
			tidy.setTrimEmptyElements(false);
		
			StringReader sr = new StringReader(htmlContent);
			Document doc = tidy.parseDOM(sr, null);
			Node node = (Node)XPathFactory.newInstance().newXPath().evaluate(xPath, doc, XPathConstants.NODE);
			if (node != null) {
				String text = collectTextPreorder(node);
				if (regEx == null || regEx.trim().equals("")) {
					ret = text;
				}
				else {
					Matcher matcher = Pattern.compile(regEx).matcher(text);
					if (matcher.find()) {
						ret = matcher.group(1);
					}
				}
			}
			else {
				throw new IllegalArgumentException("Node is null. XPath may be incorrect or invalid.");
			}
		}
		
		return ret;
	}
	
	private static String collectTextPreorder(Node current) {
		if (current.getNodeType() == Node.TEXT_NODE) {
			return current.getNodeValue();
		}
		else if (current.getNodeType() == Node.ELEMENT_NODE) {
			String nodeValue = "";
			NodeList childNodes = current.getChildNodes();
			for (int i=0; i<childNodes.getLength(); ++i) {
				nodeValue += collectTextPreorder(childNodes.item(i));
			}
			return nodeValue;
		}
		else {
			throw new UnsupportedOperationException("Encountered node of unhandled type. Type is: " + current.getNodeType());
		}
	}

	public static String fetchAndExtract(String url, String xPath, String regEx) throws MalformedURLException, IOException, XPathExpressionException {
		String html = WebUtil.fetchWebPage(url, false, null, 10000);
		return extract(xPath, regEx, html);
	}

}
