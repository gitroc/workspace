package com.corebase.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferencesUtils {

	public static void setPreferences(Context context, String preference, String key, boolean value){
		if(context==null) return;
        SharedPreferences sharedPreferences = context.getSharedPreferences(preference, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
    
    public static void setPreferences(Context context, String preference, String key, long value){
    	if(context==null) return;
        SharedPreferences sharedPreferences = context.getSharedPreferences(preference, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.commit();
    }
    
    public static void setPreferences(Context context, String preference, String key, String value){
    	if(context==null) return;
        SharedPreferences sharedPreferences = context.getSharedPreferences(preference, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }
    
    public static String getPreference(Context context, String preference, String key, String defaultValue){
    	if(context==null) return null;
        SharedPreferences sharedPreferences = context.getSharedPreferences(preference, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defaultValue);
    }
    
    public static void setPreferences(Context context, String preference, String key, int value){
    	if(context==null) return;
        SharedPreferences sharedPreferences = context.getSharedPreferences(preference, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }
    
    public static boolean getPreference(Context context, String preference, String key, boolean defaultValue){
    	if(context==null) return defaultValue;
        SharedPreferences sharedPreferences = context.getSharedPreferences(preference, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, defaultValue);
    }
    
    public static long getPreference(Context context, String preference, String key, long defaultValue){
    	if(context==null) return -1;
        SharedPreferences sharedPreferences = context.getSharedPreferences(preference, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(key, defaultValue);
    }
    
    public static int getPreference(Context context, String preference, String key, int defaultValue){
    	if(context==null) return -1;
        SharedPreferences sharedPreferences = context.getSharedPreferences(preference, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, defaultValue);
    }
}
