//****************************************************************************
//                     WiFi-Manager DLL interface
//                    Copyright (c) 2007-2009 Nicomsoft
//                 Copyright (c) 2007-2009 Michael Kapustin
//                       support@nicomsoft.com
//                     www.nicomsoft.com/wifiman/
//
//****************************************************************************


#define WIFIAPI	extern "C" __declspec(dllimport) DWORD __stdcall

// Types

//for GetAvailableNetworkAuthMode
typedef enum _NDIS_802_11_AUTHENTICATION_MODE
{
    Ndis802_11AuthModeOpen,
    Ndis802_11AuthModeShared,
    Ndis802_11AuthModeAutoSwitch,
    Ndis802_11AuthModeWPA,
    Ndis802_11AuthModeWPAPSK,
    Ndis802_11AuthModeWPANone,
    Ndis802_11AuthModeWPA2,
    Ndis802_11AuthModeWPA2PSK,
    Ndis802_11AuthModeMax               // Not a real mode, defined as upper bound
} NDIS_802_11_AUTHENTICATION_MODE, *PNDIS_802_11_AUTHENTICATION_MODE;


//for GetAvailableNetworkCipherMode
typedef enum _DOT11_CIPHER_ALGORITHM {
    DOT11_CIPHER_ALGO_NONE = 0x00,
    DOT11_CIPHER_ALGO_WEP40 = 0x01,
    DOT11_CIPHER_ALGO_TKIP = 0x02,
    DOT11_CIPHER_ALGO_CCMP = 0x04,
    DOT11_CIPHER_ALGO_WEP104 = 0x05,
    DOT11_CIPHER_ALGO_WPA_USE_GROUP = 0x100,
    DOT11_CIPHER_ALGO_RSN_USE_GROUP = 0x100,
    DOT11_CIPHER_ALGO_WEP = 0x101,
    DOT11_CIPHER_ALGO_IHV_START = 0x80000000,
    DOT11_CIPHER_ALGO_IHV_END = 0xffffffff
} DOT11_CIPHER_ALGORITHM, * PDOT11_CIPHER_ALGORITHM;

// for GetAvailableNetworkType
typedef enum _DOT11_BSS_TYPE {
    dot11_BSS_type_infrastructure = 1,
    dot11_BSS_type_independent = 2,
    dot11_BSS_type_any = 3
} DOT11_BSS_TYPE, * PDOT11_BSS_TYPE;


// Constants

// Error codes
#define ERROR_OFFSET				0x70000000
#define ERROR_INIT					0x70000001
#define ERROR_APIENUM				0x70000002
#define ERROR_INVALIDADAPTER		0x70000003
#define ERROR_SMALLBUFFER			0x70000004
#define ERROR_APIQUERY				0x70000005
#define ERROR_ADAPTERBUSY			0x70000006
#define ERROR_INVALIDNETWORK		0x70000007
#define ERROR_INVALIDPROFILE		0x70000008
#define ERROR_APISET				0x70000009
#define ERROR_PROFILENOTFOUND		0x7000000A
#define ERROR_INVALIDPARAMETER		0x7000000B
#define ERROR_DATACHANGED			0x7000000C
#define ERROR_FAIL					0x7000000D //unspecified error
#define ERROR_ADAPTERNOTFOUND		0x7000000E
#define ERROR_SCAN					0x7000000F
#define ERROR_NOCURRENTNETWORK		0x70000010
#define ERROR_NOTSUPPORTED			0x70000011
#define ERROR_NOSERVICE				0x70000012
#define ERROR_ACCESSDENIED			0x70000013
#define ERROR_SERVICEDISABLED		0x70000014
#define ERROR_PINGTIMEOUT			0x70000015
#define ERROR_PINGFAIL				0x70000016
#define ERROR_HOSTRESOLVE			0x70000017
#define ERROR_UNSUPPORTEDPLATFORM	0x70000018
#define ERROR_PROFILESETTINGS		0x70000019
#define ERROR_AUTHERROR				0x7000001A

