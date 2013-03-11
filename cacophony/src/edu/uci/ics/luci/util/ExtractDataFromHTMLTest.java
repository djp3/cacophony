package edu.uci.ics.luci.util;

import static org.junit.Assert.*;

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
		
		String data = ExtractDataFromHTML.extractData("/html/body/div",
				"<<(.*):[0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*>:[-]?[0-9][0-9]*>",
				"<html><body><div class=\"sensor_data\">&lt;&lt;UCInet Mobile Access:00:19:a9:54:59:ae&gt;:-62&gt;</div></body></html>");
		
		assertEquals(data,"UCInet Mobile Access");
		
		data = ExtractDataFromHTML.extractData("/html/body/div",
				"<<.*:([0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*)>:[-]?[0-9][0-9]*>",
				"<html><body><div class=\"sensor_data\">&lt;&lt;UCInet Mobile Access:00:19:a9:54:59:ae&gt;:-62&gt;</div></body></html>");
		assertEquals(data,"00:19:a9:54:59:ae");
	}

	@Test
	public void testFetchAndExtractData() {
		String data = ExtractDataFromHTML.fetchAndExtractData("http://www.cnn.com", 
				"//*[@id=\"cnn_ftrcntntinner\"]/div[9]/div[1]/text()[2]", "(.*)");
		assertEquals(data, " All Rights Reserved.");
	}

}
