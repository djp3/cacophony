package edu.uci.ics.luci.cacophony.node;

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

import edu.uci.ics.luci.util.FailoverFetch;
import edu.uci.ics.luci.utility.Globals;
import edu.uci.ics.luci.utility.database.DBConnection;
import edu.uci.ics.luci.utility.database.LUCIDBConnectionPool;
import edu.uci.ics.luci.utility.datastructure.Pair;

public class MySQL extends CNodeLoader{
	
	private transient volatile Logger log = null;
	public Logger getLog(){
		if(log == null){
			log = Logger.getLogger(MySQL.class);
		}
		return log;
	}

	private DBConnection connection = null;
	private PreparedStatement nodeQueryPS = null;
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
		
		String nodeQuery=null;
		try {
			nodeQuery = options.getString("nodeQuery");
		} catch (JSONException e) {
		}
		if(nodeQuery == null){
			getLog().error("Unable to get the \"nodeQuery\"");
			error = true;
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
					nodeQueryPS = connection.prepareStatement(nodeQuery);
				} catch (SQLException e) {
					getLog().error("Query failed:"+nodeQuery+"\n"+e);
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
	public List<CNode> loadCNodes(CNodePool parent, FailoverFetch failoverFetch, List<Pair<Long, String>> baseUrls) {
		Map<String,CNode> nodes = new TreeMap<String,CNode>();
		List<CNode> ret = new ArrayList<CNode>();
		ResultSet rs = null;
		
		try{
			if(error == false){
				try {
					rs = nodeQueryPS.executeQuery();
					while(rs.next()){
						try {
							CNode c = new CNode();
							c.setFailoverFetch(failoverFetch);
							c.setParentPool(parent);
							c.setBaseUrls(baseUrls);
							
							String id = rs.getString("ID");
							c.setMetaCNodeGUID(id.trim());
							
							String name = rs.getString("NAME");
							c.setNodeName(name);
							
							nodes.put(id,c);
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
				String configuration = null;
				for(Entry<String, CNode> es:nodes.entrySet()){
					try{
						configurationQueryPS.setString(1,es.getKey());
					}
					catch(SQLException e){
						//probably due to not have any parameters
						multipleConfigurations = false;
					}
					
					if(multipleConfigurations || (configuration == null)){
						try{
							rs = configurationQueryPS.executeQuery();
							while(rs.next()){
								try {
									configuration = rs.getString("CONFIGURATION");
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
					}
					es.getValue().setConfiguration(configuration);
					ret.add(es.getValue());
				}
				
			}
		}
		finally{
			if(nodeQueryPS != null){
				try {
					nodeQueryPS.close();
				} catch (SQLException e) {
				}
				finally{
					nodeQueryPS = null;
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
