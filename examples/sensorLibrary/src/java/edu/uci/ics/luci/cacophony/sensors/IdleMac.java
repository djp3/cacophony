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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class IdleMac extends Idle {
	
    private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = LogManager.getLogger(IdleMac.class);
		}
		return log;
	}
	
	
	@Override
	protected String getNativeLibraryName() {
		return "idle";
	}

	@Override
	protected boolean initialize() {
		return true;
	}
	
	
	@Override
	protected void shutdown() {
		super.shutdown();
	}
	
    /* Returns microseconds ! */
    private static native synchronized long getIdleTimeNative();
    
    public Integer senseIdleTime(){
    	long test = getIdleTimeNative()/1000L;
    	if(test> Integer.MAX_VALUE){
    		return Integer.MAX_VALUE;
    	}
    	else{
    		return (int) test;
    	}
    }
    


}

