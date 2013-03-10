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

public enum PowerSourceEnum {
	WALL, BATTERY;

	public static PowerSourceEnum fromString(String string) {
		if (string == null) {
			return null;
		} else {
			for(PowerSourceEnum e:PowerSourceEnum.values()){
				if (string.equals(e.toString())) {
					return e;
				}
			}
			return null;
		}
	}
	
	public static PowerSourceEnum fromInteger(Integer i) {
		if (i == null) {
			return null;
		} else {
			for(PowerSourceEnum e:PowerSourceEnum.values()){
				if (i.equals(e.toInteger())) {
					return e;
				}
			}
			return null;
		}
	}
	
	public Integer toInteger() {
		int i = 0;
		for(PowerSourceEnum e:PowerSourceEnum.values()){
			if (this.equals(e)) {
				return i;
			}
			i++;
		}
		return(null);
	}
}
