#include <PowerSourceSensor_Windows.h>
#include <iostream>
#include <windows.h>

#define UNKNOWN 0xFFFFFFFF

using namespace std;

/*
 * Class:     PowerSourceSensor_Windows
 * Method:    dump
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_edu_uci_ics_luci_nomatic_service_sensors_PowerSourceSensor_1Windows_dump (JNIEnv *, jobject)
{
	SYSTEM_POWER_STATUS status;

    GetSystemPowerStatus( &status );
    int life = status.BatteryLifePercent;
    int secs = status.BatteryLifeTime;
    int acStat = status.ACLineStatus;

    cout << life << "%   ->   ";
    switch (status.BatteryFlag) {
    	case 1: cout << "High";
        break;
               case 2: cout << "Low";
                    break;
               case 4: cout << "Critical";
                    break;
               case 8: cout << "Charging";
                    break;
               case 128: cout << "No system battery";
                    break;
               case 256: cout << "Unknown status";
                    break;
        }

        if (secs = UNKNOWN) {
                cout << endl << "Amount of time remaining is unknown";
        }
        else cout << endl << secs << " seconds remaining";

        if (acStat == 0)
        {
            cout << endl << "AC is NOT Connected.";
        }
        else if (acStat == 1)
        {
            cout << endl << "AC is Connected.";
        }
        else
            cout << endl << "AC Status is Unknown.";

        Sleep( 2000 );

}

/*
 * Class:     PowerSourceSensor_Windows
 * Method:    getBatteryCharge
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_edu_uci_ics_luci_nomatic_service_sensors_PowerSourceSensor_1Windows_getBatteryCharge (JNIEnv *, jobject)
{
   SYSTEM_POWER_STATUS status;
   GetSystemPowerStatus (&status);

   int life = status.BatteryLifePercent;
   return (jint)life;
}

/*
 * Class:     PowerSourceSensor_Windows
 * Method:    isAcConnected
 * Signature: ()
 */
JNIEXPORT jboolean JNICALL
Java_edu_uci_ics_luci_nomatic_service_sensors_PowerSourceSensor_1Windows_isAcConnected(JNIEnv *, jobject) {
//   return true;
   SYSTEM_POWER_STATUS status;
   GetSystemPowerStatus (&status);

   int acStat = status.ACLineStatus;
   if (acStat == 0)
   {
      return false;
   }
   else if (acStat == 1)
   {
      return true;
   }
   else
      return true;
}

/*
 * Class:     edu_uci_ics_luci_nomatic_service_sensorsPowerSourceSensor_Windows_ * Method:    getBatteryFlag
 * Signature: ()I
 */
JNIEXPORT jbyte JNICALL
Java_edu_uci_ics_luci_nomatic_service_sensors_PowerSourceSensor_1Windows_getBatteryFlag (JNIEnv *, jobject)
  {
  	SYSTEM_POWER_STATUS status;
   GetSystemPowerStatus (&status);
   return status.BatteryFlag;
}

/*
 * Class:     edu_uci_ics_luci_nomatic_service_sensorsPowerSourceSensor_Windows
 * Method:    getSystemPowerStatus
 * Signature: ()Lcom/ss/SystemPowerStatus;
 */
	JNIEXPORT jobject JNICALL
	Java_edu_uci_ics_luci_nomatic_service_sensors_PowerSourceSensor_1Windows_getSystemPowerStatus (JNIEnv *env, jobject thisObj)
	  {
	  	SYSTEM_POWER_STATUS status;
	    GetSystemPowerStatus (&status);
	    
	    jclass clazz;
		jclass clazzTemp;
		jmethodID mid;
		jobject tempObj;
		
	    //first instantiate the java object
		clazz = env->FindClass("com/ss/SystemPowerStatus");//env->GetObjectClass(env, retObj);
		mid  = env->GetMethodID(clazz, "<init>", "()V");	
	    
	    //Get java Object
		tempObj = env->NewObject(clazz, mid);
		
		//Call Setters on java object.
		jmethodID bflagmid = env->GetMethodID(clazz, "setBatteryFlag", "(B)V");
		if (bflagmid != 0)
			env->CallVoidMethod(tempObj, bflagmid, (jbyte)status.BatteryFlag);
			
		jmethodID aclsmid = env->GetMethodID(clazz, "setAcLineStatus", "(B)V");
		if (aclsmid != 0)
			env->CallVoidMethod(tempObj, aclsmid, (jbyte)status.ACLineStatus);
			
		jmethodID blpmid = env->GetMethodID(clazz, "setBatteryLifePercent", "(B)V");
		if (blpmid != 0)
			env->CallVoidMethod(tempObj, blpmid, (jbyte)status.BatteryLifePercent);
			
		jmethodID resmid = env->GetMethodID(clazz, "setReserved1", "(B)V");
		if (resmid != 0)
			env->CallVoidMethod(tempObj, resmid, (jbyte)status.Reserved1);
			
		jmethodID bltmid = env->GetMethodID(clazz, "setBatteryLifeTime", "(I)V");
		if (bltmid != 0)
			env->CallVoidMethod(tempObj, bltmid, (jint)status.BatteryLifeTime);
		
		jmethodID bfltmid = env->GetMethodID(clazz, "setBatteryFullLifeTime", "(I)V");
		if (bfltmid != 0)
			env->CallVoidMethod(tempObj, bfltmid, (jint)status.BatteryFullLifeTime);
		
		// return java object.
		return tempObj;
}
