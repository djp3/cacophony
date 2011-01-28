package edu.uci.ics.luci.cacophony;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class Visualization {

	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(Visualization.class);
		}
		return log;
	}
		
		

	public static byte[] getWebsite(boolean testing) {
		
		StringBuilder contents = new StringBuilder();
			
		try {
			String aFile;
			if(testing){
				aFile = "../viz/graphicsStatsRaw.html";
			}
			else{
				aFile = "viz/graphicsStatsRaw.html";
			}
			

			//use buffering, reading one line at a time
			//FileReader always assumes default encoding is OK!
			BufferedReader input =  new BufferedReader(new FileReader(aFile));
			try {
				String line = null; //not declared within while loop
				/*
				 * readLine is a bit quirky :
				 * it returns the content of a line MINUS the newline.
				 * it returns null only for the END of the stream.
				 * it returns an empty String if two newlines appear in a row.
				 */
				while (( line = input.readLine()) != null){
					contents.append(line);
					contents.append(System.getProperty("line.separator"));
				}
			}
			finally {
				input.close();
			}
		}
		catch (IOException ex){
			getLog().error(ex);
		}
		
		StringBuilder lineGraph = new StringBuilder();
			
		Pattern p = Pattern.compile("<\\$lineGraph\\$>");
		Matcher m = p.matcher(contents); // get a matcher object
		while(m.find()) {
			contents.replace(m.start(), m.end(), lineGraph.toString());
			m.reset();
		}
		return contents.toString().getBytes();
	}

}
