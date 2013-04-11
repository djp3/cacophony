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

public class ExtractDataFromJSONTest {

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
	
	final static String jsonData = 
		"{ \"store\": {"+
		"    \"book\": [ "+
		"      { \"category\": \"reference\","+
		"        \"author\": \"Nigel Rees\","+
		"        \"title\": \"Sayings of the Century\","+
		"        \"price\": 8.95"+
		"      },"+
		"      { \"category\": \"fiction\","+
		"        \"author\": \"Evelyn Waugh\","+
		"        \"title\": \"Sword of Honour\","+
		"        \"price\": 12.99"+
		"      },"+
		"      { \"category\": \"fiction\","+
		"        \"author\": \"Herman Melville\","+
		"        \"title\": \"Moby Dick\","+
		"        \"isbn\": \"0-553-21311-3\","+
		"        \"price\": 8.99"+
		"      },"+
		"      { \"category\": \"fiction\","+
		"        \"author\": \"J. R. R. Tolkien\","+
		"        \"title\": \"The Lord of the Rings\","+
		"        \"isbn\": \"0-395-19395-8\","+
		"        \"price\": 22.99"+
		"      }"+
		"    ],"+
		"    \"bicycle\": {"+
		"      \"color\": \"red\","+
		"      \"price\": 19.95"+
		"    }"+
		"  }"+
		"}";

	
	@Test
	public void testExtractDataDegenerative() {
		String data = null;
		String jsonpath = null;
		try {
			data = ExtractDataFromJSON.extractData(null,null,null);
			assertTrue(data == null);
			
			jsonpath="$.error.book[*].author";
			data = ExtractDataFromJSON.extractData(jsonpath,null,jsonData);
			assertTrue(data == null);
			
			jsonpath="$.store.book[*].author";
			data = ExtractDataFromJSON.extractData(jsonpath,null,jsonData);
			assertEquals(data,"Nigel Rees");
			
			jsonpath="$.store.book[*].category";
			data = ExtractDataFromJSON.extractData(jsonpath," ",jsonData);
			assertEquals(data,"reference");
		} catch (XPathExpressionException e) {
			fail("There was a problem with the XPath expression '" + jsonpath + "'" + e);
		}
		
		try{
			jsonpath="=$.store.book[*].category";
			ExtractDataFromJSON.extractData(jsonpath," ",jsonData);
			fail("There was a problem with the method because it should have thrown an exception" + jsonpath);
		} catch (XPathExpressionException e) {
		}
		
	}

	@Test
	public void testExtractData() {
		
		String data = null;
		String jsonpath="$.store.book[*].isbn";
		try {
			data = ExtractDataFromJSON.extractData(jsonpath,"[^-]*-([0-9]*)-[0-9]*-[0-9]",jsonData);
			assertEquals("553", data);
			
			data = ExtractDataFromJSON.extractData(jsonpath,"[^-]*-[0-9]*-([0-9]*)-[0-9]",jsonData);
			assertEquals(data,"21311");
		} catch (XPathExpressionException e) {
			fail("There was a problem with the JSONPATT expression '" + jsonpath + "'" + e);
		}
	}

	@Test
	public void testFetchAndExtractData() {
		String url = "http://data.mtgox.com/api/1/BTCUSD/ticker";
		String jsonpath = "$.return.vol.currency";
		try {
			String data = ExtractDataFromJSON.fetchAndExtractData(url, jsonpath, "(.*)");
			assertEquals("BTC", data);
		} catch (MalformedURLException e) {
			fail("The URL '" + url + "' is invalid" + e);
		} catch (IOException e) {
			fail("There was a problem fetching and extracting data for the URL '" + url + "'" + e);
		} catch (XPathExpressionException e) {
			fail("There was a problem with the JSONPath expression '" + jsonpath + "'" + e);
		}
	}

}
