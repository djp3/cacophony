package edu.uci.ics.luci.cacophony.node;

import net.minidev.json.JSONObject;

public abstract class Translator {	
	
	/**
	 * 
	 * @param jo JSON options to initialize the translator
	 */
	abstract void initialize(JSONObject jo);
	
	/**
	 * 
	 * @param s
	 * @return the value
	 */
	abstract WekaAttributeTypeValuePair translate(String s);

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Translator))
			return false;
		return true;
	}
	
	
}

