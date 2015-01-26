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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.SyncBasicHttpContext;

import android.content.Context;

/**
 * The AsyncHttpClient can be used to make asynchronous GET, POST, PUT and
 * DELETE HTTP requests in your Android applications. Requests can be made with
 * additional parameters by passing a {@link RequestParams} instance, and
 * responses can be handled by passing an anonymously overridden
 * {@link AsyncHttpResponseHandler} instance.
 * <p>
 * For example:
 * <p>
 * 
 * <pre>
 *  AsyncHttpClient client = AsyncHttpClient.getHttpClientInstance();
 *  client.get(context,"http://www.google.com", cacheParams,new AsyncHttpResponseHandler() {
 *     &#064;Override
 *     public void onSuccess(String response) {
 *         TODO handler UI interaction
 *     }
 *     不重写此方法自动处理网络异常、数据为空、Json异常
 *     public void onFailure(Throwable error, String content) {
 *         如果要自己处理以上异常，将super.onFailure（）去掉
 *         super.onFailure(error, content);
 *         TODO handler download failure
 *     }
 * });
 * 
 * 说明：
 * 1. context : 上下文对象
 * 2. cacheParams ： 根据业务去设置里面的参数，如果不需要缓存调用get时直接设置null即可。
 * 3. 如果只是执行下载，不与ui页面进行交互，只需要最后一个参数设置为null。
 * 
 *  根据自己的需求去调用get方法。
 * 
 * </pre>
 */
public class AsyncHttpClient {
	// Android用户代理
	public static final String USER_AGENT = "PCGroup Android APP";
	private static final int DEFAULT_MAX_CONNECTIONS = 10;
	private static final int DEFAULT_SOCKET_TIMEOUT = 15 * 1000;
	private static final int DEFAULT_MAX_RETRIES = 5;
	private static final int DEFAULT_SOCKET_BUFFER_SIZE = 8192;
	private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
	private static final String ENCODING_GZIP = "gzip";
	public static Context context;
	private static int maxConnections = DEFAULT_MAX_CONNECTIONS;
	private static int socketTimeout = DEFAULT_SOCKET_TIMEOUT;

	private static DefaultHttpClient httpClient;

	private static HttpContext httpContext;
	private static ThreadPoolExecutor threadPool;
	private static Map<Context, List<WeakReference<Future<?>>>> requestMap;

	// private static Map<String, String> clientHeaderMap;

	private static class SingleHolder {
		private static final int CORE_POOL_SIZE = 5;
		private static final int MAXIMUM_POOL_SIZE = 128;
		private static final int KEEP_ALIVE = 0;
		private static final AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		private static final Map<Context, List<WeakReference<Future<?>>>> requestMap = new WeakHashMap<Context, List<WeakReference<Future<?>>>>();
		private static final ThreadPoolExecutor cachedThreadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		private static final BlockingQueue<Runnable> sWorkQueue = new LinkedBlockingQueue<Runnable>();
		private static final ThreadFactory sThreadFactory = new ThreadFactory() {
			private final AtomicInteger mCount = new AtomicInteger(1);

			public Thread newThread(Runnable r) {
				return new Thread(r, "Async#" + mCount.getAndIncrement());
			}
		};

