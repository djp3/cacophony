/*
	Copyright 2007-2013
		University of California, Irvine (c/o Donald J. Patterson)
*/
/*
	This file is part of Cacophony

    Cacophony is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Cacophony is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cacophony.  If not, see <http://www.gnu.org/licenses/>.
*/

package edu.uci.ics.luci.cacophony.sensors;

import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.uci.ics.luci.utility.datastructure.MapComparable;
import edu.uci.ics.luci.utility.datastructure.Pair;


public class WiFiMac extends WiFi {

	 private static transient volatile Logger log = null;
	 public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(WiFiMac.class);
		}
		return log;
	}
		
		
	@Override
	protected String getNativeLibraryName() {
		return "wifi";
	}
	
	@Override
	protected boolean initialize() {
		return true;
	}
	
	@Override
	protected void shutdown() {
		super.shutdown();
	}	
	
	public static synchronized native Object[] _getAPMAC();
	public static synchronized native Object[][] _getAllAPMAC();
	
	/**
	 * Get the information for the currently connected WiFi Access Point
	 * @return <<SSID,MAC/BSSID>, RSSI>
	 * SSID is like "UCINet Mobile Access"
	 * BSSID is like "00:19:a9:54:23:91"
	 * RSSI is like -67
	 */
	public Pair<Pair<String,String>, Integer> getAPMAC() {
		 Object[] o = _getAPMAC();
		 
		 return WiFi.constructAP(o);
	}
	
	
	/**
	 * Get all the visible WiFi Access Points
	 * @return a map of <<SSID,MAC/BSSID>, RSSI>
	 * SSID is like "UCINet Mobile Access"
	 * BSSID is like "00:19:a9:54:23:91"
	 * RSSI is like -67
	 */
	public MapComparable<Pair<String,String>, Integer> getAllAPMAC() {
		
		MapComparable<Pair<String,String>,Integer> answer = new MapComparable<Pair<String,String>,Integer>(new HashMap<Pair<String,String>,Integer>());
		
		Object[][] source = _getAllAPMAC();
		
		for(Object[] o:source){
			try{
				answer.put(WiFi.constructAP(o));
			}
			catch(RuntimeException e){
				getLog().error("Not getting Wifi access points correctly");
			}
		}
		
		return(answer);
	}
}
