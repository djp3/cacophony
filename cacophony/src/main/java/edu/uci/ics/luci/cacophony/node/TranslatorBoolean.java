package edu.uci.ics.luci.cacophony.node;

import net.minidev.json.JSONObject;

public class TranslatorBoolean implements Translator<Boolean> {
	

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
		if (!(obj instanceof TranslatorBoolean))
			return false;
		return true;
	}

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
