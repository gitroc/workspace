package cn.com.aa.common.android.utils;

import android.content.Context;

/**
 * 用于转换界面尺寸的工具类
 */
public class DisplayUtils {
    public static int convertDIP2PX(Context context, float dip) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dip*scale + 0.5f*(dip>=0?1:-1));
    }

    //转换px为dip
    public static int convertPX2DIP(Context context, float px) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(px/scale + 0.5f*(px>=0?1:-1));
    }
}
