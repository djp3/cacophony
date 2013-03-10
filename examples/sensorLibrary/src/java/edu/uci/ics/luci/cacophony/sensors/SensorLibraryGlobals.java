package edu.uci.ics.luci.cacophony.sensors;

import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.configuration.XMLPropertiesConfiguration;
import org.apache.log4j.Logger;

import edu.uci.ics.luci.utility.CalendarCache;
import edu.uci.ics.luci.utility.Globals;


public class SensorLibraryGlobals extends Globals{
	
	private static final String PROPERTY_FILENAME_DEFAULT = "sensorLibrary.log4j.properties";
	public static final String CONFIG_FILENAME_DEFAULT = "sensorLibrary.properties";
	public static final int DEFAULT_PORT = 2011;
	private static transient volatile Logger log = null;
		
	static{
		/* Test that we are using GMT as the default */
		if(!TimeZone.getDefault().equals(CalendarCache.TZ_GMT)){
			throw new RuntimeException("We are in the wrong timezone:\n"+TimeZone.getDefault()+"\n We want to be in:\n "+CalendarCache.TZ_GMT);
		}
			
		/* Test that we are using UTF-8 as default */
		String c = java.nio.charset.Charset.defaultCharset().name();
		if(!c.equals("UTF-8")){
			throw new IllegalArgumentException("The character set is not UTF-8:"+c);
		}
		
		
	}
	
	public static synchronized Logger getLog(){
		if(log == null){
			log = Logger.getLogger(SensorLibraryGlobals.class);
		}
		return log;
	}
	
	private XMLPropertiesConfiguration config = null;
	
	/**
	 * This is only need so far for testing in which the _globals sticks around from previous tests and is not reinitialized
	 * @return
	 */
	public static void resetGlobals(){
		setGlobals(null);
	}
	
	public void setConfig(XMLPropertiesConfiguration config){
		this.config = config;
	}
	
	public XMLPropertiesConfiguration getConfig(){
		return (this.config);
	}
		
	public List<String> getBadGuyList(){
		return(Arrays.asList(
				"ns2.bomfim.com.br",
				"dcortes.net",
				"59-124-107-247.hinet-ip.hinet.net",
				"59.108.230.218",
				"59-124-107-247.hinet-ip.hinet.net",
				"202.153.121.205",
				"190.208.29.147",
				"94.75.206.156",
				"loft2552.serverloft.com",
				"118.102.25.161",
				"202.117.3.30",
				"59.108.230.218",
				"80.84.236.230",
				"190.144.35.98",
				"200-168-188-236.copercana.com.br",
				"202.153.121.205",
				"203.125.118.252",
				"67.223.227.141",
				"tdh.spree.de",
				"189.103.205.71",
				"190.144.35.98",
				"201.38.252.182",
				"67.223.227.141",
				"tdh.spree.de",
				"80.84.236.230",
				"host131.200-45-174.telecom.net.ar"));
	}
	
	public String getSystemVersion() {
		return GitRevision.SYSTEM_REVISION;
	}
	

	public SensorLibraryGlobals() {
		super(PROPERTY_FILENAME_DEFAULT);
		super.setPropertyFileName(PROPERTY_FILENAME_DEFAULT);
		super.reloadLog4jProperties();
	}
}

