package edu.uci.ics.luci.cacophony.node;

import java.util.List;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;

public abstract class ModelWeka implements Model {
	protected FastVector mWekaAttributes = null;
	
	@Override
	public void train(List<Observation> observations) {
	}

	@Override
	public Object predict(Observation observation) {
		return null;
	}

	protected Instance createWekaInstance(Observation obs) {
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

	protected Attribute createWekaAttribute(SensorReading reading) {
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
				throw new UnsupportedOperationException("The model does not currently support date or nominal (i.e., categorical) types.");
			default:
				throw new UnsupportedOperationException(String.format("Encountered unknown attribute type: %i", translation.getWekaAttributeType()));
		}
		return attribute;
	}
	
	protected void setValueInInstance(Instance instance, Attribute attribute, SensorReading reading) {
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
			throw new UnsupportedOperationException("The model does not currently support date or nominal (i.e., categorical) types.");
		default:
			throw new UnsupportedOperationException(String.format("Encountered unknown attribute type: %i", translation.getWekaAttributeType()));
		}
	}
}
