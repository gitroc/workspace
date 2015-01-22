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

package cn.com.aa.android.framework.http.client;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import cn.com.aa.android.framework.cache.CacheManager;
import cn.com.aa.common.android.utils.FileUtils;
import cn.com.aa.common.android.utils.StringUtils;

class AsyncHttpRequest implements Runnable {
	private final AbstractHttpClient client;
	private final HttpContext context;
	private final Context mycontext;
	private final HttpUriRequest request;
	private final AsyncHttpResponseHandler responseHandler;
	private int executionCount;
	private CacheParams cacheParams;
	private String url;
	private boolean isGetByDateBase;// 是否从数据库拿缓存数据
	private String requestParam;// post请求url都是一样的 所以保存缓存文件都是一样的加上一个参数作为区分
	private int statusCode;

	public AsyncHttpRequest(AbstractHttpClient client, HttpContext context, HttpUriRequest request, CacheParams cacheParams, AsyncHttpResponseHandler responseHandler, Context mycontext, String requestParam) {
		this.client = client;
		this.context = context;
		this.mycontext = mycontext;
		this.request = request;
		this.responseHandler = responseHandler;
		this.cacheParams = cacheParams;
		this.url = request.getURI().toString();
		this.requestParam = requestParam;
	}

	public AsyncHttpRequest(AbstractHttpClient client, HttpContext context, HttpUriRequest request, CacheParams cacheParams, AsyncHttpResponseHandler responseHandler, Context mycontext) {
		this.client = client;
		this.context = context;
		this.mycontext = mycontext;
		this.request = request;
		this.responseHandler = responseHandler;
		this.cacheParams = cacheParams;
		this.url = request.getURI().toString();
	}

	public AsyncHttpRequest(AbstractHttpClient client, HttpContext context, HttpUriRequest request, CacheParams cacheParams, AsyncHttpResponseHandler responseHandler, Context mycontext, boolean isGetByDateBase) {
		this.client = client;
		this.context = context;
		this.mycontext = mycontext;
		this.request = request;
		this.responseHandler = responseHandler;
		this.cacheParams = cacheParams;
		this.url = request.getURI().toString();
		this.isGetByDateBase = isGetByDateBase;
	}

	public void run() {
		try {
			// 发送开始消息
			if (responseHandler != null) {
				responseHandler.sendStartMessage();
			}
			// 获取数据
			makeRequestWithRetries();
			// 发送获取数据完成消息
			if (responseHandler != null) {
				responseHandler.sendFinishMessage();
			}
		} catch (IOException e) {
			e.printStackTrace();
			if (responseHandler != null) {
				responseHandler.sendFinishMessage();
				responseHandler.sendFailureMessage(mycontext, e, (String) null);
			}
		}
	}

	/**
	 * 真正拿去数据的方法
	 * 
	 * @throws IOException
	 */
	private void makeRequest() throws IOException {
		Log.i("urlMsg", "拿数据" + "  cacheParams:" + url);
		if (!Thread.currentThread().isInterrupted()) {
			// pcgroup缓存处理
			if (null != cacheParams && !cacheParams.isRefresh() && !isGetByDateBase) {
				String cacheName = url;
				if (!TextUtils.isEmpty(requestParam)) {
					cacheName = url + requestParam;
				}
				Log.i("urlMsg", "拿数据" + "  cacheParams:" + cacheName + "  cacheParams.isRefresh():" + cacheParams.isRefresh() + " isGetByDateBase:" + isGetByDateBase);
				byte[] cacheArray = CacheManager.getCache(cacheName);
				if (cacheName.startsWith("http://10.32.17.236:9200/")) {
					Log.i("urlMsg", "取出缓存数据:" + cacheName + " cacheArray" + FileUtils.readTextInputStream(new ByteArrayInputStream(cacheArray)));
				}
				if (null != cacheArray) {
					responseHandler.sendResponseMessage(mycontext, FileUtils.byteToInputSteram(cacheArray));
				} else {
					getInternetData();
					Log.i("msg", "没有缓存拿新数据");
				}
			} else {
				getInternetData();
				Log.i("msg", "直接拿新数据");
			}
		}
	}

	public static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

	public static String log = "asyncHttpClient";

