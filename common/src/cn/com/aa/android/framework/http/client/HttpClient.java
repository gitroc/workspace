package cn.com.aa.android.framework.http.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.SyncBasicHttpContext;

import cn.com.aa.android.framework.cache.CacheManager;
import android.util.Log;
/**
 * 下载类
 * @author user
 *
 */
public class HttpClient {
    private static final String TAG = "HttpClient";
    
    private static final String USER_AGENT = AsyncHttpClient.USER_AGENT;
    private static final int DEFAULT_MAX_CONNECTIONS = 10;
    private static final int DEFAULT_SOCKET_TIMEOUT = 10 * 1000;
    private static final int DEFAULT_MAX_RETRIES = 5;
    private static final int DEFAULT_SOCKET_BUFFER_SIZE = 8192;
    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String ENCODING_GZIP = "gzip";
        
    private static int maxConnections = DEFAULT_MAX_CONNECTIONS;
    private static int socketTimeout = DEFAULT_SOCKET_TIMEOUT;

    private static HttpContext httpContext;
    private static DefaultHttpClient httpClient;
    //保存请求Header
    private static Map<String, String> clientHeaderMap;
    
    private byte[] buffer;
    private int BUFFER_SIZE = 1024;
    
    private static class SingleHolder{
        private static final HttpClient singleHttpClient = new HttpClient();
        
        private static final DefaultHttpClient httpClient = getHttpClient();
    }
    
    /**
     * 获取HttpClient对象
     * @param context
     * @return
     */
    public static final HttpClient getHttpClientInstance(){
        return SingleHolder.singleHttpClient;
    } 
    
    private static void init(){
        httpContext = new SyncBasicHttpContext(new BasicHttpContext());
        clientHeaderMap = new HashMap<String, String>();
    }
    
