package edu.uci.ics.luci.cacophony.node;

public class SensorReading {
	private final SensorConfig sensorConfig;
	private final String rawValue;
	
	public SensorReading(SensorConfig sensorConfig, String rawValue) {
		this.sensorConfig = sensorConfig;
		this.rawValue = rawValue;
	}
	
	public SensorConfig getSensorConfig() {
		return sensorConfig;
	}
	
	public String getRawValue() {
		return rawValue;
	}
}
