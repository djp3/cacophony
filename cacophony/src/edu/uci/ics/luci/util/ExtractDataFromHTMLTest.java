package edu.uci.ics.luci.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.xml.xpath.XPathExpressionException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ExtractDataFromHTMLTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testExtractData() {
		
		String data = null;
		String xpath = "/html/body/div";
		try {
			data = ExtractDataFromHTML.extractData(xpath,
				"<<(.*):[0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*>:[-]?[0-9][0-9]*>",
				"<html><body><div class=\"sensor_data\">&lt;&lt;UCInet Mobile Access:00:19:a9:54:59:ae&gt;:-62&gt;</div></body></html>");
		
			assertEquals("UCInet Mobile Access", data);
		
			data = ExtractDataFromHTML.extractData(xpath,
				"<<.*:([0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*)>:[-]?[0-9][0-9]*>",
				"<html><body><div class=\"sensor_data\">&lt;&lt;UCInet Mobile Access:00:19:a9:54:59:ae&gt;:-62&gt;</div></body></html>");
		assertEquals(data,"00:19:a9:54:59:ae");
		} catch (XPathExpressionException e) {
			fail("There was a problem with the XPath expression '" + xpath + "'" + e);
		}
	}

	@Test
	public void testFetchAndExtractData() {
		String url = "http://www.cnn.com";
		String xpath = "//*[@id=\"cnn_ftrcntntinner\"]/div[9]/div[1]/text()[2]";
		try {
			String data = ExtractDataFromHTML.fetchAndExtractData(url, xpath, "(.*)");
			assertEquals(" All Rights Reserved.", data);
		} catch (MalformedURLException e) {
			fail("The URL '" + url + "' is invalid" + e);
		} catch (IOException e) {
			fail("There was a problem fetching and extracting data for the URL '" + url + "'" + e);
		} catch (XPathExpressionException e) {
			fail("There was a problem with the XPath expression '" + xpath + "'" + e);
		}
	}

}
