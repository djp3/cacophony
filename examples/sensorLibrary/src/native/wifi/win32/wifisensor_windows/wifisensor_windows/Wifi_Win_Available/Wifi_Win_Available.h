// Wifi_Win_Available.h
#define _WIN32_DCOM

#include <windows.h>
#include "WiFiMan.h"
#include <string>

#import "WiFiMan.dll"

#pragma comment(lib, "Wifi_Win_Available/WiFiMan.lib")
#pragma comment(lib, "Wifi_Win_Available/wifiman_x64.lib")

using namespace std;
using namespace System;

// get win32 error from HRESULT
#define WIN32_FROM_HRESULT(hr)           \
    (SUCCEEDED(hr) ? ERROR_SUCCESS :    \
        (HRESULT_FACILITY(hr) == FACILITY_WIN32 ? HRESULT_CODE(hr) : (hr)))

namespace Wifi_Win_Available {

	public ref class WifiAvailable
	{
		__stdcall WifiAvailable(){
		}
		public:void GetConnectedWifi(char result[])
				   {
							Console::WriteLine("\n\n\t\tGetConnectedWifi\n\n");
					   		int res = EnumerateAdapters();
							if (res > ERROR_OFFSET)
							{
								//EnumerateAdapters ERROR;
								return;
							}
							
							BYTE d1, d2, d3, d4, d5, d6;

							res = GetCurrentNetworkMac(0, &d1, &d2, &d3, &d4, &d5, &d6);
							if (res > ERROR_OFFSET)
							{
								//EnumerateAdapters ERROR;
								return;
							}

							res = EnumerateAvailableNetworks(0, 1);
							int n = GetAvailableNetworkSignalQuality(0, 0);
							if (n > ERROR_OFFSET)
						    {
								n = 0;
							}
							n = 100 - n;
							
							sprintf(result, "%x:%x:%x:%x:%x:%x(%d)", d1, d2, d3, d4, d5, d6, n);
							Console::WriteLine("\n\n\t\tLEAVING GetConnectedWifi\n\n");
							FreeAllResources();
							return;
					}
		public:jobjectArray GetAvaiableWifi(JNIEnv * env)
				   {
							Console::WriteLine("\n\n\t\tGetAvailableWifi\n\n");

							int res = EnumerateAdapters();
							
							res = EnumerateAvailableNetworks(0, 1);

							jobjectArray results = env->NewObjectArray(res, env->FindClass("java/lang/String"), 0);

							Console::WriteLine("\n\n\t\TEST a\n\n");

							if (res > ERROR_OFFSET)
							{
								//EnumerateAdapters ERROR;
								return results;
							}
							
							Console::WriteLine("\n\n\t\TEST b\n\n");

							for(int i = 0; i < res; i++)
							{
								BYTE d1, d2, d3, d4, d5, d6;

								Console::WriteLine("\n\n\t\TEST c #"+i+"\n\n");
					
								GetAvailableNetworkMac(0, i, &d1, &d2, &d3, &d4, &d5, &d6);

								Console::WriteLine("\n\n\t\TEST d #"+i+"\n\n");

								int n = GetAvailableNetworkSignalQuality(0, i);

								if (n > ERROR_OFFSET)
						        {
									n = 0;
								}

								n = 100 - n;

								Console::WriteLine("\n\n\t\TEST e #"+i+"\n\n");

					   			char mac_address[256];

								Console::WriteLine("\n\n\t\TEST 1 #"+i+"\n\n");

								sprintf(mac_address, "%X:%X:%X:%X:%X:%X(%d)", d1, d2, d3, d4, d5, d6, n);

								Console::WriteLine("\n\n\t\TEST 2 #"+i+"\n\n");
							
								env->SetObjectArrayElement(results, i, env->NewStringUTF(mac_address) );

								Console::WriteLine("\n\n\t\TEST 3 #"+i+"\n\n");
							}

							Console::WriteLine("\n\tHave "+sizeof(results)/sizeof(jobjectArray) +" elements\n\n");
							Console::WriteLine("\n\n\t\tLEAVING GetAvailableWifi\n\n");
							FreeAllResources() ;
							return results;
				   }

		public:void GetConnectedSSID(char buf[])
			   {
							Console::WriteLine("\n\n\t\tGetConnectedSSID\n\n");
							int res = EnumerateAdapters();
							
							res = GetCurrentNetworkName(0, LPTSTR(buf), 256);

							Console::WriteLine("\n\n\t\tLEAVING GetConnectedSSID\n\n");
							FreeAllResources();							
			   }
			   
		public:int GetConnectedSignalStrength()
			   {
							Console::WriteLine("\n\n\t\tGetConnectedSignalStrength\n\n");
							int res = EnumerateAvailableNetworks(0, 1);
							int n = GetAvailableNetworkSignalQuality(0, 0);
							if (n > ERROR_OFFSET)
						    {
								n = 0;
							}
							Console::WriteLine("\n\n\t\tLEAVING GetConnectedSignalStrength\n\n");
							FreeAllResources() ;
							return 100 - n;
			   }
	};
}
