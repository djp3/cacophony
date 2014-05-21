package edu.uci.ics.luci.cacophony.node;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import edu.uci.ics.luci.utility.StringStuff;

/** WARNING! WARNING! WARNING!
 * Be mindful of SQL injection attacks when modifying this code! 
 * 
 */

/** TODO: This code is taking sensor names (presumably) input by a user, and using them as column names.
/* This raises issues with:
 *   (1) changes to sensor names: how do we map DB columns to sensors in the Cacophony configuration if the name of a sensor changes?
 *       (a) Possible solutions include making column names immutable
 *       (b) having some immutable unique ID besides the displayed sensor name, and using that immutable unique ID as the column name
 *   (2) SQL injection: escaping the values being inserted into the DB shouldn't be an issue as long as we use the bind syntax. But
 *       what about column names? These are currently based on user input. We're sanitizing these column names below, but if we're
 *       overlooking something, little Bobby Tables might find us.
 */

public class SensorReadingsDAO {
	private static final File DATABASE_FILE = new File("cacophony_db");
	private static final String SENSOR_READINGS_TABLE = "SensorReadings";
		
	/**
	 * 
	 * @param Sensor readings to store
	 */
	public static int store(List<SensorReading> sensorReadings) {
		SQLiteConnection db = null;
		SQLiteStatement st = null;
    try {
    	db = new SQLiteConnection(DATABASE_FILE);
			db.open(true);
			List<SensorConfig> sensorConfigs = SensorReading.flattenSensorReadingsIntoConfigs(sensorReadings);
			createTableIfMissing(sensorConfigs);
			
	    st = db.prepare("INSERT INTO " + sanitizeIdentifier(SENSOR_READINGS_TABLE)
	    								+ " (" + buildColumnsString(sensorConfigs) + ")"
	    								+ " VALUES (" + buildQuestionMarkString(sensorReadings.size()) + ")");
	    for(int i=0; i < sensorReadings.size(); ++i) {
	    	st.bind(i+1, sensorReadings.get(i).getRawValue());
	    }
			st.step();
		} catch (SQLiteException e) {
			// TODO: log error
			e.printStackTrace();
		} catch (SqlSanitizingException e) {
			// TODO: log error?
			e.printStackTrace();
		} finally {
			if (st != null) {
				st.dispose();
			}
			db.dispose();
		}
    return 0; // TODO: this should return the ID of the inserted row
	}
	
	/**
	 * 
	 * @return the stored sensor readings
	 */
	public static List<Observation> retrieve(List<SensorConfig> sensors) {
		SQLiteConnection db = null;
		SQLiteStatement st = null;
    List<Observation> allObservations = null;
    try {
    	db = new SQLiteConnection(DATABASE_FILE);
			db.open(true);
	    st = db.prepare("SELECT " + buildColumnsString(sensors) + " FROM " + sanitizeIdentifier(SENSOR_READINGS_TABLE));
	    allObservations = new ArrayList<Observation>();
	    while (st.step()){
	    	List<SensorReading> readings = new ArrayList<SensorReading>();
	    	for (int i=0; i < sensors.size(); ++i) {
	    		readings.add(new SensorReading(sensors.get(i), st.columnString(i)));
	    	}
	    	SensorReading target = readings.get(readings.size()-1);
	    	readings.remove(readings.size()-1); 
	    	allObservations.add(new Observation(readings, target));
	    }
		} catch (SQLiteException e) {
			// TODO: log error
			e.printStackTrace();
		} catch (SqlSanitizingException e) {
			// TODO: log error?
			e.printStackTrace();
		} finally {
			if (st != null) {
				st.dispose();
			}
			db.dispose();
		}
    return allObservations;
	}
	
	private static void createTableIfMissing(List<SensorConfig> sensorConfigs) throws SqlSanitizingException {
		List<String> columns = new ArrayList<String>();
		columns.add("id INTEGER PRIMARY KEY AUTOINCREMENT");
		columns.add("insert_time DATETIME DEFAULT(STRFTIME('%Y-%m-%d %H:%M:%f', 'NOW')) NOT NULL");
		for (SensorConfig sensor : sensorConfigs){
			columns.add(sanitizeIdentifier(sensor.getName()) + " TEXT");
		}		
		String createTableSql = String.format("CREATE TABLE IF NOT EXISTS %s (%s)", sanitizeIdentifier(SENSOR_READINGS_TABLE), StringStuff.join(",\n", columns));
		
		SQLiteConnection db = null;
		SQLiteStatement st = null;
		try {
			db = new SQLiteConnection(DATABASE_FILE);
			db.open(true);
			st = db.prepare(createTableSql);
			st.step();
		} catch (SQLiteException e) {
			// TODO: log error
			e.printStackTrace();
		} finally {
			if (st != null) {
				st.dispose();
			}
			db.dispose();
		}
	}
	
	private static String buildColumnsString(List<SensorConfig> sensorConfigs) throws SqlSanitizingException {
		List<String> sensorNames = new ArrayList<String>(); 
		for(SensorConfig sensor : sensorConfigs){
			sensorNames.add(sanitizeIdentifier(sensor.getName()));
		}
		return StringStuff.join(",", sensorNames);
	}
	
	private static String buildQuestionMarkString(int numberOfQuestionMarks) {
		String s = StringStuff.repeatString("?,", numberOfQuestionMarks);
		return s.substring(0,s.length()-1);
	}
	
	private static String sanitizeIdentifier(String identifier) throws SqlSanitizingException {
		/* Based on logic at http://stackoverflow.com/a/6701665
		 * Be careful about modifying this due to potential vulnerability to SQL injection. Here be dragons.
		 */
		
		// Make sure the string can be encoded in UTF-8
		byte[] b;
		String sanitized = null;
		try {
			b = identifier.getBytes("UTF-8");
			sanitized = new String(b, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new SqlSanitizingException("The identifier string is not valid UTF-8", ex);
		}
		
		// Check the string for any null terminator characters
		if (sanitized.contains("\0")) {
			throw new SqlSanitizingException("The identifier string contains a null terminator character");
		}
		
		// Replace all " with "" (standard SQL escape sequence for double quotes)
		sanitized = sanitized.replaceAll("\"", "\"\""); 
		
		// Wrap the identifier in double quotes
		return "\"" + identifier + "\"";
	}
}
