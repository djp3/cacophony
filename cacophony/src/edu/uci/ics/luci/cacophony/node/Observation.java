package edu.uci.ics.luci.cacophony.node;

import java.util.Date;
import java.util.List;

public class Observation {
	private final int ID;
	private final Date storageTime;
	private final List<SensorReading> features;
	private final SensorReading target;
	
	public Observation(int ID, Date storageTime, List<SensorReading> features, SensorReading target) {
		this.ID = ID;
		this.storageTime = storageTime;
		this.features = features;
		this.target = target;
	}
	
	public Observation(Date storageTime, List<SensorReading> features, SensorReading target) {
		this(-1, storageTime, features, target);
	}
	
	public Observation(List<SensorReading> features, SensorReading target) {
		this(-1, null, features, target);
	}
	
	public Observation(List<SensorReading> features) {
		this(-1, null, features, null);
	}
	
	public int getID() {
		return ID;
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
