package edu.uci.ics.luci.cacophony.directory.nodelist;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.quub.Globals;
import com.quub.GlobalsTest;
import com.quub.util.Pair;

public class CNodeReferenceTest {
	
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
	
	@Test
	public void testJSONConversion(){
		
		CNodeReference c = makeCNodeReference();
		
		JSONObject j = null;
		CNodeReference c2 = null;
		
		j = CNodeReference.toJSONObject(c);
		c2 = CNodeReference.fromJSONObject(j);
		assertTrue(c.equals(c2));
		assertEquals(c.hashCode(),c2.hashCode());
		assertEquals(c2,c);
		assertEquals(c2.hashCode(),c.hashCode());
			
		j = c.toJSONObject();
		c2 = CNodeReference.fromJSONObject(j);
		assertTrue(c.equals(c2));
		assertEquals(c.hashCode(),c2.hashCode());
		assertEquals(c2,c);
		assertEquals(c2.hashCode(),c.hashCode());
		
		/* Test some default configs */
		assertEquals(c,c);
		assertEquals(c.hashCode(),c.hashCode());
		
		assertEquals(c2,c2);
		assertEquals(c2.hashCode(),c2.hashCode());
		
		assertTrue(!c.equals(Integer.valueOf(1)));
		
		c2 = new CNodeReference(c);
		assertEquals(c,c2);
		assertEquals(c2,c);
		
		/* Go through nulls */
		c.setAccessRoutesForUI(null);
		assertTrue(!c.equals(c2));
		c2.setAccessRoutesForUI(null);
		assertEquals(c,c2);
		assertEquals(c.hashCode(),c2.hashCode());
		j = c.toJSONObject();
		c2 = CNodeReference.fromJSONObject(j);
		assertEquals(c,c2);
		assertEquals(c.hashCode(),c2.hashCode());
		
		c.setAccessRoutesForAPI(null);
		assertTrue(!c.equals(c2));
		c2.setAccessRoutesForAPI(null);
		assertEquals(c,c2);
		assertEquals(c.hashCode(),c2.hashCode());
		j = c.toJSONObject();
		c2 = CNodeReference.fromJSONObject(j);
		assertEquals(c,c2);
		assertEquals(c.hashCode(),c2.hashCode());
		
		c.setCNodeGuid(null);
		assertTrue(!c.equals(c2));
		c2.setCNodeGuid(null);
		assertEquals(c,c2);
		assertEquals(c.hashCode(),c2.hashCode());
		j = c.toJSONObject();
		c2 = CNodeReference.fromJSONObject(j);
		assertEquals(c,c2);
		assertEquals(c.hashCode(),c2.hashCode());
		
		c.setMetaCNodeGuid(null);
		assertTrue(!c.equals(c2));
		c2.setMetaCNodeGuid(null);
		assertEquals(c,c2);
		assertEquals(c.hashCode(),c2.hashCode());
		j = c.toJSONObject();
		c2 = CNodeReference.fromJSONObject(j);
		assertEquals(c,c2);
		assertEquals(c.hashCode(),c2.hashCode());
		
		c2 = CNodeReference.fromJSONObject(CNodeReference.toJSONObject(null));
		assertEquals((CNodeReference)null,c2);
		
	}
	
	@Test
	public void testToSTring(){
		
		CNodeReference c = makeCNodeReference();
		
		String x = c.toString();
		assertTrue(x.contains("CNodeReference"));
		assertTrue(x.contains("CNodeGuid"));
		assertTrue(x.contains("heartbeat"));
		assertTrue(x.contains("UI Routes"));
		assertTrue(x.contains("API Routes"));
		
	}

	public static CNodeReference makeCNodeReference() {
		CNodeReference c = new CNodeReference();
		c.setMetaCNodeGuid(Integer.toString(r.nextInt()));
		c.setCNodeGuid(Integer.toString(r.nextInt()));
		c.setLastHeartbeat(System.currentTimeMillis());
		
		Set<Pair<Long, String>> foo = new TreeSet<Pair<Long,String>>(Collections.reverseOrder());
		for(int i = 0 ; i < 100; i++){
			Pair<Long,String> p = new Pair<Long,String>(r.nextLong(),"http://www.foo.com/"+Integer.toString(r.nextInt()));
			foo.add(p);
		}
		c.setAccessRoutesForUI(foo);
		
		foo = new TreeSet<Pair<Long,String>>(Collections.reverseOrder());
		for(int i = 0 ; i < 100; i++){
			Pair<Long,String> p = new Pair<Long,String>(r.nextLong(),"http://www.bar.com/"+Integer.toString(r.nextInt()));
			foo.add(p);
		}
		c.setAccessRoutesForAPI(foo);
		return c;
	}

}
