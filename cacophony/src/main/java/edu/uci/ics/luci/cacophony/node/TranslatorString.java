package edu.uci.ics.luci.cacophony.node;

import net.minidev.json.JSONObject;

public class TranslatorString implements Translator<String> {
	
	@Override
	public void initialize(JSONObject jo) {
	}
	
	@Override
	public String translate(String s) {
		if (s == null) {
			return null;
		}
		return s;
	}
	
	
}
