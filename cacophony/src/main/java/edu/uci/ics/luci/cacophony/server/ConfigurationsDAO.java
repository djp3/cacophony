package edu.uci.ics.luci.cacophony.server;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

import edu.uci.ics.luci.cacophony.node.CNodeConfiguration;
import edu.uci.ics.luci.cacophony.node.StorageException;

public class ConfigurationsDAO {
	private final static String CONFIGURATION_TABLE = "configs";
	private final static File DATABASE_FILE = new File("cnode_configs.sqlite3");
	
	/**
	 * Initializes the database. This only needs to be called once during the lifetime of the database.
	 * Calling this method more than once will not harm anything. 
	 * @throws StorageException
	 */
	public static void initializeDBIfNecessary() throws StorageException {
		try {
			createConfigurationsTable();
		} catch (SQLiteException e) {
			throw new StorageException("Error while trying to initialize the database.", e);
		}
	}
	
	private static void createConfigurationsTable() throws SQLiteException {
		String createTableSql = String.format("CREATE TABLE IF NOT EXISTS %s (cnode_ID TEXT NOT NULL UNIQUE, configuration_JSON TEXT NOT NULL)", CONFIGURATION_TABLE);
		
		SQLiteConnection db = null;
		SQLiteStatement st = null;
		try {
			db = new SQLiteConnection(DATABASE_FILE);
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
	
	/**
	 * Stores configuration info in the DB
	 * @param ID CNode ID to store
	 * @param configuration CNode configuration to store
	 * @throws StorageException 
	 */
	public static void store(String ID, CNodeConfiguration configuration) throws StorageException {
		SQLiteConnection db = null;
		SQLiteStatement st = null;
		
		try {
	    	db = new SQLiteConnection(DATABASE_FILE);
			db.open(false);
			st = db.prepare("INSERT INTO " + CONFIGURATION_TABLE
	    								+ " (cnode_ID, configuration_JSON)"
	    								+ " VALUES (?, ?)");
			st.bind(1, ID);
			st.bind(2, configuration.toJSONObject().toJSONString());
			st.step();
		} catch (SQLiteException e) {
			throw new StorageException("Unable to store configuration info.", e);
		} finally {
			if (st != null) {
				st.dispose();
			}
			db.dispose();
		}
	}
	
	/**
	 * Retrieves map of CNode IDs to CNode configurations
	 * @return Map of CNode IDs to CNode configurations
	 * @throws StorageException 
	 */
	public static Map<String, CNodeConfiguration> retrieve() throws StorageException {
		SQLiteConnection db = null;
		SQLiteStatement st = null;

		Map<String, CNodeConfiguration> map = new HashMap<String, CNodeConfiguration>();
	    try {
	    	db = new SQLiteConnection(DATABASE_FILE);
			db.open(false);
		    st = db.prepare("SELECT cnode_ID, configuration_JSON FROM " + CONFIGURATION_TABLE);
		    while (st.step()){
		    	String ID = st.columnString(0);
		    	JSONObject json = (JSONObject)JSONValue.parse(st.columnString(1));
		    	CNodeConfiguration configuration = new CNodeConfiguration(json);
		    	map.put(ID, configuration);
		    }
		} catch (SQLiteException e) {
			throw new StorageException("Unable to retrieve storage times.", e);
		} finally {
			if (st != null) {
				st.dispose();
			}
			db.dispose();
		}
    	return map;
	}
}
