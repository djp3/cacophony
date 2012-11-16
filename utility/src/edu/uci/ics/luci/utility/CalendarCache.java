package edu.uci.ics.luci.utility;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class CalendarCache {
	public transient static final TimeZone TZ_LosAngeles = TimeZone.getTimeZone("America/Los_Angeles");
	public transient static final Calendar C_LosAngeles = Calendar.getInstance(TZ_LosAngeles);
	
	public transient static final TimeZone TZ_GMT = TimeZone.getTimeZone("GMT");
	public transient static final Calendar C_GMT = Calendar.getInstance(TZ_GMT);
	
	private transient static String defaultTimeZoneS = TZ_LosAngeles.getID();
	private transient static TimeZone defaultTimeZoneTZ = TZ_LosAngeles;
	
	private transient static Map<String,Calendar> cache = new HashMap<String,Calendar>();
	
	public static final CalendarCache calendarCache = new CalendarCache();
	
	public static synchronized String getDefaultTimeZoneS() {
		return defaultTimeZoneS;
	}

	public static synchronized void setDefaultTimeZoneS(String defaultTimeZoneS) {
		CalendarCache.defaultTimeZoneS = defaultTimeZoneS;
	}

	public static synchronized TimeZone getDefaultTimeZoneTZ() {
		return defaultTimeZoneTZ;
	}

	public static synchronized void setDefaultTimeZoneTZ(TimeZone defaultTimeZoneTZ) {
		CalendarCache.defaultTimeZoneTZ = defaultTimeZoneTZ;
	}

	public CalendarCache(){
	}
	
	public CalendarCache(String s){
		if(s == null){
			throw new IllegalArgumentException("Could not resolve null string to a TimeZone");
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
		
		setDefaultTimeZoneTZ(n);
		setDefaultTimeZoneS(s);
	}
	
	public CalendarCache(TimeZone tz){
		if(tz == null){
			throw new IllegalArgumentException("Could not resolve null TimeZone to a String");
		}
		
		String n = tz.getID();
		
		if(n == null){
			throw new IllegalArgumentException("Could not resolve null TimeZone to a String");
		}
		
		setDefaultTimeZoneTZ(tz);
		setDefaultTimeZoneS(n);
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
