/*
    Copyright 2007-2009
        University of California, Irvine (c/o Donald J. Patterson)
*/
/*
    This file is part of Nomatic*IM.

    Nomatic*IM is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Nomatic*IM is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Nomatic*IM.  If not, see <http://www.gnu.org/licenses/>.
*/
#define __IDLE_EXAMPLE__

#include "idle.c"

int main(int argc, char **argv) {

	int i;
	for(i = 0; i< 100; i++){
		printf("Idle time (in seconds) = %lld\n",getIdleTime(30));
		sleep(1);
	}
  
  return 0;
}


