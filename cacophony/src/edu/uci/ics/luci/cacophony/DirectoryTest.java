package edu.uci.ics.luci.cacophony;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DirectoryTest {

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
	public void testStartHeartbeat() {
		Directory d = new Directory();
		d.startHeartbeat(0L, 500L);
		try{
			String me = InetAddress.getLocalHost().getHostAddress();
			Long heartbeat01 = d.getHeartbeat(me);
			assertTrue(heartbeat01 != null);
			Thread.sleep(750);
			Long heartbeat02 = d.getHeartbeat(me);
			assertTrue(heartbeat02 != null);
			assertTrue(!heartbeat01.equals(heartbeat02));
		} catch (UnknownHostException e) {
			fail(e.toString());
		} catch (InterruptedException e) {
			fail(e.toString());
		}
	}

}
