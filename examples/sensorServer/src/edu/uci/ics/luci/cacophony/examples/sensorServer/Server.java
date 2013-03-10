/*
Copyright 2007-2013
	University of California, Irvine (c/o Donald J. Patterson)
*/
/*
This file is part of the Laboratory for Ubiquitous Computing java Utility package, i.e. "Utilities"

Utilities is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Utilities is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Utilities.  If not, see <http://www.gnu.org/licenses/>.
*/

package edu.uci.ics.luci.cacophony.examples.sensorServer;

import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.uci.ics.luci.utility.Globals;
import edu.uci.ics.luci.utility.Quittable;
import edu.uci.ics.luci.utility.webserver.AccessControl;
import edu.uci.ics.luci.utility.webserver.HandlerAbstract;
import edu.uci.ics.luci.utility.webserver.RequestDispatcher;
import edu.uci.ics.luci.utility.webserver.WebServer;
import edu.uci.ics.luci.utility.webserver.handlers.HandlerFavicon;
import edu.uci.ics.luci.utility.webserver.handlers.HandlerFileServer;
import edu.uci.ics.luci.utility.webserver.handlers.HandlerVersion;

public class Server implements Quittable{
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(Server.class);
		}
		return log;
	}
	
	private static Object classLock = new Object();
	public static final String API_VERSION = "1.0";
	
	private WebServer ws = null;
	HashMap<String,HandlerAbstract> requestHandlerRegistry;
	private boolean quitting = false;
	
	Server(){
		Globals.setGlobals(new SensorServerGlobals());
	}
		
	
	public void initialize(){
		Globals.getGlobals().addQuittables(this);
		try {
			requestHandlerRegistry = new HashMap<String, HandlerAbstract>();
			requestHandlerRegistry.put(null,new HandlerFileServer(edu.uci.ics.luci.utility.Globals.class,"/www/"));
			HandlerVersion handler = new HandlerVersion(Globals.getGlobals().getSystemVersion());
			requestHandlerRegistry.put("",handler);
			requestHandlerRegistry.put("version",handler);
			requestHandlerRegistry.put("sense",new HandlerSensor());
			requestHandlerRegistry.put("favicon.ico", new HandlerFavicon());
			requestHandlerRegistry.put("shutdown",new HandlerShutdown());
		
			RequestDispatcher dispatcher = new RequestDispatcher(requestHandlerRegistry);
			ws = new WebServer(dispatcher, SensorServerGlobals.DEFAULT_PORT, false, new AccessControl());
			ws.start();
			Globals.getGlobals().addQuittables(ws);
		} catch (RuntimeException e) {
			getLog().error("Couldn't start webserver"+e);
		}
	}
	
	@Override
	public void setQuitting(boolean quitting) {
		synchronized(classLock){
			if(!isQuitting()){
				this.quitting = quitting;
				classLock.notifyAll();
			}
		}
	}


	@Override
	public boolean isQuitting() {
		return this.quitting;
	}
	
	public static void main(String args[]) {
		Server server = new Server();
		server.initialize();
		synchronized(classLock){
			while(!server.isQuitting()){
				try {
					classLock.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}


}
