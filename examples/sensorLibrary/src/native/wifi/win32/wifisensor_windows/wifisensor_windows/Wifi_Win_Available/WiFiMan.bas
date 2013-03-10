Attribute VB_Name = "WiFiMan"

'****************************************************************************
'                     WiFi-Manager DLL interface
'                    Copyright (c) 2007-2009 Nicomsoft
'                 Copyright (c) 2007-2009 Michael Kapustin
'                       support@nicomsoft.com
'                     www.nicomsoft.com/wifiman/
'****************************************************************************

'Constants

'for GetAvailableNetworkAuthMode
Public Const Ndis802_11AuthModeOpen = 0
Public Const Ndis802_11AuthModeShared = 1
Public Const Ndis802_11AuthModeAutoSwitch = 2
Public Const Ndis802_11AuthModeWPA = 3
Public Const Ndis802_11AuthModeWPAPSK = 4
Public Const Ndis802_11AuthModeWPANone = 5
Public Const Ndis802_11AuthModeWPA2 = 6
Public Const Ndis802_11AuthModeWPA2PSK = 7

'for GetAvailableNetworkCipherMode
Public Const DOT11_CIPHER_ALGO_NONE = &H0
Public Const DOT11_CIPHER_ALGO_WEP40 = &H1
Public Const DOT11_CIPHER_ALGO_TKIP = &H2
Public Const DOT11_CIPHER_ALGO_CCMP = &H4
Public Const DOT11_CIPHER_ALGO_WEP104 = &H5
Public Const DOT11_CIPHER_ALGO_WPA_USE_GROUP = &H100
Public Const DOT11_CIPHER_ALGO_WEP = &H101

'for GetAvailableNetworkType
Public Const dot11_BSS_type_infrastructure = 1
Public Const dot11_BSS_type_independent = 2
Public Const dot11_BSS_type_any = 3



' Error codes
Public Const ERROR_OFFSET = &H70000000
Public Const ERROR_INIT = &H70000001
Public Const ERROR_APIENUM = &H70000002
Public Const ERROR_INVALIDADAPTER = &H70000003
Public Const ERROR_SMALLBUFFER = &H70000004
Public Const ERROR_APIQUERY = &H70000005
Public Const ERROR_ADAPTERBUSY = &H70000006
Public Const ERROR_INVALIDNETWORK = &H70000007
Public Const ERROR_INVALIDPROFILE = &H70000008
Public Const ERROR_APISET = &H70000009
Public Const ERROR_PROFILENOTFOUND = &H7000000A
Public Const ERROR_INVALIDPARAMETER = &H7000000B
Public Const ERROR_DATACHANGED = &H7000000C
Public Const ERROR_FAIL = &H7000000D                       'unspecified error
Public Const ERROR_ADAPTERNOTFOUND = &H7000000E
Public Const ERROR_SCAN = &H7000000F
Public Const ERROR_NOCURRENTNETWORK = &H70000010
Public Const ERROR_NOTSUPPORTED = &H70000011
Public Const ERROR_NOSERVICE = &H70000012
Public Const ERROR_ACCESSDENIED = &H70000013
Public Const ERROR_SERVICEDISABLED = &H70000014
Public Const ERROR_PINGTIMEOUT = &H70000015
Public Const ERROR_PINGFAIL = &H70000016
Public Const ERROR_HOSTRESOLVE = &H70000017
Public Const ERROR_UNSUPPORTEDPLATFORM = &H70000018
Public Const ERROR_PROFILESETTINGS = &H70000019
Public Const ERROR_AUTHERROR = &H7000001A
  
Public Const ERROR_TOOMANYPROFILES = &H70000020
Public Const ERROR_CREATEXMLDOCUMENT = &H70000021
Public Const ERROR_INVALIDTEMPLATE = &H70000022
Public Const ERROR_LOADXML = &H70000023
Public Const ERROR_SAVEXML = &H70000024
Public Const ERROR_OPTIONNOTFOUND = &H70000025
Public Const ERROR_INVALIDOPTIONVALUE = &H70000026
Public Const ERROR_BADPROFILE = &H70000027
Public Const ERROR_RECORDNOTFOUND = &H70000028
Public Const ERROR_MACSET = &H70000030

