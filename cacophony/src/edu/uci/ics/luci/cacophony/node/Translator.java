package edu.uci.ics.luci.cacophony.node;

public interface Translator {	
	/**
	 * 
	 * @param s
	 * @return the value
	 */
	abstract WekaAttributeTypeValuePair translate(String s);
}

