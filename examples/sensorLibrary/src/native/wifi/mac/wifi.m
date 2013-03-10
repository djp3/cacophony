#import <Foundation/Foundation.h>
#import <Cocoa/Cocoa.h>
#import <CoreWLAN/CoreWLAN.h>

#include <mach-o/dyld.h>
#include <dlfcn.h>
#include <unistd.h>
#include <objc/objc.h>
#include <objc/objc-runtime.h>
#include <stdio.h>
#include "jni.h"

/** Utility to throw a java exception */
void throwException(JNIEnv *env, const char *why){
	if(env != NULL){
		jclass excCls = (*env)->FindClass(env, "java/lang/Exception");
		if (excCls != 0){
			(*env)->ThrowNew(env, excCls, why);
		}
	}
}


NSMutableDictionary *getAPMac(JNIEnv *env){

	//printf("interface name %s\n",[[[CWInterface interface] interfaceName] UTF8String]);

	NSMutableDictionary *myDictionary = [[NSMutableDictionary alloc] init];
	if(myDictionary != nil){
		NSString *ssid = [[CWInterface interface] ssid];
		if(ssid != nil){
			//printf("ssid = %s\n",[ssid UTF8String]);
			[myDictionary setObject:ssid forKey:@"ssid"];
		}

		NSString *bssid = [[CWInterface interface] bssid]; 
		if(bssid != nil){
			//printf("bssid = %s\n",[bssid UTF8String]);
			[myDictionary setObject:bssid forKey:@"bssid"];
		}

		NSInteger _rssi = [[CWInterface interface] rssiValue];
		if(_rssi < 0){
			NSNumber *rssi = [NSNumber numberWithInt:_rssi];
			if(rssi != nil){
				//printf("rssi = %s\n",[[rssi stringValue] UTF8String]);
				[myDictionary setObject:[rssi stringValue] forKey:@"rssi"];
			}
		}
	}

	return myDictionary;
}




NSSet *getAllAPMac(JNIEnv *env){
	NSError *err = nil;
	NSString *params = nil;
	NSSet* scan = nil;


	@try{
		scan = [[CWInterface interface] scanForNetworksWithName:params error:&err];
	}
	@catch (NSException *e){
			/*
		@try{
			NSString *one = nil;
			NSString *error = nil;
			@try{
				one = @"Unable to scan for Wi-Fi Access Points:";
				error = [one stringByAppendingString:[e reason]];
				throwException(env,[error UTF8String]);
			}
			@finally{
				one = nil;
				error = nil;
			}
		}
		@finally{
			scan = nil;
			err = nil;
		}*/
	}
	/*
	if(err != nil){
		NSString *one = nil;
		NSString *error = nil;
		@try{
			one = @"Unable to scan for Wi-Fi Access Points:";
			error = [one stringByAppendingString:[err localizedDescription]];
			throwException(env,[error UTF8String]);
		}
		@finally{
			one = nil;
			error = nil;
		}
	}*/
	err = nil;
	return scan;
}

/*
 * Class:     edu_uci_ics_luci_cacophony_sensors_WiFiMac
 * Method:    _getAPMAC
 * Signature: ()[Ljava/lang/Object;
 */
JNIEXPORT jobjectArray JNICALL Java_edu_uci_ics_luci_cacophony_sensors_WiFiMac__1getAPMAC
  (JNIEnv *env, jclass obj)
{
	fprintf(stderr,"native code: wifi: getAPMAC\n");

    jobjectArray results = (*env)->NewObjectArray(env, 3, (*env)->FindClass(env, "java/lang/String"), NULL);
	if(results == NULL){		
		throwException(env,"_getAPMac failed because results == NULL. Out of memory?");
	}
	else{
		NSMutableDictionary* singleScan = getAPMac(env);
		if(singleScan != nil){
			NSString *ssid = [singleScan objectForKey:@"ssid"];
			if(ssid != nil){
				//printf("ssid = %s\n",[ssid UTF8String]);
				(*env)->SetObjectArrayElement(env, results, 0, (*env)->NewStringUTF( env, [ssid UTF8String]));
				ssid = nil;
			}
			else{
				//printf("ssid scan failed\n");
				(*env)->SetObjectArrayElement(env, results, 0, (*env)->NewStringUTF( env, NULL));
			}

			NSString *bssid = [singleScan objectForKey:@"bssid"];
			if(bssid != nil){
				//printf("bssid = %s\n",[bssid UTF8String]);
				(*env)->SetObjectArrayElement(env, results, 1, (*env)->NewStringUTF( env, [bssid UTF8String]));
				bssid = nil;
			}
			else{
				//printf("bssid scan failed\n");
				(*env)->SetObjectArrayElement(env, results, 1, (*env)->NewStringUTF( env, NULL));
			}

			NSString *rssi = [singleScan objectForKey:@"rssi"];
			if(rssi != nil){
				//printf("rssi = %s\n",[rssi UTF8String]);
				(*env)->SetObjectArrayElement(env, results, 2, (*env)->NewStringUTF( env, [rssi UTF8String]));
				rssi = nil;
			}
			else{
				//printf("rssi scan failed\n");
				(*env)->SetObjectArrayElement(env, results, 2, (*env)->NewStringUTF( env, NULL));
			}

			[singleScan release];
			singleScan = nil;
		}
		else{
			throwException(env,"_getAPMac failed because the scan as nil.\n");
		}
	}
	return results;
}


