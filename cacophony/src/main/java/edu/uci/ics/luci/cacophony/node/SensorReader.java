package edu.uci.ics.luci.cacophony.node;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.Callable;

import javax.xml.xpath.XPathExpressionException;

public class SensorReader implements Callable<SensorReading> {
	private final SensorConfig sensorConfig;
	
	SensorReader(SensorConfig config) {
		this.sensorConfig = config;
	}
	
	public SensorReading call(){
		String extractedString = null;
		try {
			if(sensorConfig.getFormat().equals("html")){
				extractedString = StringExtractorHTML.fetchAndExtract(sensorConfig.getURL(), sensorConfig.getPathExpression(), sensorConfig.getRegEx());
			}
			else if(sensorConfig.getFormat().equals("json")){
				extractedString = StringExtractorJSON.fetchAndExtract(sensorConfig.getURL(), sensorConfig.getPathExpression(), sensorConfig.getRegEx());
			}
			else{
				// TODO: implement logging
				// getLog().warn("Unrecognized CNode format: " + format);
			}
		} catch (MalformedURLException e) {
			// TODO: implement logging
			// getLog().error("The URL '" + url + "' is invalid\n" + e);
		} catch (XPathExpressionException e) {
			// TODO: implement logging
			// getLog().error("There was a problem with the XPath expression '" + dataPath + "'\n" + e);
		} catch (IOException e) {
			// TODO: implement logging
			// getLog().error("There was a problem fetching and extracting data for the URL '" + url + "'\n" + e);
		}
		return new SensorReading(sensorConfig, extractedString);
	}
}
