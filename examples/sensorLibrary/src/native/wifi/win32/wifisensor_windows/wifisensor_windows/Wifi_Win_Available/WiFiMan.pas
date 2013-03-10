unit WiFiMan;

//****************************************************************************
//                     WiFi-Manager DLL interface
//                    Copyright (c) 2007-2009 Nicomsoft
//                 Copyright (c) 2007-2009 Michael Kapustin
//                       support@nicomsoft.com
//                     www.nicomsoft.com/wifiman/
//
//****************************************************************************


                               interface

uses Windows, Classes;


// Constants

// Error codes
const
  ERROR_OFFSET =              $70000000;
  ERROR_INIT =                $70000001;
  ERROR_APIENUM	=             $70000002;
  ERROR_INVALIDADAPTER =      $70000003;
  ERROR_SMALLBUFFER	=         $70000004;
  ERROR_APIQUERY =            $70000005;
  ERROR_ADAPTERBUSY	=         $70000006;
  ERROR_INVALIDNETWORK =      $70000007;
  ERROR_INVALIDPROFILE =	  	$70000008;
  ERROR_APISET =              $70000009;
  ERROR_PROFILENOTFOUND	=     $7000000A;
  ERROR_INVALIDPARAMETER =    $7000000B;
  ERROR_DATACHANGED	=         $7000000C;
  ERROR_FAIL =                $7000000D; //unspecified error
  ERROR_ADAPTERNOTFOUND	=     $7000000E;
  ERROR_SCAN =                $7000000F;
  ERROR_NOCURRENTNETWORK =		$70000010;
  ERROR_NOTSUPPORTED =			  $70000011;
  ERROR_NOSERVICE	=		      	$70000012;
  ERROR_ACCESSDENIED =		  	$70000013;
  ERROR_SERVICEDISABLED	=   	$70000014;
  ERROR_PINGTIMEOUT	=		      $70000015;
  ERROR_PINGFAIL =				    $70000016;
  ERROR_HOSTRESOLVE	=		      $70000017;
  ERROR_UNSUPPORTEDPLATFORM	= $70000018;
  ERROR_PROFILESETTINGS	=     $70000019;
  ERROR_AUTHERROR	=     			$7000001A;

  ERROR_TOOMANYPROFILES	=	    $70000020;
  ERROR_CREATEXMLDOCUMENT	=	  $70000021;
  ERROR_INVALIDTEMPLATE	=	    $70000022;
  ERROR_LOADXML	=			        $70000023;
  ERROR_SAVEXML	=			        $70000024;
  ERROR_OPTIONNOTFOUND =		  $70000025;
  ERROR_INVALIDOPTIONVALUE =	$70000026;
  ERROR_BADPROFILE =        	$70000027;
  ERROR_RECORDNOTFOUND =    	$70000028;
  ERROR_MACSET =					    $70000030;

  ERROR_NDISBUSY =            $70000040; //some software already uses adapter
  ERROR_OLDDRIVERVERSION =		$70000041; //version of wifimanio.sys is too old

  ERROR_DRIVERNOTINSTALLED =	$70000051;

  ERROR_DEMOVERSION_NOTSUPPORTED =  $700000F0;
  ERROR_DEMOVERSION_EXPIRED =       $700000F1;

//Constants
  UNKNOWNVALUEOFFSET =			  $100; //offset for unknown values

//for CreateTmpProfile function
  TEMPLATE_EMPTY =				    0;
  TEMPLATE_UNSECURE_OPEN =		1;
  TEMPLATE_WEP_OPEN	=		      2;
  TEMPLATE_UNSECURE_SHARED =	3;
  TEMPLATE_WEP_SHARED	=		    4;

//_NDIS_802_11_AUTHENTICATION_MODE, for GetAvailableNetworkAuthMode
  Ndis802_11AuthModeOpen = 0;
  Ndis802_11AuthModeShared = 1;
  Ndis802_11AuthModeAutoSwitch = 2;
  Ndis802_11AuthModeWPA = 3;
  Ndis802_11AuthModeWPAPSK = 4;
  Ndis802_11AuthModeWPANone = 5;
  Ndis802_11AuthModeWPA2 = 6;
  Ndis802_11AuthModeWPA2PSK = 7;