/*
 * Class:     edu_uci_ics_luci_cacophony_sensors_WiFiMac
 * Method:    _getAllAPMAC
 * Signature: ()[[Ljava/lang/Object;
 */
JNIEXPORT jobjectArray JNICALL Java_edu_uci_ics_luci_cacophony_sensors_WiFiMac__1getAllAPMAC
  (JNIEnv *env, jclass obj)
{
	fprintf(stderr,"native code: wifi: getAllAPMAC\n");

	NSSet* multiScan = getAllAPMac(env);
	jobjectArray results = NULL;

	if(multiScan == nil){
		results = (*env)->NewObjectArray(env, 0, (*env)->FindClass(env, "java/lang/Object"), NULL);
	}
	else{
		results = (*env)->NewObjectArray(env, [multiScan count], (*env)->FindClass(env, "java/lang/Object"), NULL);
		if(results != NULL){
			int i = 0;
			for (id obj in multiScan){
				//printf("%s %s %d\n",[[obj ssid] UTF8String],[[obj bssid] UTF8String],(int) [obj rssiValue] );
				jobjectArray entry = (*env)->NewObjectArray(env, 3 , (*env)->FindClass(env, "java/lang/String"), NULL);
				if(entry != NULL){
					(*env)->SetObjectArrayElement(env, entry, 0, (*env)->NewStringUTF( env, [[obj ssid] UTF8String]));
					(*env)->SetObjectArrayElement(env, entry, 1, (*env)->NewStringUTF( env, [[obj bssid] UTF8String]));
					(*env)->SetObjectArrayElement(env, entry, 2, (*env)->NewStringUTF( env, [[NSString stringWithFormat:@"%ld", (long)[obj rssiValue] ] UTF8String]));

					(*env)->SetObjectArrayElement(env, results, i, entry);
					i++;
				}
				else{
					throwException(env,"_getAllAPMac failed because entry == NULL. Out of memory?");
				}
			}
		}
		[multiScan release];
		multiScan = nil;
	}

	if(results == NULL){		
		throwException(env,"_getAllAPMac failed because results == NULL. Out of memory?");
	}

	return results;
}


int main(int argc, const char * argv[]){

	printf("Scanning for connected WiFi Access Point\n");
	NSMutableDictionary* singleScan = getAPMac(NULL);
	if(singleScan != nil){
		NSString *ssid = [singleScan objectForKey:@"ssid"];
		if(ssid != nil){
			printf("%25s ",[ssid UTF8String]);
			//printf("ssid = %s\n",[ssid UTF8String]);
			ssid = nil;
		}
		else{
			printf("%25s ","");
			//printf("ssid scan failed\n");
		}

		NSString *bssid = [singleScan objectForKey:@"bssid"];
		if(bssid != nil){
			printf("%16s ",[bssid UTF8String]);
			//printf("bssid = %s\n",[bssid UTF8String]);
			bssid = nil;
		}
		else{
			printf("%16s ","");
			//printf("bssid scan failed\n");
		}

		NSString *rssi = [singleScan objectForKey:@"rssi"];

		if(rssi != nil){
			printf("%3s\n",[rssi UTF8String]);
			//printf("rssi = %s\n",[rssi UTF8String]);
			rssi = nil;
		}
		else{
			printf("%3s\n","");
			//printf("rssi scan failed\n");
		}

		[singleScan release];
		singleScan = nil;
	}

	printf("\nScanning for visible WiFi Access Points\n");

	NSSet* multiScan = getAllAPMac(NULL);
	if(multiScan != nil){
		for (id obj in multiScan){
			printf("%25s %16s %3d\n",[[obj ssid] UTF8String],[[obj bssid] UTF8String],(int) [obj rssiValue] );

		}
		[multiScan release];
		multiScan = nil;
	}

	return (0);
}



