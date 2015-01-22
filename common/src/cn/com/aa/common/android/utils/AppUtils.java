package cn.com.aa.common.android.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * 应用相关的工具
 *
 */
public class AppUtils {
	
	/**
     * 获取当前app栈顶Task
     * @param context
     * @return
     */
    public static String getTopActivity(Context context){
         ActivityManager manager=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
         List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(100);
         for (RunningTaskInfo info : runningTaskInfos) {
             if (info.topActivity.getPackageName().equals(context.getPackageName()) && 
                     info.baseActivity.getPackageName().equals(context.getPackageName())) {
                 return info.topActivity.getClassName();
             }
         }
         return null;
    }
    
    /**
     * 判断app是否正在运行
     * @param context
     * @return
     */
    public static boolean isRunning(Context context){
        boolean isAppRunning = false;
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> list = am.getRunningTasks(100);
        for (RunningTaskInfo info : list) {
            if (info.topActivity.getPackageName().equals(context.getPackageName()) && 
                info.baseActivity.getPackageName().equals(context.getPackageName())) {
                isAppRunning = true;
                break;
            }
        }
        return isAppRunning;
    }
	
	/**
     * 启动终端上包名为apkPackage的APP
     * @param apkPackage
     */
    public static void startApp(Activity activity ,String apkPackage) {
        PackageManager pm = activity.getPackageManager();
        Intent i = pm.getLaunchIntentForPackage(apkPackage);
        // 如果该程序不可启动（像系统自带的包，有很多是没有入口的）会返回NULL
        if (i != null) {
        	activity.startActivity(i);
        }
    }
    
    /**
     * 判断包名为apkPackage的APP是否已经安装
     * @param apkPackage
     * @return
     */
    public static boolean isExistApp(Activity activity,String apkPackage) {
        PackageManager pm = activity.getPackageManager();
        List<PackageInfo> packs = pm
                .getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        List<String> packageList = new ArrayList<String>();
        for (PackageInfo pi : packs) {
            packageList.add(pi.packageName);
        }
        if (packageList.contains(apkPackage)) {
            return true;
        }
        return false;
    }
    

    /**
     * 判断apkPackage是否是当前APP的包名
     * @param apkPackage
     * @return
     */
    public static boolean isCurrentAppPackage(Activity activity,String apkPackage) {
        PackageInfo info;
        try {
            info = activity.getPackageManager().getPackageInfo(
            		activity.getPackageName(), 0);
            // 当前应用的版本名称
            // String versionName = info.versionName;
            // 当前版本的版本号
            // int versionCode = info.versionCode;
            // 当前版本的包名
            String packageName = info.packageName;
            if (apkPackage.equals(packageName)) {
                return true;
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    //判断是否锁屏
    public static boolean isScreenLocked(Context context) {
        KeyguardManager mKeyguardManager = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
        return mKeyguardManager.inKeyguardRestrictedInputMode();
    }
    
    
	/**
	 * 判断是否是第一次进入App
	 * @return
	 */
	public static boolean isFirstIn(Context context) {
		boolean isFirst = PreferencesUtils.getPreference(context,"app_first_in400", "isFirst400", true);
		if (isFirst) {
			PreferencesUtils.setPreferences(context, "app_first_in400","isFirst400", false);
			return true;
		} else {
			return false;
		}
	}
	
	
	/**
     * 获取状态栏高度/像素
     * @return
     */
    @SuppressWarnings("unused")
	private static int getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }

}
