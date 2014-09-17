package edu.uci.ics.luci.cacophony.node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
			case Attribute.NOMINAL:
				List<SensorConfig> sensorList = new ArrayList<SensorConfig>();
				sensorList.add(reading.getSensorConfig());
				List<Observation> observations;
				try {
					observations = SensorReadingsDAO.retrieve(sensorList);
				} catch (UnknownSensorException e) {
					throw new UnsupportedOperationException("The sensor is unknown..");
				} catch (StorageException e) {
					// TODO log error
					return null;
				}
				Set<String> uniqueValues = new HashSet<String>();
				for (Observation obs : observations) {
					String value = obs.getTarget().getTranslatedValue().getValue().toString();
					if (!uniqueValues.contains(value)) {
						uniqueValues.add(value);
					}
				}
				FastVector categories = new FastVector(uniqueValues.size());
				for (String value : uniqueValues) {
					categories.addElement(value);
				}
				attribute = new Attribute(reading.getSensorConfig().getID(), categories);
			case Attribute.DATE:
				throw new UnsupportedOperationException("The model does not currently support date types.");
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
		case Attribute.NOMINAL:
			instance.setValue(attribute, translation.getValue().toString());
			break;
		case Attribute.DATE:
			throw new UnsupportedOperationException("The model does not currently support date types.");
		default:
			throw new UnsupportedOperationException(String.format("Encountered unknown attribute type: %i", translation.getWekaAttributeType()));
		}
	}
}
