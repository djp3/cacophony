package edu.uci.ics.luci.utility.webserver;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.ics.luci.utility.webserver.handlers.HandlerVersion;

public class RequestDispatcherTest {
	
	Random random = new Random();

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
	public void testRequestDispatcher() {
		final String testVersion = Integer.toString(random.nextInt());
		
		try {
			Map<String, HandlerAbstract> requestHandlerRegistry = new HashMap<String, HandlerAbstract>();
			
			HandlerVersion versionHandler= new HandlerVersion(testVersion);
			requestHandlerRegistry.put(null,versionHandler);
			requestHandlerRegistry.put("",versionHandler);
			requestHandlerRegistry.put("version",versionHandler);
			
			
			RequestDispatcher dispatcher = new RequestDispatcher(requestHandlerRegistry);
			
			assertEquals(3,dispatcher.getRequestHandlerRegistrySize());
			
			HandlerAbstract handler = dispatcher.getHandler("version");
			assertTrue(handler != null);
			
			while(dispatcher.getNumInstantiatingThreadsInvoked() == 0){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
			
			while(dispatcher.getNumLiveInstantiatingThreads() > 0){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
			
			assertEquals(dispatcher.getNumInstantiatingThreadsInvoked(),1);
			assertEquals(dispatcher.getInstancesToStage(),dispatcher.getRequestHandlersSize(HandlerVersion.class));
			
		} catch (RuntimeException e) {
			fail("Couldn't start webserver"+e);
		}
	}

}
