package edu.uci.ics.luci.cacophony.node;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import weka.core.Instances;
import weka.experiment.InstanceQuery;
import edu.uci.ics.luci.util.FailoverFetch;
import edu.uci.ics.luci.utility.Globals;
import edu.uci.ics.luci.utility.datastructure.Pair;

public class MySQL extends CNodeLoader{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -30178893631678570L;
	private transient volatile Logger log = null;
	public Logger getLog(){
		if(log == null){
			log = Logger.getLogger(MySQL.class);
		}
		return log;
	}

	private InstanceQuery idQuery;
	private String username;
	private String password;
	private String databaseDomain;
	private String database;
	private String trainingQueryString;
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
		
		try {
			databaseDomain = options.getString("server_address");
		} catch (JSONException e) {
		}
		if(databaseDomain == null){
			getLog().error("Unable to get the \"server_address\"");
			error = true;
		}
		
		try {
			database = options.getString("database");
		} catch (JSONException e) {
		}
		if(database == null){
			getLog().error("Unable to get the \"database\"");
			error = true;
		}
		
		try {
			username = options.getString("user");
		} catch (JSONException e) {
		}
		if(username == null){
			getLog().error("Unable to get the \"user\"");
			error = true;
		}
		
		try {
			password = options.getString("password");
		} catch (JSONException e) {
		}
		if(password == null){
			getLog().error("Unable to get the \"password\"");
			error = true;
		}
		
		String nodeQueryString=null;
		try {
			nodeQueryString = options.getString("nodeQuery");
		} catch (JSONException e) {
		}
		if(nodeQueryString == null){
			getLog().error("Unable to get the \"nodeQuery\"");
			error = true;
		}
		
		try {
			trainingQueryString = options.getString("trainingQuery");
		} catch (JSONException e) {
		}
		if(trainingQueryString == null){
			getLog().error("Unable to get the \"trainingQuery\"");
			error = true;
		}
		
		if(!error){
			try {
				idQuery = new InstanceQuery();
				idQuery.setUsername(username);
				idQuery.setPassword(password);
				idQuery.setDatabaseURL("jdbc:mysql://"+databaseDomain+"/"+database);
				idQuery.setQuery(nodeQueryString);
			} catch (Exception e) {
				try {
					getLog().error("Unable to load cnodes using: "+options.toString(1));
				} catch (JSONException e1) {
					getLog().error("Unable to load cnodes in "+this.getClass().getCanonicalName());
				}
			}
		}
	}


	@Override
	public List<CNode> loadCNodes(CNodePool parent, FailoverFetch failoverFetch, List<Pair<Long, String>> baseUrls) {
		List<CNode> ret = new ArrayList<CNode>();
		Instances data = null;
		try {
			data = idQuery.retrieveInstances();
			for(int i = 0; i< data.numInstances(); i++){
		    	String zid = data.instance(i).attributeSparse(0).value((int) Math.round(data.instance(i).value(0)));
		    	String name = data.instance(i).attributeSparse(1).value((int) Math.round(data.instance(i).value(1)));
		    	CNode c = new CNode();
		    	c.setFailoverFetch(failoverFetch);
		    	c.setParentPool(parent);
		    	c.setBaseUrls(baseUrls);
		    	c.setMetaCNodeGUID(zid);
		    	c.setNodeName(name);
		    	
		    	InstanceQuery trainingQuery;
				try {
		    		trainingQuery = new InstanceQuery();
		    		trainingQuery.setUsername(username);
		    		trainingQuery.setPassword(password);
		    		trainingQuery.setDatabaseURL("jdbc:mysql://"+databaseDomain+"/"+database);
		    		trainingQuery.setQuery(trainingQueryString.replaceAll("_NODE_ID_", zid));
		    		c.setTrainingQuery(trainingQuery);
				} catch (Exception e) {
					getLog().error("Unable to load cnode training using: "+zid);
				}
				ret.add(c);
			}
		} catch (Exception e) {
			getLog().error("Unable to get training instances from database\n"+e);
		}
		return ret;
	}
}