Public Const ERROR_NDISBUSY = &H70000040 'some service already uses adapter
Public Const ERROR_OLDDRIVERVERSION = &H70000041 'version of wifimanio.sys is too old

Public Const ERROR_CANNOTINSTALLDRIVER = &H70000051

Public Const ERROR_DEMOVERSION_NOTSUPPORTED = &H700000F0
Public Const ERROR_DEMOVERSION_EXPIRED = &H700000F1


'Constants
Public Const UNKNOWNVALUEOFFSET = &H100

'for CreateTmpProfile function
Public Const TEMPLATE_EMPTY = 0
Public Const TEMPLATE_UNSECURE_OPEN = 1
Public Const TEMPLATE_WEP_OPEN = 2
Public Const TEMPLATE_UNSECURE_SHARED = 3
Public Const TEMPLATE_WEP_SHARED = 4

'for Get/SetAdapterOption function
Public Const ADAPTER_NETTYPE = &H3                   'type of networks to access
Public Const ADAPTER_USEWINDOWS = &H8000             'use windows to configure adapter
Public Const ADAPTER_CONNECTFLAG = &H4000            'automatically connect to non preferred networks

Public Const NETTYPE_ANY = &H2                       'any networks
Public Const NETTYPE_APONLY = &H1                    'AP networks only (infrastructure)
Public Const NETTYPE_ADHOCONLY = &H0                 'computer-to-computer networks only (ad-hoc)

Public Const USEWINDOWS_FLAG = &H8000                'use windows to configure adapter
Public Const CONNECTFLAG_FLAG = &H4000               'automatically connect to non preferred networks

'for SetStorageOptions function flags (Advanced WiFi-Manager only)
Public Const STORAGE_FILE = 0
Public Const STORAGE_MEMORY = 1

'for Get/SetLibraryOption function
'OPT_MODE must be set BEFORE calling any other functions, otherwise SetLibraryOption will fail
Public Const OPT_MODE = 0                          'see OPT_MODE_AUTO and OPT_MODE_NDISMODE
Public Const OPT_ADVWIFIMAN = 1              'checks if it's Advanced WiFi-Manager library (1) or not (0)
Public Const OPT_WLANCONNECTFLAGS = 2 'dwFlags field of WLAN_CONNECTION_PARAMETERS structure at connection in Vista, see MSDN for possible values, default 0
Public Const OPT_SCANDELAY = 3 'available networks scan delay in milliseconds (Advanced WiFi-Manager only), default 3000ms
Public Const OPT_XPNATIVEWIFI = 4 'use NativeWiFi for XP patch from Microsoft if possible instead of WZC API
Public Const OPT_SKIPPROFILECHECK = 6 'dont check profile settings at conection


Public Const OPT_MODE_AUTO = 0               'Advanced WiFi-Manager only: detect OS and use Ndis, WCZ or NativeWiFi (default mode)
Public Const OPT_MODE_NDIS = 1               'Advanced WiFi-Manager only: use Ndis for any OS


' WiFi-Manager functions

Public Declare Function EnableLog Lib "WiFiMan.dll" (ByVal path As String, removeold As Long) As Long
Public Declare Function DisableLog Lib "WiFiMan.dll" () As Long
Public Declare Function WriteLog Lib "WiFiMan.dll" (ByVal Str As String) As Long

Public Declare Function IsNativeWIFI Lib "WiFiMan.dll" () As Long
Public Declare Function IsDirectWIFI Lib "WiFiMan.dll" () As Long
Public Declare Function GetWIFIManagerVersion Lib "WiFiMan.dll" () As Long
Public Declare Function GetLibraryOption Lib "WiFiMan.dll" (ByVal OptionIndex As Long) As Long
Public Declare Function SetLibraryOption Lib "WiFiMan.dll" (ByVal OptionIndex As Long, ByVal Value As Long) As Long
Public Declare Function SetStorageOptions Lib "WiFiMan.dll" (ByVal FileName As String, ByVal Flags As Long) As Long
Public Declare Function SetStorageData Lib "WiFiMan.dll" (ByVal Data As String, ByVal Length As Long) As Long
Public Declare Function GetStorageData Lib "WiFiMan.dll" (ByVal Data As String, ByVal MaxLength As Long) As Long
Public Declare Function InstallDriver Lib "WiFiMan.dll" (ByVal path As String) As Long
Public Declare Function UninstallDriver Lib "WiFiMan.dll" () As Long
Public Declare Function IsInstalledDriver Lib "WiFiMan.dll" () As Long

