package edu.uci.ics.luci.cacophony.node;

import net.minidev.json.JSONObject;
import weka.core.Attribute;

public class TranslatorNumeric extends Translator {
	
	@Override
	public void initialize(JSONObject jo) {
	}
	

	@Override
	public WekaAttributeTypeValuePair translate(String s) {
		if (s == null) {
			return null;
		}
		
		s = s.replace(",", ""); // removing thousands separators
		double value;
		try {
			value = Double.parseDouble(s);
		}
		catch (NumberFormatException e) {
			return null;
		}
		return new WekaAttributeTypeValuePair(Attribute.NUMERIC, value);
	}
	

}
