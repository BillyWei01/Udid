package com.horizon.did;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;
import utils.AutoExtendByteBuffer;
import utils.MHash;

import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;

public class DarkPhysicsInfo {
    public static long getHash(Context context){
        return getSensorHash(context) ^ MHash.hash64(getNetworkInterfaceNames());
    }

    private static long getSensorHash(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager == null) {
            return 0L;
        }
        AutoExtendByteBuffer buffer = new AutoExtendByteBuffer(1024);
        List<Sensor> list = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : list) {
            buffer.putString(sensor.getName());
            buffer.putString(sensor.getVendor());
            buffer.putInt(sensor.getVersion());
            buffer.putInt(sensor.getType());
            buffer.putFloat(sensor.getMaximumRange());
            buffer.putFloat(sensor.getResolution());
            buffer.putFloat(sensor.getPower());
            buffer.putInt(sensor.getMinDelay());
        }

        return MHash.hash64(buffer.array(), buffer.position());
    }

    private static String getNetworkInterfaceNames() {
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            if (enumeration == null) {
                return "";
            }
            StringBuilder builder = new StringBuilder(1024);
            while (enumeration.hasMoreElements()) {
                NetworkInterface netInterface = enumeration.nextElement();
                builder.append(netInterface.getName()).append(',');
            }
            if (builder.length() > 0) {
                builder.setLength(builder.length() - 1);
            }
            return builder.toString();
        } catch (Exception e) {
            Log.e("tag", e.getMessage(), e);
        }
        return "";
    }
}
