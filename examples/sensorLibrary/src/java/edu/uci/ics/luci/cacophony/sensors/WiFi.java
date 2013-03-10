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
import org.apache.log4j.Logger;

import edu.uci.ics.luci.utility.Globals;
import edu.uci.ics.luci.utility.datastructure.MapComparable;
import edu.uci.ics.luci.utility.datastructure.Pair;

public abstract class WiFi extends Abstract{

	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(WiFi.class);
		}
		return log;
	}
	
	/*********************************************************
	/** These two variables need to be static to the specific sensor, but are managed by the Abstract class
	 * 
	 */
    private volatile static Object nativeLoadLock = new Object();
    private static boolean nativeLoadComplete = false;
    
	@Override
	protected synchronized Object getNativeLoadLock() {
		return nativeLoadLock;
	}
	
	@Override
	protected boolean getNativeLoadComplete(){
		synchronized(getNativeLoadLock()){
			return nativeLoadComplete;
		}
	}
	
	@Override
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value={"ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD"}, justification="Covered with a synchronization")
	protected void setNativeLoadComplete(boolean x){
		synchronized(getNativeLoadLock()){
			nativeLoadComplete = x;
		}
	}
	/*********************************************************/
	
	
	/*********************************************************
	 * These must be static so there is one per class of sensor
	 */
	private static Object sensingAvailableLock = new Object();   //Grab this before changing sensingAvailable
	private static Boolean sensingAvailable = null;
	
	private static Object sensorLock = new Object();			//Grab this before using sensor
	private static WiFi sensor = null;
	/*********************************************************/
	
	/** use getSensor instead of constructor **/
	protected WiFi(){
		super();
	}
	
	/**
	 * Factory method for instantiating the OS specific sensor
	 * Shutdown is handled via Globals.setQuitting() method
	 */
	public static WiFi getSensor(){
		synchronized (sensorLock){
			if(sensor == null){
				switch(os){
					case MAC_OS_X: 
						sensor = new WiFiMac();
						break;
					case WINDOWS_VISTA:
					case WINDOWS_XP:
						sensor = new WiFiWindows();
						break;
					default:break;
				}
				Globals.getGlobals().addQuittables(sensor);
			}
			return sensor;
		}
	}
	
	
	@Override
	public synchronized Pair<Pair<String, String>, Integer> sense(){
		return getAPMAC(); 
	}
	
	@Override
	protected void shutdown() {
		synchronized(sensorLock){
			if(sensor != null){
				sensor = null;
			}
		}
	}
	
	
	@Override
	protected Object getSensingAvailableLock() {
		return sensingAvailableLock;
	}

	@Override
	protected Boolean getSensingAvailable() {
		synchronized(getSensingAvailableLock()){
			return sensingAvailable;
		}
	}

	@Override
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value={"ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD"}, justification="Covered with a synchronization")
	protected void setSensingAvailable(Boolean sA) {
		synchronized(getSensingAvailableLock()){
			sensingAvailable = sA;
		}
	}
	
	

	/*********************************************************************************/
	/** Sensor specific interface
	/*********************************************************************************/

	/**
	 * Get the information for the currently connected WiFi Access Point
	 * @return <<SSID,MAC/BSSID>, RSSI>
	 * SSID is like "UCINet Mobile Access"
	 * BSSID is like "00:19:a9:54:23:91"
	 * RSSI is like -67
	 */
	abstract public Pair<Pair<String,String>,Integer> getAPMAC();
	abstract public MapComparable<Pair<String,String>,Integer> getAllAPMAC();

	/**
	 * Convenience method for the platform specific classes that converts an object with a single WiFi AP data into the 
	 * data structure that is returned by the WiFiSensor in other places
	 * @param o, o[0] is a String with "SSID" in it, o[1] is a String with the Mac Address, o[2] is an integer with the rssi
	 * @return
	 */
	static public Pair<Pair<String, String>, Integer> constructAP(Object[] o){
		String ssid = null;
		 try{
			ssid = (String)o[0];
		 }
		 catch (RuntimeException e){
		 }
		 
		String bssid = null;
		try{
			bssid = (String)o[1];
		}
		catch (RuntimeException e){
		}
		
		Integer rssi = null;
		try{
			rssi = Integer.valueOf((String)o[2]);
		}
		catch(RuntimeException e){
		}
		
		if((ssid != null)||(bssid != null)||(rssi!=null)){
			Pair<String,String> name = new Pair<String,String>(ssid,bssid);
			return(new Pair<Pair<String,String>,Integer>(name,rssi));
		}
		else{
			return null;
		}
	}

}
