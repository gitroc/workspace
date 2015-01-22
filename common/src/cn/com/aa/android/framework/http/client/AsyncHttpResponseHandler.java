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

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LogWriter;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;

import cn.com.aa.common.android.utils.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Used to intercept and handle the responses from requests made using
 * {@link AsyncHttpClient}. The {@link #onSuccess(String)} method is designed to
 * be anonymously overridden with your own response handling code.
 * <p>
 * Additionally, you can override the {@link #onFailure(Throwable, String)},
 * {@link #onStart()}, and {@link #onFinish()} methods as required.
 * <p>
 * For example:
 * <p>
 * 
 * <pre>
 * AsyncHttpClient client = new AsyncHttpClient();
 * client.get(&quot;http://www.google.com&quot;, new AsyncHttpResponseHandler() {
 * 	&#064;Override
 * 	public void onStart() {
 * 		// Initiated the request
 * 	}
 * 
 * 	&#064;Override
 * 	public void onSuccess(String response) {
 * 		// Successfully got a response
 * 	}
 * 
 * 	&#064;Override
 * 	public void onFailure(Throwable e, String response) {
 * 		// Response failed :(
 * 	}
 * 
 * 	&#064;Override
 * 	public void onFinish() {
 * 		// Completed the request (either success or failure)
 * 	}
 * });
 * </pre>
 */
public class AsyncHttpResponseHandler {
	// 设置下载数据的阀值 最大为1M
	private final int MAX = 1024 * 1024;
	// private Context context;

	protected final int SUCCESS_STATUS_CODE = 200;
	protected static final int SUCCESS_MESSAGE = 0;
	protected static final int FAILURE_MESSAGE = 1;
	protected static final int START_MESSAGE = 2;
	protected static final int FINISH_MESSAGE = 3;

	private Handler handler;

	// public Context getContext() {
	// return context;
	// }
	//
	//
	// public void setContext(Context context) {
	// this.context = context;
	// }

	/**
	 * Creates a new AsyncHttpResponseHandler
	 */
	public AsyncHttpResponseHandler() {
		// Set up a handler to post events back to the correct thread if
		// possible
		if (Looper.myLooper() != null) {
			handler = new Handler() {
				public void handleMessage(Message msg) {
					AsyncHttpResponseHandler.this.handleMessage(msg);
				}
			};
		}
	}

	//
	// Callbacks to be overridden, typically anonymously
	//

	/**
	 * Fired when the request is started, override to handle in your own code
	 */
	public void onStart() {
	}

	/**
	 * Fired in all cases when the request is finished, after both success and
	 * failure, override to handle in your own code
	 */
	public void onFinish() {
	}

	/**
	 * Fired when a request returns successfully, override to handle in your own
	 * code
	 * 
	 * @param content
	 *            the body of the HTTP response from the server
	 */
	public void onSuccess(String content) {
	}

	/**
	 * Fired when a request returns successfully, override to handle in your own
	 * code
	 * 
	 * @param statusCode
	 *            the status code of the response
	 * @param content
	 *            the body of the HTTP response from the server
	 */
	public void onSuccess(int statusCode, String content) {
		onSuccess(content);
	}

	/**
	 * Fired when a request fails to complete, override to handle in your own
	 * code
	 * 
	 * @param error
	 *            the underlying cause of the failure
	 * @deprecated use {@link #onFailure(Throwable, String)}
	 */
	public void onFailure(Throwable error) {
	}

	/**
	 * Fired when a request fails to complete, override to handle in your own
	 * code
	 * 
	 * @param error
	 *            the underlying cause of the failure
	 * @param content
	 *            the response body, if any
	 */
	public void onFailure(Throwable error, String content) {
		// By default, call the deprecated onFailure(Throwable) for
		// compatibility
		onFailure(error);
	}

	//
	// Pre-processing of messages (executes in background threadpool thread)
	//

	protected void sendSuccessMessage(Context context, int statusCode, String responseBody) {
		// this.context = context;
		sendMessage(obtainMessage(SUCCESS_MESSAGE, new Object[] { new Integer(statusCode), responseBody }));
	}

	protected void sendFailureMessage(Context context, Throwable e, String responseBody) {
		// this.context = context;
		sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[] { e, responseBody }));
	}

	// protected void sendFailureMessage(Throwable e, byte[] responseBody) {
	// sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[]{e,
	// responseBody}));
	// }

	protected void sendStartMessage() {
		sendMessage(obtainMessage(START_MESSAGE, null));
	}

	protected void sendFinishMessage() {
		sendMessage(obtainMessage(FINISH_MESSAGE, null));
	}

	//
	// Pre-processing of messages (in original calling thread, typically the UI
	// thread)
	//

	protected void handleSuccessMessage(int statusCode, String responseBody) {
		onSuccess(statusCode, responseBody);
	}

	protected void handleFailureMessage(Throwable e, String responseBody) {
		onFailure(e, responseBody);
	}

	// Methods which emulate android's Handler and Message methods
	protected void handleMessage(Message msg) {
		Object[] response;
		switch (msg.what) {
		case SUCCESS_MESSAGE:
			response = (Object[]) msg.obj;
			handleSuccessMessage(((Integer) response[0]).intValue(), (String) response[1]);
			break;
		case FAILURE_MESSAGE:
			response = (Object[]) msg.obj;
			if (response[0] instanceof HttpResponseException) {
				handleFailureMessage((HttpResponseException) response[0], (String) response[1]);

			} else {
				handleFailureMessage((Throwable) response[0], (String) response[1]);
			}
			break;
		case START_MESSAGE:
			onStart();
			break;
		case FINISH_MESSAGE:
			onFinish();
			break;
		}
	}

	protected void sendMessage(Message msg) {
		if (handler != null) {
			handler.sendMessage(msg);
		} else {
			handleMessage(msg);
		}
	}

	protected Message obtainMessage(int responseMessage, Object response) {
		Message msg = null;
		if (handler != null) {
			msg = this.handler.obtainMessage(responseMessage, response);
		} else {
			msg = new Message();
			msg.what = responseMessage;
			msg.obj = response;
		}
		return msg;
	}

	// pcgroup
	void sendResponseMessage(Context context, InputStream inputstream) {
		// setContext(context);
		String data = null;
		try {
			if (null != inputstream) {
				data = FileUtils.readTextInputStream(inputstream);
				if (null != data && !"".equals(data)) {
					sendSuccessMessage(context, SUCCESS_STATUS_CODE, data);
				} else {
					sendFailureMessage(context, new NullPointerException("data is null"), (String) null);
				}
			}
		} catch (IOException e) {
			sendFailureMessage(context, e, (String) null);
			e.printStackTrace();
		} finally {
			if (null != inputstream) {
				try {
					inputstream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected byte[] getByteArrayAndSendMessage(Context context, HttpResponse response) {
		StatusLine status = response.getStatusLine();
		byte[] responseBody = null;
		try {
			HttpEntity entity = null;
			HttpEntity temp = response.getEntity();
			if (temp != null) {
				entity = new BufferedHttpEntity(temp);
				// 针对pcgroup业务进行判断处理大于1M的时候返回NULL
				long length = entity.getContentLength();
				if (length >= MAX) {
					return null;
				}
			}
			responseBody = EntityUtils.toByteArray(entity);

		} catch (IOException e) {
			sendFailureMessage(context, e, (String) null);
			return null;
		}

		Log.v("Http StatusCode", status.getStatusCode() + "");
		if (status.getStatusCode() >= 300) {
			sendFailureMessage(context, new HttpResponseException(status.getStatusCode(), status.getReasonPhrase()), (String) null);
			return null;
		}
		if (null == responseBody) {
			// 如果输入流为空发送空消息
			sendSuccessMessage(context, SUCCESS_STATUS_CODE, null);
			return null;
		}
		return responseBody;
	}
}