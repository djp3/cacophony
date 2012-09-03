package edu.uci.ics.luci.cacophony.api.directory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.quub.webserver.WebServer;
import com.quub.webserver.WebUtil;

import edu.uci.ics.luci.cacophony.CacophonyGlobals;
import edu.uci.ics.luci.cacophony.api.CacophonyRequestHandlerHelper;

public class WebServerWarmUp {
	
	private static transient volatile Logger log = null;
	
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(WebServerWarmUp.class);
		}
		return log;
	}
	
	static public void go(WebServer ws,int port, String server) {
		go(ws,port,server,null,null);
	}

	static public void go(WebServer ws,int port, String server,Integer timeOut,Integer tries) {
		if(server == null){
			server = "https://localhost";
		}
		
		if(timeOut == null){
			timeOut = 30 *1000;
		}
		
		if(tries == null){
			tries = 10;
		}
		
		/* Warm up the web server by asking for the version */
		try{
			String responseString =  null;
			HashMap<String, String> params = new HashMap<String, String>();
			try{
				boolean success = false;
				int count = 0;
				while((!success) && (count < tries)){
					try{
						responseString = WebUtil.fetchWebPage(server+":"+port + "/version", false, params, timeOut);
						success = true;
					}
					catch(java.net.SocketTimeoutException e){
						getLog().error("Couldn't warm up web server.  Timed out. Trying again");
						count++;
					}
				}
			} catch (MalformedURLException e) {
				getLog().fatal("Couldn't ping webserver, bad URL:"+e.toString());
				ws.setQuitting(true);
			} catch (IOException e) {
				getLog().fatal("Couldn't ping webserver, IO problem:"+e.toString());
				ws.setQuitting(true);
			}
			
			if(responseString == null){
				getLog().fatal("Couldn't ping webserver: responseString was null");
				ws.setQuitting(true);
			}
			else{
				JSONObject response = null;
				try {
					response = new JSONObject(responseString);
				} catch (JSONException e) {
					getLog().fatal("Couldn't form JSON from responseString:"+e.toString()+":"+responseString);
					ws.setQuitting(true);
				}
			
				if(response == null){
					getLog().fatal("Didnt get good data from webserver: response was null");
					ws.setQuitting(true);
				}
				else{
					String error = null;
					try {
						error = response.getString("error");
					} catch (JSONException e) {
						getLog().fatal("Couldn't find errors respons from webserver:"+e.toString()+"\n"+response);
						ws.setQuitting(true);
					}
					
					if(error == null){
						getLog().fatal("Got errors while pinging webserver: error is null");
						ws.setQuitting(true);
					}
					else if(!error.equals("false")){
						getLog().fatal("Got unexpected errors while pinging webserver:"+error);
						ws.setQuitting(true);
					}
					else{
						String answer = null;
						try {
							answer = response.getString("version");
						} catch (JSONException e) {
							getLog().fatal("Couldn't form JSON string from version:"+e.toString()+"\n"+response);
							ws.setQuitting(true);
						}
							
						if(answer == null){
							getLog().fatal("Didn't get expected version number, wanted:"+CacophonyRequestHandlerHelper.getAPIVersion()+", got: null");
							ws.setQuitting(true);
						} else {
							if(!answer.equals(CacophonyRequestHandlerHelper.getAPIVersion())){
								getLog().fatal("Didn't get expected version number, wanted:"+CacophonyRequestHandlerHelper.getAPIVersion()+", got: "+answer);
								ws.setQuitting(true);
							}
							else{
								getLog().fatal("Started "+ CacophonyGlobals.class.getCanonicalName() +" WebServer version "+answer);
							}
						}
					}
				}
			}
		} catch (RuntimeException e) {
			getLog().fatal("Couldn't ping webserver");
			ws.setQuitting(true);
		}
	}
	

}