//_DOT11_CIPHER_ALGORITHM, for GetAvailableNetworkCipherMode
  DOT11_CIPHER_ALGO_NONE = $00;
  DOT11_CIPHER_ALGO_WEP40 = $01;
  DOT11_CIPHER_ALGO_TKIP = $02;
  DOT11_CIPHER_ALGO_CCMP = $04;
  DOT11_CIPHER_ALGO_WEP104 = $05;
  DOT11_CIPHER_ALGO_WPA_USE_GROUP = $100;
  DOT11_CIPHER_ALGO_WEP = $101;

//DOT11_BSS_TYPE, for GetAvailableNetworkType
  dot11_BSS_type_infrastructure = 1;
  dot11_BSS_type_independent = 2;
  dot11_BSS_type_any = 3;

//for Get/SetAdapterOption function
  ADAPTER_NETTYPE	=			    $00000003; //type of networks to access
  ADAPTER_USEWINDOWS =			$00008000; //use windows to configure adapter
  ADAPTER_CONNECTFLAG	=	   	$00004000; //automatically connect to non preferred networks
  ADAPTER_INTFOPCODE =      $10000000; //flag for WLAN_INTF_OPCODE operation to call WlanSetInterface directly

  NETTYPE_ANY	=					   	$00000002; //any networks
  NETTYPE_APONLY =				  $00000001; //AP networks only (infrastructure)
  NETTYPE_ADHOCONLY	=		  	$00000000; //computer-to-computer networks only (ad-hoc)

  USEWINDOWS_FLAG	=			    $00008000; //use windows to configure adapter
  CONNECTFLAG_FLAG =			  $00004000 ;//automatically connect to non preferred networks

//for SetStorageOptions function flags (Advanced WiFi-Manager only)
  STORAGE_FILE =			    	0;
  STORAGE_MEMORY =			   	1;

//for Get/SetLibraryOption function
//OPT_MODE must be set BEFORE calling any other functions, otherwise SetLibraryOption will fail
  OPT_MODE =					      0; //see OPT_MODE_AUTO and OPT_MODE_NDISMODE
  OPT_ADVWIFIMAN =		   		1; //checks if it's Advanced WiFi-Manager library (1) or not (0)
  OPT_WLANCONNECTFLAGS =	  2; //dwFlags field of WLAN_CONNECTION_PARAMETERS structure at connection in Vista, see MSDN for possible values, default 0
  OPT_SCANDELAY	=         	3; //available networks scan delay in milliseconds (Advanced WiFi-Manager only), default 3000ms
  OPT_XPNATIVEWIFI	= 	  	4; //use NativeWiFi for XP patch from Microsoft if possible instead of WZC API
  OPT_SKIPPROFILECHECK =    6; //dont check profile settings at conection

  OPT_MODE_AUTO	=		      	0; //Advanced WiFi-Manager only: detect OS and use Ndis, WCZ or NativeWiFi (default mode)
  OPT_MODE_NDIS	=		      	1; //Advanced WiFi-Manager only: use Ndis for any OS



const WiFiLib = 'WiFiMan.dll';

// WiFi-Manager Functions

function EnumerateAdapters:dword; stdcall; external WiFiLib;
function GetAdapterGUID(AdapterInd:integer; Guid:pansichar; MaxCount:integer):dword; stdcall; external WiFiLib;
function GetAdapterName(AdapterInd:integer; Name:pansichar; MaxCount:integer):dword; stdcall; external WiFiLib;
function CheckAdapterBusyStatus(AdapterInd:integer; Timeout:integer):dword; stdcall; external WiFiLib;
function GetAdapterOption(AdapterInd:integer; OptionCode:integer):dword; stdcall; external WiFiLib;
function SetAdapterOption(AdapterInd:integer; OptionCode:integer; Value:integer):dword; stdcall; external WiFiLib;
function CheckNdisAvailable(AdapterInd:integer):dword; stdcall; external WiFiLib;
function IsAdapterNativeWIFI(AdapterInd:integer):dword; stdcall; external WiFiLib;
function GetAdapterInfo(AdapterInd:integer; Str:pchar; MaxCount:integer):dword; stdcall; external WiFiLib;
function FreeAllResources():dword; stdcall; external WiFiLib;

