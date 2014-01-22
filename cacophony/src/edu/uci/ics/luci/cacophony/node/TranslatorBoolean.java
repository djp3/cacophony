package edu.uci.ics.luci.cacophony.node;

import weka.core.Attribute;

public class TranslatorBoolean implements Translator {

	@Override
	public WekaAttributeTypeValuePair translate(String s) {
		if (s == null) {
			return null;
		}

		double value;
		if (s.toLowerCase().equals("false")) {
			value = 0;
		}
		else if (s.toLowerCase().equals("true")) {
			value = 1;
		}
		else {
			return null;
		}
		
		return new WekaAttributeTypeValuePair(Attribute.NUMERIC, value);
	}
}
