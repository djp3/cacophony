/*
	Copyright 2007-2013
		University of California, Irvine (c/o Donald J. Patterson)
*/
/*
	This file is part of the Laboratory for Ubiquitous Computing java Utility package, i.e. "Utilities"

    Utilities is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Utilities is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Utilities.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
 *  JNI UniMotion - JNI Unified Motion detection for Apple portables.
 *
 *  Copyright (c) 2006 Daniel Shiffman All rights reserved.
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License version 2.1 as published by the Free Software Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation Inc. 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 */

/*
 * HISTORY of Motion
 * Written by Christian Klein
 * Modified for iBook compatibility by Pall Thayer
 * Modified for Hi Res Powerbook compatibility by Pall Thayer
 * Modified for MacBook Pro compatibility by Randy Green
 * Disparate forks unified into UniMotion by Lincoln Ramsay
 * Made into a Java Native Interface by Daniel Shiffman
 */

// This license applies to the portions created by Lincoln Ramsay.
/*
 *  UniMotion - Unified Motion detection for Apple portables.
 *
 *  Copyright (c) 2006 Lincoln Ramsay. All rights reserved.
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License version 2.1 as published by the Free Software Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation Inc. 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 */

// This license applies to the portions created by Cristian Klein.
/* motion.c
 *
 * a little program to display the coords returned by
 * the powerbook motion sensor
 *
 * A fine piece of c0de, brought to you by
 *
 *               ---===---
 * *** teenage mutant ninja hero coders ***
 *               ---===---
 *
 * All of the software included is copyrighted by Christian Klein <chris@5711.org>.
 *
 * Copyright 2005 Christian Klein. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author must not be used to endorse or promote
 *    products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 */
#include "jni.h"
#include <IOKit/IOKitLib.h>
#include <CoreFoundation/CoreFoundation.h>
#include <string.h>
#include <stdint.h>

// The various SMS hardware that unimotion supports
enum sms_hardware {
    unknown = 0,
    powerbook = 1,
    ibook = 2,
    highrespb = 3,
    macbookpro = 4
};

enum data_type {
    PB_IB,
    MBP
};

struct pb_ib_data {
    int8_t x;
    int8_t y;
    int8_t z;
    int8_t pad[57];
};

struct mbp_data {
    int16_t x;
    int16_t y;
    int16_t z;
    int8_t pad[34];
};

union motion_data {
    struct pb_ib_data pb_ib;
    struct mbp_data mbp;
};

static bool first = true;
static int type;

static int set_values(int type, int *kernFunc, char **servMatch, int *dataType)
{
    switch ( type ) {
        case powerbook:
            *kernFunc = 21;
            *servMatch = "IOI2CMotionSensor";
            *dataType = PB_IB;
            break;
        case ibook:
            *kernFunc = 21;
            *servMatch = "IOI2CMotionSensor";
            *dataType = PB_IB;
            break;
        case highrespb:
            *kernFunc = 21;
            *servMatch = "PMUMotionSensor";
            *dataType = PB_IB;
            break;
        case macbookpro:
            *kernFunc = 5;
            *servMatch = "SMCMotionSensor";
            *dataType = MBP;
            break;
        default:
            return 0;
    }

    return 1;
}