function IsNativeWIFI():dword; stdcall; external WiFiLib;
function IsDirectWIFI():dword; stdcall; external WiFiLib;
function GetLibraryOption(OptionIndex:integer):dword; stdcall; external WiFiLib;
function SetLibraryOption(OptionIndex:integer; Value:integer):dword; stdcall; external WiFiLib;
function GetWIFIManagerVersion():dword; stdcall; external WiFiLib;
function SetStorageOptions(FileName:pansichar; Flags:integer):dword; stdcall; external WiFiLib;
function SetStorageData(Data:pansichar; Length:integer):dword; stdcall; external WiFiLib;
function GetStorageData(Data:pansichar; MaxLength:integer):dword; stdcall; external WiFiLib;
function InstallDriver(path:pansichar):dword; stdcall; external WiFiLib;
function UninstallDriver():dword; stdcall; external WiFiLib;
function IsInstalledDriver():dword; stdcall; external WiFiLib;

function GetWIFIServiceStatus():dword; stdcall; external WiFiLib;
function SetWIFIServiceStatus(StartService:integer):dword; stdcall; external WiFiLib;

function GetAdapterCurrentIPInfo(AdapterInd:integer; IP:pansichar; Mask:pansichar; Gateway:pansichar; MaxCount:integer):dword; stdcall; external WiFiLib;
function GetAdapterIPInfo(AdapterInd:integer; IP:pansichar; Mask:pansichar; Gateway:pansichar; var DHCPEnabled:integer; MaxCount:integer):dword; stdcall; external WiFiLib;
function SetAdapterIPInfo(AdapterInd:integer; IP:pansichar; Mask:pansichar; Gateway:pansichar; DHCPEnabled:integer):dword; stdcall; external WiFiLib;
function GetAdapterDNS(AdapterInd:integer; DNS:pansichar; MaxCount:integer):dword; stdcall; external WiFiLib;
function SetAdapterDNS(AdapterInd:integer; DNS:pansichar):dword; stdcall; external WiFiLib;
function GetAdapterMac(AdapterInd:integer; var d1, d2, d3, d4, d5, d6:byte):dword; stdcall; external WiFiLib;
function GetAdapterDefaultMac(AdapterInd:integer; var d1, d2, d3, d4, d5, d6:byte):dword; stdcall; external WiFiLib;
function SetAdapterMac(AdapterInd:integer; d1, d2, d3, d4, d5, d6:byte):dword; stdcall; external WiFiLib;
function SetAdapterDefaultMac(AdapterInd:integer):dword; stdcall; external WiFiLib;
function GetAdapterFirewallState(AdapterInd:integer):dword; stdcall; external WiFiLib;
function EnableAdapterFirewall(AdapterInd:integer):dword; stdcall; external WiFiLib;
function DisableAdapterFirewall(AdapterInd:integer):dword; stdcall; external WiFiLib;
function GetAdapterTraffic(AdapterInd:integer; var InDataH, InDataL, OutDataH, OutDataL:DWORD):dword; stdcall; external WiFiLib;

function Ping(Host:pansichar; Timeout:integer):dword; stdcall; external WiFiLib;

function EnumerateAvailableNetworks(AdapterInd:integer; Search:integer):dword; stdcall; external WiFiLib;
function GetAvailableNetworkName(AdapterInd:integer; NetworkInd:integer; Name:pansichar; MaxCount:integer):dword; stdcall; external WiFiLib;
function GetAvailableNetworkIndex(AdapterInd:integer; NetName:pansichar):dword; stdcall; external WiFiLib;
function GetAvailableNetworkSignalQuality(AdapterInd:integer; NetworkInd:integer):dword; stdcall; external WiFiLib;
function GetAvailableNetworkRSSI(AdapterInd:integer; NetworkInd:integer):dword; stdcall; external WiFiLib;
function GetAvailableNetworkType(AdapterInd:integer; NetworkInd:integer):dword; stdcall; external WiFiLib;
function IsAvailableNetworkSecure(AdapterInd:integer; NetworkInd:integer):dword; stdcall; external WiFiLib;
function GetAvailableNetworkAuthMode(AdapterInd:integer; NetworkInd:integer):dword; stdcall; external WiFiLib;
function GetAvailableNetworkCipherMode(AdapterInd:integer; NetworkInd:integer):dword; stdcall; external WiFiLib;
function GetAvailableNetworkMac(AdapterInd:integer; NetworkInd:integer; var d1, d2, d3, d4, d5, d6:byte):dword; stdcall; external WiFiLib;
function GetAvailableNetworkChannel(AdapterInd:integer; NetworkInd:integer):dword; stdcall; external WiFiLib;

