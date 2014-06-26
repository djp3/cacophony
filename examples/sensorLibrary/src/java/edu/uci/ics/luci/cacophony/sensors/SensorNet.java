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

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.uci.ics.luci.utility.Quittable;
import edu.uci.ics.luci.utility.datastructure.ListComparable;
import edu.uci.ics.luci.utility.datastructure.MapComparable;
import edu.uci.ics.luci.utility.datastructure.Pair;

public final class SensorNet {
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
				log = LogManager.getLogger(SensorNet.class);
		}
		return log;
	}

	protected SensorNet() {}
	
	private static Record lastScan= new Record();
	private static int sensorsSoFar = 0;
	private static List<Quittable> myCleanup = new ArrayList<Quittable>();
	private final static Accelerometer sensorAccelerometer = Accelerometer.getSensor();
	private final static PowerSource sensorPowerSource = PowerSource.getSensor();
	private final static Process sensorProcess = Process.getSensor();
	private final static Light sensorLight = Light.getSensor();
	private final static IPAddress sensorIP = IPAddress.getSensor();
	private final static UIActivity sensorUI = UIActivity.getSensor();
	private final static Volume sensorVolume = Volume.getSensor();
	private final static WiFi sensorWiFi = WiFi.getSensor();
	static{
		myCleanup.add(sensorAccelerometer);
		myCleanup.add(sensorPowerSource);
		myCleanup.add(sensorProcess);
		myCleanup.add(sensorLight);
		myCleanup.add(sensorIP);
		myCleanup.add(sensorUI);
		myCleanup.add(sensorVolume);
		myCleanup.add(sensorWiFi);
	}
	
	/**
	 * Return a <class>Record</class> representing the state of the runtime machine sensors.  (Some caching may occur.)
	 */
	public synchronized static Record getCurrentState()
	{
		long startTime = System.currentTimeMillis();  // DIAGNOSTICS
		
		sensorsSoFar = 0;
		getLog().log(Level.INFO,sensorsSoFar+"/"+SensorNetAttribute.NUMBER_SENSED+":Initiating Sensor Scan");
		
		Thread networkSensors= new Thread(){
			public void run(){
				String remote = null;
				try{
					remote = senseRemoteIPAddress();
					lastScan.setRemoteIPAddress(remote);
					getLog().log(Level.DEBUG,sensorsSoFar+"/"+SensorNetAttribute.NUMBER_SENSED+":Remote IP Address:"+remote);
				}
				catch(Exception e){
					getLog().log(Level.ERROR,"Unable to sense Remote IP Address",e);
				}
				finally{
					sensorsSoFar++;
				}
				
				try{
					String dns = senseDNS(remote);
					lastScan.setDNS(dns);
					getLog().log(Level.DEBUG,sensorsSoFar+"/"+SensorNetAttribute.NUMBER_SENSED+":Lookup Address:"+dns);
				}
				catch(Exception e){
					getLog().log(Level.ERROR,"Unable to sense DNS",e);
				}
				finally{
					sensorsSoFar++;
				}
				
				try{
					String local = senseLocalIPAddress();
					lastScan.setLocalIPAddress(local);
					getLog().log(Level.DEBUG,sensorsSoFar+"/"+SensorNetAttribute.NUMBER_SENSED+":Local IP Address:"+local);
				}
				catch(Exception e){
					getLog().log(Level.ERROR,"Unable to sense Local IP Address",e);
				}
				finally{
					sensorsSoFar++;
				}
			}
		};
		networkSensors.setDaemon(false); /*Force a clean shutdown*/
		networkSensors.setName("Network Sensing");
		networkSensors.start();
		
		Thread wifiSensors= new Thread(){
			public void run(){
				try{
					Pair<Pair<String,String>,Integer> wifiMACAddressConnected= senseWifiMACAddressConnected();
					lastScan.setWifiMACAddressConnected(wifiMACAddressConnected);
					getLog().log(Level.DEBUG,sensorsSoFar+"/"+SensorNetAttribute.NUMBER_SENSED+":Connected Wifi MAC Address:"+wifiMACAddressConnected);
				}
				catch(Exception e){
					getLog().log(Level.ERROR,"Unable to sense WiFi Mac Address Connected to",e);
				}
				finally{
					sensorsSoFar++;
				}
				
				try{
					String wifiSSID = senseWifiSSID();
					lastScan.setWifiSSID(wifiSSID);
					getLog().log(Level.DEBUG,sensorsSoFar+"/"+SensorNetAttribute.NUMBER_SENSED+":Connected Wifi SSID:"+wifiSSID);
				}
				catch(Exception e){
					getLog().log(Level.ERROR,"Unable to sense WiFi SSID",e);
				}
				finally{
					sensorsSoFar++;
				}
				
				try{
					MapComparable<Pair<String,String>,Integer> wifiMACAddressList = senseWifiMACAddressList();
					lastScan.setWifiMACAddressList(wifiMACAddressList);
					getLog().log(Level.DEBUG,sensorsSoFar+"/"+SensorNetAttribute.NUMBER_SENSED+":Wifi MAC List:"+wifiMACAddressList);
				}
				catch(Exception e){
					getLog().log(Level.ERROR,"Unable to sense WiFi MAC Address List",e);
				}
				finally{
					sensorsSoFar++;
				}
				
			}
		};
		wifiSensors.setDaemon(false); /*Force a clean shutdown */
		wifiSensors.setName("Wifi Sensing");
		wifiSensors.start();
		
		Thread processSensors= new Thread(){
			public void run(){
				try{
					String activeProcess = senseActiveProcess();
					lastScan.setActiveProcess(activeProcess);
					getLog().log(Level.DEBUG,sensorsSoFar + "/" + SensorNetAttribute.NUMBER_SENSED + ":active Process:" + activeProcess);
				}
				catch(Exception e){
					getLog().log(Level.ERROR,"Unable to sense Active Process",e);
				}
				finally{
					sensorsSoFar++;
				}
				
				try{
					ListComparable<String> runningProcesses =senseRunningProcesses();
					if(runningProcesses == null)
						runningProcesses = new ListComparable<String>(new ArrayList<String>());
					lastScan.setRunningProcesses(runningProcesses);
					getLog().log(Level.DEBUG,sensorsSoFar + "/" + SensorNetAttribute.NUMBER_SENSED + ":Running processes:" + runningProcesses.toString());
				}
				catch(Exception e){
					getLog().log(Level.ERROR,"Unable to sense Running Processes",e);
				}
				finally{
					sensorsSoFar++;
				}
			}
		};
		processSensors.setDaemon(true);
		processSensors.setName("Process Sensing");
		processSensors.start();
		
		Thread junkDrawerSensing= new Thread(){

			public void run(){
				try{
					Long timeStamp = senseTimeStamp();
					lastScan.setTimeStamp(timeStamp);
					getLog().log(Level.DEBUG,sensorsSoFar + "/" + SensorNetAttribute.NUMBER_SENSED + ":Time Stamp:" + timeStamp);
				}
				catch(Exception e){
					getLog().log(Level.ERROR,"Unable to sense TimeStamp",e);
				}
				finally{
					sensorsSoFar++;
				}
				
				try{
					Integer dayOfTheWeek = senseDayOfTheWeek();
					lastScan.setDayOfTheWeek(dayOfTheWeek);
					getLog().log(Level.DEBUG,sensorsSoFar + "/" + SensorNetAttribute.NUMBER_SENSED + ":Day of the Week:" + dayOfTheWeek);
				}
				catch(Exception e){
					getLog().log(Level.ERROR,"Unable to sense Day of the Week",e);
				}
				finally{
					sensorsSoFar++;
				}
				
				try{
					Integer minutesSinceMidnight = senseMinutesSinceMidnight();
					lastScan.setMinutesSinceMidnight(minutesSinceMidnight);
					getLog().log(Level.DEBUG,sensorsSoFar + "/" + SensorNetAttribute.NUMBER_SENSED + ":Minutes Since Midnight:" + minutesSinceMidnight);
				}
				catch(Exception e){
					getLog().log(Level.ERROR,"Unable to sense Minutes Since Midnight",e);
				}
				finally{
					sensorsSoFar++;
				}
				
				try{
					String displayConfiguration = senseDisplayConfiguration();
					lastScan.setDisplayConfiguration(displayConfiguration);
					getLog().log(Level.DEBUG,sensorsSoFar + "/" + SensorNetAttribute.NUMBER_SENSED  + ":Display Configuration:"+ displayConfiguration);
				}
				catch(Exception e){
					getLog().log(Level.ERROR,"Unable to sense Display Configuration",e);
				}
				finally{
					sensorsSoFar++;
				}
				
				try{
					PowerSourceEnum powerSource = sensePowerSource();
					lastScan.setPowerSource(powerSource);
					getLog().log(Level.DEBUG,sensorsSoFar + "/" + SensorNetAttribute.NUMBER_SENSED  + ":Power Source:"+ powerSource);
				}
				catch(Exception e){
					getLog().log(Level.ERROR,"Unable to sense Power Source",e);
				}
				finally{
					sensorsSoFar++;
				}
				
				try{
					List<Double> a = sensorAccelerometer.senseAccelerometer();
					
					if((a != null) && (a.size() == 3)){
						try{
							Double accelerationX = a.get(0);
							lastScan.setAccelerationX(accelerationX);
							getLog().log(Level.DEBUG,sensorsSoFar + "/" + SensorNetAttribute.NUMBER_SENSED  + ":Acceleration X:"+ accelerationX);
						}
						catch(Exception e){
							getLog().log(Level.ERROR,"Unable to sense Acceleration X",e);
						}
						finally{
							sensorsSoFar++;
						}
				
						try{
							Double accelerationY = a.get(1);
							lastScan.setAccelerationY(accelerationY);
							getLog().log(Level.DEBUG,sensorsSoFar + "/" + SensorNetAttribute.NUMBER_SENSED  + ":Acceleration Y:"+ accelerationY);
						}
						catch(Exception e){
							getLog().log(Level.ERROR,"Unable to sense Acceleration Y",e);
						}
						finally{
							sensorsSoFar++;
						}
				
						try{
							Double accelerationZ = a.get(2);
							lastScan.setAccelerationZ(accelerationZ);
							getLog().log(Level.DEBUG,sensorsSoFar + "/" + SensorNetAttribute.NUMBER_SENSED  + ":Acceleration Z:"+ accelerationZ);
						}
						catch(Exception e){
							getLog().log(Level.ERROR,"Unable to sense Acceleration Z",e);
						}
						finally{
							sensorsSoFar++; 
						}
					}
					else{
						lastScan.setAccelerationX(null);
						lastScan.setAccelerationY(null);
						lastScan.setAccelerationZ(null);
					}
				}
				catch(RuntimeException e){
					getLog().log(Level.ERROR,"Unable to sense Acceleration",e);
				}
				
				try{
					Double ambientLight = 0.0+sensorLight.senseAmbientLight();
					lastScan.setAmbientLight(ambientLight);
					getLog().log(Level.DEBUG,sensorsSoFar + "/" + SensorNetAttribute.NUMBER_SENSED  + ":Ambient Light:"+ ambientLight);
				}
				catch(Exception e){
					getLog().log(Level.ERROR,"Unable to sense Ambient Light",e);
				}
				finally{
					sensorsSoFar++;
				}
				
				try{
					Double volume = sensorVolume.senseVolume();
					lastScan.setVolume(volume);
					getLog().log(Level.DEBUG,sensorsSoFar + "/" + SensorNetAttribute.NUMBER_SENSED  + ":Volume:"+ volume);
				}
				catch(Exception e){
					getLog().log(Level.ERROR,"Unable to sense Volume",e);
				}
				finally{
					sensorsSoFar++;
				}
				
				try{
					Double uIActivity = sensorUI.senseUIActivity();
					lastScan.setUIActivity(uIActivity);
					getLog().log(Level.DEBUG,sensorsSoFar + "/" + SensorNetAttribute.NUMBER_SENSED  + ":UI Activity:"+ uIActivity);
				}
				catch(Exception e){
					getLog().log(Level.ERROR,"Unable to sense UIActivity",e);
				}
				finally{
					sensorsSoFar++;
				}
			}
		};
		junkDrawerSensing.setDaemon(true);
		junkDrawerSensing.setName("Miscellaneous Sensing");
		junkDrawerSensing.start();
		
		
		try {
			networkSensors.join(5000);
			wifiSensors.join(5000);
			processSensors.join(5000);
			junkDrawerSensing.join(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
			getLog().log(Level.ERROR,"Something confusing happend:"+e);
		}
		
		getLog().info("Finished a sensor sweep in " + (double) (System.currentTimeMillis() - startTime) / 1000 + " seconds."); // DIAGNOSTICS
		
		Record ret = lastScan.clone();
		
		return(ret);
	}
	
	public static PowerSourceEnum sensePowerSource() {
		return sensorPowerSource.sensePowerSource();
	}

	private static String senseDisplayConfiguration() {
		int numDisplays;
		ArrayList<String> configurations = new ArrayList<String>(1);
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	    GraphicsDevice[] gs = ge.getScreenDevices();
		numDisplays = gs.length;
		for (int j = 0; j < numDisplays; j++) { 
		      GraphicsConfiguration[] gc = gs[j].getConfigurations();
		      /*Sometimes what java reports as the number of displays is not accurate*/
		      /*and an exception is thrown by the native code when you access the */
		      /*out of range display. */
		      for (int i=0; i < gc.length; i++){
		    	  try{
		    		  configurations.add("("+gc[i].getBounds().width+","+gc[i].getBounds().height+")");
		    	  }
		    	  catch(RuntimeException e){
		    		  getLog().warn("System is reporting "+gc.length+" screens, but screen "+i+" doesn't exist");
		    	  }
		      }
		   }
		
		// Sort (to help ensure that the same set of resolutions
		// will produce a string match in learning algorithm), and remove duplicates
		// because they're stinky-pants
		Collections.sort(configurations);
		HashSet<String> h = new HashSet<String>(configurations);
		configurations.clear();
		configurations.addAll(h);
		
		return(configurations.toString());
	}
	

	private static ListComparable<String> senseRunningProcesses() {
		try {
			return(sensorProcess.senseAllProcesses());
		} catch (Exception e) {
			getLog().log(Level.ERROR, "Could not determine the running processes. Using null.", e);
		}
		return null;
	}

	private static String senseActiveProcess() {
		try {
			return(sensorProcess.senseActiveProcess());
		} catch (Exception e) {
			getLog().log(Level.ERROR, "Could not determine the active process. Using null.", e);
		}
		return null;
	}

	private static MapComparable<Pair<String,String>,Integer> senseWifiMACAddressList() {
		if (sensorWiFi.sensingAvailable()) {
			MapComparable<Pair<String,String>,Integer> getMacs = null;
			try{
				getMacs = sensorWiFi.getAllAPMAC();
			}
			catch (Exception e){
				getLog().log(Level.WARN, "Failed to scan Wi-Fi data. Wifi is probably turned off", e);
			}
			return(getMacs);
		} else {
			return (null);
		}
	}
	
	private static String senseWifiSSID() {
		if (sensorWiFi.sensingAvailable()) {
			Pair<Pair<String,String>,Integer> ap = null;
			String x = null;
			
			try{
				ap = sensorWiFi.getAPMAC();
				x = ap.getFirst().getFirst();
			}
			catch(Exception e){
				getLog().log(Level.WARN, "Failed to scan Wi-Fi data. Wifi is probably turned off", e);
			}
			
			if((x == null)||(x.equals(""))){
				return(null);
			}
			else{
				return(x);
			}
		}
		else{
			return(null);
		}
	}
	
	static volatile Pair<Pair<String,String>,Integer> lastWIFIMAC = null;
	static boolean lastWIFIMACFirst = true;
	public static Pair<Pair<String,String>,Integer> senseWifiMACAddressConnected() {
		if (sensorWiFi.sensingAvailable()) {
			Pair<Pair<String, String>, Integer> ap = null;
			
			/* Probe for current WiFi MAC Address */
			try{
				ap = sensorWiFi.getAPMAC();
			}
			catch(Exception e){
				getLog().log(Level.WARN, "Failed to scan Wi-Fi data. Wifi is probably turned off", e);
			}
			
			/* For some reason you can get this string if the wifi is on but not connected*/
			if(ap != null){
				Pair<String, String> name = ap.getFirst();
				if(name == null){
					ap = null;
				}
				else{
					String mac = name.getSecond();
					if(mac.equals("44:44:44:44:44:44")){
						ap = null;
					}
				}
			}
			
			boolean changed = false;
			
			if((lastWIFIMAC != null) && (ap == null)){
				changed = true;
			}
			else if((lastWIFIMAC == null) && (ap != null)){
				changed = true;
			}
			else if((lastWIFIMAC != null) && (ap != null)){
				if(!lastWIFIMAC.equals(ap)){
					changed = true;
				}
			}
			
			if(changed && !lastWIFIMACFirst){
				//Do something when the Wifi connection changes
			}
			else{
				lastWIFIMACFirst = false;
			}
			
			lastWIFIMAC = ap;
			
			return(ap);
		}
		else{
			return(null);
		}
	}
	
	
	static String lastLocalIP = null;
	static boolean lastLocalIPFirst = true;
	private static String senseLocalIPAddress() {
		String currentIP = null;
		
		try {
			currentIP = InetAddress.getLocalHost().toString();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		boolean changed = false;
		if((lastLocalIP != null) && (currentIP == null)){
			changed = true;
		}
		else if((lastLocalIP == null) && (currentIP != null)){
			changed = true;
		}
		else if((lastLocalIP != null) && (currentIP != null)){
			if(!lastLocalIP.equals(currentIP)){
				changed = true;
			}
		}
			
		if(changed && !lastLocalIPFirst){
			//Do something when the ip address changes
		}
		else{
			lastLocalIPFirst = false;
		}
		
		lastLocalIP = currentIP;
		
		return(currentIP);
	}

	private static String senseDNS(String ipAddress) {
		String lookup = null;
		try {
			lookup = (ipAddress == null ? null : InetAddress.getByName(ipAddress).getHostName());
		} catch (Exception e) {
			getLog().log(Level.ERROR, "Couldn't get the IP and reverse lookup.", e);
		}
		return lookup;
	}

	static String lastRemoteIP = null;
	static int toNull = 0;
	static boolean lastRemoteIPFirst = true;
	/* Complicated change management:  If the remote IP address goes to null it may be problems with the remote site that's checking. 
	 * So we require five null reports before we believe that the remote IP is actually null */
	private static String senseRemoteIPAddress() {
		String currentIP = null;
		try{
			currentIP = sensorIP.senseIPAddress();
		}
		catch(Exception e){
			getLog().log(Level.WARN, "Failed to get remote IP address. Computer is probably offline.", e);
		}
		
		boolean changed = false;
		boolean changedToNull=false;
		
		if((lastRemoteIP != null) && (currentIP == null)){
			changed = true;
			changedToNull = true;
		}
		else if((lastRemoteIP == null) && (currentIP != null)){
			changed = true;
		}
		else if((lastRemoteIP != null) && (currentIP != null)){
			if(!lastRemoteIP.equals(currentIP)){
				changed = true;
			}
		}
			
		if(changed && !lastRemoteIPFirst){
			if(changedToNull){
				toNull++;
				if(toNull > 5){
					//Do something when remote IP has changed to Null
					toNull = 0;
					lastRemoteIP = currentIP;
					return(currentIP);
				}
				else{
					return(lastRemoteIP);
				}
			}
			else{
				//Do something when the remote IP has changed
				lastRemoteIP = currentIP;
				toNull = 0;
				return(currentIP);
			}
		}
		else{
			lastRemoteIPFirst = false;
			lastRemoteIP = currentIP;
			toNull = 0;
			return(currentIP);
		}
	}

	public static Integer senseMinutesSinceMidnight() {
		Calendar cal = Calendar.getInstance();
		return( (cal.get(Calendar.HOUR_OF_DAY) * 60) + cal.get(Calendar.MINUTE));
	}

	public static Integer senseDayOfTheWeek() {
		Calendar cal = Calendar.getInstance();
		return(cal.get(Calendar.DAY_OF_WEEK));
	}
	
	public static Long senseTimeStamp() {
		Date date = new Date();
		return(date.getTime());
	}

	public static void shutdown() {
		for(Quittable q:myCleanup){
			q.setQuitting(true);
		}
	}
}