static bool probe_sms(int kernFunc, char *servMatch, int dataType, void *data)
{
    kern_return_t result;
    mach_port_t masterPort;
    io_iterator_t iterator;
    io_object_t aDevice;
    io_connect_t  dataPort;

    IOItemCount structureInputSize;
    size_t structureOutputSize;

    union motion_data inputStructure;
    union motion_data *outputStructure;

    outputStructure = (union motion_data *)data;

    result = IOMasterPort(MACH_PORT_NULL, &masterPort);
	if( result != KERN_SUCCESS){
		return false;
	}
	else{
    	CFMutableDictionaryRef matchingDictionary = IOServiceMatching(servMatch);
		result = IOServiceGetMatchingServices(masterPort, matchingDictionary, &iterator);

		if (result != KERN_SUCCESS) {
			//fputs("IOServiceGetMatchingServices returned error.\n", stderr);
			return false;
		}
		else{
			aDevice = IOIteratorNext(iterator);

			fprintf(stderr,"probe_sms about to release iterator...");
			IOObjectRelease(iterator);
			iterator = 0;
			fprintf(stderr,"done\n");

			if (aDevice == 0) {
				//fputs("No motion sensor available\n", stderr);
				return false;
			}
			else{
				result = IOServiceOpen(aDevice, mach_task_self(), 0, &dataPort);

				fprintf(stderr,"probe_sms about to release aDevice...");
				IOObjectRelease(aDevice);
				aDevice = 0;
				fprintf(stderr,"done\n");

				if (result != KERN_SUCCESS) {
					//fputs("Could not open motion sensor device\n", stderr);
					return false;
				}
				else{

					switch ( dataType ) {
						case PB_IB:
							structureInputSize = sizeof(struct pb_ib_data);
							structureOutputSize = sizeof(struct pb_ib_data);
							break;
						case MBP:
							structureInputSize = sizeof(struct mbp_data);
							structureOutputSize = sizeof(struct mbp_data);
							break;
						default:
							return false;
					}

					memset(&inputStructure, 0, sizeof(union motion_data));
					memset(outputStructure, 0, sizeof(union motion_data));
			
					result = IOConnectCallStructMethod((mach_port_t)dataPort, kernFunc, &inputStructure, structureInputSize, outputStructure,&structureOutputSize);
			
					if (result != KERN_SUCCESS) {
						//puts("no coords");
						return false;
					}
					else{
						fprintf(stderr,"probe_sms about to close dataPort ...");
						IOServiceClose(dataPort); 
						fprintf(stderr,"done\n");
						return true;
					}
				}
			}
		}
	}

}

int detect_sms()
{
    int kernFunc;
    char *servMatch;
    int dataType;
    union motion_data data;
    int i;

    for ( i = 1; ; i++ ) {
        if ( !set_values(i, &kernFunc, &servMatch, &dataType) )
            break;
        if ( probe_sms(kernFunc, servMatch, dataType, &data) )
            return i;
    }

    return unknown;
}

bool read_sms_raw(int type, int *x, int *y, int *z)
{
    int kernFunc;
    char *servMatch;
    int dataType;
    union motion_data data;
	
	//fprintf(stderr,"native code: acclerometer: readSMS 5\n");
    if ( !set_values(type, &kernFunc, &servMatch, &dataType) )
        return false;
    if ( probe_sms(kernFunc, servMatch, dataType, &data) ) {
        switch ( dataType ) {
            case PB_IB:
                if ( x ) *x = data.pb_ib.x;
                if ( y ) *y = data.pb_ib.y;
                if ( z ) *z = data.pb_ib.z;
				return true;
            case MBP:
                if ( x ) *x = data.mbp.x;
                if ( y ) *y = data.mbp.y;
                if ( z ) *z = data.mbp.z;
				return true;
            default:
                return false;
        }
    }
    return false;
}

bool read_sms(int type, int *x, int *y, int *z)
{
	//fprintf(stderr,"native code: acclerometer: readSMS 6\n");
    int _x, _y, _z;

	if(read_sms_raw(type, &_x, &_y, &_z)){
    	int xoff, yoff, zoff;
    	Boolean ok;

		CFStringRef app = CFSTR("com.ramsayl.UniMotion");
		CFStringRef xoffstr = CFSTR("x_offset");
		CFStringRef yoffstr = CFSTR("y_offset");
		CFStringRef zoffstr = CFSTR("z_offset");

		xoff = CFPreferencesGetAppIntegerValue(xoffstr, app, &ok);
		if ( ok ) _x += xoff;

		yoff = CFPreferencesGetAppIntegerValue(yoffstr, app, &ok);
		if ( ok ) _y += yoff;

		zoff = CFPreferencesGetAppIntegerValue(zoffstr, app, &ok);
		if ( ok ) _z += zoff;

		*x = _x;
		*y = _y;
		*z = _z;

		return true;
	}
	else{
		return false;
	}
}

