package edu.uci.ics.luci.cacophony.directory.nodelist;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.ics.luci.utility.Globals;
import edu.uci.ics.luci.utility.GlobalsTest;

public class CNodeTest {
	
	static Random r = new Random(0);

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
	
	static public CNode makeCNode(){
		CNode c = new CNode();
		
		c.setGuid(Integer.toString(r.nextInt()));
		c.setLastHeartbeat(r.nextLong());
		URI uri;
		try {
			uri = new URI("http://www.foo.com/"+Integer.toString(r.nextInt()));
			c.setUri(uri);
		} catch (URISyntaxException e) {
			fail("Shouldn't fail");
		}
		
		return c;
	}
	
	
	@Test
	public void testEquals(){
		
		CNode a = makeCNode();
		CNode b = makeCNode();
		
		Object c = new Object();
		
		assertTrue(!a.equals(c));
		assertTrue(a.hashCode() != c.hashCode());
		
		assertTrue(!a.equals(b));
		assertTrue(!b.equals(a));
		assertTrue(a.hashCode() != b.hashCode());
		
		assertTrue(a.equals(a));
		assertTrue(b.equals(b));
		assertTrue(a.hashCode() == a.hashCode());
		assertTrue(b.hashCode() == b.hashCode());
		
		b = new CNode(a);
		assertTrue(a.equals(b));
		assertTrue(b.equals(a));
		assertTrue(a.hashCode() == b.hashCode());
		b.setUri(null);
		assertTrue(!a.equals(b));
		assertTrue(a.hashCode() != b.hashCode());
		
		
		b = new CNode(a);
		assertTrue(a.equals(b));
		assertTrue(b.equals(a));
		assertTrue(a.hashCode() == b.hashCode());
		b.setLastHeartbeat(null);
		assertTrue(!a.equals(b));
		assertTrue(a.hashCode() != b.hashCode());
		
		
		b = new CNode(a);
		assertTrue(a.equals(b));
		assertTrue(b.equals(a));
		assertTrue(a.hashCode() == b.hashCode());
		b.setGuid(null);
		assertTrue(!a.equals(b));
		assertTrue(a.hashCode() != b.hashCode());
		
		
		a.setGuid(null);
		b = new CNode(a);
		assertTrue(a.equals(b));
		assertTrue(b.equals(a));
		assertTrue(a.hashCode() == b.hashCode());
		
		a.setLastHeartbeat(null);
		b = new CNode(a);
		assertTrue(a.equals(b));
		assertTrue(b.equals(a));
		assertTrue(a.hashCode() == b.hashCode());
		
		a.setUri(null);
		b = new CNode(a);
		assertTrue(a.equals(b));
		assertTrue(b.equals(a));
		assertTrue(a.hashCode() == b.hashCode());
		
		
	}
		
	
	@Test
	public void testConversionJSON(){
		
		for(int i = 0 ; i < 100; i++){
			CNode a = makeCNode();
			
			assertEquals(CNode.fromJSONObject(a.toJSONObject()),a);
		}
	}

}
