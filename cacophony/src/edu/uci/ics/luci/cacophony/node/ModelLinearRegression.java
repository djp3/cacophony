package edu.uci.ics.luci.cacophony.node;

import java.util.List;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class ModelLinearRegression implements Model {
	private LinearRegression mLinearRegressionModel = new LinearRegression();
	private FastVector mWekaAttributes = null;
	
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
		
		mLinearRegressionModel = new LinearRegression();
		try {
			mLinearRegressionModel.buildClassifier(trainingSet);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Object predict(Observation obs) {
		if (mLinearRegressionModel == null || mWekaAttributes == null) {
			throw new IllegalStateException("Unable to make prediction because model has not been trained.");
		}
		Instance instance = createWekaInstance(obs);
		try {
			return mLinearRegressionModel.classifyInstance(instance);
		} catch (Exception e) {
			return null;
		}
	}
	
	private Instance createWekaInstance(Observation obs) {
		int size = (obs.getTarget() == null ? obs.getFeatures().size() : obs.getFeatures().size() + 1);
		Instance instance = new Instance(size);
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

	private Attribute createWekaAttribute(SensorReading reading) {
		WekaAttributeTypeValuePair translation = reading.getTranslatedValue();
		Attribute attribute;
		switch (translation.getWekaAttributeType()){
			case Attribute.NUMERIC:
				attribute = new Attribute(reading.getSensorConfig().getID());
				break;
			case Attribute.STRING:
				FastVector attributeValues = null;
				attribute = new Attribute(reading.getSensorConfig().getID(), attributeValues);
				break;
			case Attribute.DATE:
			case Attribute.NOMINAL:
				throw new UnsupportedOperationException("The linear regression model does not currently support date or nominal (i.e., categorical) types.");
			default:
				throw new UnsupportedOperationException(String.format("Encountered unknown attribute type: %i", translation.getWekaAttributeType()));
		}
		return attribute;
	}
	
	private void setValueInInstance(Instance instance, Attribute attribute, SensorReading reading) {
		WekaAttributeTypeValuePair translation = reading.getTranslatedValue();
		switch (translation.getWekaAttributeType()){
		case Attribute.NUMERIC:
			instance.setValue(attribute, (Double)translation.getValue());
			break;
		case Attribute.STRING:
			instance.setValue(attribute, translation.getValue().toString());
			break;
		case Attribute.DATE:
		case Attribute.NOMINAL:
			throw new UnsupportedOperationException("The linear regression model does not currently support date or nominal (i.e., categorical) types.");
		default:
			throw new UnsupportedOperationException(String.format("Encountered unknown attribute type: %i", translation.getWekaAttributeType()));
	}
	}
}
