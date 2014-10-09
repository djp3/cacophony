package edu.uci.ics.luci.cacophony.node;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jayway.jsonpath.InvalidPathException;

public class StringExtractorJSONTest {

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
		data = StringExtractorJSON.extract(null,null,null);
		assertTrue(data == null);
		
		// TODO: The JsonPath library throws the same com.jayway.jsonpath.InvalidPathException for (1) jsonPaths that are
		// poorly formed (bad syntax) and (2) for jsonPaths that are syntactically correct, but don't find anything in the json.
		// This is different from the xPath behavior, where not finding anything with an XPath just gives back null.
		// JsonPath's latest version looks like it may fix this behavior, but JARs aren't available for download, and building
		// the code is non-trivial. Waiting on a reply from the library author. -John
//		jsonPath="$.error.book[*].author";
//		data = StringExtractorJSON.extractData(jsonPath,null,jsonData,null);
//		assertTrue(data == null);
		
		jsonPath="$.store.book[*].author";
		data = StringExtractorJSON.extract(jsonPath,null,jsonData);
		assertEquals(data,"Nigel Rees");
		
		jsonPath="$.store.book[*].category";
		data = StringExtractorJSON.extract(jsonPath," ",jsonData);
		assertEquals(data,"reference");
		
		try {
			jsonPath="=$.store.book[*].category";
			StringExtractorJSON.extract(jsonPath," ",jsonData);
			fail("There was a problem with the method because it should have thrown an exception" + jsonPath);
		} catch (InvalidPathException e) {
		}
	}

	@Test
	public void testExtractData() {
		
		String data = null;
		String jsonPath="$.store.book[*].isbn";
		data = StringExtractorJSON.extract(jsonPath,"[^-]*-([0-9]*)-[0-9]*-[0-9]",jsonData);
		assertEquals("553", data);
		
		data = StringExtractorJSON.extract(jsonPath,"[^-]*-[0-9]*-([0-9]*)-[0-9]",jsonData);
		assertEquals("21311", data);
	}

	@Test
	public void testFetchAndExtractData() {
		String url = "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/4.5_day.geojson";
		String jsonpath = "$.metadata.status";
		try {
			String data = StringExtractorJSON.fetchAndExtract(url, jsonpath, "(.*)");
			assertTrue(Integer.parseInt(data) == 200);
		} catch (MalformedURLException e) {
			fail("The URL '" + url + "' is invalid\n" + e);
		} catch (IOException e) {
			fail("There was a problem fetching and extracting data for the URL '" + url + "'\n" + e);
		}
	}
}
