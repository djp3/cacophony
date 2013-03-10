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

public class LightWindows extends Light {
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(IdleWindows.class);
		}
		return log;
	}
	
	@Override
	protected String getNativeLibraryName() {
		return "light";
	}

	@Override
	protected boolean initialize() {
		return true;
	}
	
	
	@Override
	protected void shutdown() {
		super.shutdown();
	}
	
    // Native function [left][right]
    private static native synchronized long[] readLight();
    
    public long[] getLightArray(){
    	return(readLight());
    }
    
    Integer senseAmbientLight(){
    	long[] readLight = readLight();
    	long tally = 0;
    	for(int i=0;i< readLight.length;i++){
    		tally += readLight[i];
    	}
    	if(readLight.length > 0){
    		return (int) (tally/readLight.length);
    	}
    	else{
    		return null;
    	}
    }
    
	List<Integer> senseAmbientLightBoth(){
		List<Integer> ret = new ArrayList<Integer>();
    	long[] readLight = readLight();
    	for(int i=0;i< readLight.length;i++){
    		ret.add((int) readLight[i]);
    	}
    	return(ret);
	}

}

