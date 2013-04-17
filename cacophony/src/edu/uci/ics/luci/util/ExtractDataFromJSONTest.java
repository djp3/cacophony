package edu.uci.ics.luci.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jayway.jsonpath.InvalidPathException;

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
		String jsonPath = null;
		data = ExtractDataFromJSON.extractData(null,null,null,null);
		assertTrue(data == null);
		
		// TODO: The JsonPath library throws the same com.jayway.jsonpath.InvalidPathException for
		// (1) jsonPaths that are poorly formed (bad syntax) and
		// (2) for jsonPaths that are syntactically correct, but don't find anything in the json.
		// This is different from the xPath behavior, where not finding anything with an XPath just gives back null.
		// JsonPath's latest version looks like it may fix this behavior, but JARs aren't available for download, and building
		// the code is non-trivial. Waiting on a reply from the library author. -John
		
		try{
			jsonPath="$.error.book[*].author";
			data = ExtractDataFromJSON.extractData(jsonPath,null,jsonData,null);
			fail("This should thrown and exception");
		}
		catch(InvalidPathException e){
			assertTrue(data == null);
		}
		
		jsonPath="$.store.book[*].author";
		data = ExtractDataFromJSON.extractData(jsonPath,null,jsonData,null);
		assertEquals(data,"Nigel Rees");
		
		jsonPath="$.store.book[*].category";
		data = ExtractDataFromJSON.extractData(jsonPath," ",jsonData,null);
		assertEquals(data,"reference");
		
		try {
			jsonPath="=$.store.book[*].category";
			ExtractDataFromJSON.extractData(jsonPath," ",jsonData,null);
			fail("There was a problem with the method because it should have thrown an exception" + jsonPath);
		} catch (InvalidPathException e) {
		}
		
	}

	@Test
	public void testExtractData() {
		
		String data = null;
		String jsonPath="$.store.book[*].isbn";
		data = ExtractDataFromJSON.extractData(jsonPath,"[^-]*-([0-9]*)-[0-9]*-[0-9]",jsonData,null);
		assertEquals("553", data);
		
		data = ExtractDataFromJSON.extractData(jsonPath,"[^-]*-[0-9]*-([0-9]*)-[0-9]",jsonData,null);
		assertEquals("21311", data);
	}

	@Test
	public void testFetchAndExtractData() {
		String url = "http://data.mtgox.com/api/1/BTCUSD/ticker";
		String jsonpath = "$.return.vol.currency";
		try {
			String data = ExtractDataFromJSON.fetchAndExtractData(url, jsonpath, "(.*)", null);
			assertEquals("BTC", data);
		} catch (MalformedURLException e) {
			fail("The URL '" + url + "' is invalid\n" + e);
		} catch (IOException e) {
			fail("There was a problem fetching and extracting data for the URL '" + url + "'\n" + e);
		}
	}
}