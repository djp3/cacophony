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
import java.util.Arrays;

import org.apache.log4j.Logger;

import edu.uci.ics.luci.utility.datastructure.ListComparable;

public class ProcessWindows extends Process{
	
    private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(ProcessWindows.class);
		}
		return log;
	}
	
	
	@Override
	protected String getNativeLibraryName() {
		return "processsensor_win32";
	}

	@Override
	protected boolean initialize() {
		return true;
	}
	
	@Override
	protected void shutdown() {
		super.shutdown();
	}
	
	private static native synchronized String sampleActiveProcess();
    private static native synchronized String[] sampleAllProcesses();


	@Override
	public String senseActiveProcess() {
    	return(sampleActiveProcess());
	}


	@Override
	public ListComparable<String> senseAllProcesses() {
		String[] sampleAllProcesses = sampleAllProcesses();
		ListComparable<String> asList = new ListComparable<String>(new ArrayList<String>());
		asList.addAll(Arrays.asList(sampleAllProcesses));
    	return(asList);
	}
    
    
}
