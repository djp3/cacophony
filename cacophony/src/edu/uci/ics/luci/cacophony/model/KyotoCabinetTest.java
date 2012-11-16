package edu.uci.ics.luci.cacophony.model;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.ics.luci.utility.Globals;
import edu.uci.ics.luci.utility.GlobalsTest;
import edu.uci.ics.luci.utility.datastructure.Pair;

public class KyotoCabinetTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		while(Globals.getGlobals() != null){
			try{
				Thread.sleep(1000);
			}
			catch(InterruptedException e){
			}
		}
		Globals.setGlobals(new GlobalsTest());
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		Globals.getGlobals().setQuitting(true);
		Globals.setGlobals(null);
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	 static class VisitorTest extends KyotoCabinetVisitor<String,String> {
		@Override
		public Pair<Response, String> visit_full(String key, String value) {
			 if((key != null) && (key.equals("foo"))){
				 return new Pair<Response,String>(KyotoCabinetVisitor.Response.REMOVE,null);
			 }
			 else if((key!= null) && (key.equals("bar"))){
				 return new Pair<Response,String>(KyotoCabinetVisitor.Response.NOP,null);
			 }
			 else{
				 return new Pair<Response,String>(KyotoCabinetVisitor.Response.REPLACE,"replacedValue");
			 }
		}
	 }
	
	@Test
	public void testBasic(){
		Globals.getGlobals().setTesting(true);
		
		KyotoCabinet<String,String> db = new KyotoCabinet<String,String>();
		Globals.getGlobals().addQuittables(db);
		
		
		 // open the database
	    if (!db.open(true,Globals.getGlobals().isTesting())){
	    	System.err.println("open error: " + db.error());
	    }
	    
	    String test = "foo";
	    byte[] a = KyotoCabinet.serializableToBytes(test);
	    byte[] b = KyotoCabinet.serializableToBytes(test);
	    assertTrue(a.length == b.length);
	    for(int i = 0; i < a.length; i++){
	    	assertTrue(a[i] == b[i]);
	    }
	    
	    a = KyotoCabinet.serializableToBytes("foo");
	    b = KyotoCabinet.serializableToBytes("foo");
	    assertTrue(a.length == b.length);
	    for(int i = 0; i < a.length; i++){
	    	assertTrue(a[i] == b[i]);
	    }

	    // store records
	    assertTrue(db.set("foo", "hop"));
	    assertTrue(db.set("bar", "step"));
	    assertTrue(db.contains("foo"));
	    assertTrue(db.contains("bar"));
	    
	    assertTrue(!db.contains("baz"));
	    assertTrue(!db.contains("bat"));
	    assertTrue(db.set("baz", "jump"));
	    assertTrue(db.set("bat", null));
	    assertTrue(db.contains("baz"));
	    assertTrue(db.contains("bat"));
	    
	    assertTrue(!db.contains(null));
	    assertTrue(db.set(null,"hop"));
	    assertTrue(db.contains(null));
	    
	    assertTrue(db.set(null,"step"));
	    assertTrue(db.set(null,"jump"));
	    assertTrue(db.set(null,null));
	    
	    assertEquals(db.get("foo"),"hop");
	    assertEquals(db.get("foo"),"hop");
	    assertEquals(db.get("bar"),"step");
	    assertEquals(db.get("bar"),"step");
	    assertEquals(db.get("baz"),"jump");
	    assertEquals(db.get("baz"),"jump");
	    assertEquals(db.get("bat"),null);
	    assertTrue(db.get(null) == null);

	    // traverse records
	    KyotoCabinetVisitor<String,String> visitor = new VisitorTest();
	    db.iterate(visitor, true);
	    
	    assertTrue((String)db.get("foo") == null);
	    assertEquals("step",(String)db.get("bar"));
	    assertEquals("replacedValue",(String)db.get("baz"));
	    assertEquals("replacedValue",(String)db.get("bat"));
	    assertEquals("replacedValue",(String)db.get(null));
	    assertTrue((String)db.get("batter") == null);
	    
	    assertTrue(!db.remove("foo"));
	    assertTrue(db.remove("bar"));
	    assertTrue(!db.contains("foo"));
	    assertTrue(!db.contains("bar"));
	    assertTrue(db.contains("baz"));
	}

}
