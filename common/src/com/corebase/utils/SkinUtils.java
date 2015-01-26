package com.corebase.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabWidget;
import android.widget.TableLayout;
import android.widget.TextView;
import com.corebase.android.framework.http.client.HttpClient;

public class SkinUtils {
    public static void setSkin(Context context,View view,String skinName){
        setSkin(context,view,skinName,null);
    }
    
    //针对app主页底部tab item换肤做的处理限制view是为了兼容不同分辨率的手机
    public static void setSkin(Context context,View view,String skinName,LayoutParams lp){
        if(view==null) return;
        //皮肤文件路径
        String skinFilePath = context.getDir("skin", Context.MODE_PRIVATE).getAbsoluteFile()
                              + File.separator
                              +skinName;
        File file = new File(skinFilePath);
        if(null!=file && file.exists()){
            
            if(null!=lp){
                view.setLayoutParams(lp);
            }
            
            if(view instanceof LinearLayout     ||
               view instanceof FrameLayout      ||
               view instanceof RelativeLayout   ||
               view instanceof TableLayout      ||
               view instanceof ImageButton      ||
               view instanceof TextView         ||
               view instanceof TabWidget        ||
               view instanceof ImageView
              ) {
                
                BitmapDrawable image = new BitmapDrawable(context.getResources(),BitmapFactory.decodeFile(skinFilePath));
                //设置图片在手机中以手机本身的密度来显示图片
                image.setTargetDensity(context.getApplicationContext().getResources().getDisplayMetrics());
                
                view.setBackgroundDrawable(image);
            }
            
        }
    }
    
    
    /**
     * 设置皮肤工具方法,为veiw组件设置前景
     * @param context
     * @param view
     * @param skinName
     */
    public static void setSkin4Src(Context context,View view,String skinName,LayoutParams lp){
        if(view==null) return;
        //皮肤文件路径
        String skinFilePath = context.getDir("skin", Context.MODE_PRIVATE).getAbsoluteFile()
                              + File.separator
                              +skinName;
        if(null!=skinFilePath && !"".equals(skinFilePath)){
            File file = new File(skinFilePath);
            if(null!=file && file.exists()){
                if(null!=lp){
                    view.setLayoutParams(lp);
                }
                BitmapDrawable image = new BitmapDrawable(context.getResources(),BitmapFactory.decodeFile(skinFilePath));
                image.setTargetDensity(context.getApplicationContext().getResources().getDisplayMetrics());
                if(view instanceof ImageView){
                    ((ImageView)view).setImageDrawable(image);
                }
            }
        }
    }
    
    public static void setSkin4Src(Context context,View view,String skinName){
        setSkin4Src(context,view,skinName,null);
    }
    
    /**
     * 设置背景根据高度
     * @param context
     * @param view
     * @param skinName
     * @param mHeight 要设置的背景高度
     */
    public static void setSkinByHeight(Context context,View view,String skinName,int mHeight){
        if(null==view) return;
    	LayoutParams lp= view.getLayoutParams();
    	if(null!=lp){
    	    lp.width=ViewGroup.LayoutParams.MATCH_PARENT;
    	    lp.height = mHeight;
    	    view.setLayoutParams(lp);
    	}
    	setSkin(context,view,skinName);
    }
    
    /**
     * 设置src根据高度
     * @param context
     * @param view
     * @param skinName
     * @param mHeight 要设置src高度
     */
    public static void setSkin4SrcByHeight(Context context,View view,String skinName,int mHeight){
        if(null==view) return;
        LayoutParams lp= view.getLayoutParams();
        if(null!=lp){
            lp.width=ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = mHeight;
            view.setLayoutParams(lp);
        }
        setSkin4Src(context,view,skinName);
    }
    
