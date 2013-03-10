/*
    Copyright 2007-2008
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
//Complile using the following command line:
//gcc - Wall - o uiActivity uiActivity.c - framework ApplicationServices

#include <ApplicationServices/ApplicationServices.h> 
#include "jni.h"

static CGRect screenBounds;
static long count = 0;
static CFRunLoopSourceRef runLoopSource = NULL;
static CFRunLoopRef runLoopReference = NULL;
static CFMachPortRef   eventTap = NULL;

/*
 * Class:     edu_uci_ics_luci_cacophony_sensors_UIActivityMac
 * Method:    getAccumulatedMouseClicks
 * Signature: ()D
 */
JNIEXPORT jdouble JNICALL Java_edu_uci_ics_luci_cacophony_sensors_UIActivityMac_getAccumulatedMouseClicks
  (JNIEnv *env, jclass ob)
{
	fprintf(stderr,"native code: uiactivity: getAccumulatedMouseClicks\n");
	//fprintf(stderr,"Sensing\n");
	jdouble data;
	while(runLoopReference == NULL){
		fprintf(stderr,"uiActivity jni waiting indefinitely for initialization\n");
		sleep(1);
	}
	data= count+0.0;
	count = 0;
	//fprintf(stderr,"Sensing2\n");
	return data;
}

CGEventRef myNewCGEventCallback(CGEventTapProxy proxy, CGEventType type, CGEventRef event, void *refcon)
{
	count++;
	return(event);
}


/*
 * Class:     edu_uci_ics_luci_cacophony_sensors_UIActivityMac
 * Method:    nativeInit
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_edu_uci_ics_luci_cacophony_sensors_UIActivityMac_nativeInit
  (JNIEnv *env, jclass ob)
{
	fprintf(stderr,"native code: uiactivity: nativeInit\n");
	//fprintf(stderr,"Starting\n");
	while(runLoopReference != NULL){
		fprintf(stderr,"uiActivity jni waiting indefinitely for previously initialized instance to shutdown\n");
		sleep(1);
	}

	CGEventMask     eventMask;

	count = 0;

	eventMask = (kCGEventLeftMouseDown | kCGEventRightMouseDown | kCGEventOtherMouseDown );

	eventTap = CGEventTapCreate(kCGSessionEventTap, kCGHeadInsertEventTap, 0, eventMask, myNewCGEventCallback, NULL);

	if (!eventTap) {
		fprintf(stderr, "failed to create event tap\n");
		exit(1);
	}

	//Create a run loop source.
	runLoopSource = CFMachPortCreateRunLoopSource(kCFAllocatorDefault, eventTap, 0);

	runLoopReference = CFRunLoopGetCurrent();
	//Add to the current run loop.
	CFRunLoopAddSource(runLoopReference, runLoopSource, kCFRunLoopCommonModes);
	//Enable the event tap.
	CGEventTapEnable(eventTap, true);
	//fprintf(stderr,"Starting2\n");
	//Set it all running.
	CFRunLoopRun();
}



CGEventRef myCGEventCallback(CGEventTapProxy proxy, CGEventType type, CGEventRef event, void *refcon)
{

	//Do some sanity check.
	//if (type != kCGEventMouseMoved)
	//	return event;

	//The incoming mouse position.
	CGPoint location = CGEventGetLocation(event);

	//We can change aspects of the mouse event.
	// For example, we can use CGEventSetLoction(event, newLocation).
	// Here, we just print the location.

	printf("(%f, %f)\n", location.x, location.y);
	//We must return the event for it to be useful.
			return event;
}
/*

 * Class:     edu_uci_ics_luci_cacophony_sensors_UIActivityMac
 * Method:    nativeShutdown
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_edu_uci_ics_luci_cacophony_sensors_UIActivityMac_nativeShutdown
  (JNIEnv *env, jclass ob)
{
	fprintf(stderr,"native code: uiactivity: nativeShutdown\n");
	//fprintf(stderr,"Ending\n");
	while(runLoopReference == NULL){
		fprintf(stderr,"uiActivity jni waiting for initialization (so we can shutdown).  Maybe a double shutdown call?\n");
		sleep(1);
	}
	//CFRunLoopStop(CFRunLoopGetCurrent());
	CFRunLoopStop(runLoopReference);
	runLoopReference = NULL;
	//fprintf(stderr,"Ending2\n");
}

int main(void)
{
	CFMachPortRef   eventTap;
	CGEventMask     eventMask;
	CFRunLoopSourceRef runLoopSource;

	//The screen size of the primary display.
	screenBounds = CGDisplayBounds(CGMainDisplayID());

	printf("The main screen is %dx%d\n", (int) screenBounds.size.width, (int) screenBounds.size.height);

	//Create an event tap.We are interested in mouse movements.
	eventMask = (1 << kCGEventMouseMoved);
	eventMask = (kCGEventLeftMouseDown | kCGEventRightMouseDown | kCGEventOtherMouseDown | eventMask);

	eventTap = CGEventTapCreate(kCGSessionEventTap, kCGHeadInsertEventTap, 0, eventMask, myCGEventCallback, NULL);

	if (!eventTap) {
		fprintf(stderr, "failed to create event tap\n");
		exit(1);
	}
	//Create a run loop source.
		runLoopSource = CFMachPortCreateRunLoopSource(kCFAllocatorDefault, eventTap, 0);
	//Add to the current run loop.
		CFRunLoopAddSource(CFRunLoopGetCurrent(), runLoopSource, kCFRunLoopCommonModes);
	//Enable the event tap.
		CGEventTapEnable(eventTap, true);
	//Set it all running.
		CFRunLoopRun();
	//In a real program, one would have arranged for cleaning up.
		exit(0);
}
