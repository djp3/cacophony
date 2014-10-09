package edu.uci.ics.luci.cacophony.node;

import java.util.Date;
import java.util.List;

public class Observation {
	private final Date storageTime;
	private final List<SensorReading> features;
	private final SensorReading target;
	
	public Observation(Date storageTime, List<SensorReading> features, SensorReading target) {
		this.storageTime = storageTime;
		this.features = features;
		this.target = target;
	}
	
	public Observation(List<SensorReading> features, SensorReading target) {
		this(null, features, target);
	}
	
	public Observation(List<SensorReading> features) {
		this(null, features, null);
	}
	
	public Date getStorageTime() {
		return storageTime;
	}
	
	public List<SensorReading> getFeatures() {
		return features;
	}
	
	public SensorReading getTarget() {
		return target;
	}
}
