package edu.uci.ics.luci.cacophony.node;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import weka.core.Instances;
import weka.experiment.InstanceQuery;

import com.quub.Globals;

public class MySQL extends CNodeHistoryLoader{
	
	private transient volatile Logger log = null;
	public Logger getLog(){
		if(log == null){
			log = Logger.getLogger(MySQL.class);
		}
		return log;
	}

	private InstanceQuery query;
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
		String historyQuery=null;
		try {
			historyQuery = options.getString("historyQuery");
		} catch (JSONException e) {
		}
		if(historyQuery == null){
			getLog().error("Unable to get the \"historyQuery\"");
			error = true;
		}
		
		if(!error){
			try {
				query = new InstanceQuery();
				query.setUsername(username);
				query.setPassword(password);
				query.setDatabaseURL("jdbc:mysql://"+databaseDomain+"/"+database);
				query.setQuery(historyQuery);
			} catch (Exception e) {
				try {
					getLog().error("Unable to load history using: "+options.toString(1));
				} catch (JSONException e1) {
					getLog().error("Unable to load history in "+this.getClass().getCanonicalName());
				}
			}
		}
	}


	@Override
	public Instances loadCNodeHistory() {
		Instances data = null;
		try {
			data = query.retrieveInstances();
		} catch (Exception e) {
			getLog().error("Unable to get training instances from database\n"+e);
		}
		return data;
	}

}