function EnumerateProfiles(AdapterInd:integer):dword; stdcall; external WiFiLib;
function GetProfileName(AdapterInd:integer; ProfileInd:integer; Name:pansichar; MaxCount:integer):dword; stdcall; external WiFiLib;
function GetProfileNetworkIndex(AdapterInd:integer; NetName:pansichar):dword; stdcall; external WiFiLib;

function GetCurrentNetworkName(AdapterInd:integer; Name:pansichar; MaxCount:integer):dword; stdcall; external WiFiLib;
function GetCurrentNetworkSpeed(AdapterInd:integer):dword; stdcall; external WiFiLib;
function GetCurrentNetworkChannel(AdapterInd:integer):dword; stdcall; external WiFiLib;
function GetCurrentNetworkMac(AdapterInd:integer; var d1, d2, d3, d4, d5, d6:byte):dword; stdcall; external WiFiLib;

function ConnectToNetwork(AdapterInd:integer; Name:pansichar):dword; stdcall; external WiFiLib;
function ConnectToNetworkByMAC(AdapterInd:integer; d1, d2, d3, d4, d5, d6:byte):dword; stdcall; external WiFiLib;

function DisconnectFromNetwork(AdapterInd:integer):dword; stdcall; external WiFiLib;

function MoveProfile(AdapterInd:integer; ProfileInd:integer; NewIndex:integer):dword; stdcall; external WiFiLib;
function DeleteProfile(AdapterInd:integer; ProfileInd:integer):dword; stdcall; external WiFiLib;

function OpenProfilesUI(AdapterInd:integer):dword; stdcall; external WiFiLib;
function RepairAdapter(AdapterInd:integer):dword; stdcall; external WiFiLib;

function EnableLog(Path:pansichar; RemoveOld:dword):dword; stdcall; external WiFiLib;
function DisableLog():dword; stdcall; external WiFiLib;
function WriteLog(Str:pansichar):dword; stdcall; external WiFiLib;

function CreateTmpProfile(Template:integer):dword; stdcall; external WiFiLib;
function LoadTmpProfile(FileName:pansichar):dword; stdcall; external WiFiLib;
function LoadTmpProfileFromString(Str:pansichar):dword; stdcall; external WiFiLib;
function SaveTmpProfile(ProfileHandle:integer; FileName:pansichar):dword; stdcall; external WiFiLib;
function SaveTmpProfileToString(ProfileHandle:integer; Str:pansichar; MaxCount:integer):dword; stdcall; external WiFiLib;
function CloneTmpProfile(ProfileHandle:integer):dword; stdcall; external WiFiLib;
function GetTmpProfileOption(ProfileHandle:integer; OptionName:pansichar; Value:pansichar; MaxCount:integer):dword; stdcall; external WiFiLib;
function SetTmpProfileOption(ProfileHandle:integer; OptionName:pansichar; Value:pansichar):dword; stdcall; external WiFiLib;
function DeleteTmpProfileOption(ProfileHandle:integer; OptionName:pansichar):dword; stdcall; external WiFiLib;
function SetDefaultEapConfig(ProfileHandle:integer; EapEnabled:integer; UseWindowsCredentials:integer):dword; stdcall; external WiFiLib;
function FreeTmpProfile(ProfileHandle:integer):dword; stdcall; external WiFiLib;
function GetTmpProfileFromAdapter(AdapterInd:integer; ProfileInd:integer):dword; stdcall; external WiFiLib;
function SetTmpProfileToAdapter(ProfileHandle:integer; AdapterInd:integer):dword; stdcall; external WiFiLib;

function GetWindowsFirewallStatus(var ExceptionsNotAllowed:integer; var NotificationsDisabled:integer):dword; stdcall; external WiFiLib;
function EnableWindowsFirewall(ExceptionsNotAllowed:integer;  NotificationsDisabled:integer):dword; stdcall; external WiFiLib;
function DisableWindowsFirewall():dword; stdcall; external WiFiLib;

