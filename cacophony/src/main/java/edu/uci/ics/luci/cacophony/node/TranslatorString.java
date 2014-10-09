package edu.uci.ics.luci.cacophony.node;

import net.minidev.json.JSONObject;

public class TranslatorString implements Translator<String> {
	
	public void initialize(JSONObject jo) {
	}
	
	public String translate(String s) {
		if (s == null) {
			return null;
		}
		return s;
	}
	
	
}