#define ERROR_TOOMANYPROFILES		0x70000020
#define ERROR_CREATEXMLDOCUMENT		0x70000021
#define ERROR_INVALIDTEMPLATE		0x70000022
#define ERROR_LOADXML				0x70000023
#define ERROR_SAVEXML				0x70000024
#define ERROR_OPTIONNOTFOUND		0x70000025
#define ERROR_INVALIDOPTIONVALUE	0x70000026
#define ERROR_BADPROFILE			0x70000027
#define ERROR_RECORDNOTFOUND		0x70000028
#define ERROR_MACSET				0x70000030

#define ERROR_NDISBUSY				0x70000040 //some service already uses adapter 
#define ERROR_OLDDRIVERVERSION		0x70000041 //version of wifimanio.sys is too old

#define ERROR_DRIVERNOTINSTALLED		0x70000051

#define ERROR_DEMOVERSION_NOTSUPPORTED		0x700000F0
#define ERROR_DEMOVERSION_EXPIRED			0x700000F1


//Constants

#define UNKNOWNVALUEOFFSET			0x100

#define TEMPLATE_EMPTY				0
#define TEMPLATE_UNSECURE_OPEN		1
#define TEMPLATE_WEP_OPEN			2
#define TEMPLATE_UNSECURE_SHARED	3
#define TEMPLATE_WEP_SHARED			4

//for Get/SetAdapterOption function
#define ADAPTER_NETTYPE				0x00000003 //type of networks to access
#define ADAPTER_USEWINDOWS			0x00008000 //use windows to configure adapter
#define ADAPTER_CONNECTFLAG			0x00004000 //automatically connect to non preferred networks
#define ADAPTER_INTFOPCODE			0x10000000 //flag for WLAN_INTF_OPCODE operation to call WlanSetInterface directly

#define NETTYPE_ANY					0x00000002 //any networks
#define NETTYPE_APONLY				0x00000001 //AP networks only (infrastructure)
#define NETTYPE_ADHOCONLY			0x00000000 //computer-to-computer networks only (ad-hoc)

#define USEWINDOWS_FLAG				0x00008000 //use windows to configure adapter
#define CONNECTFLAG_FLAG			0x00004000 //automatically connect to non preferred networks

//for SetStorageOptions function flags (Advanced WiFi-Manager only)
#define STORAGE_FILE				0
#define STORAGE_MEMORY				1

//for Get/SetLibraryOption function
//OPT_MODE must be set BEFORE calling any other functions, otherwise SetLibraryOption will fail
#define OPT_MODE					0 //see OPT_MODE_AUTO and OPT_MODE_NDISMODE
#define OPT_ADVWIFIMAN				1 //checks if it's Advanced WiFi-Manager library (1) or not (0)
#define OPT_WLANCONNECTFLAGS			2 //dwFlags field of WLAN_CONNECTION_PARAMETERS structure at connection in Vista, see MSDN for possible values, default 0
#define OPT_SCANDELAY				3 //available networks scan delay in milliseconds (Advanced WiFi-Manager only), default 3000ms
#define OPT_XPNATIVEWIFI			4 //use NativeWiFi for XP patch from Microsoft if possible instead of WZC API
#define OPT_SKIPPROFILECHECK			6 //dont check profile settings at conection

#define OPT_MODE_AUTO				0 //Advanced WiFi-Manager only: detect OS and use Ndis, WCZ or NativeWiFi (default mode)
#define OPT_MODE_NDIS				1 //Advanced WiFi-Manager only: use Ndis for any OS



// WiFi-Manager Functions 
WIFIAPI EnableLog(char *Path, int RemoveOld);
WIFIAPI DisableLog();
WIFIAPI WriteLog(char * str);

WIFIAPI EnumerateAdapters();
WIFIAPI GetAdapterGUID(int AdapterInd, LPTSTR Guid, int MaxCount);
WIFIAPI GetAdapterName(int AdapterInd, LPTSTR Name, int MaxCount);
WIFIAPI CheckAdapterBusyStatus(int AdapterInd, int Timeout);
WIFIAPI GetAdapterOption(int AdapterInd, int OptionCode);
WIFIAPI SetAdapterOption(int AdapterInd, int OptionCode, int Value);
WIFIAPI CheckNdisAvailable(int AdapterInd);
WIFIAPI IsAdapterNativeWIFI(int AdapterInd);
WIFIAPI GetAdapterInfo(int AdapterInd, char *Str, int MaxCount);
WIFIAPI FreeAllResources();

