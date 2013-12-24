package edu.uci.ics.luci.cacophony.node;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CNodeAddressTest {

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
			new CNodeAddress(null,null);
			fail("Should throw an exception");
		}
		catch(IllegalArgumentException e){
			// Expected
		}
		catch(RuntimeException e){
			fail("Should not throw this exception");
		}
	
	
		try{
			new CNodeAddress("",null);
			fail("Should throw an exception");
		}
		catch(IllegalArgumentException e){
			// Expected
		}
		catch(RuntimeException e){
			fail("Should not throw this exception");
		}
	
	
		try{
			new CNodeAddress(null,"");
			fail("Should throw an exception");
		}
		catch(IllegalArgumentException e){
			// Expected
		}
		catch(RuntimeException e){
			fail("Should not throw this exception");
		}
		
		try{
			new CNodeAddress("","");
		}
		catch(IllegalArgumentException e){
			fail("Should not throw an exception");
		}
		catch(RuntimeException e){
			fail("Should not throw this exception");
		}
		
		String testAddress = "p2p://server/simple_path";
		CNodeAddress cna = new CNodeAddress(testAddress);
		assertEquals("server",cna.getServer());
		assertEquals("/simple_path",cna.getPath());
		assertEquals(cna.toString(),testAddress);
		
		cna.setPath("/");
		assertEquals(cna.toString(),"p2p://server/");
		
		try{
			cna.setPath("");
		}
		catch(IllegalArgumentException e){
			fail("Should not throw an exception");
		}
		catch(RuntimeException e){
			fail("Should not throw this exception");
		}
		
		assertEquals("p2p://server/",cna.toString());
		
		
		testAddress = "p2p://server";
		cna = new CNodeAddress(testAddress);
		assertEquals("server",cna.getServer());
		assertEquals("",cna.getPath());
		assertEquals(testAddress+"/",cna.toString());
		
		
		testAddress = "p2p://server/";
		cna = new CNodeAddress(testAddress);
		assertEquals("server",cna.getServer());
		assertEquals("/",cna.getPath());
		assertEquals(cna.toString(),testAddress);
		
		testAddress = "//server";
		cna = new CNodeAddress(testAddress);
		assertEquals("server",cna.getServer());
		assertEquals("",cna.getPath());
		assertEquals("p2p:"+testAddress+"/",cna.toString());
		
		testAddress = "server";
		cna = new CNodeAddress(testAddress);
		assertEquals("server",cna.getServer());
		assertEquals("",cna.getPath());
		assertEquals("p2p://"+testAddress+"/",cna.toString());
		
		
		
		
		testAddress = "//server/simple_path";
		cna = new CNodeAddress(testAddress);
		assertEquals("server",cna.getServer());
		assertEquals("/simple_path",cna.getPath());
		assertEquals("p2p:"+testAddress,cna.toString());
		
		testAddress = "//server/not/so//simple/path/";
		cna = new CNodeAddress(testAddress);
		assertEquals("server",cna.getServer());
		assertEquals("/not/so//simple/path/",cna.getPath());
		assertEquals("p2p:"+testAddress,cna.toString());
		
		testAddress = "server/not/so//simple/path/";
		cna = new CNodeAddress(testAddress);
		assertEquals("server",cna.getServer());
		assertEquals("/not/so//simple/path/",cna.getPath());
		assertEquals("p2p://"+testAddress,cna.toString());
		
		
		String testServer = "server3";
		String testPath = "/this/is/the/path";
		cna = new CNodeAddress(testServer,testPath);
		assertEquals(testServer,cna.getServer());
		assertEquals(testPath,cna.getPath());
		assertEquals("p2p://"+testServer+testPath,cna.toString());
		

		try{
			cna.setPath("foo/bar/does/not/start/with/a/slash");
			fail("Should throw an exception");
		}
		catch(IllegalArgumentException e){
			// Expected
		}
		catch(RuntimeException e){
			fail("Should not throw this exception");
		}
		
	}

}
