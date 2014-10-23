package edu.uci.ics.luci.cacophony.node;

import static org.junit.Assert.*;

import java.util.Random;

import net.minidev.json.JSONObject;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SensorConfigTest {
	
	static Random random = new Random();

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
	
	public long getPositiveRandom(){
		long x = -1L;
		while(x < 0){
			x = random.nextLong();
		}
		return x;
	}

	public SensorConfig makeSensorConfig() {
		String id = "RandomID+"+getPositiveRandom();
		String name = "RandomName+"+getPositiveRandom();
		String url = "http://www.random.com/+"+getPositiveRandom();
		String format = "html";
		String regex = "(.*)";
		String pathExpression = "//*[@id=\"cnn_ftrcntntinner\"]/div[9]/div[1]/text()[2]";
		Translator<?> translator = new TranslatorString();
		JSONObject translatorOptions = new JSONObject();
		translatorOptions.put("a", "thing");

		return new SensorConfig(id,
					name,
					url,
					format,
					regex,
					pathExpression,
					translator,
					translatorOptions
					);
	}
	
	
	@Test
	public void constructorTest() {

		try{
			makeSensorConfig();
		}
		catch(RuntimeException e){
			fail("This should not fail");
		}
	}
	
	
	@Test
	public void equalsTest() {
		SensorConfig x = makeSensorConfig();
		
		assertTrue(!x.equals(null));
		assertTrue(!x.equals("foo"));
		assertEquals(x,x);
		assertEquals(x.hashCode(),x.hashCode());
		
		SensorConfig y = new SensorConfig( x.getID(),
				x.getName(),
				x.getURL(),
				x.getFormat(),
				x.getRegEx(),
				x.getPathExpression(),
				x.getTranslator(),
				x.getTranslatorOptions());
		
		assertEquals(x,y);
		assertEquals(x.hashCode(),y.hashCode());
		
		
		SensorConfig z;
		y = new SensorConfig(null,null,null,null,null,null,null,null);
		z = new SensorConfig(null,null,null,null,null,null,null,null);
		assertTrue(!x.equals(y));
		assertTrue(!y.equals(x));
		assertTrue(y.equals(z));
		assertTrue(x.hashCode()!=y.hashCode());
		
		y = new SensorConfig(x.getID(),null,null,null,null,null,null,null);
		z = new SensorConfig(x.getID(),null,null,null,null,null,null,null);
		assertTrue(!x.equals(y));
		assertTrue(!y.equals(x));
		assertTrue(y.equals(z));
		assertTrue(x.hashCode()!=y.hashCode());
		
		y = new SensorConfig(x.getID()+"foo",null,null,null,null,null,null,null);
		z = new SensorConfig(x.getID()+"foo",null,null,null,null,null,null,null);
		assertTrue(!x.equals(y));
		assertTrue(!y.equals(x));
		assertTrue(y.equals(z));
		assertTrue(x.hashCode()!=y.hashCode());
		
		y = new SensorConfig(x.getID(),x.getName(),null,null,null,null,null,null);
		z = new SensorConfig(x.getID(),x.getName(),null,null,null,null,null,null);
		assertTrue(!x.equals(y));
		assertTrue(!y.equals(x));
		assertTrue(y.equals(z));
		assertTrue(x.hashCode()!=y.hashCode());
		
		y = new SensorConfig(x.getID(),x.getName()+"foo",null,null,null,null,null,null);
		z = new SensorConfig(x.getID(),x.getName()+"foo",null,null,null,null,null,null);
		assertTrue(!x.equals(y));
		assertTrue(!y.equals(x));
		assertTrue(y.equals(z));
		assertTrue(x.hashCode()!=y.hashCode());
		
		y = new SensorConfig(x.getID(),x.getName(),x.getURL(),null,null,null,null,null);
		z = new SensorConfig(x.getID(),x.getName(),x.getURL(),null,null,null,null,null);
		assertTrue(!x.equals(y));
		assertTrue(!y.equals(x));
		assertTrue(y.equals(z));
		assertTrue(x.hashCode()!=y.hashCode());
		
		y = new SensorConfig(x.getID(),x.getName(),x.getURL()+"foo",null,null,null,null,null);
		z = new SensorConfig(x.getID(),x.getName(),x.getURL()+"foo",null,null,null,null,null);
		assertTrue(!x.equals(y));
		assertTrue(!y.equals(x));
		assertTrue(y.equals(z));
		assertTrue(x.hashCode()!=y.hashCode());
		
		
		y = new SensorConfig(x.getID(),x.getName(),x.getURL(),x.getFormat(),null,null,null,null);
		z = new SensorConfig(x.getID(),x.getName(),x.getURL(),x.getFormat(),null,null,null,null);
		assertTrue(!x.equals(y));
		assertTrue(!y.equals(x));
		assertTrue(y.equals(z));
		assertTrue(x.hashCode()!=y.hashCode());
		
		y = new SensorConfig(x.getID(),x.getName(),x.getURL(),x.getFormat()+"foo",null,null,null,null);
		z = new SensorConfig(x.getID(),x.getName(),x.getURL(),x.getFormat()+"foo",null,null,null,null);
		assertTrue(!x.equals(y));
		assertTrue(!y.equals(x));
		assertTrue(y.equals(z));
		assertTrue(x.hashCode()!=y.hashCode());
		
		
		y = new SensorConfig(x.getID(),x.getName(),x.getURL(),x.getFormat(),x.getRegEx(),null,null,null);
		z = new SensorConfig(x.getID(),x.getName(),x.getURL(),x.getFormat(),x.getRegEx(),null,null,null);
		assertTrue(!x.equals(y));
		assertTrue(!y.equals(x));
		assertTrue(y.equals(z));
		assertTrue(x.hashCode()!=y.hashCode());
		
		y = new SensorConfig(x.getID(),x.getName(),x.getURL(),x.getFormat(),x.getRegEx()+"foo",null,null,null);
		z = new SensorConfig(x.getID(),x.getName(),x.getURL(),x.getFormat(),x.getRegEx()+"foo",null,null,null);
		assertTrue(!x.equals(y));
		assertTrue(!y.equals(x));
		assertTrue(y.equals(z));
		assertTrue(x.hashCode()!=y.hashCode());
		
		y = new SensorConfig(x.getID(),x.getName(),x.getURL(),x.getFormat(),x.getRegEx(),x.getPathExpression(),null,null);
		z = new SensorConfig(x.getID(),x.getName(),x.getURL(),x.getFormat(),x.getRegEx(),x.getPathExpression(),null,null);
		assertTrue(!x.equals(y));
		assertTrue(!y.equals(x));
		assertTrue(y.equals(z));
		assertTrue(x.hashCode()!=y.hashCode());
		
		y = new SensorConfig(x.getID(),x.getName(),x.getURL(),x.getFormat(),x.getRegEx(),x.getPathExpression()+"foo",null,null);
		z = new SensorConfig(x.getID(),x.getName(),x.getURL(),x.getFormat(),x.getRegEx(),x.getPathExpression()+"foo",null,null);
		assertTrue(!x.equals(y));
		assertTrue(!y.equals(x));
		assertTrue(y.equals(z));
		assertTrue(x.hashCode()!=y.hashCode());
		
		y = new SensorConfig(x.getID(),x.getName(),x.getURL(),x.getFormat(),x.getRegEx(),x.getPathExpression(),x.getTranslator(),null);
		z = new SensorConfig(x.getID(),x.getName(),x.getURL(),x.getFormat(),x.getRegEx(),x.getPathExpression(),x.getTranslator(),null);
		assertTrue(!x.equals(y));
		assertTrue(!y.equals(x));
		assertTrue(y.equals(z));
		assertTrue(x.hashCode()!=y.hashCode());
		
		y = new SensorConfig(x.getID(),x.getName(),x.getURL(),x.getFormat(),x.getRegEx(),x.getPathExpression(),x.getTranslator(),x.getTranslatorOptions());
		z = new SensorConfig(x.getID(),x.getName(),x.getURL(),x.getFormat(),x.getRegEx(),x.getPathExpression(),x.getTranslator(),x.getTranslatorOptions());
		assertTrue(x.equals(y));
		assertTrue(y.equals(x));
		assertTrue(y.equals(z));
		assertTrue(x.hashCode()==y.hashCode());
		
	}

	
	@Test
	public void serializeJSONTest() {

		SensorConfig x = makeSensorConfig();
		
		SensorConfig y = new SensorConfig(x.serializeToJSON());
		
		assertEquals(x,y);
		
		
	}
	
	

}