WIFIAPI IsNativeWIFI(); //checks if Windows uses NativeWiFi for WiFi management
WIFIAPI IsDirectWIFI(); //checks if NDIS is used for WiFi management
WIFIAPI GetLibraryOption(DWORD OptionIndex);
WIFIAPI SetLibraryOption(DWORD OptionIndex, DWORD Value);
WIFIAPI GetWIFIManagerVersion();
WIFIAPI SetStorageOptions(char * FileName, int Flags);
WIFIAPI SetStorageData(char * Data, int Length);
WIFIAPI GetStorageData(char * Data, int MaxLength);
WIFIAPI InstallDriver(char *path);
WIFIAPI UninstallDriver();
WIFIAPI IsInstalledDriver();

WIFIAPI GetWIFIServiceStatus();
WIFIAPI SetWIFIServiceStatus(int StartService);

WIFIAPI GetAdapterCurrentIPInfo(int AdapterInd, char * IP, char * Mask, char * Gateway, int MaxCount);
WIFIAPI GetAdapterIPInfo(int AdapterInd, char * IP, char * Mask, char * Gateway, int * DHCPEnabled, int MaxCount);
WIFIAPI SetAdapterIPInfo(int AdapterInd, char * IP, char * Mask, char * Gateway, int DHCPEnabled);
WIFIAPI GetAdapterDNS(int AdapterInd, char * DNS, int MaxCount);
WIFIAPI SetAdapterDNS(int AdapterInd, char * DNS);
WIFIAPI GetAdapterMac(int AdapterInd, BYTE * d1, BYTE * d2, BYTE * d3, BYTE * d4, BYTE * d5, BYTE * d6);
WIFIAPI GetAdapterDefaultMac(int AdapterInd, BYTE * d1, BYTE * d2, BYTE * d3, BYTE * d4, BYTE * d5, BYTE * d6);
WIFIAPI SetAdapterMac(int AdapterInd, BYTE d1, BYTE d2, BYTE d3, BYTE d4, BYTE d5, BYTE d6);
WIFIAPI SetAdapterDefaultMac(int AdapterInd);
WIFIAPI GetAdapterFirewallState(int AdapterInd);
WIFIAPI EnableAdapterFirewall(int AdapterInd);
WIFIAPI DisableAdapterFirewall(int AdapterInd);
WIFIAPI GetAdapterTraffic(int AdapterInd, DWORD * InDataH, DWORD * InDataL, DWORD * OutDataH, DWORD * OutDataL);


WIFIAPI Ping(char * Host, int Timeout);

WIFIAPI EnumerateAvailableNetworks(int AdapterInd, int Search);
WIFIAPI GetAvailableNetworkName(int AdapterInd, int NetworkInd, LPTSTR Name, int MaxCount);
WIFIAPI GetAvailableNetworkIndex(int AdapterInd, char * NetName);
WIFIAPI GetAvailableNetworkSignalQuality(int AdapterInd, int NetworkInd);
WIFIAPI GetAvailableNetworkRSSI(int AdapterInd, int NetworkInd);
WIFIAPI GetAvailableNetworkType(int AdapterInd, int NetworkInd);
WIFIAPI IsAvailableNetworkSecure(int AdapterInd, int NetworkInd);
WIFIAPI GetAvailableNetworkAuthMode(int AdapterInd, int NetworkInd);
WIFIAPI GetAvailableNetworkCipherMode(int AdapterInd, int NetworkInd);
WIFIAPI GetAvailableNetworkMac(int AdapterInd, int NetworkInd, BYTE * d1, BYTE * d2, BYTE * d3, BYTE * d4, BYTE * d5, BYTE * d6);
WIFIAPI GetAvailableNetworkChannel(int AdapterInd, int NetworkInd);

WIFIAPI EnumerateProfiles(int AdapterInd);
WIFIAPI GetProfileName(int AdapterInd, int ProfileInd, LPTSTR Name, int MaxCount);
WIFIAPI GetProfileNetworkIndex(int AdapterInd, char * NetName);

WIFIAPI GetCurrentNetworkName(int AdapterInd, LPTSTR Name, int MaxCount);
WIFIAPI GetCurrentNetworkChannel(int AdapterInd);
WIFIAPI GetCurrentNetworkSpeed(int AdapterInd);
WIFIAPI GetCurrentNetworkMac(int AdapterInd, BYTE * d1, BYTE * d2, BYTE * d3, BYTE * d4, BYTE * d5, BYTE * d6);

