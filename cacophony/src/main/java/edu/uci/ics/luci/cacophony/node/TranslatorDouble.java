package edu.uci.ics.luci.cacophony.node;

import net.minidev.json.JSONObject;

public class TranslatorDouble implements Translator<Number> {
	

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
		if (!(obj instanceof TranslatorDouble))
			return false;
		return true;
	}
	
	
	public void initialize(JSONObject jo) {
	}
	
	public Double translate(String s) {
		if (s == null) {
			return null;
		}
		
		s = s.replace(",", ""); // removing thousands separators
		try {
			return Double.parseDouble(s);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}
}
