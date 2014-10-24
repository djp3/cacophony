package edu.uci.ics.luci.cacophony.node;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.minidev.json.JSONObject;

public class TranslatorDate implements Translator<Date> {
	final public static String DATE_FORMAT = "yyyy-MM-dd";
	

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
		if (!(obj instanceof TranslatorDate))
			return false;
		return true;
	}

	
	public void initialize(JSONObject jo) {
	}
	
	public Date translate(String s) {
		if (s == null) {
			return null;
		}
		
		try {
			return new SimpleDateFormat(DATE_FORMAT).parse(s);
		}
		catch (ParseException e) {
			return null;
		}
	}

}