function EnumerateAllNetAdapters():dword; stdcall; external WiFiLib;
function GetNetAdapterName(NetAdapterInd:integer; Name:pansichar; MaxCount:integer):dword; stdcall; external WiFiLib;
function GetNetAdapterGUID(NetAdapterInd:integer; Guid:pansichar; MaxCount:integer):dword; stdcall; external WiFiLib;
function IsNetAdapterWiFi(NetAdapterInd:integer):dword; stdcall; external WiFiLib;
function GetNetAdapterState(NetAdapterInd:integer):dword; stdcall; external WiFiLib;
function EnableNetAdapter(NetAdapterInd:integer):dword; stdcall; external WiFiLib;
function DisableNetAdapter(NetAdapterInd:integer):dword; stdcall; external WiFiLib;
function RestartNetAdapter(NetAdapterInd:integer):dword; stdcall; external WiFiLib;
function GetNetAdapterCurrentIPInfo(NetAdapterInd:integer; IP:pansichar; Mask:pansichar; Gateway:pansichar; MaxCount:integer):dword; stdcall; external WiFiLib;
function GetNetAdapterIPInfo(NetAdapterInd:integer; IP:pansichar; Mask:pansichar; Gateway:pansichar; var DHCPEnabled:integer; MaxCount:integer):dword; stdcall; external WiFiLib;
function SetNetAdapterIPInfo(NetAdapterInd:integer; IP:pansichar; Mask:pansichar; Gateway:pansichar; DHCPEnabled:integer):dword; stdcall; external WiFiLib;
function GetNetAdapterDNS(NetAdapterInd:integer; DNS:pansichar; MaxCount:integer):dword; stdcall; external WiFiLib;
function SetNetAdapterDNS(NetAdapterInd:integer; DNS:pansichar):dword; stdcall; external WiFiLib;
function GetNetAdapterMac(NetAdapterInd:integer; var d1, d2, d3, d4, d5, d6:byte):dword; stdcall; external WiFiLib;
function GetNetAdapterDefaultMac(NetAdapterInd:integer; var d1, d2, d3, d4, d5, d6:byte):dword; stdcall; external WiFiLib;
function SetNetAdapterMac(NetAdapterInd:integer; d1, d2, d3, d4, d5, d6:byte):dword; stdcall; external WiFiLib;
function SetNetAdapterDefaultMac(NetAdapterInd:integer):dword; stdcall; external WiFiLib;
function GetNetAdapterFirewallState(NetAdapterInd:integer):dword; stdcall; external WiFiLib;
function EnableNetAdapterFirewall(NetAdapterInd:integer):dword; stdcall; external WiFiLib;
function DisableNetAdapterFirewall(NetAdapterInd:integer):dword; stdcall; external WiFiLib;
function GetNetAdapterTraffic(NetAdapterInd:integer; var InDataH, InDataL, OutDataH, OutDataL:DWORD):dword; stdcall; external WiFiLib;


//////// UNICODE SUPPORT /////////
type LPCWSTR=pwchar;

