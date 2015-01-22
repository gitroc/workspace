package cn.com.aa.common.android.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

public class PhoneUtil {

	public static TelephonyManager tm;

	/**
	 * 获得设备信息
	 * 
	 * @param context
	 * @return
	 */
	public static String getPhoneInfo(Context context) {
		tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		Build bd = new Build();

		String model = bd.MODEL;
		String display = bd.DISPLAY;
		StringBuilder sb = new StringBuilder();
		sb.append("\nDeviceId(IMEI) = " + tm.getDeviceId());
		sb.append("\n型号 = " + model);
		sb.append("\n厂商: " + android.os.Build.MANUFACTURER);
		sb.append("\n系统版本: " + android.os.Build.VERSION.RELEASE);
//		sb.append("\ndisplay = " + display);
//		sb.append("\nDeviceSoftwareVersion = " + tm.getDeviceSoftwareVersion());
//		sb.append("\nLine1Number = " + tm.getLine1Number());
//		sb.append("\nNetworkCountryIso = " + tm.getNetworkCountryIso());
//		sb.append("\nNetworkOperator = " + tm.getNetworkOperator());
//		sb.append("\nNetworkOperatorName = " + tm.getNetworkOperatorName());
//		sb.append("\nNetworkType = " + tm.getNetworkType());
//		sb.append("\nPhoneType = " + tm.getPhoneType());
//		sb.append("\nSimCountryIso = " + tm.getSimCountryIso());
//		sb.append("\nSimOperator = " + tm.getSimOperator());
//		sb.append("\nSimOperatorName = " + tm.getSimOperatorName());
//		sb.append("\nSimSerialNumber = " + tm.getSimSerialNumber());
//		sb.append("\nSimState = " + tm.getSimState());
//		sb.append("\nSubscriberId(IMSI) = " + tm.getSubscriberId());
//		sb.append("\nVoiceMailNumber = " + tm.getVoiceMailNumber());
		Log.i("info", sb.toString());
		return sb.toString();
	}
	
	
	/**
	 * @Title:
	 * @Description:
	 * @author daiyan
	 * @Company:
	 * @date 2014年10月26日
	 */
	
	/**
     * 获取手机mac地址<br/>
     * 错误返回12个0
     */
    public static String getMacAddress(Context context) {
        // 获取mac地址：
        String macAddress = "000000000000";
        try {
            WifiManager wifiMgr = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = (null == wifiMgr ? null : wifiMgr
                    .getConnectionInfo());
            if (null != info) {
                if (!TextUtils.isEmpty(info.getMacAddress()))
                    macAddress = info.getMacAddress().replace(":", "");
                else
                    return macAddress;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return macAddress;
        }
        return macAddress;
    }
    
    /**
     * 获得本机的IP地址
     * @param context
     * @return
     */
    public static String getIp(Context context) { 
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE); 
        WifiInfo wifiInfo = wifiManager.getConnectionInfo(); 
        int ipAddress = wifiInfo.getIpAddress(); 
         
        // 格式化IP address，例如：格式化前：1828825280，格式化后：192.168.1.109 
        String ip = String.format("%d.%d.%d.%d", 
                (ipAddress & 0xff), 
                (ipAddress >> 8 & 0xff), 
                (ipAddress >> 16 & 0xff), 
                (ipAddress >> 24 & 0xff)); 
        return ip; 
         
    } 

}
