package edu.uci.ics.luci.cacophony.directory.nodelist;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.quub.Globals;
import com.quub.database.DBConnection;
import com.quub.database.QuubDBConnectionPool;

public class MySQL extends NodeListLoader{
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(MySQL.class);
		}
		return log;
	}

	private DBConnection connection;
	private PreparedStatement listViewQueryPS;
	private PreparedStatement mapViewQueryPS;
	private QuubDBConnectionPool pool;
	private boolean error;
	
	public MySQL(){
		super();
	}

	@Override
	public void init(JSONObject options){
		error = false;
		Globals g = Globals.getGlobals();
		if(g == null){
			getLog().error("Globals is null");
			error = true;
		}
		String databaseDomain = null;
		try {
			databaseDomain = options.getString("server_address");
		} catch (JSONException e) {
		}
		if(databaseDomain == null){
			getLog().error("Unable to get the \"server_address\"");
			error = true;
		}
		String database = null;
		try {
			database = options.getString("database");
		} catch (JSONException e) {
		}
		if(database == null){
			getLog().error("Unable to get the \"database\"");
			error = true;
		}
		String username=null;
		try {
			username = options.getString("user");
		} catch (JSONException e) {
		}
		if(username == null){
			getLog().error("Unable to get the \"user\"");
			error = true;
		}
		String password=null;
		try {
			password = options.getString("password");
		} catch (JSONException e) {
		}
		if(password == null){
			getLog().error("Unable to get the \"password\"");
			error = true;
		}
		String listViewQuery=null;
		try {
			listViewQuery = options.getString("listViewQuery");
		} catch (JSONException e) {
		}
		if(listViewQuery == null){
			getLog().error("Unable to get the \"listViewQuery\"");
			error = true;
		}
		else{
			if(!listViewQuery.toUpperCase().contains("SELECT ")){
				getLog().error("Query (listViewQuery) does not contain \"SELECT\"");
				error = true;
			}
			if(!listViewQuery.contains("AS ID")){
				getLog().error("Query (listViewQuery) does not contain as selector for \"ID\"");
				error = true;
			}
			if(!listViewQuery.contains("AS NAME")){
				getLog().error("Query (listViewQuery) does not contain as selector for \"NAME\"");
				error = true;
			}
			if(!listViewQuery.contains("AS CALL_COUNT")){
				getLog().error("Query (listViewQuery) does not contain as selector for \"CALL_COUNT\"");
				error = true;
			}
		}
		
		String mapViewQuery=null;
		try {
			mapViewQuery = options.getString("mapViewQuery");
		} catch (JSONException e) {
		}
		if(listViewQuery == null){
			getLog().error("Unable to get the \"mapViewQuery\"");
			error = true;
		}
		else{
			if(!mapViewQuery.toUpperCase().contains("SELECT ")){
				getLog().error("Query (mapViewQuery) does not contain \"SELECT\"");
				error = true;
			}
			if(!mapViewQuery.contains("AS ID")){
				getLog().error("Query (mapViewQuery) does not contain as selector for \"ID\"");
				error = true;
			}
			if(!mapViewQuery.contains("AS X")){
				getLog().error("Query (mapViewQuery) does not contain as selector for \"X\"");
				error = true;
			}
			if(!mapViewQuery.contains("AS Y")){
				getLog().error("Query (mapViewQuery) does not contain as selector for \"Y\"");
				error = true;
			}
			if(!mapViewQuery.contains("AS MAP_WEIGHT")){
				getLog().error("Query (mapViewQuery) does not contain as selector for \"MAP_WEIGHT\"");
				error = true;
			}
		}
		
		if(!error){
			pool = new QuubDBConnectionPool(g,databaseDomain, database, username, password,1,1);
			connection = pool.getConnection();
			try {
				listViewQueryPS = connection.prepareStatement(listViewQuery);
			} catch (SQLException e) {
				getLog().error("Query failed:"+listViewQuery+"\n"+e);
				error=true;
			}
			try {
				mapViewQueryPS = connection.prepareStatement(mapViewQuery);
			} catch (SQLException e) {
				getLog().error("Query failed:"+mapViewQuery+"\n"+e);
				error=true;
			}
		}
	}

	private Map<String,MetaCNode> executeQuery() {
		Map<String,MetaCNode> ret = new TreeMap<String,MetaCNode>();
		ResultSet rs = null;
		try{
			if(error == false){
				try {
					rs = listViewQueryPS.executeQuery();
					while(rs.next()){
						try {
							MetaCNode cNode = new MetaCNode();
							String id = rs.getString("ID");
							cNode.setId(id.trim());
							String name = rs.getString("NAME");
							cNode.setName(name.trim());
							int callCount = rs.getInt("CALL_COUNT");
							cNode.setCallCount(callCount);
							ret.put(id,cNode);
						} catch (SQLException e) {
							getLog().error("Query failed to return good results\n"+e);
						}
					}
				} catch (SQLException e1) {
					getLog().error("Query failed to execute\n"+e1);
				}
				try {
					rs = mapViewQueryPS.executeQuery();
					while(rs.next()){
						try {
							MetaCNode cNode = null;
							String id = rs.getString("ID");
							id = id.trim();
							if(ret.containsKey(id)){
								cNode = ret.get(id);
							}
							else{
								cNode = new MetaCNode();
								cNode.setId(id.trim());
							}
							double lon  = rs.getDouble("X");
							cNode.setLongitude(lon);
							double lat  = rs.getDouble("Y");
							cNode.setLatitude(lat);
							double weight  = rs.getDouble("MAP_WEIGHT");
							cNode.setMapWeight(weight);
							ret.put(id,cNode);
						} catch (SQLException e) {
							getLog().error("Query failed to return good results\n"+e);
						}
					}
				} catch (SQLException e1) {
					getLog().error("Query failed to execute\n"+e1);
				}
			}
		}
		finally{
			if(rs != null){
				try {
					rs.close();
				} catch (SQLException e) {
				}
				finally{
					rs = null;
				}
			}
			if(listViewQueryPS != null){
				try {
					listViewQueryPS.close();
				} catch (SQLException e) {
				}
				finally{
					listViewQueryPS = null;
				}
			}
			if(mapViewQueryPS != null){
				try {
					mapViewQueryPS.close();
				} catch (SQLException e) {
				}
				finally{
					mapViewQueryPS = null;
				}
			}
			if(connection != null){
				try {
					connection.close();
				} catch (SQLException e) {
				}
				finally{
					connection = null;
				}
			}
			if(pool != null){
				try{
					pool.setQuitting(true);
				}
				finally{
					pool = null;
				}
			}
		}
		
		return(ret);
	}

	@Override
	public Map<String, MetaCNode> loadNodeList() {
		return executeQuery();
	}

}
