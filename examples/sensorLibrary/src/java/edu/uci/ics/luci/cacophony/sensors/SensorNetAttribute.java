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

public enum SensorNetAttribute {
TIME_STAMP,
DAY_OF_THE_WEEK,
MINUTES_SINCE_MIDNIGHT,
REMOTE_IP_ADDRESS,
DNS,
LOCAL_IP_ADDRESS,
WIFI_MAC_ADDRESS_CONNECTED,
WIFI_MAC_ADDRESS_LIST,
WIFI_SSID,
ACTIVE_PROCESS,
RUNNING_PROCESSES,
DISPLAY_CONFIG,
POWER_SOURCE,
ACC_X,
ACC_Y,
ACC_Z,
AMBIENT_LIGHT,
VOLUME,
AMBIENT_SOUND,
UI_ACTIVITY,

/* Derived things */
NUM_DISPLAYS,
ACC_MAG;

public final static SensorNetAttribute FIRST_ATTRIBUTE = TIME_STAMP;
public final static SensorNetAttribute LAST_ATTRIBUTE = ACC_MAG;
public final static int NUMBER_SENSED = 20;
public final static int NUMBER_DERIVED = 2;

public static boolean derived(SensorNetAttribute x) {
	if (x == NUM_DISPLAYS) {
		return true;
	} else if (x == ACC_MAG) {
		return true;
	} else {
		return false;
	}
}

public boolean derived(){
	return SensorNetAttribute.derived(this);
}

public String toString() {
	switch (this) {
	case TIME_STAMP:
		return ("TIME_STAMP");
	case DAY_OF_THE_WEEK:
		return ("DAY_OF_THE_WEEK");
	case MINUTES_SINCE_MIDNIGHT:
		return ("MINUTES_SINCE_MIDNIGHT");
	case REMOTE_IP_ADDRESS:
		return ("REMOTE_IP_ADDRESS");
	case DNS:
		return ("DNS");
	case LOCAL_IP_ADDRESS:
		return ("LOCAL_IP_ADDRESS");
	case WIFI_MAC_ADDRESS_CONNECTED:
		return ("WIFI_MAC_ADDRESS_CONNECTED");
	case WIFI_MAC_ADDRESS_LIST:
		return ("WIFI_MAC_ADDRESS_LIST");
	case WIFI_SSID:
		return ("WIFI_SSID");
	case ACTIVE_PROCESS:
		return ("ACTIVE_PROCESS");
	case RUNNING_PROCESSES:
		return ("RUNNING_PROCESSES");
	case DISPLAY_CONFIG:
		return ("DISPLAY_CONFIG");
	case POWER_SOURCE:
		return ("POWER_SOURCE");
	case ACC_X:
		return ("ACC_X");
	case ACC_Y:
		return ("ACC_Y");
	case ACC_Z:
		return ("ACC_Z");
	case AMBIENT_LIGHT:
		return ("AMBIENT_LIGHT");
	case VOLUME:
		return ("VOLUME");
	case AMBIENT_SOUND:
		return ("AMBIENT_SOUND");
	case UI_ACTIVITY:
		return ("UI_ACTIVITY");
		/* Derived things */
	case NUM_DISPLAYS:
		return ("NUM_DISPLAYS");
	case ACC_MAG:
		return ("ACC_MAG");
	default:
		return ("UNKNOWN");
	}
}

public String toSQLTypeString() {
		String longType = "LONG";
		String intType = "INTEGER";
		String doubleType = "REAL";
		String stringType = "TEXT";

	switch (this) {
	case TIME_STAMP: return longType;
	case DAY_OF_THE_WEEK: return intType;
	case MINUTES_SINCE_MIDNIGHT: return intType;
	case REMOTE_IP_ADDRESS: return stringType;
	case DNS: return stringType;
	case LOCAL_IP_ADDRESS: return stringType;
	case WIFI_MAC_ADDRESS_CONNECTED: return stringType;
	case WIFI_MAC_ADDRESS_LIST: return stringType;
	case WIFI_SSID: return stringType;
	case ACTIVE_PROCESS: return stringType;
	case RUNNING_PROCESSES:return stringType;
	case DISPLAY_CONFIG:return stringType;
	case POWER_SOURCE:return intType;
	case ACC_X: return doubleType;
	case ACC_Y: return doubleType;
	case ACC_Z: return doubleType;
	case AMBIENT_LIGHT:return doubleType;
	case VOLUME: return doubleType;
	case AMBIENT_SOUND: return doubleType;
	case UI_ACTIVITY: return doubleType;
	case NUM_DISPLAYS: return intType;
	case ACC_MAG:return doubleType;
	default: return null;
	}
}

public SensorNetAttribute next() {
	switch (this) {
	case TIME_STAMP:
		return (DAY_OF_THE_WEEK);
	case DAY_OF_THE_WEEK:
		return (MINUTES_SINCE_MIDNIGHT);
	case MINUTES_SINCE_MIDNIGHT:
		return (REMOTE_IP_ADDRESS);
	case REMOTE_IP_ADDRESS:
		return (DNS);
	case DNS:
		return (LOCAL_IP_ADDRESS);
	case LOCAL_IP_ADDRESS:
		return (WIFI_MAC_ADDRESS_CONNECTED);
	case WIFI_MAC_ADDRESS_CONNECTED:
		return (WIFI_MAC_ADDRESS_LIST);
	case WIFI_MAC_ADDRESS_LIST:
		return (WIFI_SSID);
	case WIFI_SSID:
		return (ACTIVE_PROCESS);
	case ACTIVE_PROCESS:
		return (RUNNING_PROCESSES);
	case RUNNING_PROCESSES:
		return (DISPLAY_CONFIG);
	case DISPLAY_CONFIG:
		return (POWER_SOURCE);
	case POWER_SOURCE:
		return (ACC_X);
	case ACC_X:
		return (ACC_Y);
	case ACC_Y:
		return (ACC_Z);
	case ACC_Z:
		return (AMBIENT_LIGHT);
	case AMBIENT_LIGHT:
		return (VOLUME);
	case VOLUME:
		return (AMBIENT_SOUND);
	case AMBIENT_SOUND:
		return (UI_ACTIVITY);
	case UI_ACTIVITY:
		return (NUM_DISPLAYS);
		/* Derived things */
	case NUM_DISPLAYS:
		return (ACC_MAG);
	case ACC_MAG:
		return (null);
	default:
		return (null);

	}
}
}

