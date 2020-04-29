package com.horizon.did;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import utils.MHash;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;

public class PhysicsInfo {
    private static final String TAG = "PhysicsInfo";

    public static long getHash(Context context) {
        String totalInfo = Build.MODEL + ","
                + PhysicsInfo.getCPUCores() + ","
                + PhysicsInfo.getRamSize(context) + ","
                + PhysicsInfo.getRomSize() + ","
                + PhysicsInfo.getWindowInfo(context);
        return MHash.hash64(totalInfo);
    }

    public static int getCPUCores() {
        int cores;
        try {
            cores = new File("/sys/devices/system/cpu/").listFiles(CPU_FILTER).length;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            cores = Runtime.getRuntime().availableProcessors();
        }
        return cores;
    }

    private static final FileFilter CPU_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
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
        }
    };

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

}
