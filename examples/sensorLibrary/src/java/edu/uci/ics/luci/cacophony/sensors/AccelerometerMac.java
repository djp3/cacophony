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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class AccelerometerMac extends Accelerometer{
	
    private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(AccelerometerMac.class);
		}
		return log;
	}
	
	
	@Override
	protected synchronized String getNativeLibraryName() {
		return "accelerometer";
	}

	@Override
	protected synchronized boolean initialize() {
		return true;
	}
	
	@Override
	protected synchronized void shutdown() {
		super.shutdown();
	}
	
	// Native function [X][Y][Z]
    private static native synchronized int[] readSMS();
    
	@Override
	public synchronized List<Double> senseAccelerometer() {
		int[] a = readSMS();
		if((a!= null)&&(a.length >= 3)){
			List<Double> ret = new ArrayList<Double>(3);
			ret.add(Double.valueOf(a[0]));
			ret.add(Double.valueOf(a[1]));
			ret.add(Double.valueOf(a[2]));
			return(ret);
		}
		else{
			return(null);
		}
	}



}
