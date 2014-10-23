package edu.uci.ics.luci.cacophony.node;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

import edu.uci.ics.luci.utility.Globals;
import edu.uci.ics.luci.utility.StringStuff;


public class SensorReadingsDAO {
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = LogManager.getLogger(SensorReadingsDAO.class);
		}
		return log;
	}
	
	private static Map<String, SensorReadingsDAO> daoInstances = new HashMap<String, SensorReadingsDAO>();
	private File databaseFile;
	private final String SENSOR_READINGS_TABLE = "SensorReadings";
	private final String SENSOR_COLUMN_NAMES_TABLE = "SensorColumnNames";
	private final String DATABASE_FILE_DIRECTORY = "databases";
	private final String FILE_EXTENSION = "sqlite3";
	
	private SensorReadingsDAO(String databaseName) {
		File directory = new File(DATABASE_FILE_DIRECTORY);
		directory.mkdir();
		databaseFile = new File(directory, databaseName + "." + FILE_EXTENSION);
	}
	
	public static synchronized SensorReadingsDAO getInstance(String databaseName) {
		if (!daoInstances.containsKey(databaseName)) {
			daoInstances.put(databaseName, new SensorReadingsDAO(databaseName));
		}
		return daoInstances.get(databaseName); 
	}

	/**
	 * Initializes the database for the given sensors. This only needs to be called once during the lifetime of the database, unless there are new sensors for which to store readings.
	 * Calling this method more than once will not harm anything, even if there aren't any new sensors. 
	 * @param sensorConfigs Configuration info for the sensors for which we're storing sensor readings
	 * @throws StorageException
	 */
	public void initializeDBIfNecessary(List<SensorConfig> sensorConfigs) throws StorageException {
		try {
			createColumnNamesTableIfMissing();
			createSensorReadingsTableIfMissing(sensorConfigs);	
			populateColumnNamesTable(sensorConfigs);
		} catch (SQLiteException e) {
			throw new StorageException("Error while trying to initialize the database.", e);
		}
	}
	
	/**
	 * Stores sensor readings in the DB.
	 * @param sensorReadings sensor readings to store
	 * @throws StorageException 
	 */
	public void store(List<SensorReading> sensorReadings) throws StorageException {
		SQLiteConnection db = null;
		SQLiteStatement st = null;
		
		Map<String, String> featureIDtoColumnNameMap;
		try {
			featureIDtoColumnNameMap = getFeatureIDtoColumnNameMap();
		} catch (SQLiteException e) {
			throw new StorageException("Unable to store sensor readings.", e);
		}
		List<String> columnNames = new ArrayList<String>();
		for (SensorConfig sc : SensorReading.flattenSensorReadingsIntoConfigs(sensorReadings)){
			columnNames.add(featureIDtoColumnNameMap.get(sc.getID()));
		}
		
		try {
	    	db = new SQLiteConnection(databaseFile);
			db.open(false);
			st = db.prepare("INSERT INTO " + SENSOR_READINGS_TABLE
	    								+ " (" + StringStuff.join(", ", columnNames) + ")"
	    								+ " VALUES (" + buildQuestionMarkString(sensorReadings.size()) + ")");
		    for(int i=0; i < sensorReadings.size(); ++i) {
		    	st.bind(i+1, sensorReadings.get(i).getRawValue());
		    }
			st.step();
		} catch (SQLiteException e) {
			throw new StorageException("Unable to store sensor readings.", e);
		} finally {
			if (st != null) {
				st.dispose();
			}
			db.dispose();
		}
	}
	
	/**
	 * Retrieves previously-stored sensor readings from the DB.
	 * @param sensors a list of sensor configurations for the sensors we want to retrieve previously-stored data for
	 * @return stored sensor readings for the given list of sensors
	 * @throws UnknownSensorException 
	 * @throws StorageException 
	 */
	public List<Observation> retrieve(List<SensorConfig> sensors) throws UnknownSensorException, StorageException {
		SQLiteConnection db = null;
		SQLiteStatement st = null;
		
		Map<String, String> featureIDtoColumnNameMap;
		try {
			featureIDtoColumnNameMap = getFeatureIDtoColumnNameMap();
		} catch (SQLiteException e) {
			throw new StorageException("Unable to retrieve sensor data.", e);
		}
		List<String> columnNames = new ArrayList<String>();
		for (SensorConfig sc : sensors){
			String columnName = featureIDtoColumnNameMap.get(sc.getID()); 
			if (columnName == null) {
				throw new UnknownSensorException("Unknown sensor ID: " + sc.getID());
			}
			columnNames.add(columnName);
		}
		
    List<Observation> allObservations = null;
    try {
    	db = new SQLiteConnection(databaseFile);
		db.open(false);
	    st = db.prepare("SELECT id, insert_time, " + StringStuff.join(", ", columnNames) + " FROM " + SENSOR_READINGS_TABLE + " ORDER BY insert_time DESC");
	    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
	    dateFormatter.setLenient(false);
	    dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
	    allObservations = new ArrayList<Observation>();
	    while (st.step()){
	    	int id = st.columnInt(0);
	    	Date storageTime = dateFormatter.parse(st.columnString(1));
	    	List<SensorReading> readings = new ArrayList<SensorReading>();
	    	for (int i=0; i < sensors.size()-1; ++i) {
	    		readings.add(new SensorReading(sensors.get(i), st.columnString(i+2)));
	    	}
	    	// target should be the last sensor in the list of sensors configurations 
	    	SensorReading target = new SensorReading(sensors.get(sensors.size()-1), st.columnString(sensors.size()+1));
	    	allObservations.add(new Observation(id, storageTime, readings, target));
	    }
		} catch (SQLiteException e) {
			throw new StorageException("Unable to retrieve sensor data.", e);
		} catch (ParseException e) {
			throw new StorageException("Unable to retrieve sensor data.", e);
		} finally {
			if (st != null) {
				st.dispose();
			}
			db.dispose();
		}
    return allObservations;
	}
	
	/**
	 * Updates observation in the DB.
	 * @param ID The ID of the observation for which we're updating the prediction
	 * @param prediction The prediction to store
	 * @throws StorageException 
	 */
	public void updatePrediction(int ID, Object prediction) throws StorageException {
	// TODO: write a unit test for this method
		SQLiteConnection db = null;
		SQLiteStatement st = null;
		try {
	    	db = new SQLiteConnection(databaseFile);
			db.open(false);
			st = db.prepare("UPDATE " + SENSOR_READINGS_TABLE + " SET prediction = ? WHERE ID = ?");
			st.bind(1, prediction.toString());
			st.bind(2, ID);
			st.step();
		} catch (SQLiteException e) {
			throw new StorageException("Unable to update prediction.", e);
		} finally {
			if (st != null) {
				st.dispose();
			}
			db.dispose();
		}
	}
	
	/**
	 * Retrieves timestamps for when the most recent n observations were stored in the DB.
	 * @param n The number of storage times to retrieve, starting from the most recent
	 * @return storage times for the previous n observations
	 * @throws StorageException 
	 */
	public List<Date> retrieveStorageTimes(int n) throws StorageException {
		SQLiteConnection db = null;
		SQLiteStatement st = null;

    List<Date> storageTimes = new ArrayList<Date>();
    try {
    	db = new SQLiteConnection(databaseFile);
			db.open(false);
	    st = db.prepare("SELECT insert_time FROM " + SENSOR_READINGS_TABLE + " ORDER BY insert_time DESC LIMIT " + n);
	    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT);
	    dateFormatter.setLenient(false);
	    dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
	    while (st.step()){
	    	Date storageTime = dateFormatter.parse(st.columnString(0));
	    	storageTimes.add(storageTime);
	    }
		} catch (SQLiteException e) {
			throw new StorageException("Unable to retrieve storage times.", e);
		} catch (ParseException e) {
			throw new StorageException("Unable to retrieve storage times.", e);
		} finally {
			if (st != null) {
				st.dispose();
			}
			db.dispose();
		}
    return storageTimes;
	}
	
	/**
	 * Deletes the database.
	 * @param confirmationText you must enter the correct confirmation text in order for the deletion to take place
	 * @return true if the database was deleted, false if it was not deleted (possibly because it didn't exist in the first place)
	 */
	public boolean deleteDatabase(String confirmationText) {
		final String REQUIRED_TEXT = "Yes, I know this will delete all of the data stored in the database. I'm not stupid.";
		
		if (confirmationText.equals(REQUIRED_TEXT) && databaseFile.exists()) {
			return databaseFile.delete();
		}
		return false;
	}
	
	private void createColumnNamesTableIfMissing() throws SQLiteException {
		List<String> columns = new ArrayList<String>();
		columns.add("insert_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL");
		columns.add("feature_ID TEXT NOT NULL UNIQUE");
		columns.add("column_name TEXT NOT NULL");
		String createTableSql = String.format("CREATE TABLE IF NOT EXISTS %s (%s)", SENSOR_COLUMN_NAMES_TABLE, StringStuff.join(",\n", columns));
		
		SQLiteConnection db = null;
		SQLiteStatement st = null;
		try {
			db = new SQLiteConnection(databaseFile);
			db.open(true);
			st = db.prepare(createTableSql);
			st.step();
		}
		catch(Exception e){
			getLog().error("Threw an exception:"+e);
		} finally {
			if (st != null) {
				st.dispose();
			}
			if(db != null){
				db.dispose();
			}
		}
	}
	
	private void populateColumnNamesTable(List<SensorConfig> sensorConfigs) throws SQLiteException {
		SQLiteConnection db = null;
		SQLiteStatement st = null;
    try {
    	db = new SQLiteConnection(databaseFile);
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
		} finally {
			if (st != null) {
				st.dispose();
			}
			db.dispose();
		}
	}
	
	private void createSensorReadingsTableIfMissing(List<SensorConfig> sensorConfigs) throws SQLiteException {
		List<String> columns = new ArrayList<String>();
		columns.add("id INTEGER PRIMARY KEY AUTOINCREMENT");
		columns.add("insert_time DATETIME DEFAULT(STRFTIME('%Y-%m-%d %H:%M:%f', 'NOW', 'UTC')) NOT NULL");
		for (int i=0; i < sensorConfigs.size(); ++i){
			columns.add("column" + i + " TEXT");
		}
		columns.add("prediction TEXT");
		
		String createTableSql = String.format("CREATE TABLE IF NOT EXISTS %s (%s)", SENSOR_READINGS_TABLE, StringStuff.join(",\n", columns));
		
		SQLiteConnection db = null;
		SQLiteStatement st = null;
		try {
			db = new SQLiteConnection(databaseFile);
			db.open(true);
			st = db.prepare(createTableSql);
			st.step();
		} finally {
			if (st != null) {
				st.dispose();
			}
			db.dispose();
		}
	}
	
	private Map<String,String> getFeatureIDtoColumnNameMap() throws SQLiteException {
		SQLiteConnection db = null;
		SQLiteStatement st = null;
    Map<String,String> featureIDtoColumnNameMap = new HashMap<String,String>();
    try {
    	db = new SQLiteConnection(databaseFile);
			db.open(false);
	    st = db.prepare("SELECT feature_ID, column_name FROM " + SENSOR_COLUMN_NAMES_TABLE);
	    while (st.step()){
	    	String featureID = st.columnString(0);
	    	String columnName = st.columnString(1);
	    	featureIDtoColumnNameMap.put(featureID, columnName);
	    }
		} finally {
			if (st != null) {
				st.dispose();
			}
			db.dispose();
		}
    return featureIDtoColumnNameMap;
	}
	
	private String buildQuestionMarkString(int numberOfQuestionMarks) {
		String s = StringStuff.repeatString("?,", numberOfQuestionMarks);
		return s.substring(0,s.length()-1);
	}
}
