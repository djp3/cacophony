package edu.uci.ics.luci.cacophony.node;

import java.util.ArrayList;
import java.util.List;

public class SensorReading {
	private final SensorConfig sensorConfig;
	private final String rawValue;
	
	public SensorReading(SensorConfig sensorConfig, String rawValue) {
		this.sensorConfig = sensorConfig;
		this.rawValue = rawValue;
	}
	
	public WekaAttributeTypeValuePair getTranslatedValue() {
		return sensorConfig.getTranslator().translate(rawValue);
	}
	
	public SensorConfig getSensorConfig() {
		return sensorConfig;
	}
	
	public String getRawValue() {
		return rawValue;
	}
	
	public static List<SensorConfig> flattenSensorReadingsIntoConfigs(List<SensorReading> sensorReadings) {
		List<SensorConfig> sensorConfigs = new ArrayList<SensorConfig>();
		for (SensorReading reading : sensorReadings){
			sensorConfigs.add(reading.getSensorConfig());
		}
		return sensorConfigs;
	}
}
