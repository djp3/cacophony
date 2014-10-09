package edu.uci.ics.luci.cacophony.server.responder;

import static org.junit.Assert.assertTrue;

import java.util.Map;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.ics.luci.cacophony.node.CNode;

public class CNodeServerResponderTest {

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
	
	static class MyResponder extends CNodeServerResponder{

		@Override
		public void handle(JSONObject jo, Map<String, CNode> cnodes) {
			
		}};
		
		
	@Test
	public void testAppendError() {
		
		MyResponder myResponder = new MyResponder();
		myResponder.appendError("foo");
		JSONObject r = myResponder.constructResponse();
		assertTrue(r.get("errors") != null);
		assertTrue(((String)((JSONArray)r.get("errors")).get(0)).equals("foo"));
		assertTrue(r.get("responses") == null);
	}
	
	@Test
	public void testReplaceError() {
		
		/* First add one error */
		MyResponder myResponder = new MyResponder();
		myResponder.appendError("foo");
		JSONObject r = myResponder.constructResponse();
		assertTrue(r.get("errors") != null);
		assertTrue(((String)((JSONArray)r.get("errors")).get(0)).equals("foo"));
		
		/* Now replace that with a set of two errors */
		JSONArray errors = new JSONArray();
		errors.add("whoosh");
		errors.add("smoosh");
		myResponder.replaceErrors(errors);
		
		r = myResponder.constructResponse();
		assertTrue(r.get("errors") != null);
		assertTrue(((JSONArray)r.get("errors")).size() == 2);
		String a = (String)((JSONArray)r.get("errors")).get(0);
		String b = (String)((JSONArray)r.get("errors")).get(1);
		assertTrue((a.equals("whoosh") && b.equals("smoosh")) ||
				   (a.equals("smoosh") && b.equals("whoosh")));
		
		assertTrue(r.get("responses") == null);
		
		/* Now try and replace the errors with null */
		myResponder.replaceErrors(null);
		
		r = myResponder.constructResponse();
		assertTrue(r.get("errors") != null);
		assertTrue(((JSONArray)r.get("errors")).size() == 2);
		a = (String)((JSONArray)r.get("errors")).get(0);
		b = (String)((JSONArray)r.get("errors")).get(1);
		assertTrue((a.equals("whoosh") && b.equals("smoosh")) ||
				   (a.equals("smoosh") && b.equals("whoosh")));
		
		assertTrue(r.get("responses") == null);
		
	}
	
	
	@Test
	public void testAppendResponse() {
		
		MyResponder myResponder = new MyResponder();
		myResponder.appendResponse("bar");
		JSONObject r = myResponder.constructResponse();
		assertTrue(r.get("responses") != null);
		assertTrue(((String)((JSONArray)r.get("responses")).get(0)).equals("bar"));
		assertTrue(r.get("errors") == null);
	}
	
	@Test
	public void testReplaceResponses() {
		
		/* First add one response */
		MyResponder myResponder = new MyResponder();
		myResponder.appendResponse("bar");
		JSONObject r = myResponder.constructResponse();
		assertTrue(r.get("responses") != null);
		assertTrue(((String)((JSONArray)r.get("responses")).get(0)).equals("bar"));
		
		/* Now replace that with a set of two responses */
		JSONArray responses = new JSONArray();
		responses.add("bling");
		responses.add("bam");
		myResponder.replaceResponses(responses);
		
		r = myResponder.constructResponse();
		assertTrue(r.get("responses") != null);
		assertTrue(((JSONArray)r.get("responses")).size() == 2);
		String a = (String)((JSONArray)r.get("responses")).get(0);
		String b = (String)((JSONArray)r.get("responses")).get(1);
		assertTrue((a.equals("bling") && b.equals("bam")) ||
				   (a.equals("bam") && b.equals("bling")));
		
		assertTrue(r.get("errors") == null);
		
		/* Now try and replace the errors with null */
		myResponder.replaceResponses(null);
		
		r = myResponder.constructResponse();
		assertTrue(r.get("responses") != null);
		assertTrue(((JSONArray)r.get("responses")).size() == 2);
		a = (String)((JSONArray)r.get("responses")).get(0);
		b = (String)((JSONArray)r.get("responses")).get(1);
		assertTrue((a.equals("bling") && b.equals("bam")) ||
				   (a.equals("bam") && b.equals("bling")));
		
		assertTrue(r.get("errors") == null);
		
	}

}
