package com.maintab;

/**
 * Created by songjunpeng on 2015/1/9.
 */
public class JniString {
    public static native String getJniStringFromNative();

    static {
        System.loadLibrary("JniSample");
    }
}
