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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PowerSourceMac extends PowerSource{
	
    private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = LogManager.getLogger(PowerSourceMac.class);
		}
		return log;
	}
	
	@Override
	protected String getNativeLibraryName() {
		return null;
	}

	@Override
	protected boolean initialize() {
		return true;
	}
	
	@Override
	protected void shutdown() {
		super.shutdown();
	}
    
	@Override 
    public PowerSourceEnum sensePowerSource() {
		
		Runtime runtime = Runtime.getRuntime();
		java.lang.Process result;
		try {
			result = runtime.exec(new String[] { "system_profiler", "SPPowerDataType" });
		} catch (IOException e) {
			getLog().log(Level.ERROR,"Unable to run a script to detect Mac PowerSource",e);
			return null;
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(result.getInputStream()));
		String s = null;

		try {
			while((s = in.readLine()) != null){
				if(s.contains("AC Charger Information")){ // found beginning of AC Power Information
					while((s = in.readLine()) != null){
						if(s.contains("Connected")){ // line should be "Connected: Yes/No"
							if(s.contains("Yes")){
								return PowerSourceEnum.WALL;
							}
							else{
								return PowerSourceEnum.BATTERY;
							}
						}
					}
				}
			}
		} catch (IOException e) {
			getLog().log(Level.ERROR,"Unable to stream info todetect Mac PowerSource",e);
			try {
				in.close();
			} catch (IOException e1) {
				getLog().log(Level.ERROR,"Unable to close stream",e1);
			}
			return null;
		}
		finally{
			try {
				in.close();
			} catch (IOException e1) {
				getLog().log(Level.ERROR,"Unable to close stream",e1);
			}
		}
		return null;
	}

}