    private HttpClient() {
        httpClient =getHttpClient();
        
        init();
        
        httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
            public void process(HttpRequest request, HttpContext context) {
                if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
                    request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
                }
                if(!request.containsHeader(USER_AGENT)){
                    if(null!=USER_AGENT && !"".equals(USER_AGENT)){
                        request.addHeader("User-Agent", USER_AGENT);
                    }
                }
                for (String header : clientHeaderMap.keySet()) {
                    request.addHeader(header, clientHeaderMap.get(header));
                }
            }
        });
        
        httpClient.addResponseInterceptor(new HttpResponseInterceptor() {
            public void process(HttpResponse response, HttpContext context) {
                clientHeaderMap.clear();
            }
        });

        httpClient.setHttpRequestRetryHandler(new RetryHandler(DEFAULT_MAX_RETRIES));
    }
    
    /**
     * 下载数据到指定文件
     * @param urlStr 下载url
     * @param dest   写入文件
     * @param append 是否断点续传
     * @return
     * @throws Exception
     */
    public long downloadToFile(String urlStr, File dest,boolean append) throws Exception {
        long fileSize = -1;
        //清除目标文件
        if(!append && dest.exists() && dest.isFile()) {
            dest.delete();
        }

        //预处理断点续传
        if(append && dest.exists() && dest.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(dest);
                fileSize = fis.available();
            } catch(IOException e) {
                Log.i(TAG, "Get local file size fail: " + dest.getAbsolutePath());
                throw e;
            } finally {
                if(fis != null) {
                    fis.close();
                }
            }
        }
        
        InputStream is = null;
        FileOutputStream os = null;
        try{
            is=downloadWithCache(urlStr,null,null);
            os = new FileOutputStream(dest, append);
            byte buffer[] = new byte[BUFFER_SIZE];
            int readSize = 0;
            while((readSize = is.read(buffer)) > 0){
                os.write(buffer, 0, readSize);
                os.flush();
                fileSize += readSize;
            }
        }finally{
            if(os != null) {
                os.close();
            }
            if(is != null) {
                is.close();
            }
        }
        
        return fileSize;
    }
    
    
    /**
     * 执行下载并缓存
     * @param url               需要下载的链接
     * @param cacheParams       缓存参数
     * @param requestParams     请求参数
     * @return inputStream
     */
    public InputStream downloadWithCache(String url, CacheParams cacheParams,RequestParams requestParams) throws Exception{
        InputStream inputStream = null;
        if(null!=cacheParams&&!cacheParams.isRefresh()){//从缓存获取
        	byte[] content = CacheManager.getCache(url);
            if(null!=content&&content.length>0){
            	return  new ByteArrayInputStream(content);
            }
        }
        if(null==url || "".equals(url)) return null;
        
        HttpGet request=null;
        ByteArrayOutputStream baos = null;
        int len = 0;
        buffer = new byte[BUFFER_SIZE];
        byte[] outbuffer = null;
        try{
            request = new HttpGet(getUrlWithQueryString(url, requestParams));
            HttpResponse response = httpClient.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                inputStream = response.getEntity().getContent();
                Header contentEncoding = response.getFirstHeader("Content-Encoding");
                if(contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase(ENCODING_GZIP)) {
                    inputStream = new GZIPInputStream(inputStream);
                }
                if(null!=inputStream && cacheParams!=null && null!=buffer){
                    baos = new ByteArrayOutputStream();
                    while ((len = inputStream.read(buffer, 0, buffer.length)) != -1) {                     
                        baos.write(buffer, 0, len);
                    }
                    outbuffer =  baos.toByteArray();
                    CacheManager.setCache(url, outbuffer, cacheParams.getExpireTime()*1000, cacheParams.getStoreType());
                }
            }
        }catch(IllegalArgumentException e){
            e.printStackTrace();
            Log.e(TAG, "request url is invalid");
            return null;
        }catch(Exception e){
        	e.printStackTrace();
        }finally{
        	buffer = null;
        	outbuffer = null;
        	if(null!=baos){
        		baos.close();
        	}
        }
        return inputStream;
    }
    
    private static DefaultHttpClient getHttpClient(){
    	BasicHttpParams httpParams = new BasicHttpParams();

        ConnManagerParams.setTimeout(httpParams, socketTimeout);
        ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(maxConnections));
        ConnManagerParams.setMaxTotalConnections(httpParams, DEFAULT_MAX_CONNECTIONS);

        HttpConnectionParams.setSoTimeout(httpParams, socketTimeout);
        HttpConnectionParams.setConnectionTimeout(httpParams, socketTimeout);
        HttpConnectionParams.setTcpNoDelay(httpParams, true);
        HttpConnectionParams.setSocketBufferSize(httpParams, DEFAULT_SOCKET_BUFFER_SIZE);

        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(httpParams, schemeRegistry);

        return httpClient = new DefaultHttpClient(cm, httpParams);
    }
    
    /**
     * 发送网络数据
     * @param urlStr  接收数据的url
     * @return        是否发送成功
     * @throws        IOException
     */
    public static boolean sendData(String urlStr, RequestParams requestParams) {
        boolean success =  false;
        try {
            HttpPost request = new HttpPost(getUrlWithQueryString(urlStr, requestParams));
            DefaultHttpClient httpClient = SingleHolder.httpClient;
            System.out.println("httpClient : "+httpClient);
            HttpResponse response = SingleHolder.httpClient.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                success = true;
            }
        } catch(Exception e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }
    
    
    /**
     * 添加请求header
     * @param header 名称
     * @param value  内容
     */
    public void addHeader(String header, String value) {
        clientHeaderMap.put(header, value);
    }
    
    /**
     * 设置cookie
     * @param cookieStore
     */
    public void setCookieStore(CookieStore cookieStore) {
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
    }
    
    public static String getUrlWithQueryString(String url, RequestParams params) {
        if(params != null) {
            String paramString = params.getParamString();
            if (url.indexOf("?") == -1) {
                url += "?" + paramString;
            } else {
                url += "&" + paramString;
            }
        }
        return url;
    }
}
