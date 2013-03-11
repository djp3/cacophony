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

public class UIActivityWindows extends UIActivity{
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
				log = Logger.getLogger(UIActivityWindows.class);
		}
		return log;
	}
		
		
	@Override
	protected String getNativeLibraryName() {
		return "uiactivity";
	}
	
	private static Object nativeLock = new Object();
	private static native double getAccumulatedMouseClicks();
	private static native void nativeInit();
	private static native void nativeShutdown();
	
	@Override
	protected boolean initialize() {
		synchronized(nativeLock){
			try{
				nativeInit();
				this.senseUIActivity(); /*The jni blocks until initialization is complete */
			}
			catch(RuntimeException e){
				return false;
			}
		}
		return true;
	}
		
		
	@Override
	protected void shutdown() {
		synchronized(nativeLock){
			super.shutdown();
			nativeShutdown();
		}
	}
	
	
	private long lastTimeSensed = System.currentTimeMillis();
	private double runningTotalClicks=1.0;
	private double runningTotalTime=1.0;
	
	@Override
	public Double senseUIActivity() {
		double x;
		synchronized(nativeLock){
			x = getAccumulatedMouseClicks();
		}
		long now = System.currentTimeMillis();
		double elapsed = (now-lastTimeSensed);
					
		/* Smooth the estimate */
		runningTotalClicks /= 2.0;
		runningTotalTime /= 2.0;
					
		/* Update the estimate*/
		runningTotalClicks += x;
		runningTotalTime += elapsed;
		lastTimeSensed = now;
		/*Mouse clicks per second*/
		return((runningTotalClicks*1000.0)/runningTotalTime);
	}
		
}