Public Declare Function GetWIFIServiceStatus Lib "WiFiMan.dll" () As Long
Public Declare Function SetWIFIServiceStatus Lib "WiFiMan.dll" (ByVal StartService As Long) As Long

Public Declare Function GetAdapterCurrentIPInfo Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal IP As String, ByVal Mask As String, ByVal Gateway As String, ByVal MaxCount As Long) As Long
Public Declare Function GetAdapterIPInfo Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal IP As String, ByVal Mask As String, ByVal Gateway As String, ByRef DHCPEnabled As Long, ByVal MaxCount As Long) As Long
Public Declare Function SetAdapterIPInfo Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal IP As String, ByVal Mask As String, ByVal Gateway As String, ByVal DHCPEnabled As Long) As Long
Public Declare Function GetAdapterDNS Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal DNS As String, ByVal MaxCount As Long) As Long
Public Declare Function SetAdapterDNS Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal DNS As String) As Long
Public Declare Function GetAdapterMac Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByRef d1 As Byte, ByRef d2 As Byte, ByRef d3 As Byte, ByRef d4 As Byte, ByRef d5 As Byte, ByRef d6 As Byte) As Long
Public Declare Function GetAdapterDefaultMac Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByRef d1 As Byte, ByRef d2 As Byte, ByRef d3 As Byte, ByRef d4 As Byte, ByRef d5 As Byte, ByRef d6 As Byte) As Long
Public Declare Function SetAdapterMac Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal d1 As Byte, ByVal d2 As Byte, ByVal d3 As Byte, ByVal d4 As Byte, ByVal d5 As Byte, ByVal d6 As Byte) As Long
Public Declare Function SetAdapterDefaultMac Lib "WiFiMan.dll" (ByVal AdapterInd As Long) As Long
Public Declare Function GetAdapterFirewallState Lib "WiFiMan.dll" (ByVal AdapterInd As Long) As Long
Public Declare Function EnableAdapterFirewall Lib "WiFiMan.dll" (ByVal AdapterInd As Long) As Long
Public Declare Function DisableAdapterFirewall Lib "WiFiMan.dll" (ByVal AdapterInd As Long) As Long
Public Declare Function GetAdapterTraffic Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByRef InDataH As Long, ByRef InDataL As Long, ByRef OutDataH As Long, ByRef OutDataL As Long) As Long

Public Declare Function Ping Lib "WiFiMan.dll" (ByVal Host As String, ByVal Timeout As Long) As Long

Public Declare Function EnumerateAdapters Lib "WiFiMan.dll" () As Long
Public Declare Function GetAdapterGUID Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal Guid As String, ByVal MaxCount As Long) As Long
Public Declare Function GetAdapterName Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal Name As String, ByVal MaxCount As Long) As Long
Public Declare Function CheckAdapterBusyStatus Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal Timeout As Long) As Long
Public Declare Function GetAdapterOption Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal OptionCode As Long) As Long
Public Declare Function SetAdapterOption Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal OptionCode As Long, ByVal Value As Long) As Long
Public Declare Function CheckNdisAvailable Lib "WiFiMan.dll" (ByVal AdapterInd As Long) As Long
Public Declare Function IsAdapterNativeWIFI Lib "WiFiMan.dll" (ByVal AdapterInd As Long) As Long
Public Declare Function GetAdapterInfo Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal Str As String, ByVal MaxCount As Long) As Long
Public Declare Function FreeAllResources Lib "WiFiMan.dll" () As Long

Public Declare Function EnumerateAvailableNetworks Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal Search As Long) As Long
Public Declare Function GetAvailableNetworkName Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal NetworkInd As Long, ByVal Name As String, ByVal MaxCount As Long) As Long
Public Declare Function GetAvailableNetworkIndex Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal NetName As String) As Long
Public Declare Function GetAvailableNetworkSignalQuality Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal NetworkInd As Long) As Long
Public Declare Function GetAvailableNetworkRSSI Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal NetworkInd As Long) As Long
Public Declare Function GetAvailableNetworkType Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal NetworkInd As Long) As Long
Public Declare Function IsAvailableNetworkSecure Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal NetworkInd As Long) As Long
Public Declare Function GetAvailableNetworkAuthMode Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal NetworkInd As Long) As Long
Public Declare Function GetAvailableNetworkCipherMode Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal NetworkInd As Long) As Long
Public Declare Function GetAvailableNetworkMac Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal NetworkInd As Long, ByRef d1 As Byte, ByRef d2 As Byte, ByRef d3 As Byte, ByRef d4 As Byte, ByRef d5 As Byte, ByRef d6 As Byte) As Long
Public Declare Function GetAvailableNetworkChannel Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal NetworkInd As Long) As Long

