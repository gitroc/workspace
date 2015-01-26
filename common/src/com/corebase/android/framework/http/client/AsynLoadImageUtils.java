package com.corebase.android.framework.http.client;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.corebase.android.framework.cache.Cache;
import com.corebase.android.framework.cache.CacheManager;
import com.corebase.android.framework.cache.LruCache;
import com.corebase.utils.BitmapDecoder;

/**
 * 加载图片工具类  
 * @author Liyang
 *
 */
public class AsynLoadImageUtils {
    
    public static BitmapLruCache mMemoryCache = new BitmapLruCache(Cache.memoryCacheSize);
    
    private Animation animation;
    
	private Bitmap bitmap;
	
	private Params params;
    
    private AsynLoadImageUtils() {}
    
    private static class SingleHolder{
        private static final AsynLoadImageUtils asyncLoadSmallImage = new AsynLoadImageUtils();
    }
    
    public static AsynLoadImageUtils getInstance(){
        return SingleHolder.asyncLoadSmallImage;
    }
    
    //大图处理监听器
    public static abstract class DownloadImgListener{
        public abstract void success(String url,InputStream inputStream);
        public abstract void failure(Throwable error, String content);
    }
    
    public static interface OnLoadBitmapSuccess {
        void onLoadBitmapSuccess(String url, Bitmap bitmap);
    }
    
