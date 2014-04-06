package edu.uci.ics.luci.cacophony.node;

import weka.core.Attribute;

public class TranslatorString implements Translator {
	@Override
	public WekaAttributeTypeValuePair translate(String s) {
		if (s == null) {
			return null;
		}
		
		return new WekaAttributeTypeValuePair(Attribute.STRING, s);
	}
}
