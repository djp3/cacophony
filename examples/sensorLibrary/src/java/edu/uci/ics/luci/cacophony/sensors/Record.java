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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.uci.ics.luci.utility.datastructure.ListComparable;
import edu.uci.ics.luci.utility.datastructure.MapComparable;
import edu.uci.ics.luci.utility.datastructure.Pair;


public final class Record implements Cloneable, Serializable {
private static final long serialVersionUID = -6855888492539831371L;

private static transient volatile Logger log = null;
public static Logger getLog(){
	if(log == null){
		log = Logger.getLogger(Record.class);
	}
	return log;
}


/*****************************************************************/
/** Things that are sensed
/** The number of milliseconds since January 1, 1970, 00:00:00 GMT
 *  this is what java.util.Date.getTime() returns*/
private Long timeStamp= null;
/** dayOfTheWeek is governed by Calendar.<DAY_OF_WEEK> */
private Integer dayOfTheWeek= null;
/** midnight is 0, 1am is 60, 2am is 120 etc. */
private Integer minutesSinceMidnight= null;
/** This is the IP address of the user's machine as seen from the Internet */
private String remoteIPAddress= null;
/** This is the host name lookup of the remoteIPAddress */
private String dns= null;
/** This is the IP address of the user's machine as seen from the local network */
private String localIPAddress= null;
/** This is the MAC address of the connected WiFi MAC Address */
private Pair<Pair<String,String>,Integer> wifiMACAddressConnected= null;
/** This is list of visible WiFi MAC Addresses */
private MapComparable<Pair<String, String>, Integer> wifiMACAddressList= null;
/** These are the MAC SSID of the connected WiFi Mac Address*/
private String wifiSSID= null;
/** active process is the last process to have window focus*/ 
private String activeProcess= null;
/** The set of processes running on the user's machine */
private ListComparable<String> runningProcesses= null;
/** The number of displays connected to the user's machine*/ 
private Integer numberOfDisplays= null;
/** The resolution of the connected displays*/ 
private String displayConfiguration= null;
/** The source of a user's power*/ 
private PowerSourceEnum powerSource= null;
/** The acceleration in various directions */
private Double accelerationX= null;
private Double accelerationY= null;
private Double accelerationZ= null;
/** The computers current volume setting */
private Double ambientLight= null;
/** The computers currently observed sound levels*/
private Double volume= null;
/** The computers currently observed light levels*/
private Double ambientSound= null;
/** Not defined/implemented*/
private Double uIActivity= null;

/*****************************************************************/
/** Things that are derived from sensed fields */
/** There are more than this.  This one is just locally cached **/

/** A derived field of the 3 accelerations */
private Double accelerationMagnitude= null;

/*****************************************************************/

public String getRemoteIPAddress() {
	return remoteIPAddress;
}

public void setRemoteIPAddress(String remoteIPAddress) {
	this.remoteIPAddress = remoteIPAddress;
}

public String getLocalIPAddress() {
	return localIPAddress;
}

public void setLocalIPAddress(String localIPAddress) {
	this.localIPAddress = localIPAddress;
}

public String getDisplayConfiguration() {
	return displayConfiguration;
}

public void setDisplayConfiguration(String displayConfiguration) {
	this.displayConfiguration = displayConfiguration;
}

public PowerSourceEnum getPowerSource() {
	return powerSource;
}

public void setPowerSource(PowerSourceEnum powerSource) {
	this.powerSource = powerSource;
}
public void setPowerSource(String powerSource) {
	if(powerSource.equalsIgnoreCase("Battery")){
		this.powerSource = PowerSourceEnum.BATTERY;
	}
	else if(powerSource.equalsIgnoreCase("Wall")){
		this.powerSource = PowerSourceEnum.WALL;
	}
	else 
		this.powerSource = (PowerSourceEnum.fromInteger(Integer.parseInt(powerSource)));
}

public Double getAccelerationX() {
	return accelerationX;
}

public void setAccelerationX(Double accelerationX) {
	this.accelerationX = accelerationX;
	updateAccelerationMagnitude();
}

public Double getAccelerationY() {
	return accelerationY;
}

public void setAccelerationY(Double accelerationY) {
	this.accelerationY = accelerationY;
	updateAccelerationMagnitude();
}

public Double getAccelerationZ() {
	return accelerationZ;
}

public void setAccelerationZ(Double accelerationZ) {
	this.accelerationZ = accelerationZ;
	updateAccelerationMagnitude();
}

public Double getAccelerationMagnitude() {
	return accelerationMagnitude;
}

private void updateAccelerationMagnitude() {

	if ((accelerationX == null) && (accelerationY == null) && (accelerationZ == null)) {
		this.accelerationMagnitude = null;
	} else {
		double ax;
		double ay;
		double az;

		if (accelerationX != null) {
			ax = accelerationX.doubleValue();
		} else {
			ax = 0.0;
		}
		if (accelerationY != null) {
			ay = accelerationY.doubleValue();
		} else {
			ay = 0.0;
		}
		if (accelerationZ != null) {
			az = accelerationZ.doubleValue();
		} else {
			az = 0.0;
		}
		this.accelerationMagnitude = Math.sqrt(ax * ax + ay * ay + az * az);
	}
}

public Double getVolume() {
	return volume;
}

public void setVolume(Double volume) {
	this.volume = volume;
}

public Double getAmbientLight() {
	return ambientLight;
}

public void setAmbientLight(Double ambientLight) {
	this.ambientLight = ambientLight;
}

public Double getAmbientSound() {
	return ambientSound;
}

public void setAmbientSound(Double ambientSound) {
	this.ambientSound = ambientSound;
}

public Double getUIActivity() {
	return uIActivity;
}

public void setUIActivity(Double activity) {
	uIActivity = activity;
}

public Long getTimeStamp() {
	return timeStamp;
}

public void setTimeStamp(Long timeStamp) {
	this.timeStamp = timeStamp;
}

public Integer getDayOfTheWeek() {
	return dayOfTheWeek;
}

public void setDayOfTheWeek(Integer dayOfTheWeek) {
	this.dayOfTheWeek = dayOfTheWeek;
}

public Integer getMinutesSinceMidnight() {
	return minutesSinceMidnight;
}

public void setMinutesSinceMidnight(Integer minutesSinceMidnight) {
	this.minutesSinceMidnight = minutesSinceMidnight;
}

public String getDNS() {
	return dns;
}

public void setDNS(String dns) {
	this.dns = dns;
}

public Pair<Pair<String, String>, Integer> getWifiMACAddressConnected() {
	return wifiMACAddressConnected;
}

public void setWifiMACAddressConnected(Pair<Pair<String,String>,Integer> wifiMACAddress) {
	this.wifiMACAddressConnected = wifiMACAddress;
}

public MapComparable<Pair<String, String>, Integer> getWifiMACAddressList() {
	return wifiMACAddressList;
}

public void setWifiMACAddressList(MapComparable<Pair<String,String>,Integer> wifiMACAddress) {
	this.wifiMACAddressList = wifiMACAddress;
}

public String getWifiSSID() {
	return wifiSSID;
}

public void setWifiSSID(String wifiSSID) {
	this.wifiSSID = wifiSSID;
}


public String getActiveProcess() {
	return activeProcess;
}

public void setActiveProcess(String activeProcess) {
	this.activeProcess = activeProcess;
}

public List<String> getRunningProcesses() {
	return runningProcesses;
}

public void setRunningProcesses(ListComparable<String> runningProcesses) {
	this.runningProcesses = runningProcesses;
}

public Integer getNumberOfDisplays() {
	String dc = getDisplayConfiguration();
	if(dc == null){
		return (null);
	}
	else{
		/* find all display pairs (x,y) */
		String s[] = dc.split("\\([^\\)]*\\)");
		return s.length-1;
	}
}


public Record()
{
	super();
}

/**
 * 
 * @param timestamp
 * @param dayOfTheWeek
 * @param minutesSinceMidnight
 * @param remoteIPAddress
 * @param dns
 * @param localIPAddress
 * @param wifiMACAddressConnected
 * @param wifiMACAddressList
 * @param wifiSSID
 * @param activeProcess
 * @param runningProcesses
 * @param numberOfDisplays
 * @param displayConfiguration
 * @param powerSource
 * @param accelerationX
 * @param accelerationY
 * @param accelerationZ
 * @param ambientLight
 * @param volume
 * @param ambientSound
 * @param uIActivity
 */
public Record(
		Long timestamp,
		Integer dayOfTheWeek,
		Integer minutesSinceMidnight,
		String remoteIPAddress,
		String dns,
		String localIPAddress,
		Pair<Pair<String, String>, Integer> wifiMACAddressConnected,
		MapComparable<Pair<String, String>, Integer> wifiMACAddressList,
		String wifiSSID,
		String activeProcess,
		ListComparable<String> runningProcesses,
		Integer numberOfDisplays,
		String displayConfiguration,
		PowerSourceEnum powerSource,
		Double accelerationX,
		Double accelerationY,
		Double accelerationZ,
		Double ambientLight,
		Double volume,
		Double ambientSound,
		Double uIActivity) {
	super();
	this.timeStamp = timestamp;
	this.dayOfTheWeek = dayOfTheWeek;
	this.minutesSinceMidnight = minutesSinceMidnight;
	this.remoteIPAddress = remoteIPAddress;
	this.dns = dns;
	this.localIPAddress = localIPAddress;
	this.wifiMACAddressConnected = wifiMACAddressConnected;
	this.wifiMACAddressList = wifiMACAddressList;
	this.wifiSSID = wifiSSID;
	this.activeProcess = activeProcess;
	this.runningProcesses = runningProcesses;
	this.numberOfDisplays = numberOfDisplays;
	this.displayConfiguration = displayConfiguration;
	this.powerSource = powerSource;
	this.accelerationX = accelerationX;
	this.accelerationY = accelerationY;
	this.accelerationZ = accelerationZ;
	this.updateAccelerationMagnitude();
	this.ambientLight = ambientLight;
	this.volume = volume;
	this.ambientSound = ambientSound;
	this.uIActivity = uIActivity;
}

public Record clone() {
		return new Record(
				timeStamp,
				dayOfTheWeek,
				minutesSinceMidnight,
				remoteIPAddress,
				dns,
				localIPAddress,
				wifiMACAddressConnected,
				wifiMACAddressList,
				wifiSSID,
				activeProcess,
				runningProcesses,
				numberOfDisplays,
				displayConfiguration,
				powerSource,
				accelerationX,
				accelerationY,
				accelerationZ,
				ambientLight,
				volume,
				ambientSound,
				uIActivity);
}

public boolean equals(Object otherObject) {
	
	/* Check typing */
	if (!(otherObject instanceof Record))
		return false;
	
	Record otherRecord = (Record) otherObject;
	
	/*Go through each attribute.  If each is equal, or both null then the 
	 * attributes are equal.
	 */
	for(SensorNetAttribute att: SensorNetAttribute.values()){
		if((this.get(att) != null) && (otherRecord.get(att) != null)){
				if(!this.get(att).equals(otherRecord.get(att))){
					return(false);
				}
		}
		else{
			if((this.get(att) != null) ||  (otherRecord.get(att) != null)){
				return(false);
			}
		}
	}
	return(true);
}

@Override
public int hashCode() {
	final int prime = 31;
	int result = 1;
	
	for(SensorNetAttribute att: SensorNetAttribute.values()){
		if(this.get(att) != null){
			result = prime * result + this.get(att).hashCode();
		}
	}
	return result;
}

public String toString()
{
	return "I("+
	timeStamp+","+
	dayOfTheWeek+","+
	minutesSinceMidnight+","+
	remoteIPAddress+","+
	dns+","+
	localIPAddress+","+
	wifiMACAddressConnected+","+
	wifiMACAddressList+","+
	wifiSSID+","+
	activeProcess+","+
	runningProcesses+","+
	numberOfDisplays+","+
	displayConfiguration+","+
	powerSource+","+
	accelerationX+","+
	accelerationY+","+
	accelerationZ+","+
	accelerationMagnitude+","+
	ambientLight+","+
	volume+","+
	ambientSound+","+
	uIActivity+")";
}

public Comparable<?> get(SensorNetAttribute att)
{
	Method getter = getGetterForAttribute(att);
	try {
		return (Comparable<?>) getter.invoke(this, (Object[])null);
	} catch (IllegalAccessException e) {
		getLog().log(Level.ERROR, "Couldn't get "+att+".", e);
		return null;
	} catch (InvocationTargetException e) {
		getLog().log(Level.ERROR, "Couldn't get "+att+".", e);
		return null; 
	}
}

public void set(SensorNetAttribute att,Object value)
{
	Method setter = getSetterForAttribute(att);
	Object[] oArray = new Object[1];
	oArray[0] = value;
	try {
		setter.invoke(this, oArray);
		return;
	} catch (IllegalAccessException e) {
		getLog().log(Level.ERROR, "Couldn't set "+att+".", e);
		return;
	} catch (InvocationTargetException e) {
		getLog().log(Level.ERROR, "Couldn't set "+att+".", e);
		return; 
	}
}

private Method getGetterForAttribute(SensorNetAttribute att)
{
	try {
		switch (att) {
		case TIME_STAMP:
			return Record.class.getDeclaredMethod("getTimeStamp");
		case DAY_OF_THE_WEEK:
			return Record.class.getDeclaredMethod("getDayOfTheWeek");
		case MINUTES_SINCE_MIDNIGHT:
			return Record.class.getDeclaredMethod("getMinutesSinceMidnight");
		case REMOTE_IP_ADDRESS:
			return Record.class.getDeclaredMethod("getRemoteIPAddress");
		case DNS:
			return Record.class.getDeclaredMethod("getDNS");
		case LOCAL_IP_ADDRESS:
			return Record.class.getDeclaredMethod("getLocalIPAddress");
		case WIFI_MAC_ADDRESS_CONNECTED:
			return Record.class.getDeclaredMethod("getWifiMACAddressConnected");
		case WIFI_MAC_ADDRESS_LIST:
			return Record.class.getDeclaredMethod("getWifiMACAddressList");
		case WIFI_SSID:
			return Record.class.getDeclaredMethod("getWifiSSID");
		case ACTIVE_PROCESS:
			return Record.class.getDeclaredMethod("getActiveProcess");
		case RUNNING_PROCESSES:
			return Record.class.getDeclaredMethod("getRunningProcesses");
		case NUM_DISPLAYS:
			return Record.class.getDeclaredMethod("getNumberOfDisplays");
		case DISPLAY_CONFIG:
			return Record.class.getDeclaredMethod("getDisplayConfiguration");
		case POWER_SOURCE:
			return Record.class.getDeclaredMethod("getPowerSource");
		case ACC_X:
			return Record.class.getDeclaredMethod("getAccelerationX");
		case ACC_Y:
			return Record.class.getDeclaredMethod("getAccelerationY");
		case ACC_Z:
			return Record.class.getDeclaredMethod("getAccelerationZ");
		case ACC_MAG:
			return Record.class.getDeclaredMethod("getAccelerationMagnitude");
		case AMBIENT_LIGHT:
			return Record.class.getDeclaredMethod("getAmbientLight");
		case VOLUME:
			return Record.class.getDeclaredMethod("getVolume");
		case AMBIENT_SOUND:
			return Record.class.getDeclaredMethod("getAmbientSound");
		case UI_ACTIVITY:
			return Record.class.getDeclaredMethod("getUIActivity");
		default:
			throw new IllegalArgumentException("Unknown attribute (" + att + ") given.");
		}
	}
	catch (NoSuchMethodException e) {
		getLog().log(Level.ERROR, "Couldn't find getter method for attribute (" + att + ") given.", e);
		throw new IllegalArgumentException("Couldn't find getter method for attribute (" + att + ") given.");
	}
}

private Method getSetterForAttribute(SensorNetAttribute att)
{
	try {
		switch (att) {
		case TIME_STAMP:
			return Record.class.getDeclaredMethod("setTimeStamp",Long.class);
		case DAY_OF_THE_WEEK:
			return Record.class.getDeclaredMethod("setDayOfTheWeek",Integer.class);
		case MINUTES_SINCE_MIDNIGHT:
			return Record.class.getDeclaredMethod("setMinutesSinceMidnight",Integer.class);
		case REMOTE_IP_ADDRESS:
			return Record.class.getDeclaredMethod("setRemoteIPAddress",String.class);
		case DNS:
			return Record.class.getDeclaredMethod("setDNS",String.class);
		case LOCAL_IP_ADDRESS:
			return Record.class.getDeclaredMethod("setLocalIPAddress",String.class);
		case WIFI_MAC_ADDRESS_CONNECTED:
			return Record.class.getDeclaredMethod("setWifiMACAddressConnected",Pair.class);
		case WIFI_MAC_ADDRESS_LIST:
			return Record.class.getDeclaredMethod("setWifiMACAddressList",MapComparable.class);
		case WIFI_SSID:
			return Record.class.getDeclaredMethod("setWifiSSID",String.class);
		case ACTIVE_PROCESS:
			return Record.class.getDeclaredMethod("setActiveProcess",String.class);
		case RUNNING_PROCESSES:
			return Record.class.getDeclaredMethod("setRunningProcesses",ListComparable.class);
		case DISPLAY_CONFIG:
			return Record.class.getDeclaredMethod("setDisplayConfiguration",String.class);
		case POWER_SOURCE:
			return Record.class.getDeclaredMethod("setPowerSource",PowerSourceEnum.class);
		case ACC_X:
			return Record.class.getDeclaredMethod("setAccelerationX",Double.class);
		case ACC_Y:
			return Record.class.getDeclaredMethod("setAccelerationY",Double.class);
		case ACC_Z:
			return Record.class.getDeclaredMethod("setAccelerationZ",Double.class);
		case AMBIENT_LIGHT:
			return Record.class.getDeclaredMethod("setAmbientLight",Double.class);
		case VOLUME:
			return Record.class.getDeclaredMethod("setVolume",Double.class);
		case AMBIENT_SOUND:
			return Record.class.getDeclaredMethod("setAmbientSound",Double.class);
		case UI_ACTIVITY:
			return Record.class.getDeclaredMethod("setUIActivity",Double.class);
		default:
			throw new IllegalArgumentException("Unknown attribute (" + att + ") given.");
		}
	}
	catch (NoSuchMethodException e) {
		getLog().log(Level.ERROR, "Couldn't find setter method for attribute (" + att + ") given.", e);
		throw new IllegalArgumentException("Couldn't find setter method for attribute (" + att + ") given.");
	}
}



// This method is public because the unit test needs to get to it. It shouldn't be.
public static String arrayListToString(ArrayList<String> arr) {
	if (arr == null)
		return null;
	if (arr.size() == 0)
		return "";
	StringBuilder b = new StringBuilder();
	for (String s : arr)
		b.append(s + ",");
	return b.substring(0, b.length()-1);
}

// This method is public because the unit test needs to get to it. It shouldn't be.
public static String mapToString(Map<String,Integer> arr) {
	if (arr == null)
		return null;
	if (arr.size() == 0)
		return "";
	StringBuilder b = new StringBuilder();
	for (Entry<String, Integer> e : arr.entrySet())
		b.append(e.getKey() +"-"+e.getValue()+",");
	return b.substring(0, b.length()-1);
}

}

