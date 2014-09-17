package edu.uci.ics.luci.cacophony.node;

import java.util.List;

import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;


public class ModelRandomForest extends ModelWeka {
	private RandomForest mRandomForestModel = new RandomForest();
	
	@Override
	public void train(List<Observation> observations) {
		if (observations.size() == 0) {
			return;
		}
		
		// To set the properties of the Weka attributes, we need to know the data type for each sensor. We
		// arbitrarily look at the sensor readings in the first observation to get this information.
		Observation firstObservation = observations.get(0);  
		mWekaAttributes = new FastVector(firstObservation.getFeatures().size());
		for (SensorReading reading : firstObservation.getFeatures()){
			mWekaAttributes.addElement(createWekaAttribute(reading));
		}
		Attribute targetAttribute = createWekaAttribute(firstObservation.getTarget());
		mWekaAttributes.addElement(targetAttribute);
		
		Instances trainingSet = new Instances("Cacophony", mWekaAttributes, observations.size()); // "Cacophony" name is arbitrary
		trainingSet.setClassIndex(mWekaAttributes.size() - 1);
		for (Observation obs : observations) {
			Instance instance = createWekaInstance(obs);
			trainingSet.add(instance);
		}
		
		mRandomForestModel = new RandomForest();
		try {
			mRandomForestModel.buildClassifier(trainingSet);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Object predict(Observation obs) {
		if (mRandomForestModel == null || mWekaAttributes == null) {
			throw new IllegalStateException("Unable to make prediction because model has not been trained.");
		}
		Instance instance = createWekaInstance(obs);
		try {
			return mRandomForestModel.classifyInstance(instance);
		} catch (Exception e) {
			return null;
		}
	}
}
