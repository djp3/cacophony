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
import java.util.List;

import org.apache.log4j.Logger;

import edu.uci.ics.luci.utility.Globals;

public abstract class Accelerometer extends Abstract {

	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
				log = Logger.getLogger(Accelerometer.class);
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
	private static Accelerometer sensor = null;
	/*********************************************************/
	
	/** use getSensor instead of constructor **/
	protected Accelerometer(){
		super();
	}
	
	/**
	 * Factory method for instantiating the OS specific sensor
	 * Shutdown is handled via Globals.setQuitting() method
	 */
	public static Accelerometer getSensor(){
		synchronized (sensorLock){
			if(sensor == null){
				switch(os){
					case MAC_OS_X: 
						sensor = new AccelerometerMac();
						break;
					case WINDOWS_VISTA:
					case WINDOWS_XP:
						sensor = new AccelerometerWindows();
						break;
					default:break;
				}
				Globals.getGlobals().addQuittables(sensor);
			}
			return sensor;
		}
	}
	
	@Override
	public synchronized List<Double> sense(){
		List<Double> x = senseAccelerometer();
		getLog().debug("Sensing accelerometer: "+x.toString());
		return x;
		//return senseAccelerometer(); 
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
	
	public abstract List<Double> senseAccelerometer();
	
	
	public void main(String args[]) {
		Accelerometer s = Accelerometer.getSensor();
		
		for (int i = 0; i < 100; i++) {
			List<Double> answer = s.senseAccelerometer();
			if((answer != null) && (answer.size() == 3)){
				System.out.println(answer.get(0) + ":" + answer.get(1) + ":" + answer.get(2));
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}


}
