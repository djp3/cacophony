package edu.uci.ics.luci.cacophony.node;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import net.minidev.json.JSONObject;
import weka.core.Attribute;

public class TranslatorDate extends Translator {
	final public static String DATE_FORMAT = "yyyy-MM-dd";
	
	
	@Override
	public void initialize(JSONObject jo) {
	}
	
	@Override
	public WekaAttributeTypeValuePair translate(String s) {
		if (s == null) {
			return null;
		}
		
		try {
			new SimpleDateFormat(DATE_FORMAT).parse(s);
		}
		catch (ParseException e) {
			return null;
		}
		
		return new WekaAttributeTypeValuePair(Attribute.DATE, s);
	}

}
