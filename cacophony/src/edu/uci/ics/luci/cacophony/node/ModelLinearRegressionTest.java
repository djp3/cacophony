package edu.uci.ics.luci.cacophony.node;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import net.minidev.json.JSONObject;
import org.junit.Test;

public class ModelLinearRegressionTest {
	private Model model;
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
		
		Model model = new ModelLinearRegression();
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
	}

	private List<SensorConfig> makeSensorConfigs() {
		SensorConfig sensorConfigFeature = new SensorConfig("Feature_TestID", "Feature_TestName", "Feature_TestURL", "html", ".*", "", new TranslatorNumeric(), new JSONObject());
		SensorConfig sensorConfigTarget = new SensorConfig("Target_TestID", "Target_TestName", "Target_TestURL", "html", ".*", "", new TranslatorNumeric(), new JSONObject());
		List<SensorConfig> sensorConfigs = new ArrayList<SensorConfig>();
		sensorConfigs.add(sensorConfigFeature);
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
