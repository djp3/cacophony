package edu.uci.ics.luci.cacophony.node;

import net.minidev.json.JSONObject;

public class TranslatorString implements Translator<String> {
	

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
		if (!(obj instanceof TranslatorString))
			return false;
		return true;
	}
	
	
	public void initialize(JSONObject jo) {
	}
	
	public String translate(String s) {
		if (s == null) {
			return null;
		}
		return s;
	}
	
	
}
