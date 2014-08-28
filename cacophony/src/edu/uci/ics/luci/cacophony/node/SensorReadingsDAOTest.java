package edu.uci.ics.luci.cacophony.node;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
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
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		SensorReadingsDAO.deleteDatabase("Yes, I know this will delete all of the data stored in the database. I'm not stupid.");
	}

	@After
	public void tearDown() throws Exception {
		SensorReadingsDAO.deleteDatabase("Yes, I know this will delete all of the data stored in the database. I'm not stupid.");
	}
	
	@Test
	public void testInitializeDBIfNecessary() {
		List<SensorConfig> sensorConfigs = makeSensorConfigs();
		try {
			SensorReadingsDAO.initializeDBIfNecessary(sensorConfigs);
		} catch (StorageException e) {
			fail("Encountered a StorageException: " + e.getMessage());
		}
	}

	@Test
	public void testStore() {
		List<SensorConfig> sensorConfigs = makeSensorConfigs();
		List<SensorReading> sensorReadings = makeSensorReadings(sensorConfigs);
		
		try {
			SensorReadingsDAO.initializeDBIfNecessary(sensorConfigs);
			SensorReadingsDAO.store(sensorReadings);
		} catch (StorageException e) {
			fail("Encountered a StorageException: " + e.getMessage());
		}
	}

	@Test
	public void testRetrieve() {
		List<SensorConfig> sensorConfigs = makeSensorConfigs();
		List<SensorReading> sensorReadings = makeSensorReadings(sensorConfigs);
		
		try {
			SensorReadingsDAO.initializeDBIfNecessary(sensorConfigs);
			SensorReadingsDAO.store(sensorReadings);
			List<Observation> observations = SensorReadingsDAO.retrieve(sensorConfigs);
			if (observations.size() != 1) {
				fail("Retrieved the wrong number of observations. Expected 1, found " + observations.size());
			}
			else {
				Observation observation = observations.get(0);
				if (observation.getFeatures().size() != 1) {
					fail("Retrieved the wrong number of features. Expected 1, found " + observation.getFeatures().size());
				}
				else if (!observation.getFeatures().get(0).getRawValue().equals(FEATURE_VALUE)) {
					fail(String.format("The retrieved feature had a wrong value of \"{0}\"", observation.getFeatures().get(0).getRawValue()));
				}
				
				if (!observation.getTarget().getRawValue().equals(TARGET_VALUE)) {
					fail(String.format("The retrieved feature had a wrong value of \"{0}\"", observation.getFeatures().get(0).getRawValue()));
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
			SensorReadingsDAO.initializeDBIfNecessary(sensorConfigs);
			SensorReadingsDAO.store(sensorReadings1);
			SensorReadingsDAO.store(sensorReadings2);
			SensorReadingsDAO.store(sensorReadings3);
			List<Date> timestamps = SensorReadingsDAO.retrieveStorageTimes(3);
			if (timestamps.size() != 3) {
				fail("Retrieved the wrong number of storage times. Expected 3, found " + timestamps.size());
			}
			
			timestamps = SensorReadingsDAO.retrieveStorageTimes(4);
			if (timestamps.size() != 3) {
				fail("Retrieved the wrong number of storage times. Expected 3, found " + timestamps.size());
			}
			
			timestamps = SensorReadingsDAO.retrieveStorageTimes(2);
			if (timestamps.size() != 2) {
				fail("Retrieved the wrong number of storage times. Expected 2, found " + timestamps.size());
			}
		} catch (StorageException e) {
			fail();
		}
	}
	
	private List<SensorConfig> makeSensorConfigs() {
		Translator translator = new TranslatorString();
		JSONObject translatorOptions = new JSONObject();
		SensorConfig sensorConfig = new SensorConfig("TestID", "TestName", "TestURL", "html", ".*", "", translator, translatorOptions);
		List<SensorConfig> sensorConfigs = new ArrayList<SensorConfig>();
		sensorConfigs.add(sensorConfig);
		
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
