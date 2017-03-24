package com.friendlyarm.AndroidSDK;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectMgr {
	/**
	 * 对网络连接状态进行判断
	 * @return  true, 可用； false， 不可用
	 */
	
	public static final int MOBLIE_ANY_CNT = 1;
	public static final int MOBLIE_3G_CNT = 2;
	public static final int MOBLIE_GPRS_CNT = 3;
	public static final int MOBLIE_ETH_CNT = 4;
	public static final int MOBLIE_WIFI_CNT = 5;
	
	public static boolean isWifiConnected(Context context) { 
		if (context != null) { 
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context 
			.getSystemService(Context.CONNECTIVITY_SERVICE); 
			NetworkInfo mWiFiNetworkInfo = mConnectivityManager 
			.getNetworkInfo(ConnectivityManager.TYPE_WIFI); 
			if (mWiFiNetworkInfo != null) { 
			return mWiFiNetworkInfo.isAvailable(); 
				} 
			} 
			return false; 
		} 
	
	public static int getConnectedType(Context context) { 
		if (context != null) { 
		ConnectivityManager mConnectivityManager = (ConnectivityManager) context 
		.getSystemService(Context.CONNECTIVITY_SERVICE); 
		NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo(); 
		if (mNetworkInfo != null && mNetworkInfo.isAvailable()) { 
		return mNetworkInfo.getType(); 
		} 
		} 
		return -1; 
		}
	
	
	public static boolean isMoblie3GConnected(Context context) { 
		if (context != null) { 
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context 
			.getSystemService(Context.CONNECTIVITY_SERVICE); 
			NetworkInfo mWiFiNetworkInfo = mConnectivityManager 
			.getNetworkInfo(ConnectivityManager.TYPE_MOBILE); 
			if (mWiFiNetworkInfo != null) { 
			return mWiFiNetworkInfo.isAvailable(); 
				} 
			} 
			return false; 
		} 
	
	
}
