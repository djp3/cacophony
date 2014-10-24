package edu.uci.ics.luci.cacophony.node;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import net.minidev.json.JSONObject;

import org.junit.Test;

import weka.classifiers.functions.LinearRegression;
import weka.core.SelectedTag;

public class ModelWekaLinearRegressionTest {
	private static Model model;
	private final Double FEATURE_VALUE_1A = 2.4;
	private final Double FEATURE_VALUE_1B = 5.0;
	private final Double FEATURE_VALUE_2A = 7.2;
	private final Double FEATURE_VALUE_2B = 2.0;
	private final Double FEATURE_VALUE_3A = 21.0;
	private final Double FEATURE_VALUE_3B = -4.2;
	private final Double TARGET_VALUE_1 = 7.0;
	private final Double TARGET_VALUE_2 = 4.5;
	private final Double TARGET_VALUE_3 = 12.0;
	
	@Test
	public void testTrain() {
		List<SensorConfig> sensorConfigs = makeSensorConfigs();
		List<SensorReading> sensorReadings1 = makeSensorReadings(sensorConfigs, FEATURE_VALUE_1A, FEATURE_VALUE_1B, TARGET_VALUE_1);
		List<SensorReading> sensorReadings2 = makeSensorReadings(sensorConfigs, FEATURE_VALUE_2A, FEATURE_VALUE_2B, TARGET_VALUE_2);
		List<SensorReading> sensorReadings3 = makeSensorReadings(sensorConfigs, FEATURE_VALUE_3A, FEATURE_VALUE_3B, TARGET_VALUE_3);
		
		// Extract the target reading from the list of all sensor readings so that we can make an Observation instance to train on
		SensorReading targetReading1 = sensorReadings1.get(sensorReadings1.size() - 1);
		sensorReadings1.remove(sensorReadings1.size() - 1);
		SensorReading targetReading2 = sensorReadings2.get(sensorReadings2.size() - 1);
		sensorReadings2.remove(sensorReadings2.size() - 1);
		SensorReading targetReading3 = sensorReadings3.get(sensorReadings3.size() - 1);
		sensorReadings3.remove(sensorReadings3.size() - 1);
		
		Observation observation1 = new Observation(sensorReadings1, targetReading1);
		Observation observation2 = new Observation(sensorReadings2, targetReading2);
		Observation observation3 = new Observation(sensorReadings3, targetReading3);
		List<Observation> observations = new ArrayList<Observation>();
		observations.add(observation1);
		observations.add(observation2);
		observations.add(observation3);
		
		LinearRegression lr = new LinearRegression();
		// By default, Weka will eliminate colinear attributes and use the AIC to simplify the model. For purposes of testing, we disable
		// these behaviors so that we can calculate the expected result by hand. 
		lr.setEliminateColinearAttributes(false);
		lr.setAttributeSelectionMethod(new SelectedTag(LinearRegression.SELECTION_NONE, LinearRegression.TAGS_SELECTION));
		model = new ModelWeka(lr);
		model.train(observations);
	}

	@Test
	public void testPredict() {
		List<SensorConfig> sensorConfigs = makeSensorConfigs();
		List<SensorReading> sensorReadings = makeSensorReadings(sensorConfigs, FEATURE_VALUE_1A, FEATURE_VALUE_1B, null);	
		Observation observation = new Observation(sensorReadings);
		
		Double prediction = (Double)model.predict(observation);
		// We check for a range instead of an exact value due to possible floating point inaccuracies.
		// Can manually calculate expected value in MATLAB with this code:
		// >> X = [1 2.4 5; 1 7.2 2; 1 21 -4.2]
		// >> Y = [7;4.5;12]
		// >> beta = (X'*X)\X'*Y
		// >> [1 2.4 5.0]*beta
		if (prediction < TARGET_VALUE_1-.01 || prediction > TARGET_VALUE_1+.01 ) {
			fail(String.format("The linear regression model's prediction was wrong. Expected %f, but got %f.", TARGET_VALUE_1, prediction));
		}
		
		sensorConfigs = makeSensorConfigs();
		sensorReadings = makeSensorReadings(sensorConfigs, FEATURE_VALUE_1A, 42.0, null);	
		observation = new Observation(sensorReadings);
		
		prediction = (Double)model.predict(observation);
		if (prediction.equals(TARGET_VALUE_1)) {
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
