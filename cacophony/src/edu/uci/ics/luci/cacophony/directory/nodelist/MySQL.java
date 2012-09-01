package edu.uci.ics.luci.cacophony.directory.nodelist;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.quub.Globals;
import com.quub.database.DBConnection;
import com.quub.database.QuubDBConnectionPool;

public class MySQL extends NodeListLoader{
	
	private transient volatile Logger log = null;
	public Logger getLog(){
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
			pool = new QuubDBConnectionPool(databaseDomain, database, username, password,1,1);
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

	private List<MetaCNode> executeQuery() {
		
		Map<String,MetaCNode> nodes = new TreeMap<String,MetaCNode>();
		List<MetaCNode> ret = new ArrayList<MetaCNode>();
		ResultSet rs = null;
		try{
			if(error == false){
				try {
					rs = listViewQueryPS.executeQuery();
					while(rs.next()){
						try {
							MetaCNode metaCNode = new MetaCNode();
							metaCNode.setCreationTime(System.currentTimeMillis());
							String id = rs.getString("ID");
							metaCNode.setGuid(id.trim());
							String name = rs.getString("NAME");
							metaCNode.setName(name.trim());
							
							nodes.put(id,metaCNode);
						} catch (SQLException e) {
							getLog().error("Query failed to return good results\n"+e);
						}
					}
				} catch (SQLException e1) {
					getLog().error("Query failed to execute\n"+e1);
				}
				finally{
					try{
						if(rs != null){
							rs.close();
						}
					} catch (SQLException e) {
					}
					finally{
						rs = null;
					}
				}
				try {
					rs = mapViewQueryPS.executeQuery();
					while(rs.next()){
						try {
							MetaCNode metaCNode = null;
							String id = rs.getString("ID");
							id = id.trim();
							if(nodes.containsKey(id)){
								metaCNode = nodes.get(id);
							}
							else{
								metaCNode = new MetaCNode();
								metaCNode.setCreationTime(System.currentTimeMillis());
								metaCNode.setGuid(id);
							}
							double lon  = rs.getDouble("X");
							metaCNode.setLongitude(lon);
							double lat  = rs.getDouble("Y");
							metaCNode.setLatitude(lat);
							double weight  = rs.getDouble("MAP_WEIGHT");
							metaCNode.setMapWeight(weight);
							nodes.put(id,metaCNode);
							//System.out.println("X:"+lon+",Y:"+lat+", MAP_WEIGHT:"+weight);
							//good++;
						} catch (SQLException e) {
							getLog().error("Query failed to return good results\n"+e);
						}
					}
				} catch (SQLException e1) {
					getLog().error("Query failed to execute\n"+e1);
				}
				finally{
					try{
						if(rs != null){
							rs.close();
						}
					} catch (SQLException e) {
					}
					finally{
						rs = null;
					}
				}
				
				for(MetaCNode c:nodes.values()){
					c.setConfiguration(super.getConfiguration(c.getGuid()));
					ret.add(c);
					/*if(c.getId() != null) good_Id++;
					if(c.getName() != null) good_Name++;
					if(c.getCallCount() != null) good_CallCount++;
					if(c.getConfiguration() != null) good_Configuration++;
					if(c.getLatitude() != null) good_Latitude++;
					if(c.getLongitude() != null) good_Longitude++;
					if(c.getMapWeight() != null) good_MapWeight++;*/
				}
				
			}
		}
		finally{
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
	public List<MetaCNode> loadNodeList() {
		return executeQuery();
	}

}
