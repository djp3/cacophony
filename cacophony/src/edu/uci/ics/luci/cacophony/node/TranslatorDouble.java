package edu.uci.ics.luci.cacophony.node;

import net.minidev.json.JSONObject;

public class TranslatorDouble implements Translator<Number> {
	
	@Override
	public void initialize(JSONObject jo) {
	}
	
	@Override
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