Public Declare Function EnumerateProfiles Lib "WiFiMan.dll" (ByVal AdapterInd As Long) As Long
Public Declare Function GetProfileName Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal ProfileInd As Long, ByVal Name As String, ByVal MaxCount As Long) As Long
Public Declare Function GetProfileNetworkIndex Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal NetName As String) As Long

Public Declare Function GetCurrentNetworkName Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal Name As String, ByVal MaxCount As Long) As Long
Public Declare Function GetCurrentNetworkChannel Lib "WiFiMan.dll" (ByVal AdapterInd) As Long
Public Declare Function GetCurrentNetworkSpeed Lib "WiFiMan.dll" (ByVal AdapterInd) As Long
Public Declare Function GetCurrentNetworkMac Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByRef d1 As Byte, ByRef d2 As Byte, ByRef d3 As Byte, ByRef d4 As Byte, ByRef d5 As Byte, ByRef d6 As Byte) As Long

Public Declare Function ConnectToNetwork Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal Name As String) As Long
Public Declare Function ConnectToNetworkByMAC Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal d1, ByVal d2, ByVal d3, ByVal d4, ByVal d5, ByVal d6) As Long
Public Declare Function DisconnectFromNetwork Lib "WiFiMan.dll" (ByVal AdapterInd As Long) As Long

Public Declare Function MoveProfile Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal ProfileInd As Long, ByVal NewIndex As Long) As Long
Public Declare Function DeleteProfile Lib "WiFiMan.dll" (ByVal AdapterInd As Long, ByVal ProfileInd As Long) As Long

Public Declare Function OpenProfilesUI Lib "WiFiMan.dll" (ByVal AdapterInd As Long) As Long
Public Declare Function RepairAdapter Lib "WiFiMan.dll" (ByVal AdapterInd As Long) As Long

Public Declare Function CreateTmpProfile Lib "WiFiMan.dll" (ByVal Template) As Long
Public Declare Function FreeTmpProfile Lib "WiFiMan.dll" (ByVal ProfileHandle) As Long
Public Declare Function CloneTmpProfile Lib "WiFiMan.dll" (ByVal ProfileHandle) As Long
Public Declare Function LoadTmpProfile Lib "WiFiMan.dll" (ByVal FileName As String) As Long
Public Declare Function LoadTmpProfileFromString Lib "WiFiMan.dll" (ByVal Str As String) As Long
Public Declare Function GetTmpProfileFromAdapter Lib "WiFiMan.dll" (ByVal AdapterInd, ByVal ProfileInd) As Long
Public Declare Function SetTmpProfileToAdapter Lib "WiFiMan.dll" (ByVal ProfileHandle, ByVal AdapterInd) As Long
Public Declare Function SetTmpProfileOption Lib "WiFiMan.dll" (ByVal ProfileHandle, ByVal OptionName As String, ByVal Value As String) As Long
Public Declare Function GetTmpProfileOption Lib "WiFiMan.dll" (ByVal ProfileHandle, ByVal OptionName As String, ByVal Value As String, ByVal MaxCount As Long) As Long
Public Declare Function DeleteTmpProfileOption Lib "WiFiMan.dll" (ByVal ProfileHandle, ByVal OptionName As String) As Long
Public Declare Function SetDefaultEapConfig Lib "WiFiMan.dll" (ByVal ProfileHandle, ByVal EapEnabled As Long, ByVal UseWindowsCredentials As Long) As Long

Public Declare Function SaveTmpProfile Lib "WiFiMan.dll" (ByVal ProfileHandle, ByVal FileName As String) As Long

