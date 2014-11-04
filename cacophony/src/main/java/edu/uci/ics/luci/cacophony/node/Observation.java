package edu.uci.ics.luci.cacophony.node;

import java.util.List;

public class Observation {
	private final int ID;
	private final long storageTime;
	private final List<SensorReading> features;
	private final SensorReading target;
	
	public Observation(int ID, long storageTime, List<SensorReading> features, SensorReading target) {
		this.ID = ID;
		this.storageTime = storageTime;
		this.features = features;
		this.target = target;
	}
	
	public Observation(long storageTime, List<SensorReading> features, SensorReading target) {
		this(-1, storageTime, features, target);
	}
	
	public Observation(List<SensorReading> features, SensorReading target) {
		this(-1, -1, features, target);
	}
	
	public Observation(List<SensorReading> features) {
		this(-1, -1, features, null);
	}
	
	public int getID() {
		return ID;
	}
	
	public long getStorageTime() {
		return storageTime;
	}
	
	public List<SensorReading> getFeatures() {
		return features;
	}
	
	public SensorReading getTarget() {
		return target;
	}
}
