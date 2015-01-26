package com.corebase.utils;

import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtils {
    private static boolean debugMode = false;
    private static File logDir;

    public static void enableDebug() {
        debugMode = true;
    }

    public static void setLogDir(File dir) {
        logDir = dir;
    }

    public static String getExceptionString(Throwable exception, Date time) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return timeFormat.format(time) + "\r\n" + Log.getStackTraceString(exception) + "\r\n\r\n";
    }

    public static void printLog(String msg){
        if(debugMode){
            System.out.println(msg);
        }
    }
    
    public static void writeLog(String msg){
        if(debugMode){
            try{
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date time = new Date();
                File logFile = new File(logDir, dateFormat.format(time) + ".log");
                FileWriter fileWriter = null;
                try {
                    fileWriter = new FileWriter(logFile, true);
                    fileWriter.write(timeFormat.format(time) + " " + msg + "\r\n");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if(fileWriter != null) {
                        try {
                            fileWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
