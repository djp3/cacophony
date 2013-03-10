// wapNative.cpp : Defines the entry point for the DLL application.
//

#include "stdafx.h"
#include <windows.h>
#include <winioctl.h>
#include <Iphlpapi.h>
#include <iostream>

//note
//to compile this code, please download the recent version of windows ddk
#include "E:\WinDDK\6001.18000\inc\api\ntddndis.h"
//#include "E:\WinDDK\6001.18000\src\network\ndis\ndisprot\sys\nuiouser.h"

//bug free comparisons
#include <shlwapi.h>

#include "BssList.h"


using namespace std;


bool getFirstWirelessDeviceName(char *OUTcDevName);
void bin2Ascii(UCHAR *INcBin, char *OUTcAsc);
void getConnectedNetworkInfo(HANDLE hDev, char *OUTcSSID);

BOOL APIENTRY DllMain( HANDLE hModule, 
                       DWORD  ul_reason_for_call, 
                       LPVOID lpReserved
					 )
{
    return TRUE;
}


JNIEXPORT jobjectArray JNICALL Java_BssList_gestAPsInfo
(JNIEnv *env, jclass clazz){
	
	char szFirstWDevName[1024] = {0};
	char szDevPath[1024*2] = {0};
	if(false == getFirstWirelessDeviceName(szFirstWDevName)){
		return 0;
	}
	sprintf(szDevPath, "\\\\.\\%s", szFirstWDevName);

	clazz = env->FindClass("BssInfo");
	if(clazz == 0){
		return 0;
	}

	HANDLE hDev = CreateFile(
		szDevPath,
		GENERIC_READ|GENERIC_WRITE,
		0,
		NULL,
		OPEN_EXISTING,
		FILE_ATTRIBUTE_NORMAL,
		(HANDLE) INVALID_HANDLE_VALUE); 
	if(hDev == INVALID_HANDLE_VALUE){
		return 0;
	}

	char cConnectedToSSID[50] = {0};
	//get the network SSID we are currently conneted to so that we can mark
	//it while iterating over the visible networks
	getConnectedNetworkInfo(hDev, cConnectedToSSID);


	DWORD oid = OID_802_11_BSSID_LIST, dwBytesReturned=0;
	DWORD dwCtlCode = IOCTL_NDIS_QUERY_GLOBAL_STATS;
	//one item takes about 200 bytes on average, so we have
	//enough room for 153 stations
	char QueryBuffer[1024*30] = {0};


	jobjectArray retArray = NULL; 
	//we use it to convert the binary mac to ascii mac... temp buffer
	char temp_mac[20] = {0};

	if (DeviceIoControl(hDev,
		dwCtlCode,
		(LPVOID) &oid,
		sizeof(OID_802_11_BSSID_LIST),
		(LPVOID) &QueryBuffer[0],
		sizeof(QueryBuffer),
		&dwBytesReturned,
		NULL))
	{
		NDIS_802_11_BSSID_LIST_EX *bssList = (NDIS_802_11_BSSID_LIST_EX*)QueryBuffer;
		NDIS_WLAN_BSSID_EX *bssInfo = (NDIS_WLAN_BSSID_EX*)(bssList->Bssid);
		ULONG currIndex = 0;

		//allocate the return array here
		retArray = env->NewObjectArray(bssList->NumberOfItems, clazz, 0);

		for(int i=0; i<bssList->NumberOfItems; i++){
		//	printf("%10d %s\n", repeat, bssInfo->Ssid.Ssid);
			jobject objInfo = env->AllocObject(clazz);

			//set the SSID
			jfieldID fldId = env->GetFieldID(clazz, "ssid","Ljava/lang/String;");
			if(fldId == 0){
				break;
			}
			jstring sSSID = env->NewStringUTF((char*)bssInfo->Ssid.Ssid);
			env->SetObjectField(objInfo, fldId, sSSID);

			//set the MAC address
			fldId = env->GetFieldID(clazz, "mac","Ljava/lang/String;");
			if(fldId == 0){
				break;
			}
			bin2Ascii((UCHAR*)&bssInfo->MacAddress, temp_mac);
			jstring sMAC = env->NewStringUTF((char*)temp_mac);
			env->SetObjectField(objInfo, fldId, sMAC);

			//Set whether we are connected to this network
			if(strcmp(cConnectedToSSID,(char*)bssInfo->Ssid.Ssid) == 0){
				fldId = env->GetFieldID(clazz, "isConnectedTo","Z");
				if(fldId == 0){
					break;
				}
				env->SetBooleanField(objInfo, fldId, true);
			}
            
			env->SetObjectArrayElement(retArray, i, objInfo);
            
			currIndex += bssInfo->Length;
			bssInfo = (NDIS_WLAN_BSSID_EX*)((char*)bssList->Bssid + currIndex);
		}

		CloseHandle(hDev);
	}
    
	return retArray;
}


bool 
getFirstWirelessDeviceName(char *OUTcDevName){
	bool ret = false;

	DWORD res = ERROR_SUCCESS;
	ULONG ulLen = 0;
	IP_ADAPTER_INFO *adpInfo = new IP_ADAPTER_INFO[1];

    res = GetAdaptersInfo(adpInfo, &ulLen);
	if(res == ERROR_BUFFER_OVERFLOW){
		delete adpInfo;
		adpInfo = new IP_ADAPTER_INFO[ulLen / sizeof(IP_ADAPTER_INFO)];
	}

	res = GetAdaptersInfo(adpInfo, &ulLen);
	if(res == NO_ERROR){
		for(int i=0; i<ulLen/sizeof(IP_ADAPTER_INFO); i++){
			if(StrStrI(adpInfo[i].Description,"wireless") > 0){
				strcpy(OUTcDevName, adpInfo[i].AdapterName);
				ret = true;
				break;
			}
		}
	}

	return ret;
}

void 
bin2Ascii(UCHAR *INcBin, char *OUTcAsc){

	sprintf(OUTcAsc, "%02X-%02X-%02X-%02X-%02X-%02X",
		INcBin[0],
		INcBin[1],
		INcBin[2],
		INcBin[3],
		INcBin[4],
		INcBin[5]
		);
}

//getConnectedNetworkInfo
//returns the ssid to which the adapter is currently connected
//if no connection exists, then nothing is copied to the output buffer
void 
getConnectedNetworkInfo(HANDLE hDev, char *OUTcSSID){
 
	DWORD oid = OID_802_11_SSID, dwBytesReturned=0;
	DWORD dwCtlCode = IOCTL_NDIS_QUERY_GLOBAL_STATS;
	//SSID can be 32 bytes and ULONG 4 bytes
	char QueryBuffer[50] = {0};

	if (DeviceIoControl(hDev,
		dwCtlCode,
		(LPVOID) &oid,
		sizeof(OID_802_11_SSID),
		(LPVOID) &QueryBuffer[0],
		sizeof(QueryBuffer),
		&dwBytesReturned,
		NULL)){

		NDIS_802_11_SSID *pSSID = (NDIS_802_11_SSID*) QueryBuffer;
		if(pSSID->SsidLength > 0){
			strncpy(OUTcSSID, (char*)pSSID->Ssid, pSSID->SsidLength);
		}//if(pSSID->SsidLength > 0){
	}//if
}