bool read_sms_real(int type, double *x, double *y, double *z)
{
    int _x, _y, _z;

    if (read_sms_raw(type, &_x, &_y, &_z)){
    	int xscale, yscale, zscale;
    	Boolean ok;
			
    	CFStringRef app = CFSTR("com.ramsayl.UniMotion");
	    CFStringRef xscalestr = CFSTR("x_scale");
	    CFStringRef yscalestr = CFSTR("y_scale");
	    CFStringRef zscalestr = CFSTR("z_scale");

	    xscale = CFPreferencesGetAppIntegerValue(xscalestr, app, &ok);
	    if ( !ok ) return false;

		yscale = CFPreferencesGetAppIntegerValue(yscalestr, app, &ok);
		if ( !ok ) return false;

		zscale = CFPreferencesGetAppIntegerValue(zscalestr, app, &ok);
		if ( !ok ) return false;
    
		*x = _x / (double)xscale;
		*y = _y / (double)yscale;
		*z = _z / (double)zscale;

		return true;
	}
	else{
		return false;
	}
}

/*
 * Class:     edu_uci_ics_luci_cacophony_sensors_AccelerometerSensor_Mac
 * Method:    readSMS
 * Signature: ()[I
 */
JNIEXPORT jintArray JNICALL Java_edu_uci_ics_luci_cacophony_sensors_AccelerometerMac_readSMS
  (JNIEnv *env, jclass this)
{
	char *name;
	int x,y,z;
	x = y = z = 0;

	//fprintf(stderr,"native code: acclerometer: readSMS\n");
	
	if (first) {
		//fprintf(stderr, "Detecting SMS\n");
		type = detect_sms();
		if ( type == unknown ) {
			fprintf(stderr, "Nomatic*IM: Could not detect an SMS.\n");
			//return 1;
		}
		switch ( type ) {
			case powerbook:
				name = "powerbook";
				break;
			case ibook:
				name = "ibook";
				break;
			case highrespb:
				name = "highrespb";
				break;
			case macbookpro:
				name = "macbookpro";
				break;
			default:
				name = "???";
				break;
		}		
		/*fprintf(stderr, "Nomatic*IM: Detected SMS type %d (%s)\n", type, * name);	*/
		first = false;
	}

	//fprintf(stderr,"native code: acclerometer: readSMS 2\n");

	jsize size = 3;
	jintArray jr = (*env)->NewIntArray(env, size);
	if(jr == NULL){
		return NULL; /* out of memory error */
	}
	else{
		if(read_sms_raw(type, &x,&y,&z)){
			jint data[size];
			data[0] = (jint)x; data[1] = (jint)y; data[2] = (jint)z;
			(*env)->SetIntArrayRegion(env,jr, (jsize)0, size,data);
			/* ArrayIndexOutofBoundsException */
			if((*env)->ExceptionOccurred(env)) {
				return NULL;
			}
			
			//fprintf(stderr,"native code: acclerometer: readSMS 3\n");
			return jr;
		}
		else{
			//fprintf(stderr,"native code: acclerometer: readSMS 4\n");
			return NULL;
		}
	}
}


int main(){
	char *name;
	int x,y,z;
	x = y = z = 0;
	
	fprintf(stderr, "Detecting SMS\n");
	type = detect_sms();
	if ( type == unknown ) {
		fprintf(stderr, "Nomatic*IM: Could not detect an SMS.\n");
		return 1;
	}

	switch ( type ) {
		case powerbook:
			name = "powerbook";
			break;
		case ibook:
			name = "ibook";
			break;
		case highrespb:
			name = "highrespb";
			break;
		case macbookpro:
			name = "macbookpro";
			break;
		default:
			name = "???";
			break;
	}		

	fprintf(stderr, "Nomatic*IM: Detected SMS type %d (%s)\n", type, name);	
	first = false;
	
	int ok = 0;
	ok = read_sms_raw(type, &x,&y,&z);
    if ( ok ) {
		printf("%d %d %d\n",x,y,z);
	}
	return(0);
}

