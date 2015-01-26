/*
    Android Asynchronous Http Client
    Copyright (c) 2011 James Smith <james@loopj.com>
    http://loopj.com

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.corebase.android.framework.http.client;

import java.io.IOException;
import java.net.ConnectException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.corebase.android.framework.cache.CacheManager;

class MyHttpRequest {
    private final AbstractHttpClient client;
    private final HttpContext context;
    private final HttpUriRequest request;
    private int executionCount;
    private CacheParams cacheParams;
    private String url;

    public MyHttpRequest(AbstractHttpClient client, HttpContext context, HttpUriRequest request, CacheParams cacheParams) {
        this.client = client;
        this.context = context;
        this.request = request;
        this.cacheParams=cacheParams;
        this.url = request.getURI().toString();
    }

    //下载并缓存
    public byte[] downloadWithCache() throws ConnectException, Exception{
        byte[] data = null;
        boolean retry = true;
        IOException cause = null;
        HttpRequestRetryHandler retryHandler = client.getHttpRequestRetryHandler();
        while (retry) {
            try {
                //pcgroup缓存处理
                if(null!=cacheParams && !cacheParams.isRefresh()){
                    data = CacheManager.getCache(url);
                    if(null!=data){
                        return data;
                    }else{
                        data = executeTask();
                    }
                }else{
                    data = executeTask();
                }
            } catch (IOException e) {
                cause = e;
                retry = retryHandler.retryRequest(cause, ++executionCount, context);
            } catch (NullPointerException e) {
                cause = new IOException("NPE in HttpClient" + e.getMessage());
                retry = retryHandler.retryRequest(cause, ++executionCount, context);
            }
        }
        
        // no retries left, crap out with exception
        ConnectException ex = new ConnectException();
        ex.initCause(cause);
        throw ex;
    }
    
    private byte[] executeTask() throws ClientProtocolException, IOException{
        byte[] byteArray=null;
        HttpResponse response = client.execute(request, context);
        byteArray = getByteArrayAndSendMessage(response);
        
        //缓存参数不为空的时候设置缓存
        if(null!=byteArray && null!=cacheParams){
            CacheManager.setCache(url, byteArray, cacheParams.getExpireTime(), cacheParams.getStoreType());
        }
        return byteArray;
    }
    
    protected byte[] getByteArrayAndSendMessage(HttpResponse response) throws IOException {
        byte[] responseBody = null;
        Header[] contentTypeHeaders = response.getHeaders("Content-Type");
        if(contentTypeHeaders.length != 1) {
            return null;
        }
        HttpEntity entity = null;
        HttpEntity temp = response.getEntity();
        if(temp != null) {
            entity = new BufferedHttpEntity(temp);
        }
        responseBody = EntityUtils.toByteArray(entity);

        return responseBody;
    }
}
