package edu.uci.ics.luci.cacophony.node;

import net.minidev.json.JSONObject;

public interface Translator<T> {	

	/**
	 * 
	 * @param jo JSON options to initialize the translator
	 */
	void initialize(JSONObject jo);
	
	/**
	 * 
	 * @param s
	 * @return the value
	 */
	T translate(String s);
}

