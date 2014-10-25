package edu.uci.ics.luci.cacophony.node;

import static org.junit.Assert.*;

import java.security.InvalidParameterException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

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
			CNode cn = null;
			CNodeConfiguration cnn = null;
			try{
				JSONObject js = ResponderConfigurationLoaderTest.makeLoadConfigurationRequest(CNodeServerTest.makeARandomP2PServerAddress(), "p2p://foo/bar");
				JSONObject data = (JSONObject)js.get("data");
				JSONArray cnodes = (JSONArray)data.get("c_nodes");
				JSONObject cnode = (JSONObject)cnodes.get(0);
				cnn = new CNodeConfiguration((JSONObject)cnode.get("configuration"));
				cn = new CNode(cnn, UUID.randomUUID().toString());
				assertTrue(cn.getConfiguration() == cnn);
			}
			catch(IllegalArgumentException e){
				fail("This shouldn't throw an exception");
			} catch (StorageException e) {
				fail("This shouldn't throw an exception:"+e);
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
		try{
			CNode.getPercentile(1.0,1,0.0);
			fail("This should fail on 0.0");
		}
		catch(InvalidParameterException e){
			//ok
		}
		catch(Exception e){
			fail("This should not happen:"+e);
		}
		
		
		Double confidenceInterval = Double.valueOf(0.95);
		try{
			CNode.getPercentile(1.0,-1,confidenceInterval);
			fail("This should fail on -1");
		}
		catch(InvalidParameterException e){
			//ok
		}
		catch(Exception e){
			fail("This should not happen:"+e);
		}
		
		try{
			CNode.getPercentile(1.0,0,confidenceInterval);
			fail("This should fail on 0");
		}
		catch(InvalidParameterException e){
			//ok
		}
		catch(Exception e){
			fail("This should not happen:"+e);
		}
		
		assertTrue(Math.abs(1.96 - CNode.getPercentile(1.0,1,confidenceInterval)) < EPSILON);
	}
	
	@Test
	public void testWaitingTimes() {
		List<Date> dates = new ArrayList<Date>();
		assertEquals(CNode.DEFAULT_WAITING_TIME, CNode.getWaitingTime(dates));
		
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT);
    dateFormatter.setLenient(false);
    
    try{
	    Date d0 = dateFormatter.parse("2014-07-10 23:45:00.000");
	    Date d1 = dateFormatter.parse("2014-07-10 23:45:00.010");
			Date d2 = dateFormatter.parse("2014-07-10 23:45:00.020");
			Date d3 = dateFormatter.parse("2014-07-10 23:45:00.030");
			Date d4 = dateFormatter.parse("2014-07-10 23:45:00.040");
			
			dates.add(d0);
			assertEquals(CNode.DEFAULT_WAITING_TIME, CNode.getWaitingTime(dates));
			
			dates.add(d1);
			dates.add(d2);
			dates.add(d3);
			dates.add(d4);
			
			assertEquals(10L, CNode.getWaitingTime(dates));
			
			dates = new ArrayList<Date>();
			dates.add(d0);
			dates.add(d1);
			dates.add(d3);
			
			assertEquals(8L, CNode.getWaitingTime(dates));
    }
    catch (ParseException e){
    	fail("Error when trying to parse test dates: " + e);
    }
	}
	
	@Test
	public void testNextUpdateTimes() {
		List<Date> dates = new ArrayList<Date>();
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT);
    dateFormatter.setLenient(false);
		
    try{
	    Date d0 = dateFormatter.parse("2014-07-10 23:45:00.000");
	    Date d1 = dateFormatter.parse("2014-07-10 23:45:10.000");
	    Date d2 = dateFormatter.parse("2014-07-10 23:45:30.000");
	    dates.add(d0);
	    dates.add(d1);
			dates.add(d2);
			
			Long guess = 8000L;
			Long toleranceInMS = 500L;
			assertTrue(Math.abs(guess - CNode.getWaitingTime(dates)) < toleranceInMS);
    }
    catch (ParseException e){
    	fail("Error when trying to parse test dates: " + e);
    }
	}

}