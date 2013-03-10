/*
Copyright 2007-2013
	University of California, Irvine (c/o Donald J. Patterson)
*/
/*
This file is part of Cacophony

Cacophony is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Cacophony is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Cacophony.  If not, see <http://www.gnu.org/licenses/>.
*/

package edu.uci.ics.luci.cacophony.sensors;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.ics.luci.utility.datastructure.ListComparable;
import edu.uci.ics.luci.utility.datastructure.MapComparable;
import edu.uci.ics.luci.utility.datastructure.Pair;

public class RecordTest {
	private Record i;
	private Record notI;
	private Record a;
	private ListComparable<String> runningProcesses = new ListComparable<String>(new ArrayList<String>(3));
	private MapComparable<Pair<String,String>,Integer> wifiMACList = new MapComparable<Pair<String,String>,Integer>(new HashMap<Pair<String,String>,Integer>());
	private int countA = 0;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		runningProcesses.add("firefox");
		runningProcesses.add("eclipse");
		runningProcesses.add("mail");
		wifiMACList.put(new Pair<String,String>("foossid","00:00:00:00:00"),10);
		wifiMACList.put(new Pair<String,String>("barssid","11:11:11:11:11"),20);
	
		Date date = new Date(0);
	
		i = new Record(
			date.getTime(),
			Calendar.SUNDAY,
			0,
			"128.195.1.1",
			"dhcp-mobile.ics.uci.edu",
			"192.168.0.50",
			new Pair<Pair<String,String>,Integer>(new Pair<String,String>("test","00:16:cb:c9:e4:42"),51), 
			wifiMACList,
			"UCInet Mobile",
			"firefox",
			runningProcesses,
			2,
			"[(800x600),(1440x960)]",
			PowerSourceEnum.WALL,
			0.1,
			0.1,
			0.1,
			0.9,
			1.0,
			0.1,
			1.0);
		
		MapComparable<Pair<String,String>,Integer> notWifiMACList = new MapComparable<Pair<String,String>,Integer>(new HashMap<Pair<String,String>,Integer>());
		notWifiMACList.putAll(wifiMACList);

		notWifiMACList.put(new Pair<String,String>("bazssid","00:11:11:22:22:33"),35);
		ListComparable<String> notRunningProcesses =  new ListComparable<String>(new ArrayList<String>(runningProcesses));
		notRunningProcesses.add("firefoxy");
	
		notI= new Record(
			date.getTime()+1,
			Calendar.MONDAY,
			1,
			"128.195.1.2",
			"dhcp-mobile.ics.uci.com",
			"192.168.0.51",
			new Pair<Pair<String,String>,Integer>(new Pair<String,String>("test","00:16:cb:c9:e4:43"),31), 
			notWifiMACList,
			"UCInet Mobile1",
			"firefoxy",
			notRunningProcesses,
			1,
			"(1440x960)",
			PowerSourceEnum.BATTERY,
			0.2,
			0.2,
			0.2,
			0.8,
			0.9,
			0.2,
			0.9); 
		
