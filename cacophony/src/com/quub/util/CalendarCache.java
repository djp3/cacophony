package com.quub.util;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class CalendarCache {
	public transient static TimeZone TZ_LosAngeles = TimeZone.getTimeZone("America/Los_Angeles");
	public transient static Calendar C_LosAngeles = Calendar.getInstance(TZ_LosAngeles);
	
	public transient static TimeZone TZ_GMT = TimeZone.getTimeZone("GMT");
	public transient static Calendar C_GMT = Calendar.getInstance(TZ_GMT);
	
	private transient static String defaultTimeZoneS = TZ_LosAngeles.getID();
	private transient static TimeZone defaultTimeZoneTZ = TZ_LosAngeles;
	
	private transient static Map<String,Calendar> cache = new HashMap<String,Calendar>();
	
	public static CalendarCache calendarCache = new CalendarCache();
	
	public CalendarCache(){
	}
	
	public CalendarCache(String s){
		if(s == null){
			throw new IllegalArgumentException("Could not resolve string:"+s+" to a TimeZone");
		}
		
		boolean match = false;
		for(String ctz:TimeZone.getAvailableIDs()){
			if(s.equals(ctz)){
				match = true;
			}
		}
		
		if(!match){
			throw new IllegalArgumentException("Could not resolve string:"+s+" to a TimeZone");
		}
		
		TimeZone n = TimeZone.getTimeZone(s);
		
		defaultTimeZoneTZ = n;
		defaultTimeZoneS = s;
	}
	
	public CalendarCache(TimeZone tz){
		if(tz == null){
			throw new IllegalArgumentException("Could not resolve TimeZone:"+tz+" to a String");
		}
		
		String n = tz.getID();
		
		if(n == null){
			throw new IllegalArgumentException("Could not resolve TimeZone:"+tz+" to a String");
		}
		
		defaultTimeZoneS = n;
		defaultTimeZoneTZ = tz;
	}
	
	public void clear(){
		cache.clear();
	}

	public Calendar getCalendar(String tz) {
		if((tz == null)||(tz.length()==0)){
			tz = defaultTimeZoneS;
		}
		
		/* Get a calendar. Check cache first */
		Calendar c = cache.get(tz);
		if(c == null){
			TimeZone usersTimeZone = TimeZone.getTimeZone(tz);
			c = Calendar.getInstance(usersTimeZone);
			cache.put(tz,(Calendar) c.clone());
		}
		return c;
	}
	
	public Calendar getCalendar(TimeZone usersTimeZone) {
		
		if(usersTimeZone == null){
			usersTimeZone = defaultTimeZoneTZ;
		}
		
		/* Get a calendar. Check cache first */
		String tz = usersTimeZone.getID();
		Calendar c = cache.get(tz);
		if(c == null){
			c = Calendar.getInstance(usersTimeZone);
			cache.put(tz,(Calendar) c.clone());
		}
		return c;
	}

}