		private static final ThreadPoolExecutor fixThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.MILLISECONDS, sWorkQueue, sThreadFactory);
		// private static final ThreadPoolExecutor
		// fixThreadPool=(ThreadPoolExecutor)Executors.newFixedThreadPool(threadPoolSize);
	}

	/**
	 * 获取AsyncHttpClient对象
	 * 
	 * @param context
	 * @return
	 */
	public static final AsyncHttpClient getHttpClientInstance() {
		// init();
		requestMap = SingleHolder.requestMap;
		threadPool = SingleHolder.cachedThreadPool;
		return SingleHolder.asyncHttpClient;
	}

	/**
	 * 获取AsyncHttpClient对象和FixedThreadPool(指定线程池中线程数)
	 * 
	 * @param threadPoolSize
	 * @return
	 */
	public static AsyncHttpClient getHttpClientAndFixedThreadPool(int size) {
		// init();
		requestMap = SingleHolder.requestMap;
		threadPool = SingleHolder.fixThreadPool;
		return SingleHolder.asyncHttpClient;
	}

	private static void init() {
		// clientHeaderMap = new HashMap<String, String>();
		httpContext = new SyncBasicHttpContext(new BasicHttpContext());
	}

	/**
	 * 线程池中的任务均执行完毕后，关闭线程池
	 */
	public static void shutdown() {
		threadPool.shutdown();
	}

	/**
	 * 线程池中的任务均执行完毕后，关闭线程池
	 */
	public static void shutdownNow() {
		threadPool.shutdownNow();
	}

	/**
	 * Creates a new AsyncHttpClient.
	 * 
	 * @throws KeyStoreException
	 */
	private AsyncHttpClient() {
//		CustomSSLSocketFactory customSSLSocketFactory = null;
//		try {
//			customSSLSocketFactory = initCustomSSLSocketFactory();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		BasicHttpParams httpParams = new BasicHttpParams();

		ConnManagerParams.setTimeout(httpParams, socketTimeout);
		ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(maxConnections));
		ConnManagerParams.setMaxTotalConnections(httpParams, DEFAULT_MAX_CONNECTIONS);

		HttpConnectionParams.setSoTimeout(httpParams, socketTimeout);
		HttpConnectionParams.setConnectionTimeout(httpParams, socketTimeout);
		HttpConnectionParams.setTcpNoDelay(httpParams, true);
		HttpConnectionParams.setSocketBufferSize(httpParams, DEFAULT_SOCKET_BUFFER_SIZE);

		HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
		// HttpProtocolParams.setUserAgent(httpParams,
		// String.format("android-async-http/%s (http://loopj.com/android-async-http)",
		// VERSION));
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		// schemeRegistry.register(new Scheme("https", customSSLSocketFactory !=
		// null ? customSSLSocketFactory : SSLSocketFactory.getSocketFactory(),
		// 443));
		ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
		httpClient = new DefaultHttpClient(cm, httpParams);

		httpClient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
		/*
		 * if(Proxy.getAddress()!=null){//网宿代码，设置代理 HttpHost proxyHost = new
		 * HttpHost(Proxy.getAddress().getHost(),Proxy.getAddress().getPort());
		 * httpClient
		 * .getParams().setParameter(ConnRouteParams.DEFAULT_PROXY,proxyHost); }
		 */

		init();

		httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
			public void process(HttpRequest request, HttpContext context) {
				if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
					request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
				}
				if (!request.containsHeader(USER_AGENT)) {
					if (null != USER_AGENT && !"".equals(USER_AGENT)) {
						request.addHeader("User-Agent", USER_AGENT);
					}
				}

				// for (String header : clientHeaderMap.keySet()) {
				// request.addHeader(header, clientHeaderMap.get(header));
				// }
			}
		});

		httpClient.addResponseInterceptor(new HttpResponseInterceptor() {
			public void process(HttpResponse response, HttpContext context) {
				final HttpEntity entity = response.getEntity();
				if (entity == null) {
					return;
				}
				final Header encoding = entity.getContentEncoding();
				if (encoding != null) {
					for (HeaderElement element : encoding.getElements()) {
						if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
							response.setEntity(new InflatingEntity(response.getEntity()));
							break;
						}
					}
				}
			}

		});

		httpClient.setHttpRequestRetryHandler(new RetryHandler(DEFAULT_MAX_RETRIES));

		/**
		 * 创建一个可根据需要创建新线程的线程池，但是在以前构造的线程可用时将重用它们。
		 * 对于执行很多短期异步任务的程序而言，这些线程池通常可提高程序性能。调用 execute
		 * 将重用以前构造的线程（如果线程可用）。如果现有线程没有可用的，则创建一个新线程 并添加到池中。终止并从缓存中移除那些已有 60
		 * 秒钟未被使用的线程。因此，长时间保 持空闲的线程池不会使用任何资源。
		 */
		// threadPool = (ThreadPoolExecutor)Executors.newCachedThreadPool();

		/**
		 * 创建一个可重用固定线程数的线程池，以共享的无界队列方式来运行这些线程。在任意点， 在大多数 nThreads
		 * 线程会处于处理任务的活动状态。如果在所有线程处于活动状态时提
		 * 交附加任务，则在有可用线程之前，附加任务将在队列中等待。如果在关闭前的执行期间
		 * 由于失败而导致任何线程终止，那么一个新线程将代替它执行后续的任务（如果需要）。 在某个线程被显式地关闭之前，池中的线程将一直存在。
		 */
		// threadPool = (ThreadPoolExecutor)Executors.newFixedThreadPool(3);

		// requestMap = new WeakHashMap<Context,
		// List<WeakReference<Future<?>>>>();
	}

	/**
	 * 初始化自己的SSLSocketFactory用来连接https服务器
	 * 
	 * @return
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 */
	private CustomSSLSocketFactory initCustomSSLSocketFactory() throws KeyStoreException, KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException {

		KeyStore keyStore = null;
		try {
			InputStream ins = context.getAssets().open("app_pay.cer"); // 下载的证书放到项目中的assets目录中
			if (ins != null) {
				CertificateFactory cerFactory = CertificateFactory.getInstance("X.509");
				Certificate cer = cerFactory.generateCertificate(ins);
				keyStore = KeyStore.getInstance("PKCS12", "BC");
				keyStore.load(null, null);
				keyStore.setCertificateEntry("trust", cer);
			} else {
				keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
				keyStore.load(null, null);
			}
			CustomSSLSocketFactory customSSLSocketFactory = new CustomSSLSocketFactory(keyStore);
			customSSLSocketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			return customSSLSocketFactory;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get the underlying HttpClient instance. This is useful for setting
	 * additional fine-grained settings for requests by accessing the client's
	 * ConnectionManager, HttpParams and SchemeRegistry.
	 */
	public HttpClient getHttpClient() {
		return httpClient;
	}

	/**
	 * Get the underlying HttpContext instance. This is useful for getting and
	 * setting fine-grained settings for requests by accessing the context's
	 * attributes such as the CookieStore.
	 */
	public HttpContext getHttpContext() {
		return httpContext;
	}

	/**
	 * Sets an optional CookieStore to use when making requests
	 * 
	 * @param cookieStore
	 *            The CookieStore implementation to use, usually an instance of
	 *            {@link PersistentCookieStore}
	 */
	public void setCookieStore(CookieStore cookieStore) {
		httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
	}

	/**
	 * 删除Cookie
	 */
	/*
	 * public void removeCookie(){
	 * httpContext.removeAttribute(ClientContext.COOKIE_STORE); }
	 */

	/**
	 * Overrides the threadpool implementation used when queuing/pooling
	 * requests. By default, Executors.newCachedThreadPool() is used.
	 * 
	 * @param threadPool
	 *            an instance of {@link ThreadPoolExecutor} to use for
	 *            queuing/pooling requests.
	 */
	// public void setThreadPool(ThreadPoolExecutor threadPool) {
	// this.threadPool = threadPool;
	// }

	/**
	 * Sets the User-Agent header to be sent with each request. By default,
	 * "Android Asynchronous Http Client/VERSION (http://loopj.com/android-async-http/)"
	 * is used.
	 * 
	 * @param userAgent
	 *            the string to use in the User-Agent header.
	 */
	public void setUserAgent(String userAgent) {
		HttpProtocolParams.setUserAgent(httpClient.getParams(), userAgent);
	}

	/**
	 * Sets the connection time oout. By default, 10 seconds
	 * 
	 * @param timeout
	 *            the connect/socket timeout in milliseconds
	 */
	public void setTimeout(int timeout) {
		final HttpParams httpParams = httpClient.getParams();
		ConnManagerParams.setTimeout(httpParams, timeout);
		HttpConnectionParams.setSoTimeout(httpParams, timeout);
		HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
	}

	/**
	 * Sets the SSLSocketFactory to user when making requests. By default, a
	 * new, default SSLSocketFactory is used.
	 * 
	 * @param sslSocketFactory
	 *            the socket factory to use for https requests.
	 */
	public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
		httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", sslSocketFactory, 443));
	}

	/**
	 * Sets headers that will be added to all requests this client makes (before
	 * sending).
	 * 
	 * @param header
	 *            the name of the header
	 * @param value
	 *            the contents of the header
	 */
	// public void addHeader(String header, String value) {
	// clientHeaderMap.put(header, value);
	// }

	/*
	 * public void removeHeader(){ clientHeaderMap.clear(); }
	 */

	/**
	 * Sets basic authentication for the request. Uses AuthScope.ANY. This is
	 * the same as setBasicAuth('username','password',AuthScope.ANY)
	 * 
	 * @param username
	 * @param password
	 */
	public void setBasicAuth(String user, String pass) {
		AuthScope scope = AuthScope.ANY;
		setBasicAuth(user, pass, scope);
	}

	/**
	 * Sets basic authentication for the request. You should pass in your
	 * AuthScope for security. It should be like this
	 * setBasicAuth("username","password", new
	 * AuthScope("host",port,AuthScope.ANY_REALM))
	 * 
	 * @param username
	 * @param password
	 * @param scope
	 *            - an AuthScope object
	 * 
	 */
	public void setBasicAuth(String user, String pass, AuthScope scope) {
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user, pass);
		httpClient.getCredentialsProvider().setCredentials(scope, credentials);
	}

	/**
	 * 移除所有的凭据
	 */
	/*
	 * public void removeAllCredentials(){
	 * this.httpClient.getCredentialsProvider().clear(); }
	 */

	/**
	 * Cancels any pending (or potentially active) requests associated with the
	 * passed Context.
	 * <p>
	 * <b>Note:</b> This will only affect requests which were created with a
	 * non-null android Context. This method is intended to be used in the
	 * onDestroy method of your android activities to destroy all requests which
	 * are no longer required.
	 * 
	 * @param context
	 *            the android Context instance associated to the request.
	 * @param mayInterruptIfRunning
	 *            specifies if active requests should be cancelled along with
	 *            pending requests.
	 */
	public void cancelRequests(Context context, boolean mayInterruptIfRunning) {
		List<WeakReference<Future<?>>> requestList = requestMap.get(context);
		if (requestList != null) {
			for (WeakReference<Future<?>> requestRef : requestList) {
				Future<?> request = requestRef.get();
				if (request != null) {
					request.cancel(mayInterruptIfRunning);
				}
			}
		}
		requestMap.remove(context);
	}

	/**
	 * Perform a HTTP GET request, without any parameters.
	 * 
	 * @param url
	 *            the URL to send the request to.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void get(String url, CacheParams cacheParams, AsyncHttpResponseHandler responseHandler) {
		get(null, url, null, cacheParams, responseHandler);
	}

	/**
	 * 数据库缓存读取模式
	 * 
	 * @param url
	 * @param cacheParams
	 * @param responseHandler
	 * @param isGetDataBase
	 */
	public void getByDateBase(String url, CacheParams cacheParams, AsyncHttpResponseHandler responseHandler, boolean isGetDataBase) {
		getByDateBase(null, url, null, cacheParams, responseHandler, isGetDataBase);
	}

	/**
	 * Perform a HTTP GET request with parameters.
	 * 
	 * @param url
	 *            the URL to send the request to.
	 * @param params
	 *            additional GET parameters to send with the request.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void get(String url, RequestParams params, CacheParams cacheParams, AsyncHttpResponseHandler responseHandler) {
		// pcgroup 添加cacheParams
		get(null, url, params, cacheParams, responseHandler);
	}

	/**
	 * Perform a HTTP GET request without any parameters and track the Android
	 * Context which initiated the request.
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void get(Context context, String url, CacheParams cacheParams, AsyncHttpResponseHandler responseHandler) {
		// pcgroup 添加cacheParams
		get(context, url, null, cacheParams, responseHandler);
	}

	/**
	 * Perform a HTTP GET request and track the Android Context which initiated
	 * the request.
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param params
	 *            additional GET parameters to send with the request.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void get(Context context, String url, RequestParams params, CacheParams cacheParams, AsyncHttpResponseHandler responseHandler) {
		HttpUriRequest request = null;
		if (null == url || "".equals(url))
			return;
		try {
			request = new HttpGet(getUrlWithQueryString(url, params));
		} catch (IllegalArgumentException e) {
			responseHandler.sendFailureMessage(context, e, "uri is invalid");
			e.printStackTrace();
			return;
		}
		setTimeout(socketTimeout);
		sendRequest(httpClient, httpContext, request, null, cacheParams, responseHandler, context);
	}

	/**
	 * 加缓存的post
	 * 
	 * @param context
	 * @param url
	 * @param params
	 * @param cacheParams
	 * @param responseHandler
	 */
	public void post(Context context, String url, RequestParams params, CacheParams cacheParams, AsyncHttpResponseHandler responseHandler) {
		HttpPost request = null;
		if (null == url || "".equals(url))
			return;
		try {
			request = new HttpPost(url);
		} catch (IllegalArgumentException e) {
			responseHandler.sendFailureMessage(context, e, "uri is invalid");
			e.printStackTrace();
			return;
		}
		setTimeout(socketTimeout);
		sendRequest(httpClient, httpContext, request, null, cacheParams, responseHandler, context);
	}

	/**
	 * 未联网数据库取缓存
	 * 
	 * @param context
	 * @param url
	 * @param params
	 * @param cacheParams
	 * @param responseHandler
	 * @param isGetDataBase
	 */
	public void getByDateBase(Context context, String url, RequestParams params, CacheParams cacheParams, AsyncHttpResponseHandler responseHandler, boolean isGetDataBase) {
		HttpUriRequest request = null;
		if (null == url || "".equals(url))
			return;
		try {
			request = new HttpGet(getUrlWithQueryString(url, params));
		} catch (IllegalArgumentException e) {
			responseHandler.sendFailureMessage(context, e, "uri is invalid");
			e.printStackTrace();
			return;
		}
		setTimeout(socketTimeout);
		sendRequestByDateBase(httpClient, httpContext, request, null, cacheParams, responseHandler, context, isGetDataBase);
	}

	/**
	 * Perform a HTTP GET request and track the Android Context which initiated
	 * the request with customized headers
	 * 
	 * @param url
	 *            the URL to send the request to.
	 * @param headers
	 *            set headers only for this request
	 * @param params
	 *            additional GET parameters to send with the request.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void get(Context context, String url, Map<String, String> clientHeaderMap, RequestParams params, CacheParams cacheParams, AsyncHttpResponseHandler responseHandler) {
		HttpUriRequest request = null;
		if (null == url || "".equals(url))
			return;
		try {
			request = new HttpGet(getUrlWithQueryString(url, params));
		} catch (IllegalArgumentException e) {
			responseHandler.sendFailureMessage(context, e, "uri is invalid");
			e.printStackTrace();
			return;
		}

		if (clientHeaderMap != null && clientHeaderMap.size() > 0) {
			for (String header : clientHeaderMap.keySet()) {
				request.addHeader(header, clientHeaderMap.get(header));
			}
		}

		setTimeout(socketTimeout);
		// if(headers != null) request.setHeaders(headers);

		sendRequest(httpClient, httpContext, request, null, cacheParams, responseHandler, context);
	}

	//
	// HTTP POST Requests
	//

	/**
	 * Perform a HTTP POST request, without any parameters.
	 * 
	 * @param url
	 *            the URL to send the request to.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void post(String url, AsyncHttpResponseHandler responseHandler) {
		post(null, url, null, responseHandler);
	}

	/**
	 * Perform a HTTP POST request with parameters.
	 * 
	 * @param url
	 *            the URL to send the request to.
	 * @param params
	 *            additional POST parameters or files to send with the request.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		post(null, url, params, responseHandler);
	}

	/**
	 * Perform a HTTP POST request and track the Android Context which initiated
	 * the request.
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param params
	 *            additional POST parameters or files to send with the request.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void post(Context context, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		post(context, url, paramsToEntity(params), null, responseHandler);
	}

	/**
	 * 通过Json作为实体请求的缓存post
	 * 
	 * @param context
	 * @param url
	 * @param cacheParams
	 * @param params
	 * @param responseHandler
	 * @throws UnsupportedEncodingException
	 */
	public void postToJsonParam(Context context, String url, CacheParams cacheParams, String jsonString, String requestParam, AsyncHttpResponseHandler responseHandler) throws UnsupportedEncodingException {
		StringEntity stringEntity = new StringEntity(jsonString, "UTF-8");
		stringEntity.setContentEncoding("UTF-8");
		stringEntity.setContentType("application/json");
		postToCache(context, url, stringEntity, cacheParams, null, responseHandler, requestParam);
	}

	/**
	 * Perform a HTTP POST request and track the Android Context which initiated
	 * the request.
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param entity
	 *            a raw {@link HttpEntity} to send with the request, for
	 *            example, use this to send string/json/xml payloads to a server
	 *            by passing a {@link org.apache.http.entity.StringEntity}.
	 * @param contentType
	 *            the content type of the payload you are sending, for example
	 *            application/json if sending a json payload.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void post(Context context, String url, HttpEntity entity, String contentType, AsyncHttpResponseHandler responseHandler) {
		HttpPost httpPost;
		if (null == url || "".equals(url))
			return;
		try {
			httpPost = new HttpPost(url);
		} catch (IllegalArgumentException e) {
			responseHandler.sendFailureMessage(context, e, "uri is invalid");
			e.printStackTrace();
			return;
		}
		setTimeout(socketTimeout);

		sendRequest(httpClient, httpContext, addEntityToRequestBase(httpPost, entity), contentType, null, responseHandler, context);
	}

	/**
	 * 带缓存的post请求
	 * 
	 * @param context
	 * @param url
	 * @param entity
	 * @param cacheParams
	 * @param contentType
	 * @param responseHandler
	 * @param requestParam
	 */
	public void postToCache(Context context, String url, HttpEntity entity, CacheParams cacheParams, String contentType, AsyncHttpResponseHandler responseHandler, String requestParam) {
		HttpPost httpPost;
		if (null == url || "".equals(url))
			return;
		try {
			httpPost = new HttpPost(url);
		} catch (IllegalArgumentException e) {
			responseHandler.sendFailureMessage(context, e, "uri is invalid");
			e.printStackTrace();
			return;
		}
		setTimeout(socketTimeout);

		try {
			sendRequestPost(httpClient, httpContext, addEntityToRequestBase(httpPost, entity), contentType, cacheParams, responseHandler, context, requestParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Perform a HTTP POST request and track the Android Context which initiated
	 * the request. Set headers only for this request
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param headers
	 *            set headers only for this request
	 * @param params
	 *            additional POST parameters to send with the request.
	 * @param contentType
	 *            the content type of the payload you are sending, for example
	 *            application/json if sending a json payload.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void post(Context context, String url, Map<String, String> clientHeaderMap, RequestParams params, String contentType, AsyncHttpResponseHandler responseHandler) {
		HttpEntityEnclosingRequestBase request;

		if (null == url || "".equals(url))
			return;
		try {
			request = new HttpPost(url);
		} catch (IllegalArgumentException e) {
			responseHandler.sendFailureMessage(context, e, "uri is invalid");
			e.printStackTrace();
			return;
		}
		if (params != null)
			request.setEntity(paramsToEntity(params));

		if (clientHeaderMap != null && clientHeaderMap.size() > 0) {
			for (String header : clientHeaderMap.keySet()) {
				request.addHeader(header, clientHeaderMap.get(header));
			}
		}
		setTimeout(socketTimeout);
		sendRequest(httpClient, httpContext, request, contentType, null, responseHandler, context);
	}

	public void post(String url, Map<String, String> clientHeaderMap, RequestParams params, String contentType, AsyncHttpResponseHandler responseHandler) {
		HttpEntityEnclosingRequestBase request;
		if (null == url || "".equals(url))
			return;
		try {
			request = new HttpPost(url);
		} catch (IllegalArgumentException e) {
			responseHandler.sendFailureMessage(null, e, "uri is invalid");
			e.printStackTrace();
			return;
		}

		if (params != null)
			request.setEntity(paramsToEntity(params));

		if (clientHeaderMap != null && clientHeaderMap.size() > 0) {
			for (String header : clientHeaderMap.keySet()) {
				request.addHeader(header, clientHeaderMap.get(header));
			}
		}
		setTimeout(socketTimeout);
		// if(headers != null) request.setHeaders(headers);

		sendRequest(httpClient, httpContext, request, contentType, null, responseHandler, null);
	}

	/**
	 * Perform a HTTP POST request and track the Android Context which initiated
	 * the request. Set headers only for this request
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param headers
	 *            set headers only for this request
	 * @param entity
	 *            a raw {@link HttpEntity} to send with the request, for
	 *            example, use this to send string/json/xml payloads to a server
	 *            by passing a {@link org.apache.http.entity.StringEntity}.
	 * @param contentType
	 *            the content type of the payload you are sending, for example
	 *            application/json if sending a json payload.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void post(Context context, String url, Header[] headers, HttpEntity entity, String contentType, AsyncHttpResponseHandler responseHandler) {
		HttpEntityEnclosingRequestBase req;
		if (null == url || "".equals(url))
			return;
		try {
			req = new HttpPost(url);
		} catch (IllegalArgumentException e) {
			responseHandler.sendFailureMessage(null, e, "uri is invalid");
			e.printStackTrace();
			return;
		}

		if (null == url || "".equals(url))
			return;
		HttpEntityEnclosingRequestBase request = addEntityToRequestBase(req, entity);
		if (headers != null)
			request.setHeaders(headers);
		setTimeout(socketTimeout);

		sendRequest(httpClient, httpContext, request, contentType, null, responseHandler, context);
	}

	//
	// HTTP PUT Requests
	//
	/**
	 * Perform a HTTP PUT request, without any parameters.
	 * 
	 * @param url
	 *            the URL to send the request to.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void put(String url, AsyncHttpResponseHandler responseHandler) {
		put(null, url, null, responseHandler);
	}

	/**
	 * Perform a HTTP PUT request with parameters.
	 * 
	 * @param url
	 *            the URL to send the request to.
	 * @param params
	 *            additional PUT parameters or files to send with the request.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void put(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		put(null, url, params, responseHandler);
	}

	/**
	 * Perform a HTTP PUT request and track the Android Context which initiated
	 * the request.
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param params
	 *            additional PUT parameters or files to send with the request.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void put(Context context, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		put(context, url, paramsToEntity(params), null, responseHandler);
	}

	/**
	 * Perform a HTTP PUT request and track the Android Context which initiated
	 * the request. And set one-time headers for the request
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param entity
	 *            a raw {@link HttpEntity} to send with the request, for
	 *            example, use this to send string/json/xml payloads to a server
	 *            by passing a {@link org.apache.http.entity.StringEntity}.
	 * @param contentType
	 *            the content type of the payload you are sending, for example
	 *            application/json if sending a json payload.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void put(Context context, String url, HttpEntity entity, String contentType, AsyncHttpResponseHandler responseHandler) {

		sendRequest(httpClient, httpContext, addEntityToRequestBase(new HttpPut(url), entity), contentType, null, responseHandler, context);
	}

	/**
	 * Perform a HTTP PUT request and track the Android Context which initiated
	 * the request. And set one-time headers for the request
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param headers
	 *            set one-time headers for this request
	 * @param entity
	 *            a raw {@link HttpEntity} to send with the request, for
	 *            example, use this to send string/json/xml payloads to a server
	 *            by passing a {@link org.apache.http.entity.StringEntity}.
	 * @param contentType
	 *            the content type of the payload you are sending, for example
	 *            application/json if sending a json payload.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void put(Context context, String url, Header[] headers, HttpEntity entity, String contentType, AsyncHttpResponseHandler responseHandler) {
		HttpEntityEnclosingRequestBase request = addEntityToRequestBase(new HttpPut(url), entity);
		if (headers != null)
			request.setHeaders(headers);

		sendRequest(httpClient, httpContext, request, contentType, null, responseHandler, context);
	}

	//
	// HTTP DELETE Requests
	//

	/**
	 * Perform a HTTP DELETE request.
	 * 
	 * @param url
	 *            the URL to send the request to.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void delete(String url, AsyncHttpResponseHandler responseHandler) {
		delete(null, url, responseHandler);
	}

	/**
	 * Perform a HTTP DELETE request.
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void delete(Context context, String url, AsyncHttpResponseHandler responseHandler) {
		final HttpDelete delete = new HttpDelete(url);

		sendRequest(httpClient, httpContext, delete, null, null, responseHandler, context);
	}

	/**
	 * Perform a HTTP DELETE request.
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param headers
	 *            set one-time headers for this request
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void delete(Context context, String url, Header[] headers, AsyncHttpResponseHandler responseHandler) {
		final HttpDelete delete = new HttpDelete(url);
		if (headers != null)
			delete.setHeaders(headers);

		sendRequest(httpClient, httpContext, delete, null, null, responseHandler, context);
	}

	// pcgroup 添加cacheParams
	// Private stuff
	protected void sendRequest(DefaultHttpClient client, HttpContext httpContext, HttpUriRequest uriRequest, String contentType, CacheParams cacheParams, AsyncHttpResponseHandler responseHandler, Context context) {
		if (contentType != null) {
			uriRequest.addHeader("Content-Type", contentType);
		}

		Future<?> request = threadPool.submit(new AsyncHttpRequest(client, httpContext, uriRequest, cacheParams, responseHandler, context));
		if (context != null) {
			// Add request to request map
			List<WeakReference<Future<?>>> requestList = requestMap.get(context);
			if (requestList == null) {
				requestList = new LinkedList<WeakReference<Future<?>>>();
				requestMap.put(context, requestList);
			}

			requestList.add(new WeakReference<Future<?>>(request));

			// TODO: Remove dead weakrefs from requestLists?
		}
	}

	/**
	 * 带缓存post请求
	 * 
	 * @param client
	 * @param httpContext
	 * @param uriRequest
	 * @param contentType
	 * @param cacheParams
	 * @param responseHandler
	 * @param context
	 * @param requestParam
	 */
	protected void sendRequestPost(DefaultHttpClient client, HttpContext httpContext, HttpUriRequest uriRequest, String contentType, CacheParams cacheParams, AsyncHttpResponseHandler responseHandler, Context context, String requestParam) {
		if (contentType != null) {
			uriRequest.addHeader("Content-Type", contentType);
		}

		Future<?> request = threadPool.submit(new AsyncHttpRequest(client, httpContext, uriRequest, cacheParams, responseHandler, context, requestParam));
		if (context != null) {
			// Add request to request map
			List<WeakReference<Future<?>>> requestList = requestMap.get(context);
			if (requestList == null) {
				requestList = new LinkedList<WeakReference<Future<?>>>();
				requestMap.put(context, requestList);
			}

			requestList.add(new WeakReference<Future<?>>(request));

			// TODO: Remove dead weakrefs from requestLists?
		}
	}

	/**
	 * 发送以数据库为离线缓存的请求
	 * 
	 * @param client
	 * @param httpContext
	 * @param uriRequest
	 * @param contentType
	 * @param cacheParams
	 * @param responseHandler
	 * @param context
	 * @param isGetDataBase
	 */
	protected void sendRequestByDateBase(DefaultHttpClient client, HttpContext httpContext, HttpUriRequest uriRequest, String contentType, CacheParams cacheParams, AsyncHttpResponseHandler responseHandler, Context context, boolean isGetDataBase) {
		if (contentType != null) {
			uriRequest.addHeader("Content-Type", contentType);
		}

		Future<?> request = threadPool.submit(new AsyncHttpRequest(client, httpContext, uriRequest, cacheParams, responseHandler, context, isGetDataBase));
		if (context != null) {
			// Add request to request map
			List<WeakReference<Future<?>>> requestList = requestMap.get(context);
			if (requestList == null) {
				requestList = new LinkedList<WeakReference<Future<?>>>();
				requestMap.put(context, requestList);
			}

			requestList.add(new WeakReference<Future<?>>(request));

		}
	}

	public static String getUrlWithQueryString(String url, RequestParams params) {
		if (params != null) {
			String paramString = params.getParamString();
			if (url.indexOf("?") == -1) {
				url += "?" + paramString;
			} else {
				url += "&" + paramString;
			}
		}

		return url;
	}

	private HttpEntity paramsToEntity(RequestParams params) {
		HttpEntity entity = null;
		if (params != null) {
			entity = params.getEntity();
		}
		return entity;
	}

	private HttpEntityEnclosingRequestBase addEntityToRequestBase(HttpEntityEnclosingRequestBase requestBase, HttpEntity entity) {
		if (entity != null) {
			requestBase.setEntity(entity);
		}
		return requestBase;
	}

	private static class InflatingEntity extends HttpEntityWrapper {
		public InflatingEntity(HttpEntity wrapped) {
			super(wrapped);
		}

		@Override
		public InputStream getContent() throws IOException {
			return new GZIPInputStream(wrappedEntity.getContent());
		}

		@Override
		public long getContentLength() {
			return -1;
		}
	}
}
