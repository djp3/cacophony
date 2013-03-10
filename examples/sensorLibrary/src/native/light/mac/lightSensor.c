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
/* Code based on http://osxbook.com/book/bonus/chapter10/light/ */
// gcc - o lightSensor lightSensor.c - framework IOKit - framework CoreFoundation

#include "jni.h"

//#include <mach/mach.h>
#include <IOKit/IOKitLib.h>
#include <CoreFoundation/CoreFoundation.h>
#include "lightSensor.h"

static io_connect_t dataPort = 0;

void getLight(uint64_t *left,uint64_t *right)
{
	kern_return_t   kr;
	io_service_t    serviceObject;

	//Look up a registered IOService object whose class is AppleLMUController 
	serviceObject = IOServiceGetMatchingService(kIOMasterPortDefault, IOServiceMatching("AppleLMUController"));
	if (!serviceObject) {
		serviceObject = IOServiceGetMatchingService(kIOMasterPortDefault, IOServiceMatching("IOI2CDeviceLMU"));
		if (!serviceObject) {
			//fprintf(stderr, "failed to find ambient light sensor\n");
			*left = -1;
			*right = -1;
			return;
		}
	}

	//Create a connection to the IOService object 
	kr = IOServiceOpen(serviceObject, mach_task_self(), 0, &dataPort);
	IOObjectRelease(serviceObject);
	if (kr != KERN_SUCCESS) {
		//mach_error("IOServiceOpen:", kr);
		*left = -1;
		*right = -1;
		return;
	}

	//Get the ALS reading
	uint32_t scalarOutputCount = 2;
	uint64_t values[scalarOutputCount];

	kr = IOConnectCallMethod(dataPort, 
		kGetSensorReadingID, 
		nil, 
		0, 
		nil, 
		0, 
		values, 
		&scalarOutputCount, 
		nil, 
		0);
	
	if (kr == KERN_SUCCESS) {
		*left =	 values[0]; //
		*right = values[1]; //
		return;
	} 
	if (kr == kIOReturnBusy){
		*left = -1;
		*right = -1;
		return;
	}

	*left = -1;
	*right = -1;
	return;
}

/*
 * Class:     edu_uci_ics_luci_cacophony_sensors_LightMac
 * Method:    readLight
 * Signature: ()[J
 */
JNIEXPORT jlongArray JNICALL Java_edu_uci_ics_luci_cacophony_sensors_LightMac_readLight
  (JNIEnv *env, jclass this)
{
uint64_t left, right;

	fprintf(stderr,"native code: light: readLight\n");

	getLight(&left,&right);

	jlongArray jr;

	int rlen = 2;
	jr = (*env)->NewLongArray(env, rlen);
	if(jr == NULL){
			return NULL; /*out of memory error */
	}
	int size = 2;
	jlong data[2];
	//long *data;
	//data = (long *)malloc(sizeof(SInt32)*size);
	data[0] = left; data[1] = right;
	(*env)->SetLongArrayRegion(env,jr, 0, size,data);
	//free(data);
	return jr;

}

int main(void)
{
uint64_t left, right;

	printf("Reading 10 light sensors\n");		
	for(int i = 0 ; i < 10; i++){
		getLight(&left,&right);
		printf("%8ld %8ld\n", (long)left, (long)right);
		sleep(1);
	}
	return(0);
}
