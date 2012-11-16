package edu.uci.ics.luci.utility;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GlobalsTest extends Globals {
	
	Random random = new Random();
	String version = Integer.toString(random.nextInt());

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		while(Globals.getGlobals() != null){
			try{
				Thread.sleep(1000);
			}
			catch(InterruptedException e){
			}
		}
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

	@Override
	public String getSystemVersion() {
		return version;
	}
	
	@Test
	public void testStuff(){
		assertTrue(Globals.getGlobals() == null);
		
		Globals.setGlobals(this);
		assertEquals(Globals.getGlobals(),this);
		assertEquals(this.version, getSystemVersion());
		setTesting(null);
		assertEquals(true, isTesting());
		setTesting(true);
		assertEquals(true, isTesting());
		setTesting(false);
		assertEquals(false, isTesting());
		
		setQuitting(false);
		assertEquals(false,isQuitting());
		setQuitting(true);
		assertEquals(true,isQuitting());
	}
	
	public GlobalsTest(){
		this(true);
	}
	
	protected GlobalsTest(boolean testing){
		super();
		setTesting(true);
	}

}