    /**
     * 设置APP主页面标题文本
     * @param context
     * @param view
     */
    public static void setTitle(Context context,View view){
        String skinFilePath = context.getDir("skin", Context.MODE_PRIVATE).getAbsoluteFile()
                              + File.separator
                              +"Launcher_Json.json";
        File file = new File(skinFilePath);
        if(file.exists()){
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"GBK"));
                String json = "";
                String str = "";
                while((str=br.readLine()) != null){
                    json = json+str;
                }
                if(!"".equals(json) && json!=null){
                    JSONObject jo = new JSONObject(json);
                    if(view instanceof TextView){
                        
                        TextView tv = (TextView)view;
                        String title = jo.optString("title");
                        JSONArray colorArray = jo.optJSONArray("color");
                        Integer size = Integer.valueOf(jo.optInt("size"));
                        //设置标题文字
                        if(!"".equals(title) && title!=null){
                            tv.setText(title);
                        }
                        //设置字体颜色
                        if(colorArray != null){
                            tv.setTextColor(Color.rgb(colorArray.getInt(0), colorArray.getInt(1), colorArray.getInt(2)));   
                        }
                        //设置字体大小
                        if(size != null && 0!=size ){
                            tv.setTextSize(size);               
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } 
        }
    }


    //下载最新皮肤包解压至内部存储皮肤目录下
    public static void downLoadNewSkins(Context context,String url,String versionName){
        SharedPreferences preferences = context.getSharedPreferences("Pcgroup_skins", Context.MODE_PRIVATE);
        Editor editor  = preferences.edit();
        //当前皮肤版本
        String currentSkinVersion;
        //最新皮肤版本
        String newSkinVersion;

        //查看皮肤文件夹内的皮肤是否为当前APP版本对应的皮肤,如果不是则应该清空
        String skinAppVersion = preferences.getString("appVersion", "1.0.0");
        if(!skinAppVersion.equals(versionName)){
            cleanSkins(context);
        }

        InputStream inputStream=null;
        try {
            inputStream = HttpClient.getHttpClientInstance().downloadWithCache(url, null, null);
            if(inputStream!=null){
                String json = FileUtils.readTextInputStream(inputStream);
                if(json != null && !"".equals(json)){
                    JSONObject jsonObj = new JSONObject(json);
                    JSONObject versionJsonObj = jsonObj.optJSONObject(versionName);
                    if(null==versionJsonObj) return;

                    currentSkinVersion = preferences.getString("skinVersion", "2000-01-01 00:00:00");
                    newSkinVersion = versionJsonObj.optString("time");

                    //本地皮肤版本低于网络版本
                    if(string2Date(currentSkinVersion).before(string2Date(newSkinVersion))){
                        //此时可能本地已经有皮肤,即始网络皮肤下载下来会覆盖本地,也可能由于皮肤内皮肤图片数量不一致,导至错乱
                        //故需要先清空
                        cleanSkins(context);
                        String uri = "skin";
                        //皮肤文件存在则下载
                        if(versionJsonObj.optString(uri) != null && !"".equals(versionJsonObj.optString(uri))){
                            //下载最新皮肤
                            File skinFile = new File(context.getDir("skin", Context.MODE_PRIVATE).getAbsolutePath()+File.separator+"pcgroup.zip");
                            HttpClient.getHttpClientInstance().downloadToFile(versionJsonObj.optString(uri), skinFile, false);
                            ZipUtils.unZipFolder(skinFile.getAbsolutePath(), context.getDir("skin", Context.MODE_PRIVATE).getAbsolutePath());
                            //更新本地皮肤配置信息
                            editor.putString("skinVersion", versionJsonObj.optString("time"));
                            editor.putString("skinUrl", versionJsonObj.optString(uri));
                            editor.putString("appVersion", versionName);
                            editor.commit();
                        }
                        //皮肤文件不存在则表示还原为APP默认皮肤,删除skin目录下皮肤资源
                        else{
                            cleanSkins(context);
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(inputStream!=null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 清空皮肤文件夹
     */
    private static void cleanSkins(Context context){
        //删除当前应用data/data/.../skin目录下皮肤文件
        File skinFileDir = context.getDir("skin", Context.MODE_PRIVATE);
        File[] skinFiles = skinFileDir.listFiles();
        for(int i=0;i<skinFiles.length;i++){
            FileUtils.delete(skinFiles[i]);
        }
    }
    
    //将字符转换成日期
	private static Date string2Date(String dateStr) throws ParseException{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return sdf.parse(dateStr);
    }
}
