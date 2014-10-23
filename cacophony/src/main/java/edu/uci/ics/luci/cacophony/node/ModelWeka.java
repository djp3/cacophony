package edu.uci.ics.luci.cacophony.node;

import java.util.Date;
import java.util.List;
import java.util.Set;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class ModelWeka implements Model {
	protected FastVector mWekaAttributes = null;
	protected Classifier mWekaClassifier = null;
	
	public ModelWeka(Classifier wekaClassifier) {
		mWekaClassifier = wekaClassifier;
	}
	
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

		try {
			mWekaClassifier.buildClassifier(trainingSet);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Object predict(Observation observation) {
		if (mWekaClassifier == null || mWekaAttributes == null) {
			throw new IllegalStateException("Unable to make prediction because model has not been trained.");
		}
		Instance instance = createWekaInstance(observation);
		try {
			return mWekaClassifier.classifyInstance(instance);
		} catch (Exception e) {
			return null;
		}
	}

	protected Instance createWekaInstance(Observation obs) {
		int size = (obs.getTarget() == null ? obs.getFeatures().size() : obs.getFeatures().size() + 1);
		Instance instance = new DenseInstance(size);
		for (int i=0; i<obs.getFeatures().size(); ++i) {
			SensorReading reading = obs.getFeatures().get(i);
			Attribute attribute = (Attribute)mWekaAttributes.elementAt(i);
			setValueInInstance(instance, attribute, reading);
		}
		if (obs.getTarget() != null) {
			setValueInInstance(instance, (Attribute)mWekaAttributes.elementAt(mWekaAttributes.size() - 1), obs.getTarget());
		}
		return instance;
	}

	protected Attribute createWekaAttribute(SensorReading reading) {
		Object translation = reading.getTranslatedValue();
		Attribute attribute;
		if (translation instanceof Double) {
			attribute = new Attribute(reading.getSensorConfig().getID());
		}
		else if (translation instanceof String) {
			FastVector attributeValues = null;
			attribute = new Attribute(reading.getSensorConfig().getID(), attributeValues);
		}
		else if (translation instanceof Categorical<?>) {
			@SuppressWarnings("unchecked")
			Set<String> possibleCategories = ((Categorical<String>)translation).getPossibleCategories();
			FastVector categories = new FastVector(possibleCategories.size());
			for (String category : possibleCategories)
				categories.addElement(category);
			attribute = new Attribute(reading.getSensorConfig().getID(), categories);
		}
		else if (translation instanceof Date) {
			throw new UnsupportedOperationException("The model does not currently support date types.");
		}
		else {
			String debugClass;
			if(translation == null){
				debugClass = "null";
			}
			else{
				debugClass = translation.getClass().getCanonicalName();
			}
			throw new UnsupportedOperationException(String.format("Encountered unknown attribute type: %i", debugClass));
		}
		return attribute;
	}
	
	protected void setValueInInstance(Instance instance, Attribute attribute, SensorReading reading) {
		Object translation = reading.getTranslatedValue();
		if (translation instanceof Double) {
			instance.setValue(attribute, (Double)translation);
		}
		else if (translation instanceof String) {
			instance.setValue(attribute, translation.toString());
		}
		else if (translation instanceof Categorical<?>) {
			@SuppressWarnings("unchecked")
			String value = ((Categorical<String>)translation).getCategory();
			instance.setValue(attribute, value);
		}
		else if (translation instanceof Date) {
			throw new UnsupportedOperationException("The model does not currently support date types.");
		}
		else {
			throw new UnsupportedOperationException(String.format("Encountered unknown attribute type: %i", translation.getClass()));
		}
	}
}
