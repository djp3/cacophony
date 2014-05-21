package edu.uci.ics.luci.cacophony.node;

import java.util.List;

public class Observation {
	private final List<SensorReading> features;
	private final SensorReading target;
	
	public Observation(List<SensorReading> features, SensorReading target) {
		this.features = features;
		this.target = target;
	}
	
	public List<SensorReading> getFeatures() {
		return features;
	}
	
	public SensorReading getTarget() {
		return target;
	}
}