WIFIAPI ConnectToNetwork(int AdapterInd, LPTSTR Name);
WIFIAPI ConnectToNetworkByMAC(int AdapterInd, BYTE d1, BYTE d2, BYTE d3, BYTE d4, BYTE d5, BYTE d6);
WIFIAPI DisconnectFromNetwork(int AdapterInd);

WIFIAPI MoveProfile(int AdapterInd, int ProfileInd, int NewIndex);
WIFIAPI DeleteProfile(int AdapterInd, int ProfileInd);

WIFIAPI OpenProfilesUI(int AdapterInd);
WIFIAPI RepairAdapter(int AdapterInd);

//these functions create temporary profiles
WIFIAPI CreateTmpProfile(int Template);
WIFIAPI FreeTmpProfile(int ProfileHandle);
WIFIAPI CloneTmpProfile(int ProfileHandle);
WIFIAPI LoadTmpProfile(char * FileName);
WIFIAPI LoadTmpProfileFromString(char * Str);
WIFIAPI GetTmpProfileFromAdapter(int AdapterInd, int ProfileInd);
//apply temporary profile to adapter
WIFIAPI SetTmpProfileToAdapter(int ProfileHandle, int AdapterInd);
//get/set option
WIFIAPI SetTmpProfileOption(int ProfileHandle, char * OptionName, char * Value);
WIFIAPI GetTmpProfileOption(int ProfileHandle, char * OptionName, char * Value, int MaxCount);
WIFIAPI DeleteTmpProfileOption(int ProfileHandle, char * OptionName);
//802.1x support
WIFIAPI SetDefaultEapConfig(int ProfileHandle, int EapEnabled, int UseWindowsCredentials);
//save to xml file
WIFIAPI SaveTmpProfile(int ProfileHandle, char * FileName);

WIFIAPI GetWindowsFirewallStatus(int * ExceptionsNotAllowed, int * NotificationsDisabled); 
WIFIAPI EnableWindowsFirewall(int ExceptionsNotAllowed, int NotificationsDisabled); 
WIFIAPI DisableWindowsFirewall();

WIFIAPI EnumerateAllNetAdapters();
WIFIAPI GetNetAdapterName(int NetAdapterInd, char * Name, int MaxCount);
WIFIAPI GetNetAdapterGUID(int NetAdapterInd, char * Guid, int MaxCount);
WIFIAPI IsNetAdapterWiFi(int NetAdapterInd);
WIFIAPI GetNetAdapterState(int NetAdapterInd);
WIFIAPI EnableNetAdapter(int NetAdapterInd);
WIFIAPI DisableNetAdapter(int NetAdapterInd);
WIFIAPI RestartNetAdapter(int NetAdapterInd);
WIFIAPI GetNetAdapterCurrentIPInfo(int NetAdapterInd, char * IP, char * Mask, char * Gateway, int MaxCount);
WIFIAPI GetNetAdapterIPInfo(int NetAdapterInd, char * IP, char * Mask, char * Gateway, int * DHCPEnabled, int MaxCount);
WIFIAPI SetNetAdapterIPInfo(int NetAdapterInd, char * IP, char * Mask, char * Gateway, int DHCPEnabled);
WIFIAPI GetNetAdapterDNS(int NetAdapterInd, char * DNS, int MaxCount);
WIFIAPI SetNetAdapterDNS(int NetAdapterInd, char * DNS);
WIFIAPI GetNetAdapterMac(int NetAdapterInd, BYTE * d1, BYTE * d2, BYTE * d3, BYTE * d4, BYTE * d5, BYTE * d6);
WIFIAPI GetNetAdapterDefaultMac(int NetAdapterInd, BYTE * d1, BYTE * d2, BYTE * d3, BYTE * d4, BYTE * d5, BYTE * d6);
WIFIAPI SetNetAdapterMac(int NetAdapterInd, BYTE d1, BYTE d2, BYTE d3, BYTE d4, BYTE d5, BYTE d6);
WIFIAPI SetNetAdapterDefaultMac(int NetAdapterInd);
WIFIAPI GetNetAdapterFirewallState(int NetAdapterInd);
WIFIAPI EnableNetAdapterFirewall(int NetAdapterInd);
WIFIAPI DisableNetAdapterFirewall(int NetAdapterInd);
WIFIAPI GetNetAdapterTraffic(int NetAdapterInd, DWORD * InDataH, DWORD * InDataL, DWORD * OutDataH, DWORD * OutDataL);


