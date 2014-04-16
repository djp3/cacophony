package edu.uci.ics.luci.cacophony.node;

import static org.junit.Assert.*;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.ics.luci.cacophony.server.CNodeServerTest;
import edu.uci.ics.luci.cacophony.server.responder.ResponderConfigurationLoaderTest;

public class CNodeTest {
	
	final double EPSILON = 0.0001;

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
	public void test() {
		try{
			CNode cn = new CNode(null);
			assertTrue(cn.getConfiguration() == null);
			
			CNodeConfiguration cnn = null;
			try{
				JSONObject js = ResponderConfigurationLoaderTest.makeLoadConfigurationRequest(CNodeServerTest.makeARandomP2PServerAddress(), "p2p://foo/bar");
				JSONObject data = (JSONObject) js.get("data");
				JSONArray configurations = (JSONArray) data.get("configurations");
				JSONObject configuration = (JSONObject) configurations.get(0);
				cnn = new CNodeConfiguration(configuration);
				cn.setConfiguration(cnn);
				assertTrue(cn.getConfiguration() == cnn);
			}
			catch(IllegalArgumentException e){
				fail("This shouldn't throw an exception");
			}
			
			Thread t = new Thread(cn);
			t.setDaemon(false); //Force a clean shutdown
			t.start();
		}
		catch(RuntimeException e){
			fail("Should not throw an exception:"+e);
		}
	}
	
	
	@Test
	public void testMean() {
		
		List<Long> durations = null;
		try{
			CNode.calculateMean(durations);
			fail("This should not work");
		}
		catch(Exception e){
			//ok
		}
		
		durations = new ArrayList<Long>();
		try{
			CNode.calculateMean(durations);
			fail("This should not work");
		}
		catch(Exception e){
			//ok
		}
		
		durations.add(10L);
		assertTrue(Math.abs(10.0d - CNode.calculateMean(durations)) < EPSILON);
		
		durations.add(10L);
		assertTrue(Math.abs(10.0d - CNode.calculateMean(durations)) < EPSILON);
		
		durations.add(10L);
		assertTrue(Math.abs(10.0d - CNode.calculateMean(durations)) < EPSILON);
		
		durations.add(20L);
		durations.add(20L);
		durations.add(20L);
		
		assertTrue(Math.abs(15.0d - CNode.calculateMean(durations)) < EPSILON);
		
		durations.add(-15L);
		durations.add(-15L);
		durations.add(-15L);
		durations.add(-15L);
		durations.add(-15L);
		durations.add(-15L);
		
		assertTrue(Math.abs(0.0d - CNode.calculateMean(durations)) < EPSILON);
	}
	
	
	
	@Test
	public void testStdDev() {
		
		List<Long> durations = null;
		try{
			CNode.calculateStdDev(durations,0.0);
			fail("This should not work");
		}
		catch(Exception e){
			//ok
		}
		
		durations = new ArrayList<Long>();
		try{
			CNode.calculateStdDev(durations,0.0);
			fail("This should not work");
		}
		catch(Exception e){
			//ok
		}
		
		durations.add(10L);
		assertTrue(Math.abs(0.0d - CNode.calculateStdDev(durations,CNode.calculateMean(durations))) < EPSILON);
		
		durations.add(10L);
		assertTrue(Math.abs(0.0d - CNode.calculateStdDev(durations,CNode.calculateMean(durations))) < EPSILON);
		
		durations.add(10L);
		assertTrue(Math.abs(0.0d - CNode.calculateStdDev(durations,CNode.calculateMean(durations))) < EPSILON);
		
		durations.add(20L);
		durations.add(20L);
		durations.add(20L);
		durations.add(20L);
		durations.add(20L);
		durations.add(20L);
		
		durations.add(20L);
		durations.add(20L);
		durations.add(20L);
		durations.add(20L);
		durations.add(20L);
		durations.add(20L);
		
		assertTrue(Math.abs(4.0d - CNode.calculateStdDev(durations,CNode.calculateMean(durations))) < EPSILON);
		
	}
	
	
	@Test
	public void testPercentiles() {
		double percentile;
		try{
			percentile = CNode.getPercentile(0,1.0, 1 ,0.0);
			fail("This should fail on 0.0");
		}
		catch(InvalidParameterException e){
			//ok
		}
		catch(Exception e){
			fail("This should not happen:"+e);
		}
		
		
		Double confidenceInterval = Double.valueOf(0.95);
		List<Long> durations = new ArrayList<Long>();
		
		try{
			percentile = CNode.getPercentile(0,1.0, -1 ,confidenceInterval);
			fail("This should fail on -1");
		}
		catch(InvalidParameterException e){
			//ok
		}
		catch(Exception e){
			fail("This should not happen:"+e);
		}
		
		try{
			percentile = CNode.getPercentile(0,1.0, 0 ,confidenceInterval);
			fail("This should fail on 0");
		}
		catch(InvalidParameterException e){
			//ok
		}
		catch(Exception e){
			fail("This should not happen:"+e);
		}
		
		assertTrue(Math.abs(1.96 - CNode.getPercentile(0,1.0, 1 ,confidenceInterval)) < EPSILON);
		
	}
	
	@Test
	public void testWaitingTimes() {
		List<Long> durations = new ArrayList<Long>();
		
		//This should "fail" because durations has no entries
		assertEquals(CNode.DEFAULT_WAITING_TIME, CNode.getWaitingTime(durations));
		
		durations.add(10L);
		
		assertEquals(10L, CNode.getWaitingTime(durations));
		
		durations.add(10L);
		durations.add(10L);
		durations.add(10L);
		durations.add(10L);
		
		assertEquals(10L, CNode.getWaitingTime(durations));
		
		durations = new ArrayList<Long>();
		durations.add(10L);
		durations.add(20L);
		
		assertEquals(8L, CNode.getWaitingTime(durations));
	}
	
	@Test
	public void testNextUpdateTimes() {
		List<Long> durations = new ArrayList<Long>();
		
		durations.add(10000L);
		durations.add(20000L);
		
		Long guess = System.currentTimeMillis()+8000;
		Long toleranceInMS = 500L;
		assertTrue(Math.abs(guess - CNode.getNextUpdateTime(durations)) < toleranceInMS);
	}

}