package com.corebase.android.framework.http.download;

import com.corebase.android.framework.http.download.bean.DownloadTask;
import android.os.Handler;
import android.os.Message;

/**
 * @author tony
 *	多任务多线程下载监听器
 */
public abstract class MultiDownLoaderListener extends Handler{
	
	public static final int WAIT_MESSAGE = 0;
	public static final int PAUSE_MESSAGE = 1;
	public static final int BEGIN_MESSAGE = 2;
	public static final int RUNNING_MESSAGE = 3;
	public static final int SUCCESS_MESSAGE = 4;
	public static final int CANCEL_MESSAGE = 5;
	public static final int FAIL_MESSAGE = 6;
	
	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case WAIT_MESSAGE:
			onWait();
			break;
		case PAUSE_MESSAGE:
			onPause();
			break;
		case BEGIN_MESSAGE:
			onBegin(((DownloadTask)msg.obj).getFileTotalSize());
			break;
		case RUNNING_MESSAGE:
			onRunning(((DownloadTask)msg.obj).getFileDownSize());
			break;
		case SUCCESS_MESSAGE:
			onSuccessed();
			break;
		case CANCEL_MESSAGE:
			onCancel();
			break;
		case FAIL_MESSAGE:
			onFail();
			break;
		default:
			break;
		}
	}
	
	/**
	 * 任务等待时回调方法
	 */
	public void onWait(){
		
	};
	
	/**
	 * 任务暂停时回调方法
	 */
	public void onPause(){
		
	};
	
	/**
	 * 任务开始执行时回调方法
	 */
	public void onBegin(long totalBytes){
		
	};
	
	/**
	 * 任务开始执行时回调方法
	 * @param totalBytes  任务的总字节数
	 */
	public void onTotalBytes(int totalBytes){
		
	};
	
	/**
	 * 任务正在处理时回调方法
	 * @param byteNum	任务处理进度字节数
	 */
	public void onRunning(long byteNum){
		
	};
	
	/**
	 * 任务成功运行结束时回调方法
	 */
	public void onSuccessed(){
		
	};
	
	/**
	 * 任务成功运行结束时回调方法
	 * @param filePath	下载的文件存放路径
	 */
	public void onSuccessed(String filePath){
		
	};
	
	/**
	 * 任务被取消或者删除时回调该方法
	 */
	public void onCancel(){
		
	};
	
	/**
	 * 任务异常终止时回调方法
	 */
	public void onFail(){
		
	};

}
