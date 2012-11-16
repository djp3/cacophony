package edu.uci.ics.luci.utility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Test;

public class CalendarCacheTest {
	
	

	@Test
	public void testCalendarCache() {
		CalendarCache cc = new CalendarCache();
		String tz = "America/Los_Angeles";
		
		GregorianCalendar cal = new GregorianCalendar(CalendarCache.TZ_LosAngeles);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		
		assertEquals(hour,cc.getCalendar(tz).get(Calendar.HOUR_OF_DAY));
		
		cal = new GregorianCalendar(CalendarCache.TZ_GMT);
		hour = cal.get(Calendar.HOUR_OF_DAY);
		
		assertTrue(hour != cc.getCalendar(tz).get(Calendar.HOUR_OF_DAY));
	}
	
	@Test
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value={"NP_LOAD_OF_KNOWN_NULL_VALUE"}, justification="Required to test null condition")
	public void testCalendarCacheString() {
		CalendarCache cc;
		
		String n = null;
		try{
			new CalendarCache(n);
			fail("Expected exception");
		}
		catch(RuntimeException e){
		}
		
		try{
			new CalendarCache("fake Time Zone");
			fail("Expected exception");
		}
		catch(RuntimeException e){
		}
		
		cc = new CalendarCache(CalendarCache.TZ_GMT.getID());
		String tz = null;
		
		GregorianCalendar cal = new GregorianCalendar(CalendarCache.TZ_GMT);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		
		assertEquals(hour,cc.getCalendar(tz).get(Calendar.HOUR_OF_DAY));
	}
	
	@Test
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value={"NP_LOAD_OF_KNOWN_NULL_VALUE"}, justification="Required to test null condition")
	public void testCalendarCacheTimeZone() {
		CalendarCache cc;
		TimeZone tz = null;
		try{
			new CalendarCache(tz);
			fail("Expected exception");
		}
		catch(RuntimeException e){
		}
		
		cc = new CalendarCache(CalendarCache.TZ_GMT);
		
		GregorianCalendar cal = new GregorianCalendar(CalendarCache.TZ_GMT);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		
		assertEquals(hour,cc.getCalendar(tz).get(Calendar.HOUR_OF_DAY));
		
		cc = new CalendarCache();
		
		cal = new GregorianCalendar(CalendarCache.TZ_LosAngeles);
		hour = cal.get(Calendar.HOUR_OF_DAY);
		
		assertEquals(hour,cc.getCalendar(CalendarCache.TZ_LosAngeles).get(Calendar.HOUR_OF_DAY));
	}
	
	@Test
	public void testGetCalendarString() {
		int howMany = 1000;
		CalendarCache cc = new CalendarCache();
		
		long start,middle,end;
		
		start = System.currentTimeMillis();
		for(int i = 0 ; i < howMany; i++){
			Calendar.getInstance(CalendarCache.TZ_GMT);
		}
		
		middle = System.currentTimeMillis();
		
		for(int i = 0 ; i < howMany; i++){
			cc.getCalendar(CalendarCache.TZ_GMT.getID());
		}
		
		end = System.currentTimeMillis();
		
		//System.out.println("middle-start:"+(middle-start)+",end-middle:"+(end-middle));
		assertTrue((middle-start)>(end-middle));
	}

	



	@Test
	public void testGetCalendarTimeZone() {
		int howMany = 1000;
		CalendarCache cc = new CalendarCache();
		
		long start,middle,end;
		
		start = System.currentTimeMillis();
		for(int i = 0 ; i < howMany; i++){
			Calendar.getInstance(CalendarCache.TZ_GMT);
		}
		
		middle = System.currentTimeMillis();
		
		for(int i = 0 ; i < howMany; i++){
			cc.getCalendar(CalendarCache.TZ_GMT);
		}
		
		end = System.currentTimeMillis();
		
		//System.out.println("middle-start:"+(middle-start)+",end-middle:"+(end-middle));
		assertTrue((middle-start)>(end-middle));
	}
	
	@Test
	public void testClear() {
		int howMany = 1000;
		CalendarCache cc = new CalendarCache();
		
		long start,middle,end;
		
		start = System.currentTimeMillis();
		for(int i = 0 ; i < howMany; i++){
			Calendar.getInstance(CalendarCache.TZ_GMT);
		}
		
		middle = System.currentTimeMillis();
		
		for(int i = 0 ; i < howMany; i++){
			cc.getCalendar(CalendarCache.TZ_GMT);
			cc.clear();
		}
		
		end = System.currentTimeMillis();
		
		//System.out.println("middle-start:"+(middle-start)+",end-middle:"+(end-middle));
		assertTrue((middle-start)<(end-middle));
	}
	

}
