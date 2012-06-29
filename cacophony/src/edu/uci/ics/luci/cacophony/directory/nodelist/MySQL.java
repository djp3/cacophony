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
	private PreparedStatement preparedStatement;
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
		String query=null;
		try {
			query = options.getString("query");
		} catch (JSONException e) {
		}
		if(query == null){
			getLog().error("Unable to get the \"query\"");
			error = true;
		}
		else{
			query = query.toUpperCase();
			if(!query.contains("SELECT ")){
				getLog().error("Query does not contain \"SELECT\"");
				error = true;
			}
			if(!query.contains("AS ID")){
				getLog().error("Query does not contain as selector for \"ID\"");
				error = true;
			}
			if(!query.contains("AS NAME")){
				getLog().error("Query does not contain as selector for \"NAME\"");
				error = true;
			}
		}
		if(!error){
			pool = new QuubDBConnectionPool(g,databaseDomain, database, username, password);
			connection = pool.getConnection();
			try {
				preparedStatement = connection.prepareStatement(query);
			} catch (SQLException e) {
				getLog().error("Query failed:"+query+"\n"+e);
				error=true;
			}
		}
	}

	private Map<String,CNode> executeQuery() {
		Map<String,CNode> ret = new TreeMap<String,CNode>();
		ResultSet rs = null;
		try{
			if(error == false){
				try {
					rs = preparedStatement.executeQuery();
					while(rs.next()){
						try {
							CNode cNode = new CNode();
							String id = rs.getString("ID");
							cNode.setId(id);
							String name = rs.getString("NAME");
							cNode.setName(name);
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
			if(preparedStatement != null){
				try {
					preparedStatement.close();
				} catch (SQLException e) {
				}
				finally{
					preparedStatement = null;
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
	public Map<String, CNode> loadNodeList() {
		return executeQuery();
	}

}
