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
	
	/** This is to signal that equals needs to be overriden if there is any state **/
	public int hashCode();
	public boolean equals(Object obj);
}

