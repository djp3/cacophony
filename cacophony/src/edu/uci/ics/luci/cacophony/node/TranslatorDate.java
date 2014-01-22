package edu.uci.ics.luci.cacophony.node;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import weka.core.Attribute;

public class TranslatorDate implements Translator {
	final public String DATE_FORMAT = "yyyy-MM-dd";
	
	
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
