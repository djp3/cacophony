package edu.uci.ics.luci.cacophony.directory.nodelist;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import edu.uci.ics.luci.utility.Globals;
import edu.uci.ics.luci.utility.database.DBConnection;
import edu.uci.ics.luci.utility.database.LUCIDBConnectionPool;

public class MySQL extends NodeListLoader{
	
	private transient volatile Logger log = null;
	public Logger getLog(){
		if(log == null){
			log = Logger.getLogger(MySQL.class);
		}
		return log;
	}

	private DBConnection connection = null;
	private String namespace = null;
	private PreparedStatement listViewQueryPS = null;
	private PreparedStatement mapViewQueryPS = null;
	private PreparedStatement configurationQueryPS = null;
	private LUCIDBConnectionPool pool = null;
	private boolean error = false;
	
	public MySQL(){
		super();
	}

	@Override
	public void init(JSONObject options){
		
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
		
		try{
			namespace = options.getString("namespace");
		} catch (JSONException e) {
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
		if(mapViewQuery == null){
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
		

		String configurationQuery=null;
		try {
			configurationQuery = options.getString("configurationQuery");
		} catch (JSONException e) {
		}
		if(configurationQuery == null){
			getLog().error("Unable to get the \"configurationQuery\"");
			error = true;
		}
		else{
			if(!configurationQuery.toUpperCase().contains("SELECT ")){
				getLog().error("Query (configurationQuery) does not contain \"SELECT\"");
				error = true;
			}
			if(!configurationQuery.contains("AS CONFIGURATION")){
				getLog().error("Query (configurationQuery) does not contain as selector for \"CONFIGURATION\"");
				error = true;
			}
			configurationQuery = configurationQuery.replaceAll("_NODE_ID_","?");
		}
		
		if(!error){
			pool = new LUCIDBConnectionPool(databaseDomain, database, username, password,1,1);
			connection = pool.getConnection();
			if(connection == null){
				getLog().error("Query failed, couldn't connect to database");
				error=true;
			}
			else{
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
				try {
					configurationQueryPS = connection.prepareStatement(configurationQuery);
				} catch (SQLException e) {
					getLog().error("Query failed:"+configurationQuery+"\n"+e);
					error=true;
				}
			}
		}
	}


	@Override
	public List<MetaCNode> loadNodeList() {
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
							
							metaCNode.setNamespace(namespace);
							
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
				
				boolean multipleConfigurations = true;
				JSONObject configurationJSON = null;
				for(Entry<String, MetaCNode> es:nodes.entrySet()){
					try{
						configurationQueryPS.setString(1,es.getKey());
					}
					catch(SQLException e){
						//probably due to not have any parameters
						multipleConfigurations = false;
					}
					
					if(multipleConfigurations || (configurationJSON == null)){
						try{
							rs = configurationQueryPS.executeQuery();
							while(rs.next()){
								String configuration = null;
								try {
									configuration = rs.getString("CONFIGURATION");
									configurationJSON = new JSONObject(configuration);
								} catch (SQLException e) {
									getLog().error("Query failed to return good results\n"+e);
								} catch (JSONException e) {
									getLog().error("Failed to convert configuration to JSON\n"+configuration+"\n"+e);
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
					}
					es.getValue().setConfiguration(configurationJSON);
					ret.add(es.getValue());
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
			if(configurationQueryPS != null){
				try {
					configurationQueryPS.close();
				} catch (SQLException e) {
				}
				finally{
					configurationQueryPS = null;
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
}
