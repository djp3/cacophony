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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.uci.ics.luci.utility.Globals;

public class Volume extends Abstract {

	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = LogManager.getLogger(Volume.class);
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
	private static Volume sensor = null;
	/*********************************************************/
	
	/** use getSensor instead of constructor **/
	protected Volume(){
		super();
	}
	
	/**
	 * Factory method for instantiating the OS specific sensor
	 * Shutdown is handled via Globals.setQuitting() method
	 */
	public static Volume getSensor(){
		synchronized (sensorLock){
			if(sensor == null){
				switch(os){
					case MAC_OS_X: 
					case WINDOWS_VISTA:
					case WINDOWS_XP:
					default:
						sensor = new Volume();
				}
				Globals.getGlobals().addQuittable(sensor);
			}
			return sensor;
		}
	}
	
	@Override
	public synchronized Double sense(){
		return senseVolume(); 
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

	public synchronized Double senseVolume() {
		if(Environment.getInstance().getOSType().equals(Environment.OSTypes.MAC_OS_X)){
			java.lang.Process result;
			BufferedReader err = null;
			BufferedReader out = null;
			StringBuffer output = new StringBuffer();
			try {
				result = Runtime.getRuntime().exec(
						new String[] { "/usr/bin/osascript", "-e", "get volume settings" });
				result.waitFor();

				String line;

				if (result.exitValue() != 0) {
					err = new BufferedReader( new InputStreamReader(result.getErrorStream()));
					while ((line = err.readLine()) != null) {
						output.append(line + "\n");
					}
					// TODO: This is NOT an IOException
					throw new IOException("Failed to get the volume:\n" + output.toString().trim());
				} else {
					out = new BufferedReader( new InputStreamReader(result.getInputStream()));
					while ((line = out.readLine()) != null) {
						output.append(line + "\n");
					}
				}
			} catch (IOException e) {
				getLog().error("Unable to run AppleScript to sense volume:" + e);
				return (null);
			} catch (InterruptedException e) {
				getLog().warn("Interrupted while running AppleScript to sense volume:" + e);
				return (null);
			}
			finally{
				try{
					if(err != null){
						err.close();
					}
				} catch (IOException e) {
				}
				finally{
					err = null;
					try{
						if(out != null){
							out.close();
						}
					} catch (IOException e) {
					}
					finally{
						out = null;
					}
				}
			}

			String[] s = output.toString().trim().split(" *[:,]+ *");
			int v = 100;
			boolean mute = false;
			for (int i = 0; i < s.length; i++) {
				if (s[i].equals("output volume")) {
					if ((i + 1) < s.length) {
						i++;
						v = Integer.parseInt(s[i]);
					}
				} else if (s[i].equals("output muted")) {
					if ((i + 1) < s.length) {
						i++;
						if (s[i].equals("false")) {
							mute = false;
						} else if (s[i].equals("true")) {
							mute = true;
						}
					}
				}
				// else input volume, alert volume are ignored
			}
			if (mute) {
				return (Double.valueOf(0.0d));
			} else {
				return (Double.valueOf(0.0d + v));
			}
		} else {
			// no sensors for other OSs have been implemented yet
			return null;
		}

	}

}
