package com.corebase.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * metadata解析工具类
 */
public class MetadataUtils {

    public static Metadata praseString(String str) {
        Metadata metadata = null;
        try {
            JSONObject json = new JSONObject(str);
            if(json.length() > 0) {
                metadata = new Metadata();
                Iterator<String> keys = json.keys();
                while(keys.hasNext()) {
                    String key = keys.next();
                    metadata.put(key, json.opt(key));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return metadata;
    }

    public static Metadata praseHtml(String html) {
        if(html != null && html.indexOf("/*@_HTML_META_START_")>0
                && html.indexOf("_HTML_META_END_@*/")>0) {
            String str = html.substring(html.indexOf("/*@_HTML_META_START_")+20,
                    html.indexOf("_HTML_META_END_@*/")).trim();
            return praseString(str);
        }
        return null;
    }

    public static class Metadata {
        Map<String, Object> metaMap = new HashMap<String, Object>();

        public int size() {
            return metaMap.size();
        }

        public void put(String key, Object value) {
            if(key != null && key.trim().length() > 0) {
                metaMap.put(key, value);
            }
        }

        public Object get(String key) {
            return metaMap.get(key);
        }

        public String getString(String key, String defaultValue) {
            Object value = metaMap.get(key);
            if(value != null) {
                return (String)value;
            }
            return defaultValue;
        }

        public int getInt(String key, int defaultValue) {
            Object value = metaMap.get(key);
            if(value != null) {
                return (Integer)value;
            }
            return defaultValue;
        }

        public long getInt(String key, long defaultValue) {
            Object value = metaMap.get(key);
            if(value != null) {
                return (Long)value;
            }
            return defaultValue;
        }
    }
}
