package edu.uci.ics.luci.cacophony.node;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import net.minidev.json.JSONObject;

import org.junit.Test;

import weka.classifiers.functions.LinearRegression;

public class ModelWekaLinearRegressionTest {
	private static Model model;
	private final Double FEATURE_VALUE_1 = 2.4;
	private final Double FEATURE_VALUE_2 = 5.0;
	private final Double TARGET_VALUE = 7.0;
	
	@Test
	public void testTrain() {
		List<SensorConfig> sensorConfigs = makeSensorConfigs();
		List<SensorReading> sensorReadings = makeSensorReadings(sensorConfigs, FEATURE_VALUE_1, FEATURE_VALUE_2, TARGET_VALUE);
		
		// Extract the target reading from the list of all sensor readings so that we can make an Observation instance to train on
		SensorReading targetReading = sensorReadings.get(sensorReadings.size() - 1);
		sensorReadings.remove(sensorReadings.size() - 1);
		Observation observation = new Observation(sensorReadings, targetReading);
		List<Observation> observations = new ArrayList<Observation>();
		observations.add(observation);
		
		model = new ModelWeka(new LinearRegression());
		model.train(observations);
	}

	@Test
	public void testPredict() {
		List<SensorConfig> sensorConfigs = makeSensorConfigs();
		List<SensorReading> sensorReadings = makeSensorReadings(sensorConfigs, FEATURE_VALUE_1, FEATURE_VALUE_2, null);	
		Observation observation = new Observation(sensorReadings);
		
		Double prediction = (Double)model.predict(observation);
		if (!prediction.equals(TARGET_VALUE)) {
			fail(String.format("The linear regression model's prediction was wrong. Expected {0}, but got {1}.", TARGET_VALUE, prediction));
		}
		
		sensorConfigs = makeSensorConfigs();
		sensorReadings = makeSensorReadings(sensorConfigs, FEATURE_VALUE_1, 42.0, null);	
		observation = new Observation(sensorReadings);
		
		prediction = (Double)model.predict(observation);
		if (prediction.equals(TARGET_VALUE)) {
			fail("The linear regression model's prediction should not have matched.");
		}
	}

	private List<SensorConfig> makeSensorConfigs() {
		SensorConfig sensorConfigFeature1 = new SensorConfig("Feature_TestID1", "Feature_TestName1", "Feature_TestURL1", "html", ".*", "", new TranslatorDouble(), new JSONObject());
		SensorConfig sensorConfigFeature2 = new SensorConfig("Feature_TestID2", "Feature_TestName2", "Feature_TestURL2", "html", ".*", "", new TranslatorDouble(), new JSONObject());
		SensorConfig sensorConfigTarget = new SensorConfig("Target_TestID", "Target_TestName", "Target_TestURL", "html", ".*", "", new TranslatorDouble(), new JSONObject());
		List<SensorConfig> sensorConfigs = new ArrayList<SensorConfig>();
		sensorConfigs.add(sensorConfigFeature1);
		sensorConfigs.add(sensorConfigFeature2);
		sensorConfigs.add(sensorConfigTarget);
		
		return sensorConfigs;
	}
	
	private List<SensorReading> makeSensorReadings(List<SensorConfig> sensorConfigs, Double featureValue1, Double featureValue2, Double targetValue) {
		SensorReading featureReading1 = new SensorReading(sensorConfigs.get(0), featureValue1.toString());
		SensorReading featureReading2 = new SensorReading(sensorConfigs.get(1), featureValue2.toString());
		List<SensorReading> sensorReadings = new ArrayList<SensorReading>();
		sensorReadings.add(featureReading1);
		sensorReadings.add(featureReading2);
		
		if (targetValue != null) {
			SensorReading targetReading = new SensorReading(sensorConfigs.get(2), targetValue.toString());
			sensorReadings.add(targetReading);
		}
		return sensorReadings;
	}
}
