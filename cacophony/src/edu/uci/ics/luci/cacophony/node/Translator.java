package edu.uci.ics.luci.cacophony.node;

import net.minidev.json.JSONObject;

public abstract class Translator {
	
	/**
	 * This is called after the object is instantiated to set parameters that might be necessary
	 * @param jo
	 */
	abstract void initialize(JSONObject jo);
	
	/**
	 * This is called to translate some text from a webpage into a weka type
	 * @param input
	 * @return
	 */
	abstract Object translateToWeka(String input);
}
