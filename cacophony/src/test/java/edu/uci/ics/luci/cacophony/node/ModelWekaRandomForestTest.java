package edu.uci.ics.luci.cacophony.node;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import net.minidev.json.JSONObject;

import org.junit.Test;

import weka.classifiers.trees.RandomForest;

/**
 * @author John
 *
 */
public class ModelWekaRandomForestTest {
	private static Model model;
	private final Double FEATURE_VALUE_1 = 3.0;
	private final Double FEATURE_VALUE_2 = 4.0;
	private final String TARGET_VALUE = "red";
	
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
		
		model = new ModelWeka(new RandomForest());
		model.train(observations);
	}

//	@Test
//	public void testPredict() {
//		List<SensorConfig> sensorConfigs = makeSensorConfigs();
//		List<SensorReading> sensorReadings = makeSensorReadings(sensorConfigs, FEATURE_VALUE_1, FEATURE_VALUE_2, null);	
//		Observation observation = new Observation(sensorReadings);
//		Double prediction = (Double)model.predict(observation);
//		// TODO: this needs to be rewritten once we've determined how categorical values work (i.e., how is the list of possible categories determined?)
//		if (prediction != 0) {
//			fail(String.format("The random forest model's prediction was wrong. Expected %d, but got %d.", 0, prediction));
//		}
////		if (!prediction.equals(TARGET_VALUE)) {
////			fail(String.format("The random forest model's prediction was wrong. Expected %d, but got %d.", TARGET_VALUE, prediction));
////		}
//		
//		sensorConfigs = makeSensorConfigs();
//		sensorReadings = makeSensorReadings(sensorConfigs, FEATURE_VALUE_1, 42.0, null);	
//		observation = new Observation(sensorReadings);
//		
//		prediction = (Double)model.predict(observation);
//		if (prediction == 0) {
//			fail(String.format("The random forest model's prediction was wrong. Expected %d, but got %d.", 0, prediction));
//		}
////		if (prediction.equals(TARGET_VALUE)) {
////			fail("The random forest model's prediction should not have matched.");
////		}
//	}

	private List<SensorConfig> makeSensorConfigs() {
		SensorConfig sensorConfigFeature1 = new SensorConfig("Feature_TestID1", "Feature_TestName1", "Feature_TestURL1", "html", ".*", "", new TranslatorDouble(), new JSONObject());
		SensorConfig sensorConfigFeature2 = new SensorConfig("Feature_TestID2", "Feature_TestName2", "Feature_TestURL2", "html", ".*", "", new TranslatorDouble(), new JSONObject());
		SensorConfig sensorConfigTarget = new SensorConfig("Target_TestID", "Target_TestName", "Target_TestURL", "html", ".*", "", new TranslatorCategorical(), new JSONObject());
		List<SensorConfig> sensorConfigs = new ArrayList<SensorConfig>();
		sensorConfigs.add(sensorConfigFeature1);
		sensorConfigs.add(sensorConfigFeature2);
		sensorConfigs.add(sensorConfigTarget);
		
		return sensorConfigs;
	}
	
	private List<SensorReading> makeSensorReadings(List<SensorConfig> sensorConfigs, Double featureValue1, Double featureValue2, String targetValue) {
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
