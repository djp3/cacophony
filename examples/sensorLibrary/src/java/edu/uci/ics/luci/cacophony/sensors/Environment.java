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

import java.io.File;
import java.util.Random;
import java.util.prefs.Preferences;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.uci.ics.luci.utility.Globals;


public class Environment {
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(Environment.class);
		}
		return log;
	}
	
	public static transient volatile Environment instance = new Environment();
	
	public static synchronized Environment getInstance(){
		return instance;
	}
	
	public enum BuildTypes {
		RESEARCH,
		PRODUCTION};
		
	public enum OSTypes {
		MAC_OS_X,
		LINUX,
		WINDOWS_XP,
		WINDOWS_VISTA,
		UNKNOWN};

	protected int MAJOR_VERSION = -1;
	protected int MINOR_VERSION = -1;
	protected int REVISION = -1;
		
	protected BuildTypes buildType = null;

	protected OSTypes oSType = null;


	protected String baseDirectory=null;
	protected String sQLFilePath = null;
	protected Preferences prefs = null;

	public Environment(){
		PropertyConfigurator.configure(Globals.getGlobals().getPropertyFileName()); //"sensorLibrary.log4j.properties");
		
		try {
			String s = "working directory"+Environment.class.getResource("/").getPath();
		    getLog().debug(s);
		}
		catch (Exception e) {
			getLog().log(Level.FATAL,"Problem getting working directory",e);
		}

		
		prefs = Preferences.userNodeForPackage(Environment.class);
		
		String x = System.getProperty("app.version.major");
		if(x == null){
			MAJOR_VERSION = -1;
		}
		else{
			MAJOR_VERSION = Integer.parseInt(System.getProperty("app.version.major"));
		}
		x = System.getProperty("app.version.minor");
		if(x == null){
			MINOR_VERSION = -1;
		}
		else{
			MINOR_VERSION = Integer.parseInt(System.getProperty("app.version.minor"));
		}
		
		x = System.getProperty("app.version.revision");
		if(x == null){
			REVISION = -1;
		}
		else{
			REVISION = Integer.parseInt(System.getProperty("app.version.revision"));
		}
		
		oSType = this.discoverOSType(System.getProperty("os.name"));
		
		buildType = this.discoverBuildType(System.getProperty("build.type"));
		
		/* Create the junk drawer directory if it doesn't already exist */
		if (!(new File(System.getProperty("user.home") + File.separator+".cacophony").exists())) {
			String s = System.getProperty("user.home") + File.separator+".cacophony";
			File f = new File(s);
			if(f.mkdir()){
				getLog().info("Able to make directory:"+s);
			}
			else{
				getLog().warn("Not able to make directory:"+s);
			}
		}

		if (new File(System.getProperty("user.home") + File.separator+".cacophony") .exists()) {
			baseDirectory = System.getProperty("user.home") + File.separator+".cacophony";
			setSQLFilePath(baseDirectory + File.separator+"nomatic.data");
		}
	}
	
	public int getVersionMajor() {
		return MAJOR_VERSION;
	}

	protected void setVersionMajor(int major_version) {
		MAJOR_VERSION = major_version;
	}

	public int getVersionMinor() {
		return MINOR_VERSION;
	}

	protected void setVersionMinor(int minor_version) {
		MINOR_VERSION = minor_version;
	}

	public int getVersionRevision() {
		return REVISION;
	}

	protected void setVersionRevision(int revision) {
		REVISION = revision;
	}

	public String getVersionString() {
		return "" + getVersionMajor() + "." + getVersionMinor() + "." + getVersionRevision();
	}

	protected OSTypes discoverOSType(String OS){
		if(OS.equalsIgnoreCase("Mac OS X")){
			return(OSTypes.MAC_OS_X);
		}
		else if(OS.equalsIgnoreCase("Windows XP")){
			return(OSTypes.WINDOWS_XP);
		}
		else if(OS.equalsIgnoreCase("Windows Vista")){
			return(OSTypes.WINDOWS_VISTA);
		}
		else if(OS.equalsIgnoreCase("Linux")){
			return(OSTypes.LINUX);
		}
		else{
			System.out.println("Can't figure OS CheckBoxType:"+OS+" in NomaticService.java");
			return(OSTypes.UNKNOWN);
		}
	}
	
	protected void setOSType(OSTypes type) {
		oSType = type;
	}
	
	public OSTypes getOSType() {
		return oSType;
	}

	public String getOSStringLong() {
		return oSType.toString()+"/"+System.getProperty("os.arch")+"/"+System.getProperty("os.version");
	}
	
	protected void setBaseDirectory(String baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

	public String getBaseDirectory(){
		return baseDirectory;
	}

	protected BuildTypes discoverBuildType(String buildType) {
		
		if(buildType == null){
			getLog().debug("build.type is null");
			return (BuildTypes.RESEARCH);
		}
		else if(buildType.equals("RESEARCH")){
			getLog().debug("build.type is RESEARCH");
			return(BuildTypes.RESEARCH);
		}
		else if(buildType.equals("PRODUCTION")){
			getLog().debug("build.type is PRODUCTION");
			return(BuildTypes.PRODUCTION);
		}
		else{
			getLog().error("build.type is undetected defaulting to RESEARCH:"+System.getProperty("build.type"));
			return(BuildTypes.RESEARCH);
		}
	}
	
	public BuildTypes getBuildType() {
		return buildType;
	}

	protected void setBuildType(BuildTypes buildType) {
		this.buildType = buildType;
	}

	public String getGitRevision() {
		return GitRevision.SYSTEM_REVISION;
	}

	protected void setSQLFilePath(String sQLFilePath) {
		this.sQLFilePath = sQLFilePath;
	}

	public String getSQLFilePath() {
		return sQLFilePath;
	}

	public int getUserID() {
		if (prefs.get("user_id", "nothing").equals("nothing"))
			prefs.putInt("user_id", new Random().nextInt(Integer.MAX_VALUE));
		return prefs.getInt("user_id", -1);
	}
	
}
