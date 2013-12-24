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
/* 
 gcc -Wall -framework IOKit -framework Carbon idler.c -o idler
 Insight from: http://macenterprise.org/content/view/121/140/ */

#ifndef __IDLE_EXAMPLE__ 
#include <jni.h>
#else
typedef char JNIEnv;
#endif

#include <CoreFoundation/CoreFoundation.h>
#include <CoreServices/CoreServices.h>
#include <IOKit/IOKitLib.h>

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

long long getIdleTime(int rightshift)
{
	mach_port_t masterPort;
	io_iterator_t iter;

	IOMasterPort(MACH_PORT_NULL, &masterPort);

	long long ret = -1;

	/* Get IOHIDSystem */
	kern_return_t result = IOServiceGetMatchingServices(masterPort, IOServiceMatching("IOHIDSystem"), &iter);

	if ((result != KERN_SUCCESS) || (iter == 0)){
		ret = -1;
	}
	else{
		io_registry_entry_t curObj = IOIteratorNext(iter);
  
		if (curObj == 0) {
			ret = -2;
		}
		else{
			CFMutableDictionaryRef properties = 0;
			CFTypeRef obj = NULL;

			if (IORegistryEntryCreateCFProperties(curObj, &properties, kCFAllocatorDefault, 0) != KERN_SUCCESS || properties == NULL) {
				ret = -3;
			}
			else{
				obj = CFDictionaryGetValue(properties, CFSTR("HIDIdleTime"));
				if(obj == NULL){
					ret = -5;
				}
				else{
					CFRetain(obj);
					uint64_t tHandle;

					CFTypeID type = CFGetTypeID(obj);

					if (type == CFDataGetTypeID()) {
						CFDataGetBytes((CFDataRef) obj, CFRangeMake(0, sizeof(tHandle)), (UInt8*) &tHandle);   
						ret = (long)(tHandle >>= rightshift);
					}  else if (type == CFNumberGetTypeID()) {
						CFNumberGetValue((CFNumberRef)obj,kCFNumberSInt64Type, &tHandle);
						ret = (long)(tHandle >>= rightshift);
					} else {
						printf("Nomatic*IM: Mac Idle Sensor: Unsupported type %d\n",(int)type);
						ret = -4;
					}

					fprintf(stderr,"getIdleTime about to release obj...");
					CFRelease(obj);    
					obj = nil;
					fprintf(stderr,"done\n");
				}

				fprintf(stderr,"getIdleTime about to release properties...");
				CFRelease((CFTypeRef)properties);
				properties = nil;
				fprintf(stderr,"done\n");
			}

			fprintf(stderr,"getIdleTime about to release curObj...");
			IOObjectRelease(curObj);
			curObj = 0;
			fprintf(stderr,"done\n");
		}

		fprintf(stderr,"getIdleTime about to release iter...");
		IOObjectRelease(iter);
		iter = 0;
		fprintf(stderr,"done\n");

	}

	return ret;
}


#ifndef __IDLE_EXAMPLE__ 
/*
 * Class:     edu_uci_ics_luci_nomatic_service_sensors_IdleSensor_Mac
 * Method:    getIdleTimeNative
 * Signature: ()J
 */
 JNIEXPORT jlong JNICALL Java_edu_uci_ics_luci_cacophony_sensors_IdleMac_getIdleTimeNative
  (JNIEnv *env, jclass ob)
{
	jlong ret = -8;

	fprintf(stderr,"native code: idle : getIdleTimeNative\n");

	jclass excCls = (*env)->FindClass(env, "java/lang/Exception");
	/* 
		ClassFormatError: if the class data does not specify a valid class.
		ClassCircularityError: if a class or interface would be its own superclass or superinterface.
		NoClassDefFoundError: if no definition for a requested class or interface can be found.
		OutOfMemoryError: if the system runs out of memory.
	*/
	if((*env)->ExceptionOccurred(env)) {
		ret = -7;
	}
	else{
		if ((excCls == NULL) || (excCls == 0)){
			ret = -6;
		}
		else{
			ret = (jlong) getIdleTime(10);
		}
	}

	switch(ret){
		case -1: (*env)->ThrowNew(env, excCls, "Mac Idle Sensor Failed: Error accessing IOHIDSystem\n"); break;
		case -2: (*env)->ThrowNew(env, excCls, "Mac Idle Sensor Failed: Iterator is empty\n"); break;
		case -3: (*env)->ThrowNew(env, excCls, "Mac Idle Sensor Failed: Couldn't grab system properties\n");break;
		case -4: (*env)->ThrowNew(env, excCls, "Mac Idle Sensor Failed: Unsupported type. See Console.\n");break;
		case -5: (*env)->ThrowNew(env, excCls, "Mac Idle Sensor Failed: Can't find idle time\n");break;
		case -6: (*env)->ThrowNew(env, excCls, "Mac Idle Sensor Failed: Couldn't find exception type\n");break;
		case -7: (*env)->ThrowNew(env, excCls, "Mac Idle Sensor Failed: Couldn't find class \n");break;
	}
	return(ret);
}


#endif


