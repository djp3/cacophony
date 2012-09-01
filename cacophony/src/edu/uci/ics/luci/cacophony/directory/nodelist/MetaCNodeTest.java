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
			MetaCNode mc = new MetaCNode();
			mc.setCreationTime(System.currentTimeMillis());
			mc.setGuid(Integer.toString(r.nextInt()));
			mc.setName("Name_"+Integer.toString(r.nextInt()));
			mc.setLatitude((r.nextDouble()*180.0)-90.0);
			mc.setLongitude((r.nextDouble()*360.0)-180.0);
			mc.setMapWeight(r.nextDouble());
			mc.setPriority(Math.ceil(r.nextDouble()*10.0));
			l.add(mc);
		}
		MetaCNode mc = new MetaCNode();
		mc.setCreationTime(System.currentTimeMillis());
		mc.setGuid(Integer.toString(r.nextInt()));
		mc.setName(winner);
		mc.setLatitude((r.nextDouble()*180.0)-90.0);
		mc.setLongitude((r.nextDouble()*360.0)-180.0);
		mc.setMapWeight(r.nextDouble());
		mc.setPriority(12.0);
		l.add(mc);
		
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
			MetaCNode mc = new MetaCNode();
			mc.setCreationTime(System.currentTimeMillis());
			mc.setGuid(Integer.toString(r.nextInt()));
			mc.setName("Name_"+Integer.toString(r.nextInt()));
			mc.setLatitude((r.nextDouble()*180.0)-90.0);
			mc.setLongitude((r.nextDouble()*360.0)-180.0);
			mc.setMapWeight(r.nextDouble());
			mc.setPriority(Math.ceil(r.nextDouble()*10.0));
			JSONObject j = new JSONObject();
			mc.setConfiguration(j);
			
			CNodeReference cr = CNodeReferenceTest.makeCNodeReference();
			mc.getCNodeReferences().put(cr.getCNodeGuid(),cr);
			cr = CNodeReferenceTest.makeCNodeReference();
			mc.getCNodeReferences().put(cr.getCNodeGuid(),cr);
			cr = CNodeReferenceTest.makeCNodeReference();
			mc.getCNodeReferences().put(cr.getCNodeGuid(),cr);
			for(int k = 0; k < i; k++){
				assertTrue(!l.get(k).equals(mc));
			}
			l.add(mc);
		}
		
		MetaCNode mc = new MetaCNode();
		mc.setCreationTime(System.currentTimeMillis());
		mc.setGuid(Integer.toString(r.nextInt()));
		mc.setName(winner);
		mc.setLatitude((r.nextDouble()*180.0)-90.0);
		mc.setLongitude((r.nextDouble()*360.0)-180.0);
		mc.setMapWeight(r.nextDouble());
		mc.setPriority(12.0);
		JSONObject j = new JSONObject();
		mc.setConfiguration(j);
		l.add(mc);
		
		mc = new MetaCNode();
		mc.setCreationTime(System.currentTimeMillis());
		mc.setGuid(Integer.toString(r.nextInt()));
		mc.setName("Not "+winner);
		mc.setLatitude((r.nextDouble()*180.0)-90.0);
		mc.setLongitude((r.nextDouble()*360.0)-180.0);
		mc.setMapWeight(r.nextDouble());
		mc.setPriority(11.0);
		j = new JSONObject();
		mc.setConfiguration(j);
		l.add(mc);
		
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
		
		MetaCNode mc = new MetaCNode();
		mc.setCreationTime(System.currentTimeMillis());
		mc.setGuid(Integer.toString(r.nextInt()));
		mc.setName("Name_"+Integer.toString(r.nextInt()));
		mc.setLatitude((r.nextDouble()*180.0)-90.0);
		mc.setLongitude((r.nextDouble()*360.0)-180.0);
		mc.setMapWeight(r.nextDouble());
		mc.setPriority(Math.ceil(r.nextDouble()*10.0));
		JSONObject j = new JSONObject();
		try {
			j.put("Hello", "World");
			j.put("Number", 5.0);
		} catch (JSONException e1) {
			fail("");
		}
		mc.setConfiguration(j);
		
		CNodeReference cr = CNodeReferenceTest.makeCNodeReference();
		mc.getCNodeReferences().put(cr.getCNodeGuid(),cr);
		cr = CNodeReferenceTest.makeCNodeReference();
		mc.getCNodeReferences().put(cr.getCNodeGuid(),cr);
		cr = CNodeReferenceTest.makeCNodeReference();
		mc.getCNodeReferences().put(cr.getCNodeGuid(),cr);
		
		j = null;
		MetaCNode c2 = null;
		
		j = MetaCNode.toJSONObject(mc);
		c2 = MetaCNode.fromJSONObject(j);
		assertTrue(mc.equals(c2));
		assertEquals(mc.hashCode(),c2.hashCode());
		assertEquals(c2,mc);
		assertEquals(c2.hashCode(),mc.hashCode());
			
		j = mc.toJSONObject();
		c2 = MetaCNode.fromJSONObject(j);
		assertTrue(mc.equals(c2));
		assertEquals(mc.hashCode(),c2.hashCode());
		assertEquals(c2,mc);
		assertEquals(c2.hashCode(),mc.hashCode());
		
		/* Test some default configs */
		assertEquals(mc,mc);
		assertEquals(mc.hashCode(),mc.hashCode());
		
		assertEquals(c2,c2);
		assertEquals(c2.hashCode(),c2.hashCode());
		
		assertTrue(!mc.equals(Integer.valueOf(1)));
		
		/* Go through nulls */
		mc.setConfiguration(null);
		assertTrue(!mc.equals(c2));
		c2.setConfiguration(null);
		assertEquals(mc,c2);
		assertEquals(mc.hashCode(),c2.hashCode());
		j = mc.toJSONObject();
		c2 = MetaCNode.fromJSONObject(j);
		assertEquals(mc,c2);
		assertEquals(mc.hashCode(),c2.hashCode());
		
		mc.setCNodeReferences(null);
		assertTrue(!mc.equals(c2));
		c2.setCNodeReferences(null);
		assertEquals(mc,c2);
		assertEquals(mc.hashCode(),c2.hashCode());
		j = mc.toJSONObject();
		c2 = MetaCNode.fromJSONObject(j);
		assertEquals(mc,c2);
		assertEquals(mc.hashCode(),c2.hashCode());
		
		mc.setPriority(null);
		assertTrue(!mc.equals(c2));
		c2.setPriority(null);
		assertEquals(mc,c2);
		assertEquals(mc.hashCode(),c2.hashCode());
		j = mc.toJSONObject();
		c2 = MetaCNode.fromJSONObject(j);
		assertEquals(mc,c2);
		assertEquals(mc.hashCode(),c2.hashCode());
		
		mc.setMapWeight(null);
		assertTrue(!mc.equals(c2));
		c2.setMapWeight(null);
		assertEquals(mc,c2);
		assertEquals(mc.hashCode(),c2.hashCode());
		j = mc.toJSONObject();
		c2 = MetaCNode.fromJSONObject(j);
		assertEquals(mc,c2);
		assertEquals(mc.hashCode(),c2.hashCode());
		
		mc.setLatitude(null);
		assertTrue(!mc.equals(c2));
		c2.setLatitude(null);
		assertEquals(mc,c2);
		assertEquals(mc.hashCode(),c2.hashCode());
		j = mc.toJSONObject();
		c2 = MetaCNode.fromJSONObject(j);
		assertEquals(mc,c2);
		assertEquals(mc.hashCode(),c2.hashCode());
		
		mc.setLongitude(null);
		assertTrue(!mc.equals(c2));
		c2.setLongitude(null);
		assertEquals(mc,c2);
		assertEquals(mc.hashCode(),c2.hashCode());
		j = mc.toJSONObject();
		c2 = MetaCNode.fromJSONObject(j);
		assertEquals(mc,c2);
		assertEquals(mc.hashCode(),c2.hashCode());
		
		mc.setName(null);
		assertTrue(!mc.equals(c2));
		c2.setName(null);
		assertEquals(mc,c2);
		assertEquals(mc.hashCode(),c2.hashCode());
		j = mc.toJSONObject();
		c2 = MetaCNode.fromJSONObject(j);
		assertEquals(mc,c2);
		assertEquals(mc.hashCode(),c2.hashCode());
		
		mc.setGuid(null);
		assertTrue(!mc.equals(c2));
		c2.setGuid(null);
		assertEquals(mc,c2);
		assertEquals(mc.hashCode(),c2.hashCode());
		j = mc.toJSONObject();
		c2 = MetaCNode.fromJSONObject(j);
		assertEquals(mc,c2);
		assertEquals(mc.hashCode(),c2.hashCode());
		
		/* Creation Time shouldn't affect equality */
		mc.setCreationTime(null);
		assertTrue(mc.equals(c2));
		c2.setCreationTime(null);
		assertEquals(mc,c2);
		assertEquals(mc.hashCode(),c2.hashCode());
		j = mc.toJSONObject();
		c2 = MetaCNode.fromJSONObject(j);
		assertEquals(mc,c2);
		assertEquals(mc.hashCode(),c2.hashCode());		
		
		c2 = MetaCNode.fromJSONObject(MetaCNode.toJSONObject(null));
		assertEquals((MetaCNode)null,c2);
			
		
	}
	
}
