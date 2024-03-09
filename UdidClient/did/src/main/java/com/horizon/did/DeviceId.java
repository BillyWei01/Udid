package com.horizon.did;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaDrm;
import android.media.UnsupportedSchemeException;
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
import java.util.UUID;

public class DeviceId {
    private static boolean isAndroidIdValid(String androidId){
        // 是否有其他非法的AndroidId?
        return !TextUtils.isEmpty(androidId)
                && !androidId.equals("9774d56d682e549c")
                && !androidId.equals("0000000000000000")
                && !androidId.equals("0123456789abcdef");
    }

    public static long getWidevineIdHash() {
        UUID WIDEVINE_UUID = new UUID(0xEDEF8BA979D64ACEL, 0xA3C827DCD51D21EDL);
        MediaDrm mediaDrm = null;
        try {
            mediaDrm = new MediaDrm(WIDEVINE_UUID);
            byte[] widevineId = mediaDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID);
            return MHash.hash64(widevineId, widevineId.length);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mediaDrm != null) {
                mediaDrm.release();
            }
        }

        // 如果获取不到 Widevine Id, 并且版本低于Android 8.0, 取序列号作为替代
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O ? MHash.hash64(getSerialNo()) : 0;
    }

    private static String getSerialNo() {
        String serialNo = Build.SERIAL;
        if (TextUtils.isEmpty(serialNo) || serialNo.equals(Build.UNKNOWN)) {
            return "";
        }
        return serialNo;
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

    /**
     * 获取本地设备ID （128bit / 32位字符)
     */
    public static String getLocalDevicesId(Context context) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(getLongAndroidId(context));
        buffer.putLong(PhysicsInfo.getDeviceHash(context) ^ getWidevineIdHash());
        // return HexUtil.bytes2Hex(buffer.array());
        return Base64.encodeToString(buffer.array(), Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
    }
}
