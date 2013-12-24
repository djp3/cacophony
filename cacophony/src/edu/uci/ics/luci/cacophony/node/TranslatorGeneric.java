package edu.uci.ics.luci.cacophony.node;

import net.minidev.json.JSONObject;

/** This intentionally does nothing.  It's mostly for testing */
public class TranslatorGeneric extends Translator {

	@Override
	void initialize(JSONObject jo) {
	}

	@Override
	Object translateToWeka(String input) {
		return null;
	}

}
