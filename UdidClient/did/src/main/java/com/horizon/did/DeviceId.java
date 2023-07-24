package com.horizon.did;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import utils.HexUtil;
import utils.MHash;

import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.Enumeration;

public class DeviceId {
    private static final char[] HEX_DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    private static final String INVALID_MAC_ADDRESS = "02:00:00:00:00:00";

    private static final String INVALID_ANDROID_ID = "9774d56d682e549c";

    private static byte[] getMacInArray() {
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            if (enumeration == null) {
                return null;
            }
            while (enumeration.hasMoreElements()) {
                NetworkInterface netInterface = enumeration.nextElement();
                if (netInterface.getName().equals("wlan0")) {
                    return netInterface.getHardwareAddress();
                }
            }
        } catch (Exception e) {
            Log.e("tag", e.getMessage(), e);
        }
        return null;
    }

    public static long getLongMac() {
        byte[] bytes = getMacInArray();
        if (bytes == null || bytes.length != 6) {
            return 0L;
        }
        long mac = 0L;
        for (int i = 0; i < 6; i++) {
            mac |= bytes[i] & 0xFF;
            if(i != 5){
                mac <<= 8;
            }
        }
        return mac;
    }

    public static String getMacAddress() {
        String mac = formatMac(getMacInArray());
        if (TextUtils.isEmpty(mac) || mac.equals(INVALID_MAC_ADDRESS)) {
            return "";
        }
        return mac;
    }

    private static String formatMac(byte[] bytes) {
        if (bytes == null || bytes.length != 6) {
            return "";
        }
        char[] mac = new char[17];
        int p = 0;
        for (int i = 0; i <= 5; i++) {
            byte b = bytes[i];
            mac[p] = HEX_DIGITS[(b & 0xF0) >> 4];
            mac[p + 1] = HEX_DIGITS[b & 0xF];
            if (i != 5) {
                mac[p + 2] = ':';
                p += 3;
            }
        }
        return new String(mac);
    }

    private static boolean isAndroidIdValid(String androidId){
        // 是否有其他非法的AndroidId?
        return !TextUtils.isEmpty(androidId)
                && !androidId.equals("9774d56d682e549c")
                && !androidId.equals("0000000000000000")
                && !androidId.equals("0123456789abcdef");
    }

    public static String getAndroidID(Context context) {
        if (context != null) {
            try {
                @SuppressLint("HardwareIds")
                String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                if (isAndroidIdValid(androidId)) {
                    return androidId;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    private static long getLongAndroidId(Context context){
        String androidId = getAndroidID(context);
        if(androidId.length() == 16){
            try{
                return HexUtil.hex2Long(androidId);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return MHash.hash64(androidId);
    }

    public static String getSerialNo() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            return "";
        }
        String serialNo = Build.SERIAL;
        if (TextUtils.isEmpty(serialNo) || serialNo.equals(Build.UNKNOWN)) {
            return "";
        }
        return serialNo;
    }

    public static String getLocalDevicesId(Context context) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(getLongAndroidId(context));
        buffer.putLong(PhysicsInfo.getDeviceHash(context));
        return Base64.encodeToString(buffer.array(), Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
       // return HexUtil.bytes2Hex(buffer.array());
    }
}
