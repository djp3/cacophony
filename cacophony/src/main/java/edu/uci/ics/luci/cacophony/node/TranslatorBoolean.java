package edu.uci.ics.luci.cacophony.node;

import net.minidev.json.JSONObject;

public class TranslatorBoolean implements Translator<Boolean> {

	public void initialize(JSONObject jo) {
	}
	
	public Boolean translate(String s) {
		if (s == null) {
			return null;
		}

		if (s.toLowerCase().trim().equals("false")) {
			return false;
		}
		else if (s.toLowerCase().trim().equals("true")) {
			return true;
		}
		else {
			return null;
		}
	}

}
