package edu.uci.ics.luci.cacophony.directory.nodelist;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MetaCNodeTest {
	
	Random r = new Random(0);

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
	public void testPrioritySort(){
		
		String winner = "Winner, Winner, Daddy gets a chicken dinner";
		
		List<MetaCNode> l = new ArrayList<MetaCNode>(100);
		
		for(int i = 0 ; i < 99; i++){
			MetaCNode c = new MetaCNode();
			c.setId(Integer.toString(r.nextInt()));
			c.setName("Name_"+Integer.toString(r.nextInt()));
			c.setLatitude((r.nextDouble()*180.0)-90.0);
			c.setLongitude((r.nextDouble()*360.0)-180.0);
			c.setMapWeight(r.nextDouble());
			c.setPriority(Math.ceil(r.nextDouble()*10.0));
			l.add(c);
		}
		MetaCNode c = new MetaCNode();
		c.setId(Integer.toString(r.nextInt()));
		c.setName(winner);
		c.setLatitude((r.nextDouble()*180.0)-90.0);
		c.setLongitude((r.nextDouble()*360.0)-180.0);
		c.setMapWeight(r.nextDouble());
		c.setPriority(12.0);
		l.add(c);
		
		Comparator<MetaCNode> comp = MetaCNode.ByPriority;
		for(int i=0; i < 100; i++){
			Collections.shuffle(l);
			Collections.sort(l,comp);
			assertEquals(winner,l.get(0).getName());
		}
		
		
		comp = Collections.reverseOrder(MetaCNode.ByPriority);
		for(int i=0; i < 100; i++){
			Collections.shuffle(l);
			Collections.sort(l,comp);
			assertEquals(winner,l.get(l.size()-1).getName());
		}
	}
	
	
	
	@Test
	public void testAssignmentSort(){
		
		String winner = "Winner, Winner, Daddy gets a chicken dinner";
		
		List<MetaCNode> l = new ArrayList<MetaCNode>(100);
		
		for(int i = 0 ; i < 98; i++){
			MetaCNode c = new MetaCNode();
			c.setId(Integer.toString(r.nextInt()));
			c.setName("Name_"+Integer.toString(r.nextInt()));
			c.setLatitude((r.nextDouble()*180.0)-90.0);
			c.setLongitude((r.nextDouble()*360.0)-180.0);
			c.setMapWeight(r.nextDouble());
			c.setPriority(Math.ceil(r.nextDouble()*10.0));
			JSONObject j = new JSONObject();
			c.setConfiguration(j);
			
			CNode x = CNodeTest.makeCNode();
			c.getCNodes().put(x.getGuid(),x);
			x = CNodeTest.makeCNode();
			c.getCNodes().put(x.getGuid(),x);
			x = CNodeTest.makeCNode();
			c.getCNodes().put(x.getGuid(),x);
			for(int k = 0; k < i; k++){
				assertTrue(!l.get(k).equals(c));
			}
			l.add(c);
		}
		
		MetaCNode c = new MetaCNode();
		c.setId(Integer.toString(r.nextInt()));
		c.setName(winner);
		c.setLatitude((r.nextDouble()*180.0)-90.0);
		c.setLongitude((r.nextDouble()*360.0)-180.0);
		c.setMapWeight(r.nextDouble());
		c.setPriority(12.0);
		JSONObject j = new JSONObject();
		c.setConfiguration(j);
		l.add(c);
		
		c = new MetaCNode();
		c.setId(Integer.toString(r.nextInt()));
		c.setName("Not "+winner);
		c.setLatitude((r.nextDouble()*180.0)-90.0);
		c.setLongitude((r.nextDouble()*360.0)-180.0);
		c.setMapWeight(r.nextDouble());
		c.setPriority(11.0);
		j = new JSONObject();
		c.setConfiguration(j);
		l.add(c);
		
		Comparator<MetaCNode> comp = MetaCNode.ByAssignmentPaucity;
		for(int i=0; i < 100; i++){
			Collections.shuffle(l);
			Collections.sort(l,comp);
			assertEquals(winner,l.get(0).getName());
		}
		
		
		comp = Collections.reverseOrder(MetaCNode.ByAssignmentPaucity);
		for(int i=0; i < 100; i++){
			Collections.shuffle(l);
			Collections.sort(l,comp);
			assertEquals(winner,l.get(l.size()-1).getName());
		}
	}

	@Test
	public void testJSONConversion(){
		
		MetaCNode c = new MetaCNode();
		c.setId(Integer.toString(r.nextInt()));
		c.setName("Name_"+Integer.toString(r.nextInt()));
		c.setLatitude((r.nextDouble()*180.0)-90.0);
		c.setLongitude((r.nextDouble()*360.0)-180.0);
		c.setMapWeight(r.nextDouble());
		c.setPriority(Math.ceil(r.nextDouble()*10.0));
		JSONObject j = new JSONObject();
		try {
			j.put("Hello", "World");
			j.put("Number", 5.0);
		} catch (JSONException e1) {
			fail("");
		}
		c.setConfiguration(j);
		
		CNode x = CNodeTest.makeCNode();
		c.getCNodes().put(x.getGuid(),x);
		x = CNodeTest.makeCNode();
		c.getCNodes().put(x.getGuid(),x);
		x = CNodeTest.makeCNode();
		c.getCNodes().put(x.getGuid(),x);
		
		j = null;
		MetaCNode c2 = null;
		
		j = MetaCNode.toJSONObject(c);
		c2 = MetaCNode.fromJSONObject(j);
		assertTrue(c.equals(c2));
		assertEquals(c.hashCode(),c2.hashCode());
		assertEquals(c2,c);
		assertEquals(c2.hashCode(),c.hashCode());
			
		j = c.toJSONObject();
		c2 = MetaCNode.fromJSONObject(j);
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
		
		/* Go through nulls */
		c.setConfiguration(null);
		assertTrue(!c.equals(c2));
		c2.setConfiguration(null);
		assertEquals(c,c2);
		assertEquals(c.hashCode(),c2.hashCode());
		j = c.toJSONObject();
		j.remove("configuration");
		c2 = MetaCNode.fromJSONObject(j);
		assertEquals(c,c2);
		assertEquals(c.hashCode(),c2.hashCode());
		
		c.setCNodes(null);
		assertTrue(!c.equals(c2));
		c2.setCNodes(null);
		assertEquals(c,c2);
		assertEquals(c.hashCode(),c2.hashCode());
		j = c.toJSONObject();
		j.remove("c_nodes");
		c2 = MetaCNode.fromJSONObject(j);
		assertEquals(c,c2);
		assertEquals(c.hashCode(),c2.hashCode());
		
		c.setPriority(null);
		assertTrue(!c.equals(c2));
		c2.setPriority(null);
		assertEquals(c,c2);
		assertEquals(c.hashCode(),c2.hashCode());
		j = c.toJSONObject();
		j.remove("priority");
		c2 = MetaCNode.fromJSONObject(j);
		assertEquals(c,c2);
		assertEquals(c.hashCode(),c2.hashCode());
		
		c.setMapWeight(null);
		assertTrue(!c.equals(c2));
		c2.setMapWeight(null);
		assertEquals(c,c2);
		assertEquals(c.hashCode(),c2.hashCode());
		j = c.toJSONObject();
		j.remove("map_weight");
		c2 = MetaCNode.fromJSONObject(j);
		assertEquals(c,c2);
		assertEquals(c.hashCode(),c2.hashCode());
		
		c.setLatitude(null);
		assertTrue(!c.equals(c2));
		c2.setLatitude(null);
		assertEquals(c,c2);
		assertEquals(c.hashCode(),c2.hashCode());
		j = c.toJSONObject();
		j.remove("latitude");
		c2 = MetaCNode.fromJSONObject(j);
		assertEquals(c,c2);
		assertEquals(c.hashCode(),c2.hashCode());
		
		c.setLongitude(null);
		assertTrue(!c.equals(c2));
		c2.setLongitude(null);
		assertEquals(c,c2);
		assertEquals(c.hashCode(),c2.hashCode());
		j = c.toJSONObject();
		j.remove("longitude");
		c2 = MetaCNode.fromJSONObject(j);
		assertEquals(c,c2);
		assertEquals(c.hashCode(),c2.hashCode());
		
		c.setName(null);
		assertTrue(!c.equals(c2));
		c2.setName(null);
		assertEquals(c,c2);
		assertEquals(c.hashCode(),c2.hashCode());
		j = c.toJSONObject();
		j.remove("name");
		c2 = MetaCNode.fromJSONObject(j);
		assertEquals(c,c2);
		assertEquals(c.hashCode(),c2.hashCode());
		
		c.setId(null);
		assertTrue(!c.equals(c2));
		c2.setId(null);
		assertEquals(c,c2);
		assertEquals(c.hashCode(),c2.hashCode());
		j = c.toJSONObject();
		j.remove("id");
		c2 = MetaCNode.fromJSONObject(j);
		assertEquals(c,c2);
		assertEquals(c.hashCode(),c2.hashCode());
		
		c2 = MetaCNode.fromJSONObject(MetaCNode.toJSONObject(null));
		assertEquals((MetaCNode)null,c2);
			
		
	}
	
}
