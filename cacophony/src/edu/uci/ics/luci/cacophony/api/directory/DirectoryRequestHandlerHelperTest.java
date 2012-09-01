package edu.uci.ics.luci.cacophony.api.directory;


import static org.junit.Assert.*;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.quub.util.Pair;
import com.quub.webserver.HandlerAbstract;
import com.quub.webserver.RequestDispatcher.HTTPRequest;

import edu.uci.ics.luci.cacophony.directory.Directory;

public class DirectoryRequestHandlerHelperTest extends DirectoryRequestHandlerHelper{

	public DirectoryRequestHandlerHelperTest() {
		super(new Directory());
	}
	
	
	@Override
	public HandlerAbstract copy() {
		return null;
	}

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
	public void testCases(){
		assertTrue(testNamespaceEquality(null,null) == null);
		assertTrue(testNamespaceEquality(null,"foo") != null);
		assertTrue(testNamespaceEquality("foo",null) != null);
		assertTrue(testNamespaceEquality("foo","bar") != null);
		assertTrue(testNamespaceEquality("foo","foo") == null);
		
		String namespace = "test:"+System.currentTimeMillis();
		getDirectory().setDirectoryNamespace(namespace);
		Map<String,String> params = new HashMap<String,String>();
		assertTrue(namespaceOK(params) != null);
		assertTrue(directoryAPIOK(params) != null);
		
		params.put("namespace",namespace);
		assertTrue(namespaceOK(params) == null);
		assertTrue(directoryAPIOK(params) != null);
		
		params.put("version",getAPIVersion());
		assertTrue(namespaceOK(params) == null);
		assertTrue(directoryAPIOK(params) == null);
		
		params.remove("namespace");
		assertTrue(namespaceOK(params) != null);
		assertTrue(directoryAPIOK(params) != null);
		
		getDirectory().setQuitting(true);
	}

	@Override
	public Pair<byte[], byte[]> handle(InetAddress ip, HTTPRequest httpRequestType, Map<String, String> headers, String restFunction, Map<String, String> parameters) {
		return null;
	}


}
