package com.roc.example;

/**
 * Created by songjunpeng on 2015/1/9.
 * the following order will generate *.h code and it will be used jni.
 * "cd D:\project\workspace\example\src\main"
 * "javah -d jni -classpath D:\project\android-sdk-windows\platforms\android-21\android.jar;..\..\build\intermediates\classes\debug com.roc.example.JniExample"
 */
public class JniExample {
    public static native String getString();
    public static native int square(int number);

    static {
        System.loadLibrary("JniExample");
    }
}