Public Declare Function GetWindowsFirewallStatus Lib "WiFiMan.dll" (ByRef ExceptionsNotAllowed As Long, ByRef NotificationsDisabled As Long) As Long
Public Declare Function EnableWindowsFirewall Lib "WiFiMan.dll" (ByVal ExceptionsNotAllowed As Long, ByVal NotificationsDisabled As Long) As Long
Public Declare Function DisableWindowsFirewall Lib "WiFiMan.dll" () As Long

Public Declare Function EnumerateAllNetAdapters Lib "WiFiMan.dll" () As Long
Public Declare Function GetNetAdapterGUID Lib "WiFiMan.dll" (ByVal NetAdapterInd As Long, ByVal Guid As String, ByVal MaxCount As Long) As Long
Public Declare Function IsNetAdapterWiFi Lib "WiFiMan.dll" (ByVal NetAdapterInd As Long) As Long
Public Declare Function GetNetAdapterName Lib "WiFiMan.dll" (ByVal NetAdapterInd As Long, ByVal Name As String, ByVal MaxCount As Long) As Long
Public Declare Function GetNetAdapterState Lib "WiFiMan.dll" (ByVal NetAdapterInd As Long) As Long
Public Declare Function EnableNetAdapter Lib "WiFiMan.dll" (ByVal NetAdapterInd As Long) As Long
Public Declare Function DisableNetAdapter Lib "WiFiMan.dll" (ByVal NetAdapterInd As Long) As Long
Public Declare Function RestartNetAdapter Lib "WiFiMan.dll" (ByVal NetAdapterInd As Long) As Long
Public Declare Function GetNetAdapterCurrentIPInfo Lib "WiFiMan.dll" (ByVal NetAdapterInd As Long, ByVal IP As String, ByVal Mask As String, ByVal Gateway As String, ByVal MaxCount As Long) As Long
Public Declare Function GetNetAdapterIPInfo Lib "WiFiMan.dll" (ByVal NetAdapterInd As Long, ByVal IP As String, ByVal Mask As String, ByVal Gateway As String, ByRef DHCPEnabled As Long, ByVal MaxCount As Long) As Long
Public Declare Function SetNetAdapterIPInfo Lib "WiFiMan.dll" (ByVal NetAdapterInd As Long, ByVal IP As String, ByVal Mask As String, ByVal Gateway As String, ByVal DHCPEnabled As Long) As Long
Public Declare Function GetNetAdapterDNS Lib "WiFiMan.dll" (ByVal NetAdapterInd As Long, ByVal DNS As String, ByVal MaxCount As Long) As Long
Public Declare Function SetNetAdapterDNS Lib "WiFiMan.dll" (ByVal NetAdapterInd As Long, ByVal DNS As String) As Long
Public Declare Function GetNetAdapterMac Lib "WiFiMan.dll" (ByVal NetAdapterInd As Long, ByRef d1 As Byte, ByRef d2 As Byte, ByRef d3 As Byte, ByRef d4 As Byte, ByRef d5 As Byte, ByRef d6 As Byte) As Long
Public Declare Function GetNetAdapterDefaultMac Lib "WiFiMan.dll" (ByVal NetAdapterInd As Long, ByRef d1 As Byte, ByRef d2 As Byte, ByRef d3 As Byte, ByRef d4 As Byte, ByRef d5 As Byte, ByRef d6 As Byte) As Long
Public Declare Function SetNetAdapterMac Lib "WiFiMan.dll" (ByVal NetAdapterInd As Long, ByVal d1 As Byte, ByVal d2 As Byte, ByVal d3 As Byte, ByVal d4 As Byte, ByVal d5 As Byte, ByVal d6 As Byte) As Long
Public Declare Function SetNetAdapterDefaultMac Lib "WiFiMan.dll" (ByVal NetAdapterInd As Long) As Long
Public Declare Function GetNetAdapterFirewallState Lib "WiFiMan.dll" (ByVal NetAdapterInd As Long) As Long
Public Declare Function EnableNetAdapterFirewall Lib "WiFiMan.dll" (ByVal NetAdapterInd As Long) As Long
Public Declare Function DisableNetAdapterFirewall Lib "WiFiMan.dll" (ByVal NetAdapterInd As Long) As Long
Public Declare Function GetNetAdapterTraffic Lib "WiFiMan.dll" (ByVal NetAdapterInd As Long, ByRef InDataH As Long, ByRef InDataL As Long, ByRef OutDataH As Long, ByRef OutDataL As Long) As Long










