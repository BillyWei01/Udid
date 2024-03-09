package com.horizon.did;

import android.app.ActivityManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;

import utils.AutoExtendByteBuffer;

public class PhysicsInfo {
    private static final String TAG = "PhysicsInfo";

    private static final byte SEPARATOR = (byte) (',');

    // 获取硬件信息的Hash
    public static long getDeviceHash(Context context) {
        AutoExtendByteBuffer buffer = new AutoExtendByteBuffer(1024);
        putBasicDeviceInfo(context, buffer);
        putSensorInfo(context, buffer);
        buffer.putString(getNetworkInterfaceNames());
        return buffer.getLongHash();
    }

    private static void putBasicDeviceInfo(Context context, AutoExtendByteBuffer buffer) {
        buffer.putString(Build.MODEL).put(SEPARATOR)
                .putInt(getCPUCores()).put(SEPARATOR)
                .putInt(getRamSize(context)).put(SEPARATOR)
                .putInt(getRomSize()).put(SEPARATOR)
                .putString(getWindowInfo(context));
    }

    private static int getCPUCores() {
        int cores;
        try {
            File[] files = new File("/sys/devices/system/cpu/").listFiles(CPU_FILTER);
            if (files == null || files.length == 0) {
                cores = Runtime.getRuntime().availableProcessors();
            } else {
                cores = files.length;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            cores = Runtime.getRuntime().availableProcessors();
        }
        return cores;
    }

    private static final FileFilter CPU_FILTER = pathname -> {
        String path = pathname.getName();
        if (path.startsWith("cpu")) {
            for (int i = 3; i < path.length(); i++) {
                if (path.charAt(i) < '0' || path.charAt(i) > '9') {
                    return false;
                }
            }
            return true;
        }
        return false;
    };


    private static int getRomSize() {
        long totalBytes = new StatFs(Environment.getDataDirectory().getPath()).getTotalBytes();
        // 从1G开始， 逐步试探，直到大于totalBytes
        long size = 1 << 30;
        while (totalBytes > size) {
            size <<= 1;
        }
        return (int) (size >> 30);
    }

    // 规范化为G, 因为精确到B的话变化的概率较大。
    private static int normalizeToG(long size) {
        return (int) (size >> 30) + (((size & 0x3FFFFFFF) == 0) ? 0 : 1);
    }

    private static int getRamSize(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        if (am != null) {
            am.getMemoryInfo(memoryInfo);
            return normalizeToG(memoryInfo.totalMem);
        } else {
            return normalizeToG(getTotalMemorySize());
        }
    }

    private static long getTotalMemorySize() {
        String dir = "/proc/meminfo";
        try {
            FileReader fr = new FileReader(dir);
            BufferedReader br = new BufferedReader(fr, 2048);
            String memoryLine = br.readLine();
            String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
            br.close();
            return Integer.parseInt(subMemoryLine.replaceAll("\\D+", "")) * 1024L;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return 0L;
    }

    private static String getWindowInfo(Context context) {
        DisplayMetrics dm;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            dm = new DisplayMetrics();
            wm.getDefaultDisplay().getRealMetrics(dm);
        } else {
            dm = context.getResources().getDisplayMetrics();
        }
        return dm.widthPixels + "x" + dm.heightPixels + " " + dm.xdpi + "x" + dm.ydpi;
    }

    private static void putSensorInfo(Context context, AutoExtendByteBuffer buffer) {
        try {
            SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager == null) {
                return;
            }

            List<Sensor> list = sensorManager.getSensorList(Sensor.TYPE_ALL);
            if (list != null) {
                for (Sensor sensor : list) {
                    buffer.putString(sensor.getName()).put(SEPARATOR)
                            .putString(sensor.getVendor()).put(SEPARATOR)
                            .putInt(sensor.getType()).put(SEPARATOR)
                            .putFloat(sensor.getResolution()).put(SEPARATOR);
                }
            }
        } catch (Exception e) {
            Log.e("tag", e.getMessage(), e);
        }
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
            return builder.toString();
        } catch (Exception e) {
            Log.e("tag", e.getMessage(), e);
        }
        return "";
    }
}

