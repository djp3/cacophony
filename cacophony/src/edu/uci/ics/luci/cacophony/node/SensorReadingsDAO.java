package edu.uci.ics.luci.cacophony.node;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import edu.uci.ics.luci.utility.StringStuff;


// TODO: Add comments documenting public methods
public class SensorReadingsDAO {
	private static final File DATABASE_FILE = new File("cacophony_db.sqlite3");
	private static final String SENSOR_READINGS_TABLE = "SensorReadings";
	private static final String SENSOR_COLUMN_NAMES_TABLE = "SensorColumnNames";
	
	
	public static void initializeDBIfNecessary(List<SensorConfig> sensorConfigs) {
		createColumnNamesTableIfMissing();
		createSensorReadingsTableIfMissing(sensorConfigs);	
		populateColumnNamesTable(sensorConfigs);
	}
	
	/**
	 * 
	 * @param Sensor readings to store
	 */
	public static void store(List<SensorReading> sensorReadings) {
		SQLiteConnection db = null;
		SQLiteStatement st = null;
		
		Map<String,String> featureIDtoColumnNameMap = getFeatureIDtoColumnNameMap();
		List<String> columnNames = new ArrayList<String>();
		for (SensorConfig sc : SensorReading.flattenSensorReadingsIntoConfigs(sensorReadings)){
			columnNames.add(featureIDtoColumnNameMap.get(sc.getID()));
		}
		
    try {
    	db = new SQLiteConnection(DATABASE_FILE);
			db.open(false);

			st = db.prepare("INSERT INTO " + SENSOR_READINGS_TABLE
	    								+ " (" + StringStuff.join(", ", columnNames) + ")"
	    								+ " VALUES (" + buildQuestionMarkString(sensorReadings.size()) + ")");
	    for(int i=0; i < sensorReadings.size(); ++i) {
	    	st.bind(i+1, sensorReadings.get(i).getRawValue());
	    }
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
	
	/**
	 * 
	 * @return the stored sensor readings
	 */
	public static List<Observation> retrieve(List<SensorConfig> sensors) {
		SQLiteConnection db = null;
		SQLiteStatement st = null;
		
		Map<String,String> featureIDtoColumnNameMap = getFeatureIDtoColumnNameMap();
		List<String> columnNames = new ArrayList<String>();
		for (SensorConfig sc : sensors){
			columnNames.add(featureIDtoColumnNameMap.get(sc.getID()));
		}
		
    List<Observation> allObservations = null;
    try {
    	db = new SQLiteConnection(DATABASE_FILE);
			db.open(false);
	    st = db.prepare("SELECT " + StringStuff.join(", ", columnNames) + " FROM " + SENSOR_READINGS_TABLE);
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
		} finally {
			if (st != null) {
				st.dispose();
			}
			db.dispose();
		}
    return allObservations;
	}
	
	private static void createColumnNamesTableIfMissing() {
		List<String> columns = new ArrayList<String>();
		columns.add("insert_time DATETIME DEFAULT(STRFTIME('%Y-%m-%d %H:%M:%f', 'NOW')) NOT NULL");
		columns.add("feature_ID TEXT NOT NULL UNIQUE");
		columns.add("column_name TEXT NOT NULL");
		String createTableSql = String.format("CREATE TABLE IF NOT EXISTS %s (%s)", SENSOR_COLUMN_NAMES_TABLE, StringStuff.join(",\n", columns));
		
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
	
	private static void populateColumnNamesTable(List<SensorConfig> sensorConfigs) {
		SQLiteConnection db = null;
		SQLiteStatement st = null;
    try {
    	db = new SQLiteConnection(DATABASE_FILE);
			db.open(true);
		
			// Figure out which features are currently in the DB table that maps feature IDs to column names
			// and add any that are missing.
			st = db.prepare("SELECT feature_ID FROM " + SENSOR_COLUMN_NAMES_TABLE);
	    Set<String> featureIDsFound = new HashSet<String>();
			while (st.step()){
	    	featureIDsFound.add(st.columnString(0));
	    }
						 
			int nextColumnNumber = featureIDsFound.size();
			for(SensorConfig sc : sensorConfigs) {
				if (!featureIDsFound.contains(sc.getID())) {
					st = db.prepare("INSERT INTO " + SENSOR_COLUMN_NAMES_TABLE + " (feature_ID, column_name) VALUES (?,?)");
		    	st.bind(1, sc.getID());
		    	st.bind(2, "column" + nextColumnNumber);
		    	st.step();
		    	++nextColumnNumber;
				}
	    }
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

	private static void createSensorReadingsTableIfMissing(List<SensorConfig> sensorConfigs) {
		List<String> columns = new ArrayList<String>();
		columns.add("id INTEGER PRIMARY KEY AUTOINCREMENT");
		columns.add("insert_time DATETIME DEFAULT(STRFTIME('%Y-%m-%d %H:%M:%f', 'NOW')) NOT NULL");
		for (int i=0; i < sensorConfigs.size(); ++i){
			columns.add("column" + i + " TEXT");
		}		
		String createTableSql = String.format("CREATE TABLE IF NOT EXISTS %s (%s)", SENSOR_READINGS_TABLE, StringStuff.join(",\n", columns));
		
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
	
	private static Map<String,String> getFeatureIDtoColumnNameMap() {
		SQLiteConnection db = null;
		SQLiteStatement st = null;
    Map<String,String> featureIDtoColumnNameMap = new HashMap<String,String>();
    try {
    	db = new SQLiteConnection(DATABASE_FILE);
			db.open(false);
	    st = db.prepare("SELECT feature_ID, column_name FROM " + SENSOR_COLUMN_NAMES_TABLE);
	    while (st.step()){
	    	String featureID = st.columnString(0);
	    	String columnName = st.columnString(1);
	    	featureIDtoColumnNameMap.put(featureID, columnName);
	    }
		} catch (SQLiteException e) {
			// TODO: log error
			e.printStackTrace();
		} finally {
			if (st != null) {
				st.dispose();
			}
			db.dispose();
		}
    return featureIDtoColumnNameMap;
	}
	
	private static String buildQuestionMarkString(int numberOfQuestionMarks) {
		String s = StringStuff.repeatString("?,", numberOfQuestionMarks);
		return s.substring(0,s.length()-1);
	}
}