//////// UNICODE SUPPORT /////////
WIFIAPI GetAdapterGUIDW(int AdapterInd, LPCWSTR Guid, int MaxCount);
WIFIAPI GetAdapterNameW(int AdapterInd, LPCWSTR Name, int MaxCount);
WIFIAPI GetAvailableNetworkNameW(int AdapterInd, int NetworkInd, LPCWSTR Name, int MaxCount);
WIFIAPI GetAvailableNetworkIndexW(int AdapterInd, LPCWSTR NetName);
WIFIAPI GetProfileNameW(int AdapterInd, int ProfileInd, LPCWSTR Name, int MaxCount);
WIFIAPI GetProfileNetworkIndexW(int AdapterInd, LPCWSTR NetName);
WIFIAPI GetCurrentNetworkNameW(int AdapterInd, LPCWSTR Name, int MaxCount);
WIFIAPI ConnectToNetworkW(int AdapterInd, LPCWSTR Name);
WIFIAPI EnableLogW(LPCWSTR Path, int RemoveOld);
WIFIAPI WriteLogW(LPCWSTR str);
WIFIAPI GetAdapterCurrentIPInfoW(int AdapterInd, LPCWSTR IP, LPCWSTR Mask, LPCWSTR Gateway, int MaxCount);
WIFIAPI GetAdapterIPInfoW(int AdapterInd, LPCWSTR IP, LPCWSTR Mask, LPCWSTR Gateway, int * DHCPEnabled, int MaxCount);
WIFIAPI SetAdapterIPInfoW(int AdapterInd, LPCWSTR IP, LPCWSTR Mask, LPCWSTR Gateway, int DHCPEnabled);
WIFIAPI GetAdapterDNSW(int AdapterInd, LPCWSTR DNS, int MaxCount);
WIFIAPI SetAdapterDNSW(int AdapterInd, LPCWSTR DNS);
WIFIAPI PingW(LPCWSTR Host, int Timeout);
WIFIAPI LoadTmpProfileW(LPCWSTR FileName);
WIFIAPI LoadTmpProfileFromStringW(LPCWSTR Str);
WIFIAPI SetTmpProfileOptionW(int ProfileHandle, LPCWSTR OptionName, LPCWSTR Value);
WIFIAPI GetTmpProfileOptionW(int ProfileHandle, LPCWSTR OptionName, LPCWSTR Value, int MaxCount);
WIFIAPI SaveTmpProfileW(int ProfileHandle, LPCWSTR FileName);
WIFIAPI SaveTmpProfileToStringW(int ProfileHandle, LPCWSTR Str, int MaxCount);
WIFIAPI GetNetAdapterNameW(int NetAdapterInd, LPCWSTR Name, int MaxCount);
WIFIAPI GetNetAdapterGUIDW(int NetAdapterInd, LPCWSTR Guid, int MaxCount);
WIFIAPI GetNetAdapterCurrentIPInfoW(int NetAdapterInd, LPCWSTR IP, LPCWSTR Mask, LPCWSTR Gateway, int MaxCount);
WIFIAPI GetNetAdapterIPInfoW(int NetAdapterInd, LPCWSTR IP, LPCWSTR Mask, LPCWSTR Gateway, int * DHCPEnabled, int MaxCount);
WIFIAPI SetNetAdapterIPInfoW(int NetAdapterInd, LPCWSTR IP, LPCWSTR Mask, LPCWSTR Gateway, int DHCPEnabled);
WIFIAPI GetNetAdapterDNSW(int NetAdapterInd, LPCWSTR DNS, int MaxCount);
WIFIAPI SetNetAdapterDNSW(int NetAdapterInd, LPCWSTR DNS);
WIFIAPI SetStorageOptionsW(LPCWSTR FileName, int Flags);
WIFIAPI InstallDriverW(LPCWSTR path);
WIFIAPI DeleteTmpProfileOptionW(int ProfileHandle, LPCWSTR OptionName);
/////////////////////////////////
