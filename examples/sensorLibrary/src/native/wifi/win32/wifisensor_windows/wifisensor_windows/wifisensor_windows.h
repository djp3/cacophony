#include <stdlib.h>
#include <jni.h>

#include "WiFiSensor_Windows_Nomatic.h"
#include "Wifi_Win_Available/Wifi_Win_Available.h"

using namespace std;
using namespace Wifi_Win_Available;
using namespace System::Runtime::InteropServices;

/*
 * Class:     edu_uci_ics_luci_nomatic_service_sensors_WiFiSensor_Windows
 * Method:    getAPSSID
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_edu_uci_ics_luci_nomatic_service_sensors_WiFiSensor_1Windows_getAPSSID
  (JNIEnv *env, jclass ob)
{
	printf("\t\tC++ Calling getAPSSID\n\n");
	WifiAvailable^ wa;
	char native_result[256];
	wa->GetConnectedSSID(native_result);
	jstring result =  env->NewStringUTF(native_result);
	printf("\t\tC++ Leaving getAPSSID\n\n");
	return result;
}

/*
 * Class:     edu_uci_ics_luci_nomatic_service_sensors_WiFiSensor_Windows
 * Method:    getAPSignalStrength
 * Signature: ()I
 */
JNIEXPORT jobject JNICALL Java_edu_uci_ics_luci_nomatic_service_sensors_WiFiSensor_1Windows_getAPSignalStrength
  (JNIEnv *env, jclass ob)
{
	printf("\t\tC++ Calling getAPSignalStrength\n\n");
	WifiAvailable^ wa;
	printf("\t\tC++ Leaving getAPSignalStrength\n\n");
	return (jobject)(wa->GetConnectedSignalStrength());
}


/*
 * Class:     edu_uci_ics_luci_nomatic_service_sensors_WiFiSensor_Mac
 * Method:    _getAPMAC
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_edu_uci_ics_luci_nomatic_service_sensors_WiFiSensor_1Windows__1getAPMAC
  (JNIEnv * env, jclass ob)
{
	Console::WriteLine("\n\n\t\tC++ Calling getAPMAC\n\n");
	WifiAvailable^ wa;
	char native_result[sizeof(char)*32];
	wa->GetConnectedWifi(native_result);
	jstring result =  env->NewStringUTF(native_result);
	Console::WriteLine("\n\n\t\tC++ Leaving getAPMAC\n\n");
	return result;
}

/*
 * Class:     edu_uci_ics_luci_nomatic_service_sensors_WiFiSensor_Mac
 * Method:    _getAllAPMAC
 * Signature: ()[Ljava/lang/Object;
 */
JNIEXPORT jobjectArray JNICALL Java_edu_uci_ics_luci_nomatic_service_sensors_WiFiSensor_1Windows__1getAllAPMAC
  (JNIEnv * env, jclass ob)
{
	Console::WriteLine("\t\tC++ Calling getAllAPMAC\n\n");
	WifiAvailable^ wa;
	jobjectArray allap_result = wa->GetAvaiableWifi(env);
	Console::WriteLine("\t\tC++ Leaving getAllAPMAC\n\n");
	return  allap_result;
}