	private void getInternetData() throws ClientProtocolException, IOException {
		HttpResponse response = client.execute(request, context);
		statusCode = response.getStatusLine().getStatusCode();
		Log.d(log, "返回码:" + statusCode);
		if (!Thread.currentThread().isInterrupted()) {
			if (responseHandler != null) {
				// 获得返回实体的字节数组
				byte[] byteArray = responseHandler.getByteArrayAndSendMessage(mycontext, response);
				if (null != byteArray) {// 将请求返回的数据传递出去
					responseHandler.sendResponseMessage(mycontext, FileUtils.byteToInputSteram(byteArray));
				}
				// 缓存参数不为空的时候设置缓存
				if (null != byteArray && null != cacheParams) {
					Header[] allHeaders = response.getAllHeaders();
					// 针对这两个Header对本地缓存周期进行控制，优先Cache-Control
					// Cache-Control === max-age=86400
					// Expires === Thu, 24 Oct 2013 08:58:07 GMT
					long expires = 0;
					// 满足条件对下载内容按规则进行缓存
					if (null != allHeaders && allHeaders.length > 0) {
						if (null != allHeaders && allHeaders.length > 0) {
							String value;
							for (int i = 0; i < allHeaders.length; i++) {
								String name = allHeaders[i].getName();
								if (null != name && !"".equals(name)) {
									if (name.equals("Cache-Control")) {
										value = allHeaders[i].getValue();
										// && StringUtils.isNumeric(value)
										if (null != value && !"".equals(value)) {
											String time = value.substring(value.indexOf("=") + 1);
											if (null != time && time.indexOf(",") > -1) {
												time = time.substring(0, time.indexOf(","));
											}
											if (StringUtils.isNumeric(time)) {
												expires = Long.valueOf(time) * 1000 + System.currentTimeMillis();
												Log.v("Expires", "Cache-Control =" + expires);
											}
										}
										break;
									} else if (name.equals("Expires")) {
										value = allHeaders[i].getValue();
										if (null != value && !"".equals(value)) {
											Date expiresDate = new Date(value);
											expires = expiresDate.getTime();
											Log.v("Expires", "expires : " + expires);
										}
									} else {
										expires = cacheParams.getExpireTime();
									}
								}
							}
						}
					}

					// 时间周期大于0进行本地缓存
					if (expires > 0) {
						String cacheName = url;
						if (!TextUtils.isEmpty(requestParam)) {
							cacheName = url + requestParam;
						}
						if (cacheName.contains("http://10.32.17.236:9200/")) {

							Log.i("urlMsg", "存储缓存:" + cacheName);
						}
						CacheManager.setCache(cacheName, byteArray, expires, cacheParams.getStoreType());
					}
				}
			}
		} else {
			// TODO: should raise InterruptedException? this block is reached
			// whenever the request is cancelled before its response is received
		}
	}

	private void makeRequestWithRetries() throws IOException {
		// This is an additional layer of retry logic lifted from droid-fu
		// See:
		// https://github.com/kaeppler/droid-fu/blob/master/src/main/java/com/github/droidfu/http/BetterHttpRequestBase.java
		boolean retry = true;
		IOException cause = null;
		HttpRequestRetryHandler retryHandler = client.getHttpRequestRetryHandler();
		while (retry) {
			try {
				makeRequest();
				return;
			} catch (UnknownHostException e) {
				if (responseHandler != null) {
					responseHandler.sendFailureMessage(mycontext, e, "can't resolve host");
				}
				Log.i("msg", "1");
				return;
			} catch (SocketException e) {
				// Added to detect host unreachable
				if (responseHandler != null) {
					responseHandler.sendFailureMessage(mycontext, e, "can't resolve host");
				}
				Log.i("msg", "2");
				return;
			} catch (SocketTimeoutException e) {
				if (responseHandler != null) {
					responseHandler.sendFailureMessage(mycontext, e, "socket time out");
				}
				Log.i("msg", "3");
				return;
			} catch (IOException e) {
				cause = e;
				retry = retryHandler.retryRequest(cause, ++executionCount, context);
				Log.i("msg", "4");
			} catch (NullPointerException e) {
				// there's a bug in HttpClient 4.0.x that on some occasions
				// causes
				// DefaultRequestExecutor to throw an NPE, see
				// http://code.google.com/p/android/issues/detail?id=5255
				cause = new IOException("NPE in HttpClient" + e.getMessage());
				Log.i("msg", "4");
				retry = retryHandler.retryRequest(cause, ++executionCount, context);
			} catch (Exception e) {
				e.printStackTrace();
				cause = new IOException("Exception" + e.getMessage());
				retry = retryHandler.retryRequest(cause, ++executionCount, context);
				Log.i("msg", "4");
			}
		}

		// 没有能读取到网络数据
		// ConnectException ex = new ConnectException();
		// ex.initCause(cause);
		throw cause;
	}
}