function GetAdapterGUIDW(AdapterInd:integer; Guid:LPCWSTR; MaxCount:integer):dword; stdcall; external WiFiLib;
function GetAdapterNameW(AdapterInd:integer; Name:LPCWSTR; MaxCount:integer):dword; stdcall; external WiFiLib;
function GetAvailableNetworkNameW(AdapterInd:integer; NetworkInd:integer; Name:LPCWSTR; MaxCount:integer):dword; stdcall; external WiFiLib;
function GetAvailableNetworkIndexW(AdapterInd:integer; NetName:LPCWSTR):dword; stdcall; external WiFiLib;
function GetProfileNameW(AdapterInd:integer; ProfileInd:integer; Name:LPCWSTR; MaxCount:integer):dword; stdcall; external WiFiLib;
function GetProfileNetworkIndexW(AdapterInd:integer; NetName:LPCWSTR):dword; stdcall; external WiFiLib;
function GetCurrentNetworkNameW(AdapterInd:integer; Name:LPCWSTR; MaxCount:integer):dword; stdcall; external WiFiLib;
function ConnectToNetworkW(AdapterInd:integer; Name:LPCWSTR):dword; stdcall; external WiFiLib;
function EnableLogW(Path:LPCWSTR; RemoveOld:integer):dword; stdcall; external WiFiLib;
function WriteLogW(str:LPCWSTR):dword; stdcall; external WiFiLib;
function GetAdapterCurrentIPInfoW(AdapterInd:integer; IP:LPCWSTR; Mask:LPCWSTR; Gateway:LPCWSTR; MaxCount:integer):dword; stdcall; external WiFiLib;
function GetAdapterIPInfoW(AdapterInd:integer; IP:LPCWSTR; Mask:LPCWSTR; Gateway:LPCWSTR; var DHCPEnabled:integer; MaxCount:integer):dword; stdcall; external WiFiLib;
function SetAdapterIPInfoW(AdapterInd:integer; IP:LPCWSTR; Mask:LPCWSTR; Gateway:LPCWSTR; DHCPEnabled:integer):dword; stdcall; external WiFiLib;
function GetAdapterDNSW(AdapterInd:integer; DNS:LPCWSTR; MaxCount:integer):dword; stdcall; external WiFiLib;
function SetAdapterDNSW(AdapterInd:integer; DNS:LPCWSTR):dword; stdcall; external WiFiLib;
function PingW(Host:LPCWSTR; Timeout:integer):dword; stdcall; external WiFiLib;
function LoadTmpProfileW(FileName:LPCWSTR):dword; stdcall; external WiFiLib;
function LoadTmpProfileFromStringW(Str:LPCWSTR):dword; stdcall; external WiFiLib;
function SetTmpProfileOptionW(ProfileHandle:integer; OptionName:LPCWSTR; Value:LPCWSTR):dword; stdcall; external WiFiLib;
function GetTmpProfileOptionW(ProfileHandle:integer; OptionName:LPCWSTR; Value:LPCWSTR; MaxCount:integer):dword; stdcall; external WiFiLib;
function SaveTmpProfileW(ProfileHandle:integer; FileName:LPCWSTR):dword; stdcall; external WiFiLib;
function SaveTmpProfileToStringW(ProfileHandle:integer; Str:LPCWSTR; MaxCount:integer):dword; stdcall; external WiFiLib;
function GetNetAdapterNameW(NetAdapterInd:integer; Name:LPCWSTR; MaxCount:integer):dword; stdcall; external WiFiLib;
function GetNetAdapterGUIDW(NetAdapterInd:integer; Guid:LPCWSTR; MaxCount:integer):dword; stdcall; external WiFiLib;
function GetNetAdapterCurrentIPInfoW(NetAdapterInd:integer; IP:LPCWSTR; Mask:LPCWSTR; Gateway:LPCWSTR; MaxCount:integer):dword; stdcall; external WiFiLib;
function GetNetAdapterIPInfoW(NetAdapterInd:integer; IP:LPCWSTR; Mask:LPCWSTR; Gateway:LPCWSTR; var DHCPEnabled:integer; MaxCount:integer):dword; stdcall; external WiFiLib;
function SetNetAdapterIPInfoW(NetAdapterInd:integer; IP:LPCWSTR; Mask:LPCWSTR; Gateway:LPCWSTR; DHCPEnabled:integer):dword; stdcall; external WiFiLib;
function GetNetAdapterDNSW(NetAdapterInd:integer; DNS:LPCWSTR; MaxCount:integer):dword; stdcall; external WiFiLib;
function SetNetAdapterDNSW(NetAdapterInd:integer; DNS:LPCWSTR):dword; stdcall; external WiFiLib;
function SetStorageOptionsW(FileName:LPCWSTR; Flags:integer):dword; stdcall; external WiFiLib;
function InstallDriverW(path:LPCWSTR):dword; stdcall; external WiFiLib;
/////////////////////////////////


function UnicodeToAnsi(const Str:string):pansichar;


                            implementation



function UnicodeToAnsi(const Str:string):pansichar;
begin
//this function is for Delphi2009 only.
//because Delphi2009 uses unicode strings we need to convert them to ansi strings
//not necessary for previous Delphi versions though it works there as it does nothing
//Note for Delphi2009 it's convenient to use unicode functions of WiFiMan 
  result:=pansichar(ansistring(Str));
end;



end.
