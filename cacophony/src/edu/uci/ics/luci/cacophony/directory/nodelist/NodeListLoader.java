package edu.uci.ics.luci.cacophony.directory.nodelist;

import java.util.List;


import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class NodeListLoader {
	
	private transient volatile Logger log = null;
	public Logger getLog(){
		if(log == null){
			log = Logger.getLogger(NodeListLoader.class);
		}
		return log;
	}
	
	/*TODO: Figure out how to get this out of here */
	public JSONObject getConfiguration(String id){
		JSONObject ret = new JSONObject();
		try {
			ret.put("node_id",id);
			ret.put("databaseDomain","127.0.0.1:33060");
			ret.put("username","don");
			ret.put("password","8HEYcshLBAuw6dQP");
			ret.put("database","scout_htdocs_042112");
			ret.put("trainingQuery","select CAST(z.zid as char), if(DAYOFWEEK(c.call_end + interval z.timezone hour + interval 5 hour) = 1,1.0,0.0) as sunday, if(DAYOFWEEK(c.call_end + interval z.timezone hour + interval 5 hour) = 2,1.0,0.0) as monday, if(DAYOFWEEK(c.call_end + interval z.timezone hour + interval 5 hour) = 3,1.0,0.0) as tuesday, if(DAYOFWEEK(c.call_end + interval z.timezone hour + interval 5 hour) = 4,1.0,0.0) as wednesday, if(DAYOFWEEK(c.call_end + interval z.timezone hour + interval 5 hour) = 5,1.0,0.0) as thursday, if(DAYOFWEEK(c.call_end + interval z.timezone hour + interval 5 hour) = 6,1.0,0.0) as friday, if(DAYOFWEEK(c.call_end + interval z.timezone hour + interval 5 hour) = 7,1.0,0.0) as saturday, HOUR(c.call_end + interval z.timezone hour + interval 5 hour - interval 4 hour)* 60.0 + MINUTE(c.call_end + interval z.timezone hour + interval 5 hour - interval 4 hour) as minutes_four, UNIX_TIMESTAMP(c.call_end) as unix_time, z.timezone as timezone ,c.wait_time from call_list as c join zrdata as z on c.zid = z.zid where z.zid=_NODE_ID_ and c.twilio_sid != \"\" and c.twilio_sid is not null and c.twilio_duration > 0 and c.wait_time >= 0 and c.wait_time < 240 and (DATEDIFF(c.call_end + interval z.timezone hour + interval 5 hour - interval 4 hour,'2012-07-01 00:00:00') >= 0)");
		} catch (JSONException e) {
			getLog().fatal("This should not fail:"+e);
		}
		return(ret);
	}
	
	abstract public void init(JSONObject options);
	abstract public List<MetaCNode> loadNodeList();
}