		a = new Record();
		a.setTimeStamp(date.getTime());
		a.setDayOfTheWeek(Calendar.SUNDAY);
		a.setMinutesSinceMidnight(0);
		a.setRemoteIPAddress("128.195.1.1");
		a.setDNS("dhcp-mobile.ics.uci.edu");
		a.setLocalIPAddress("192.168.0.50");
		a.setWifiMACAddressConnected( new Pair<Pair<String,String>,Integer>(new Pair<String,String>("test","00:16:cb:c9:e4:42"),51)); 
		a.setWifiMACAddressList(wifiMACList);
		a.setWifiSSID("UCInet Mobile");
		a.setActiveProcess("firefox");
		a.setRunningProcesses(runningProcesses);
		a.setDisplayConfiguration("[(800x600),(1440x960)]");
		a.setPowerSource("0");
		a.setPowerSource(PowerSourceEnum.WALL);
		a.setPowerSource("Battery");
		a.setPowerSource("Wall");
		a.setAccelerationX(0.1);
		a.setAccelerationY(0.1);
		a.setAccelerationZ(0.1);
		a.setAmbientLight(0.9);
		a.setVolume(1.0);
		a.setAmbientSound(0.1);
		a.setUIActivity(1.0);
	}



	@Test
	public void testGetTimeStamp() {
		assertTrue(i.getTimeStamp().equals(new Date(0).getTime()));
		assertTrue(a.getTimeStamp().equals(new Date(0).getTime()));
		assertTrue(i.get(SensorNetAttribute.TIME_STAMP).equals(a.getTimeStamp()));
		countA++;
	}

	@Test
	public void testGetDayOfTheWeek() {
		assertTrue(i.getDayOfTheWeek().equals(Calendar.SUNDAY));
		assertTrue(a.getDayOfTheWeek().equals(Calendar.SUNDAY));
		assertTrue(i.get(SensorNetAttribute.DAY_OF_THE_WEEK).equals( Calendar.SUNDAY));
		countA++;
	}

	@Test
	public void testGetMinutesSinceMidnight() {
		assertTrue(i.getMinutesSinceMidnight().equals(0));
		assertTrue(a.getMinutesSinceMidnight().equals(0));
		assertTrue(i.get(SensorNetAttribute.MINUTES_SINCE_MIDNIGHT).equals(0));
		countA++;
	}

	@Test
	public void testGetRemoteIPAddress() {
		assertTrue(i.getRemoteIPAddress().equals("128.195.1.1"));
		assertTrue(a.getRemoteIPAddress().equals("128.195.1.1"));
		assertTrue(i.get(SensorNetAttribute.REMOTE_IP_ADDRESS).equals( "128.195.1.1"));
		countA++;
	}

	@Test
	public void testGetDns() {
		assertTrue(i.getDNS().equals("dhcp-mobile.ics.uci.edu"));
		assertTrue(a.getDNS().equals("dhcp-mobile.ics.uci.edu"));
		assertTrue(i.get(SensorNetAttribute.DNS).equals( "dhcp-mobile.ics.uci.edu"));
		countA++;

	}

	@Test
	public void testGetLocalIPAddress() {
		assertTrue(i.getLocalIPAddress().equals("192.168.0.50"));
		assertTrue(a.getLocalIPAddress().equals("192.168.0.50"));
		assertTrue(i.get(SensorNetAttribute.LOCAL_IP_ADDRESS).equals( "192.168.0.50"));
		countA++;
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetWifiMACAddressConnected() {
		assertTrue(i.getWifiMACAddressConnected().getFirst() .equals(new Pair<String,String>("test","00:16:cb:c9:e4:42")));
		assertTrue(a.getWifiMACAddressConnected().getFirst() .equals(new Pair<String,String>("test","00:16:cb:c9:e4:42")));
		assertTrue(((Pair<Pair<String,String>, Integer>) (i .get(SensorNetAttribute.WIFI_MAC_ADDRESS_CONNECTED))) .getFirst().getSecond().equals("00:16:cb:c9:e4:42"));
		countA++;
	}

	@Test
	public void testGetWifiMACAddressList() {
		assertTrue(i.getWifiMACAddressList().equals(wifiMACList));
		assertTrue(a.getWifiMACAddressList().equals(wifiMACList));
		assertTrue(i.get(SensorNetAttribute.WIFI_MAC_ADDRESS_LIST).equals( wifiMACList));
		countA++;
	}

	@Test
	public void testGetWifiSSID() {
		assertTrue(i.getWifiSSID().equals("UCInet Mobile"));
		assertTrue(a.getWifiSSID().equals("UCInet Mobile"));
		assertTrue(i.get(SensorNetAttribute.WIFI_SSID).equals("UCInet Mobile"));
		countA++;
	}

	@Test
	public void testGetActiveProcess() {
		assertTrue(i.getActiveProcess().equals("firefox"));
		assertTrue(a.getActiveProcess().equals("firefox"));
		assertTrue(i.get(SensorNetAttribute.ACTIVE_PROCESS).equals("firefox"));
		countA++;
	}

	@Test
	public void testGetRunningProcesses() {
		assertTrue(i.getRunningProcesses().equals(runningProcesses));
		assertTrue(a.getRunningProcesses().equals(runningProcesses));
		assertTrue(i.get(SensorNetAttribute.RUNNING_PROCESSES).equals( runningProcesses));
		countA++;
	}

	@Test
	public void testGetNumberOfDisplays() {
		assertTrue(i.getNumberOfDisplays().equals(2));
		assertTrue(a.getNumberOfDisplays().equals(2));
		assertTrue(i.get(SensorNetAttribute.NUM_DISPLAYS).equals(2));
		countA++;
	}

	@Test
	public void testGetDisplayConfiguration() {
		assertTrue(i.getDisplayConfiguration().equals("[(800x600),(1440x960)]"));
		assertTrue(a.getDisplayConfiguration().equals("[(800x600),(1440x960)]"));
		assertTrue(i.get(SensorNetAttribute.DISPLAY_CONFIG).equals(
				"[(800x600),(1440x960)]"));
		countA++;
	}

	@Test
	public void testGetPowerConfiguration() {
		assertTrue(i.getPowerSource().equals(PowerSourceEnum.WALL));
		assertTrue(a.getPowerSource().equals(PowerSourceEnum.WALL));
		assertTrue(i.get(SensorNetAttribute.POWER_SOURCE).equals(
				PowerSourceEnum.WALL));
		countA++;
	}

	@Test
	public void testGetAcceleration() {
		assertTrue(i.getAccelerationX().equals(0.1));
		assertTrue(a.getAccelerationX().equals(0.1));
		assertTrue(i.getAccelerationY().equals(0.1));
		assertTrue(a.getAccelerationY().equals(0.1));
		assertTrue(i.getAccelerationZ().equals(0.1));
		assertTrue(a.getAccelerationZ().equals(0.1));
		assertTrue(i.getAccelerationMagnitude().equals( Math.sqrt(0.1 * 0.1 + 0.1 * 0.1 + 0.1 * 0.1)));
		assertTrue(i.getAccelerationMagnitude().equals( Math.sqrt(0.1 * 0.1 + 0.1 * 0.1 + 0.1 * 0.1)));
		assertTrue(i.get(SensorNetAttribute.ACC_X).equals(0.1));
		countA++;
		assertTrue(i.get(SensorNetAttribute.ACC_Y).equals(0.1));
		countA++;
		assertTrue(i.get(SensorNetAttribute.ACC_Z).equals(0.1));
		countA++;
		assertTrue(i.get(SensorNetAttribute.ACC_MAG).equals( Math.sqrt(0.1 * 0.1 + 0.1 * 0.1 + 0.1 * 0.1)));
		countA++;

		Record foo = new Record();
		assertTrue(foo.getAccelerationMagnitude() == null);
		foo.setAccelerationY(0.5);
		assertTrue(foo.getAccelerationMagnitude().equals(0.5));
	}

	@Test
	public void testGetAmbientLight() {
		assertTrue(i.getAmbientLight().equals(0.9));
		assertTrue(a.getAmbientLight().equals(0.9));
		assertTrue(i.get(SensorNetAttribute.AMBIENT_LIGHT).equals(0.9));
		countA++;
	}

	@Test
	public void testGetVolume() {
		assertTrue(i.getVolume().equals(1.0));
		assertTrue(a.getVolume().equals(1.0));
		assertTrue(i.get(SensorNetAttribute.VOLUME).equals(1.0));
		countA++;
	}

	@Test
	public void testGetAmbientSound() {
		assertTrue(i.getAmbientSound().equals(0.1));
		assertTrue(a.getAmbientSound().equals(0.1));
		assertTrue(i.get(SensorNetAttribute.AMBIENT_SOUND).equals(0.1));
		countA++;
	}

	@Test
	public void testGetUIActivity() {
		assertTrue(i.getUIActivity().equals(1.0));
		assertTrue(a.getUIActivity().equals(1.0));
		assertTrue(i.get(SensorNetAttribute.UI_ACTIVITY).equals(1.0));
		countA++;
	}

	@Test
	public void testEqualsObject() {

		assertTrue(a.equals(i));
		assertTrue(i.equals(a));
		assertTrue(i.equals(i.clone()));
		assertTrue(a.equals(a.clone()));
		assertNotSame(i, i.clone());
		assertNotSame(a, i);

		/*
		 * Test to make sure that all the attributes are considered by equals
		 * function
		 */
		for (SensorNetAttribute att : SensorNetAttribute.values()) {
			if (!SensorNetAttribute.derived(att)) {
				/* Test null */
				i.set(att, null);
				assertTrue(!i.equals(a));
				assertTrue(!a.equals(i));
				/* Test not equal */
				i.set(att, notI.get(att));
				assertTrue(!i.equals(a));
				assertTrue(!a.equals(i));
				/* Reset attribute */
				i.set(att, a.get(att));
				assertTrue(a.equals(i));
				assertTrue(i.equals(a));
			}
		}

		assertTrue(!i.equals(new Record()));
		assertTrue(!i.equals(new Object()));

	}

	@Test
	public void testToString() {
		//System.out.println(i.toString());
		assertTrue(i.toString().equals("I(0,1,0,128.195.1.1,dhcp-mobile.ics.uci.edu,192.168.0.50,<<test:00:16:cb:c9:e4:42>:51>,{\"<foossid:00:00:00:00:00>\":\"10\",\"<barssid:11:11:11:11:11>\":\"20\"},UCInet Mobile,firefox,[\"firefox\",\"eclipse\",\"mail\"],2,[(800x600),(1440x960)],WALL,0.1,0.1,0.1,0.17320508075688776,0.9,1.0,0.1,1.0)"));
	}

	@Test
	public void testAllAttributes() throws Exception {
		testGetTimeStamp();
		testGetDayOfTheWeek();
		testGetMinutesSinceMidnight();
		testGetRemoteIPAddress();
		testGetDns();
		testGetLocalIPAddress();
		testGetWifiMACAddressConnected();
		testGetWifiMACAddressList();
		testGetWifiSSID();
		testGetActiveProcess();
		testGetRunningProcesses();
		testGetNumberOfDisplays();
		testGetDisplayConfiguration();
		testGetPowerConfiguration();
		testGetAcceleration();
		testGetAmbientLight();
		testGetVolume();
		testGetAmbientSound();
		testGetUIActivity();
		testEqualsObject();
		testToString();
		// Make sure we tested all attributes
		assertEquals(countA, SensorNetAttribute.values().length);
	}

	@Test
	public void testArrayListToString() throws Exception {
		// Test a 0-length array
		assertEquals("", Record.arrayListToString(new ArrayList<String>()));
		// Test that Record.toString works after initialization,
		// in particular when 'status' is null
		Record r = new Record();
		r.toString();
	}
}