    //缓存图片类
    public static class BitmapLruCache extends LruCache<String, Bitmap> {
        public BitmapLruCache(int maxSize) {
            super(maxSize);
        }
        
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getRowBytes() * value.getHeight();
        }
    }
    
    //添加bitmap缓存
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }
    
    //获取缓存bitmap
    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }
    
    /**
     * 大图片下载
     * @param context
     * @param imageView
     * @param url
     * @param position
     * @param listener
     * @deprecated
     */
    public void loadAndFillBigImg(final Context context,final ImageView imageView,final String url,final DownloadImgListener listener){
        AsyncHttpClient asyncHttpClient = AsyncHttpClient.getHttpClientInstance();
        CacheParams cacheParams=null;
        if(imageView==null || url==null || "".equals(url)){
            Log.e(context.getClass().getSimpleName(), "imageView or url is null");
            return;
        }
        cacheParams = new CacheParams(CacheManager.TYPE_EXTERNAL,CacheManager.imageCacheExpire , false);
        asyncHttpClient.get(context, url, cacheParams,new BinaryHttpResponseHandler(){
            @Override
            public void onSuccess(InputStream inputStream) {
                super.onSuccess(inputStream);
                try{
                    listener.success(url, inputStream);
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
            
            @Override
            public void onFailure(Throwable error, String content) {
                listener.failure(error, content);
            }
        });
    }
    
   /**
    * 加载图片
    * @param context   
    * @param imageView
    * @param url
    * @param buildParams  需要下载图片的相关参数
    */
    public void loadImage(Context context,final String url,final ImageView imageView,BuildParams buildParams){
        if(buildParams!=null){
            params = buildParams.params;
        }else{
            params = new Params();
        }
        if(context!=null && url!=null && imageView!=null){
            imageView.setTag(url);
//            //从内存缓存中获取bitmap
//            bitmap = getBitmapFromMemCache(url);
//            if (null != bitmap) {
//                imageView.setImageBitmap(bitmap);
//                if (null != params.progressBar) {
//                    params.progressBar.setVisibility(View.GONE);
//                }
//                //当监听器不为空时,将bitmap传递给开发者
//                if(params.loadBitmapSuccess!=null){
//                    params.loadBitmapSuccess.onLoadBitmapSuccess(url, bitmap);
//                }
//            } else 
            if(!params.isFling){//非快速滑动时,默认为false
                AsyncHttpClient asyncHttpClient = AsyncHttpClient.getHttpClientInstance();
                CacheParams cacheParams = null;
                //当需要进度条时，并且进度条不为空，将其显示出来
                if (null != params.progressBar && !params.progressBar.isShown()) {
                    params.progressBar.setVisibility(View.VISIBLE);
                }
                
                //当需要缓存时调用
                if (params.isCache) {
                    cacheParams = new CacheParams(CacheManager.TYPE_EXTERNAL,
                            CacheManager.imageCacheExpire, params.isRefresh);
                }
                
                asyncHttpClient.get(context, url, cacheParams,new BinaryHttpResponseHandler() {
                	
                    @Override
                    public void onSuccess(InputStream inputStream) {
                        super.onSuccess(inputStream);
                        //开发者自己实现
                        if(params.downloadImgListener!=null){
                            params.downloadImgListener.success(url, inputStream);
                        }else{
                            try {
                                ImageView imageViewByTag = (ImageView) imageView.findViewWithTag(url);
                                if (imageViewByTag != null) {
                                    //开发者传递了需要的图片尺寸大小时
                                    if(params.imgSize!=null && params.imgSize.length>0){
                                        bitmap = BitmapDecoder.decodeBitmapFromStream(inputStream, params.imgSize[0], params.imgSize[1]);
                                    }else{
                                        bitmap = BitmapFactory.decodeStream(inputStream);
                                    }
                                    
                                    // 回调监听器的方法
                                    if (params.loadBitmapSuccess != null) {
                                        params.loadBitmapSuccess.onLoadBitmapSuccess(url,bitmap);
                                    }
                                    
                                    imageViewByTag.setImageBitmap(bitmap);
                                    
                                    //开启图片淡出的动画效果
                                    if(params.isFadeOut){
                                        fadeOut(imageViewByTag);
                                    }
                                    
//                                    if(url!=null && bitmap!=null){
//                                    	//添加到内存缓存的中
//                                    	addBitmapToMemoryCache(url, bitmap);
//                                    }
                                    if (null != params.progressBar) {
                                        params.progressBar.setVisibility(View.GONE);
                                    }
                                }
                            } finally {
                                if (inputStream != null) {
                                    try {
                                        inputStream.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable error, String content) {
                        if(params.downloadImgListener!=null){
                            params.downloadImgListener.failure(error, content);
                        }
                    }
                });
            }
       }else{
           Log.e("AsynLoadImageUtils", "context,imageview,url can not be empty!");
       }
    }
    
    //图片淡出
    private void fadeOut(ImageView imageView){
        animation = new AlphaAnimation(0, (float) 1.0);
        animation.setDuration(300);
        imageView.setAnimation(animation);
    }
	
    /**
     * 字节数组转成流
     * @param data
     * @return
     */
    public static InputStream byteToInputSteram(byte[] data){
        InputStream is=null;
        if(null!=data&& data.length>0){
            is = new ByteArrayInputStream(data);  
        }
        return is;
    }
    
    //从输入流读取文本内容
    public static String readTextInputStream(InputStream is) throws IOException {
        StringBuffer strbuffer = new StringBuffer();
        String line;
        BufferedReader reader = null;
        try{
    		reader = new BufferedReader(new InputStreamReader(is));
    		while((line = reader.readLine()) != null) {
    			strbuffer.append(line).append("\r\n");
    		}
        } finally {
            if(reader != null) {
                reader.close();
            }
        }
        return strbuffer.toString();
    }
    
    
    public static class BuildParams{
        Params params;
        public BuildParams(){
            params = new Params();
        }
        public BuildParams setCache(boolean isCache) {
            params.isCache = isCache;
            return this;
        }
        public BuildParams setFling(boolean isFling) {
            params.isFling = isFling;
            return this;
        }
        public BuildParams setFadeOut(boolean isFadeOut) {
            params.isFadeOut = isFadeOut;
            return this;
        }
        public BuildParams setProgressBar(ProgressBar progressBar) {
            params.progressBar = progressBar;
            return this;
        }
        public BuildParams setImgSize(final int width,final int height) {
            params.imgSize = new int[]{width,height};
            return this;
        }
        public BuildParams setRefresh(boolean isRefresh) {
            params.isRefresh = isRefresh;
            return this;
        }
        public BuildParams setDownloadImgListener(DownloadImgListener downloadImgListener) {
            params.downloadImgListener = downloadImgListener;
            return this;
        }
        
        public BuildParams setOnLoadBitmapSuccess(OnLoadBitmapSuccess loadBitmapSuccess){
            params.loadBitmapSuccess = loadBitmapSuccess;
            return this;
        }
    }
    
    private static class Params{
        private boolean isCache;    //是否缓存
        private boolean isFling;    //是否快速滑动
        private boolean isFadeOut = true;  //是否淡出
        private ProgressBar progressBar;
        private int[] imgSize;      //图片尺寸 
        private boolean isRefresh;  //是否刷新
        private DownloadImgListener downloadImgListener; //下载监听器
        private OnLoadBitmapSuccess loadBitmapSuccess;   //下载bitmap成功监听器
    }
}
