package edu.uci.ics.luci.cacophony.node;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PollingPolicyTest {

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
			PollingPolicy.fromString("This is an asinine policy");
			fail("Should throw an exception");
		}
		catch(IllegalArgumentException e){
			//Expected
		}
		
		for(PollingPolicy x: PollingPolicy.values()){
			assertEquals(x,PollingPolicy.fromString(PollingPolicy.toString(x)));
		}
		
	}

}
