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
	public void testExtractDataDegenerative() {
		String data = null;
		String xpath = null;
		try {
			data = ExtractDataFromHTML.extractData(null,null,null,null);
			assertTrue(data == null);
			
			xpath="/foo";
			data = ExtractDataFromHTML.extractData(xpath,null,
				"<html><body><div class=\"sensor_data\">&lt;&lt;UCInet Mobile Access:00:19:a9:54:59:ae&gt;:-62&gt;</div></body></html>",
				null);
			assertTrue(data == null);
			
			xpath="/html/body/div";
			data = ExtractDataFromHTML.extractData(xpath,null,
				"<html><body><div class=\"sensor_data\">&lt;&lt;UCInet Mobile Access:00:19:a9:54:59:ae&gt;:-62&gt;</div></body></html>",
				null);
			assertEquals(data,"<<UCInet Mobile Access:00:19:a9:54:59:ae>:-62>");
			
			xpath="/html/body/div";
			data = ExtractDataFromHTML.extractData(xpath," ",
				"<html><body><div class=\"sensor_data\">&lt;&lt;UCInet Mobile Access:00:19:a9:54:59:ae&gt;:-62&gt;</div></body></html>",
				null);
			assertEquals(data,"<<UCInet Mobile Access:00:19:a9:54:59:ae>:-62>");
		} catch (XPathExpressionException e) {
			fail("There was a problem with the XPath expression '" + xpath + "'\n" + e);
		}
		
		try {
			xpath="=/html/body/div";
			data = ExtractDataFromHTML.extractData(xpath," ",
				"<html><body><div class=\"sensor_data\">&lt;&lt;UCInet Mobile Access:00:19:a9:54:59:ae&gt;:-62&gt;</div></body></html>",
				null);
			fail("There was a problem with the XPath expression because it should have thrown an exception " + xpath);
		} catch (XPathExpressionException e) {
		}
		
	}

	@Test
	public void testExtractData() {
		
		String data = null;
		String xpath = "/html/body/div";
		try {
			data = ExtractDataFromHTML.extractData(xpath,
				"<<(.*):[0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*>:[-]?[0-9][0-9]*>",
				"<html><body><div class=\"sensor_data\">&lt;&lt;UCInet Mobile Access:00:19:a9:54:59:ae&gt;:-62&gt;</div></body></html>",
				null);
		
			assertEquals("UCInet Mobile Access", data);
		
			data = ExtractDataFromHTML.extractData(xpath,
				"<<.*:([0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*)>:[-]?[0-9][0-9]*>",
				"<html><body><div class=\"sensor_data\">&lt;&lt;UCInet Mobile Access:00:19:a9:54:59:ae&gt;:-62&gt;</div></body></html>",
				null);
			assertEquals("00:19:a9:54:59:ae",data);
		} catch (XPathExpressionException e) {
			fail("There was a problem with the XPath expression '" + xpath + "'\n" + e);
		}
	}

	@Test
	public void testFetchAndExtractData() {
		String url = "http://www.cnn.com";
		String xpath = "//*[@id=\"cnn_ftrcntntinner\"]/div[9]/div[1]/text()[2]";
		try {
			String data = ExtractDataFromHTML.fetchAndExtractData(url, xpath, "(.*)", null);
			assertEquals(" All Rights Reserved.", data);
		} catch (MalformedURLException e) {
			fail("The URL '" + url + "' is invalid\n" + e);
		} catch (IOException e) {
			fail("There was a problem fetching and extracting data for the URL '" + url + "'\n" + e);
		} catch (XPathExpressionException e) {
			fail("There was a problem with the XPath expression '" + xpath + "'\n" + e);
		}
	}

}
