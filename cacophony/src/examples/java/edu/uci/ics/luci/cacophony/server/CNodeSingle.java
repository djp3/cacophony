package edu.uci.ics.luci.cacophony.server;

import java.util.Random;

import net.minidev.json.JSONObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.uci.ics.luci.cacophony.node.CNode;
import edu.uci.ics.luci.cacophony.node.CNodeConfiguration;
import edu.uci.ics.luci.cacophony.node.SensorConfig;
import edu.uci.ics.luci.cacophony.node.StorageException;
import edu.uci.ics.luci.cacophony.node.Translator;
import edu.uci.ics.luci.cacophony.node.TranslatorDouble;


public class CNodeSingle {
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = LogManager.getLogger(CNodeSingle.class);
		}
		return log;
	}

	static Random r = new Random();
	public static long makePositiveLong(){
		long ret;
		while((ret = r.nextLong())<0 );
		return ret;
	}
	
	public static String makeARandomP2PServerAddress(){
		return"edu.uci.ics.luci.cacophony.test."+makePositiveLong();
	}
	
	


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		/* This is our server that will be hosting our CNode */
		CNodeServer cNodeServer = new CNodeServer(makeARandomP2PServerAddress());
		cNodeServer.start();
		
		/* Manually create a Sensor configuration for parking spots in Seattle*/
		String id = "1";
		String name = "Pike Place Market Parking";
		String url = "http://www.seattle.gov/transportation/epark/mobile/default.htm";
		String format = "html";
		String pathExpression = "//*[@id=\"garageList\"]/div[1]/table/tbody/tr/td[3]/div/span"; 
		String regEx = "(.*)";
		Translator<?> translator = new TranslatorDouble();
		JSONObject translatorOptions = new JSONObject();
		SensorConfig target = new SensorConfig(id,
				name,
				url,
				format,
				regEx,
				pathExpression,
				translator,
				translatorOptions);
		
		
		/* Manually configure a CNode for the server */
		
		String cNodePath = "pike_place_parking";
		CNodeConfiguration config = new CNodeConfiguration(cNodePath,target);
		
		/*TODO: Finish this coding */
		
	}

	
	

}
