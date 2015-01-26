package com.corebase.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * URL工具
 */
public class URLUtils {

    public static String encodeURL(String url) throws UnsupportedEncodingException {
        return URLEncoder.encode(url, "utf-8");
    }
}
