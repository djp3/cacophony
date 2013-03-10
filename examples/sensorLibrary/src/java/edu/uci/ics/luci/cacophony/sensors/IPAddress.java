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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.uci.ics.luci.utility.Globals;

public class IPAddress extends Abstract {
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
			if(log == null){
					log = Logger.getLogger(IPAddress.class);
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
	private static IPAddress sensor = null;
	/*********************************************************/
	
	/** use getSensor instead of constructor **/
	private IPAddress(){
		super();
	}
	
	/**
	 * Factory method for instantiating the OS specific sensor
	 * Shutdown is handled via Globals.setQuitting() method
	 */
	public static IPAddress getSensor(){
		synchronized (sensorLock){
			if(sensor == null){
				switch(os){
					case MAC_OS_X: 
					case WINDOWS_VISTA:
					case WINDOWS_XP:
					default:
						sensor = new IPAddress();
				}
				Globals.getGlobals().addQuittables(sensor);
			}
			return sensor;
		}
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
	
	
	@Override
	protected String getNativeLibraryName() {
		return null;
	}

	@Override
	protected boolean initialize() {
		return true;
	}
	
	@Override
	public synchronized String sense(){
		return senseIPAddress();
	}
	

	private static final int READ_TIMEOUT = 400;
	private static final int CONNECT_TIMEOUT = 300;
	private static final long BUFFER_TIME = 30000;
	private static long lastGetStartTime = Long.MIN_VALUE;
	private static long lastGetEndTime = Long.MIN_VALUE;
	private static String lastIp = null;

	
	public synchronized String senseIPAddress() {
		
		if (lastGetEndTime > (System.currentTimeMillis() - BUFFER_TIME)) {
			return getLastIp();
		}
		
		try {
			long start,end;
			
			start = System.currentTimeMillis();
			setLastGetStartTime(start);
			end = getLastGetEndTime();
				
			getLog().debug("Asking dyndns.org for IP");
				
			URL url = new URL("http://checkip.dyndns.org/");
			URLConnection c = url.openConnection();
			c.setConnectTimeout(CONNECT_TIMEOUT);
				
			if(c.getConnectTimeout() != CONNECT_TIMEOUT){
				getLog().error("Connect Timeout is not getting set");
			}
				
			c.setReadTimeout(READ_TIMEOUT);
			if(c.getReadTimeout() != READ_TIMEOUT){
				getLog().error("Read Timeout is not getting set");
			}
				
			InputStream s = (InputStream) (c.getContent());
			Scanner scanner = new Scanner(new InputStreamReader(s));
			String ip = scanner.nextLine();
				
			scanner.close();
			s.close();
				
			Pattern p = Pattern.compile(".*Current IP Address: (\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}).*");
			Matcher m = p.matcher(ip);
			
			long newEnd = System.currentTimeMillis();
			
			if (m.matches()) {
				String parsedIP = m.group(1);
				if(getLastGetStartTime() != start){
					if(getLastGetEndTime() != end){
						return getLastIp();
					}
					else{
						setLastGetEndTime(newEnd);
						setLastIP(parsedIP);
						return parsedIP;
					}
				}
				else{
					setLastGetEndTime(newEnd);
					setLastIP(parsedIP);
					return parsedIP;
				}
			}
			else {
				throw new RuntimeException("dyndns.org returned: "+ip);
			}
		} catch (MalformedURLException e) {
			getLog().log(Level.WARN, "Couldn't get IP, so we're using: "+getLastIp(), e);
			return getLastIp();
		}
		catch (IOException e) {
			getLog().log(Level.WARN, "Couldn't get IP, so we're using: "+getLastIp(), e);
			return getLastIp();
		} catch (RuntimeException e) {
			getLog().log(Level.WARN, "Couldn't get IP, so we're using: "+getLastIp(), e);
			return getLastIp();
		}
	}
	
	private synchronized static void setLastIP(String p) {
		lastIp = p;
	}
	
	private synchronized static void setLastGetStartTime(long c) {
		lastGetStartTime = c;
	}
	
	private synchronized static void setLastGetEndTime(long c) {
		lastGetEndTime = c;
	}
	
	private synchronized static long getLastGetStartTime() {
		return lastGetStartTime;
	}
	
	private synchronized static long getLastGetEndTime() {
		return lastGetEndTime;
	}
	
	private synchronized static String getLastIp() {
		return lastIp;
	}


	
}
