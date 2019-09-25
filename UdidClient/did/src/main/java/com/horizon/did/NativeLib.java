package com.horizon.did;

public class NativeLib {
    public static native long getTotalRamSize();

    public static native String getSystemProperty(String name);

    static {
        System.loadLibrary("native_lib");
    }
}
