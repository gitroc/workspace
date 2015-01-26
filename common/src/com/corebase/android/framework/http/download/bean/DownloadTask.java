package com.corebase.android.framework.http.download.bean;

import java.io.File;
/**
 * @author tony
 *	下载器任务类
 */
public class DownloadTask {
	
	private int taskState;					//任务状态
	public static int TASK_NULL = 0;		//不存在状态
	public static int TASK_WAIT = 1;		//等待状态
	public static int TASK_RUNNING = 2;		//运行状态
	public static int TASK_PAUSE = 3;		//暂停状态
	public static int TASK_EXCEPTION = 4;	//异常终止状态
	public static int TASK_CANCEL = 5;		//取消状态
	public static int TASK_OVER = 6;		//结束状态
	
	private String id;					//任务ID
	private String url;					//任务URL
	private long fileTotalSize;			//任务下载的文件大小,单位为"字节"
	private long fileDownSize;			//任务已下载的文件大小,单位为"字节"
	private File   filePath;			//任务下载的文件存放路径
	
	public DownloadTask(String url,File filePath){
		this.url = url;
		this.filePath = filePath;
		this.taskState = TASK_WAIT;
	}

	public int getTaskState() {
		return taskState;
	}

	public void setTaskState(int taskState) {
		this.taskState = taskState;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public long getFileTotalSize() {
		return fileTotalSize;
	}

	public void setFileTotalSize(long fileTotalSize) {
		this.fileTotalSize = fileTotalSize;
	}

	public long getFileDownSize() {
		return fileDownSize;
	}

	public void setFileDownSize(long fileDownSize) {
		this.fileDownSize = fileDownSize;
	}

	public File getFilePath() {
		return filePath;
	}

	public void setFilePath(File filePath) {
		this.filePath = filePath;
	}
}
