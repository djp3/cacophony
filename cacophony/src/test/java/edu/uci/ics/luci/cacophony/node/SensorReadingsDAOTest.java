package edu.uci.ics.luci.cacophony.node;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import net.minidev.json.JSONObject;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SensorReadingsDAOTest {
	final String FEATURE_VALUE = "a man a plan a canal panama";
	final String TARGET_VALUE = "foobar";
	final SensorReadingsDAO sensorReadingsDAO = SensorReadingsDAO.getInstance("cacophony_test");
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {

	}

	@Before
	public void setUp() throws Exception {
		sensorReadingsDAO.deleteDatabase("Yes, I know this will delete all of the data stored in the database. I'm not stupid.");
	}

	@After
	public void tearDown() throws Exception {
		sensorReadingsDAO.deleteDatabase("Yes, I know this will delete all of the data stored in the database. I'm not stupid.");
	}
	
	@Test
	public void testInitializeDBIfNecessary() {
		List<SensorConfig> sensorConfigs = makeSensorConfigs();
		try {
			sensorReadingsDAO.initializeDBIfNecessary(sensorConfigs);
		} catch (StorageException e) {
			fail("Encountered a StorageException: " + e.getMessage());
		}
	}

	@Test
	public void testStoreAndRetrieve() {
		List<SensorConfig> sensorConfigs = makeSensorConfigs();
		List<SensorReading> sensorReadings = makeSensorReadings(sensorConfigs);

		try {
			sensorReadingsDAO.initializeDBIfNecessary(sensorConfigs);
			sensorReadingsDAO.store(sensorReadings);
			List<Observation> observations = sensorReadingsDAO.retrieve(sensorConfigs);
			if (observations.size() != 1) {
				fail("Retrieved the wrong number of observations. Expected 1, found " + observations.size());
			}
			else {
				Observation observation = observations.get(0);
				if (observation.getFeatures().size() != 1) {
					fail("Retrieved the wrong number of features. Expected 1, found " + observation.getFeatures().size());
				}
				else if (!observation.getFeatures().get(0).getRawValue().equals(FEATURE_VALUE)) {
					fail(String.format("The retrieved feature had a wrong value of \"%s\"", observation.getFeatures().get(0).getRawValue()));
				}
				
				if (!observation.getTarget().getRawValue().equals(TARGET_VALUE)) {
					fail(String.format("The retrieved feature had a wrong value of \"%s\"", observation.getFeatures().get(0).getRawValue()));
				}
			}
			
		} catch (StorageException e) {
			fail("Encountered a StorageException: " + e.getMessage());
		} catch (UnknownSensorException e) {
			fail("Encountered an UnknownSensorException: " + e.getMessage());
		}
	}

	@Test
	public void testRetrieveStorageTimes() {
		List<SensorConfig> sensorConfigs = makeSensorConfigs();
		List<SensorReading> sensorReadings1 = makeSensorReadings(sensorConfigs);
		List<SensorReading> sensorReadings2 = makeSensorReadings(sensorConfigs);
		List<SensorReading> sensorReadings3 = makeSensorReadings(sensorConfigs);
		
		try {
			sensorReadingsDAO.initializeDBIfNecessary(sensorConfigs);
			sensorReadingsDAO.store(sensorReadings1);
			sensorReadingsDAO.store(sensorReadings2);
			sensorReadingsDAO.store(sensorReadings3);
			List<Long> timestamps = sensorReadingsDAO.retrieveStorageTimes(3);
			if (timestamps.size() != 3) {
				fail("Retrieved the wrong number of storage times. Expected 3, found " + timestamps.size());
			}
			for(Long time:timestamps){
				//System.out.println("time: "+time+", now: "+System.currentTimeMillis());
				if(Math.abs(time - System.currentTimeMillis()) > 1000){
					fail("Time between storage and retrieve is over a second suggesting something went wrong");
				}
			}
			
			timestamps = sensorReadingsDAO.retrieveStorageTimes(4);
			if (timestamps.size() != 3) {
				fail("Retrieved the wrong number of storage times. Expected 3, found " + timestamps.size());
			}
			
			timestamps = sensorReadingsDAO.retrieveStorageTimes(2);
			if (timestamps.size() != 2) {
				fail("Retrieved the wrong number of storage times. Expected 2, found " + timestamps.size());
			}
		} catch (StorageException e) {
			fail();
		}
	}
	
	private List<SensorConfig> makeSensorConfigs() {
		SensorConfig sensorConfigFeature = new SensorConfig("Feature_TestID", "Feature_TestName", "Feature_TestURL", "html", ".*", "", new TranslatorString(), new JSONObject());
		SensorConfig sensorConfigTarget = new SensorConfig("Target_TestID", "Target_TestName", "Target_TestURL", "html", ".*", "", new TranslatorString(), new JSONObject());
		List<SensorConfig> sensorConfigs = new ArrayList<SensorConfig>();
		sensorConfigs.add(sensorConfigFeature);
		sensorConfigs.add(sensorConfigTarget);
		
		return sensorConfigs;
	}
	
	private List<SensorReading> makeSensorReadings(List<SensorConfig> sensorConfigs) {
		SensorReading featureReading = new SensorReading(sensorConfigs.get(0), FEATURE_VALUE);
		SensorReading targetReading = new SensorReading(sensorConfigs.get(1), TARGET_VALUE);
		List<SensorReading> sensorReadings = new ArrayList<SensorReading>();
		sensorReadings.add(featureReading);
		sensorReadings.add(targetReading);
		
		return sensorReadings;
	}
}
