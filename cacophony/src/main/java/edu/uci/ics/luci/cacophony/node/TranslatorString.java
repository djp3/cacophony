package edu.uci.ics.luci.cacophony.node;

import net.minidev.json.JSONObject;
import weka.core.Attribute;

public class TranslatorString extends Translator {
	
	@Override
	public void initialize(JSONObject jo) {
	}
	
	@Override
	public WekaAttributeTypeValuePair translate(String s) {
		if (s == null) {
			return null;
		}
		
		return new WekaAttributeTypeValuePair(Attribute.STRING, s);
	}
	
	
}
