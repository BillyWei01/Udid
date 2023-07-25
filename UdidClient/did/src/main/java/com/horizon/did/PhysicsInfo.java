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

import utils.AutoExtendByteBuffer;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;

public class PhysicsInfo {
    private static final String TAG = "PhysicsInfo";

    private static final byte SEPARATOR = (byte) (',');

    // 获取相对稳定的设备指纹（设备信息的Hash）
    public static long getDeviceHash(Context context) {
        AutoExtendByteBuffer buffer = new AutoExtendByteBuffer(1024);
        putBasicDeviceInfo(context, buffer);
        putStrictSensorInfo(context, buffer);
        buffer.putString(getNetworkInterfaceNames());
        return buffer.getLongHash();
    }

    // 获取基本的设备信息的Hash
    public static long getBasicHash(Context context) {
        AutoExtendByteBuffer buffer = new AutoExtendByteBuffer(512);
        putBasicDeviceInfo(context, buffer);
        return buffer.getLongHash();
    }

    // 获取相对隐蔽的的设备信息的Hash
    public static long getDarkHash(Context context) {
        AutoExtendByteBuffer buffer = new AutoExtendByteBuffer(1024);
        putSensorInfo(context, buffer);
        buffer.putString(getNetworkInterfaceNames());
        return buffer.getLongHash();
    }

    private static void putBasicDeviceInfo(Context context, AutoExtendByteBuffer buffer) {
        buffer.putString(Build.MODEL).put(SEPARATOR)
                .putInt(getCPUCores()).put(SEPARATOR)
                .putString(getScalingAvailableFrequencies())
                //.putString(getScalingAvailableGovernors())
                .putInt(getRamSize(context)).put(SEPARATOR)
                .putInt(getRomSize()).put(SEPARATOR)
                .putString(getWindowInfo(context));
    }

    public static int getCPUCores() {
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

    // 不同的cpu policy， cpu最大最小频率不同
//    private static String getMinCpuFreq() {
//        return getCpuInfo("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq");
//    }
//
//    private static String getMaxCpuFreq() {
//        return getCpuInfo("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq");
//    }

    // 可用频率表，这个应该相对固定了吧
    private static String getScalingAvailableFrequencies() {
        return getCpuInfo("scaling_available_frequencies");
    }

//    private static String getScalingAvailableGovernors() {
//        return getCpuInfo("scaling_available_governors");
//    }

    private static String getCpuInfo(String name) {
        FileInputStream in = null;
        try {
            String path = "/sys/devices/system/cpu/cpu0/cpufreq/";
            File file = new File(path + name);
            if (file.canRead()) {
                in = new FileInputStream(file);
                Scanner sc = new Scanner(in);
                return sc.nextLine();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            closeQuietly(in);
        }
        return "";
    }

    static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable ignore) {
            }
        }
    }

    public static int getRomSize() {
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

    public static int getRamSize(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        if (am != null) {
            am.getMemoryInfo(memoryInfo);
            return normalizeToG(memoryInfo.totalMem);
        } else {
            return normalizeToG(getTotalMemorySize());
        }
    }

    public static long getTotalMemorySize() {
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

    public static String getWindowInfo(Context context) {
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
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager == null) {
            return;
        }
        List<Sensor> list = sensorManager.getSensorList(Sensor.TYPE_ALL);
        if (list != null) {
            for (Sensor sensor : list) {
                buffer.putString(sensor.getName()).put(SEPARATOR)
                        .putString(sensor.getVendor()).put(SEPARATOR)
                        .putInt(sensor.getVersion()).put(SEPARATOR)
                        .putInt(sensor.getType()).put(SEPARATOR)
                        .putFloat(sensor.getMaximumRange()).put(SEPARATOR)
                        .putFloat(sensor.getResolution()).put(SEPARATOR)
                        .putFloat(sensor.getPower()).put(SEPARATOR)
                        .putInt(sensor.getMinDelay()).put(SEPARATOR);
            }
        }
    }

    // 此方法用于构建本地生成的设备唯一ID，所以传感器参数采集保守一些，
    // 像version, power, minDelay这种获取会系统升级有可能变化？
    private static void putStrictSensorInfo(Context context, AutoExtendByteBuffer buffer) {
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
                            // .putInt(sensor.getVersion()).put(SEPARATOR)
                            .putInt(sensor.getType()).put(SEPARATOR)
                            //.putFloat(sensor.getMaximumRange()).put(SEPARATOR)
                            .putFloat(sensor.getResolution()).put(SEPARATOR);
                            // .putFloat(sensor.getPower()).put(SEPARATOR)
                            //.putInt(sensor.getMinDelay()).put(SEPARATOR);
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
