package com.corebase.android.framework.http.download;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
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

import com.corebase.android.framework.http.download.bean.DownloadTask;

import android.content.Context;
import android.os.Message;

/**
 * @author tony
 *	多任务下载器
 *	
 *	注意:当下载器退出时,其中所有未完成的任务状态将默认重置为暂停状态.
 */
public class MultiDownLoader implements IMultiDownLoader {

	private DefaultHttpClient httpClient;				//http客户端
	private final HttpContext httpContext;
	private TaskLogService taskLogService;				//下载器任务LOG日志
	private ExecutorService taskPool;					//下载器任务池
	private int taskNum = 10;							//下载器一次最多并行处理的任务数
	private Map listenerList;							//存活任务的监听器
	
	private FiledeleteDevice filedeleteDevice;			//文件删除器
	
	private static final String VERSION = "1.0.0";
	private static final int DEFAULT_MAX_CONNECTIONS = 10;
    private static final int DEFAULT_SOCKET_TIMEOUT = 10 * 1000;
    private static final int DEFAULT_MAX_RETRIES = 5;
    private static final int DEFAULT_SOCKET_BUFFER_SIZE = 8192;
    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String ENCODING_GZIP = "gzip";

    private static int maxConnections = DEFAULT_MAX_CONNECTIONS;
    private static int socketTimeout = DEFAULT_SOCKET_TIMEOUT;
    
	/**
	 * 创建一个最大并行处理任务数为taskNum的下载器
	 * @param taskNum
	 */
	public MultiDownLoader(Context context,int taskNum){
		this.taskNum = taskNum;
		taskPool  = Executors.newFixedThreadPool(taskNum);
		taskLogService = new TaskLogService(context);
		listenerList = new HashMap();
		
		//创建一个文件删除器,负责下载任务对应的本地文件删除工作.
		filedeleteDevice = new FiledeleteDevice();
		filedeleteDevice.start();
		
		//创建一个下载器时,应先初始化该下载器里所有状态不为NULL的任务状态为PAUSE状态
		taskLogService.initAllTaskState();
		
		BasicHttpParams httpParams = new BasicHttpParams();

        ConnManagerParams.setTimeout(httpParams, socketTimeout);
        ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(maxConnections));
        ConnManagerParams.setMaxTotalConnections(httpParams, DEFAULT_MAX_CONNECTIONS);
        
        HttpConnectionParams.setSoTimeout(httpParams, socketTimeout);
        HttpConnectionParams.setConnectionTimeout(httpParams, socketTimeout);
        HttpConnectionParams.setTcpNoDelay(httpParams, true);
        HttpConnectionParams.setSocketBufferSize(httpParams, DEFAULT_SOCKET_BUFFER_SIZE);

        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setUserAgent(httpParams, String.format("android-async-http/%s (http://loopj.com/android-async-http)", VERSION));

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(httpParams, schemeRegistry);

        httpContext = new SyncBasicHttpContext(new BasicHttpContext());
        httpClient = new DefaultHttpClient(cm, httpParams);
	}
	
	public void addTask(DownloadTask downloadTask,MultiDownLoaderListener multiDownLoaderListener) {
		
		//不能重复添加已经处于RUNNING状态的任务
		if(getTaskState(downloadTask) == DownloadTask.TASK_RUNNING){
			return;
		}
		
		taskPool.submit(new TaskThread(downloadTask,taskLogService,multiDownLoaderListener));
		listenerList.put(downloadTask, multiDownLoaderListener);
	}
	
	/**
	 * @author tony
	 *	下载任务线程
	 */
	private class TaskThread implements Runnable{
		
		private MultiDownLoaderListener multiDownLoaderListener;
		private DownloadTask downloadTask;
		private TaskLogService taskLogService;
		private HttpGet httpGet;
		
		public TaskThread(DownloadTask downLoadTask,TaskLogService taskLogService,MultiDownLoaderListener multiDownLoaderListener) {
			
			this.multiDownLoaderListener = multiDownLoaderListener;
			this.downloadTask = downLoadTask;
			this.taskLogService = taskLogService;
			
			
			//任务创建完成,处于"WAIT"状态
			updateTaskWait(this.downloadTask);
			multiDownLoaderListener.sendEmptyMessage(MultiDownLoaderListener.WAIT_MESSAGE);
			
		}
		
		
		public void run() {
			
			int startposition = 0;
			
			httpGet = new HttpGet(downloadTask.getUrl());
			
			
			
			try {
				
				HttpResponse httpResponse = httpClient.execute(httpGet);
				
				//任务准备执行,处于"BEGIN"状态
				long fileTotalSize = httpResponse.getEntity().getContentLength();
				downloadTask.setFileTotalSize(fileTotalSize);
				taskLogService.updateTotalSize(downloadTask);
				//发送任务大小消息
				Message msg1 = new Message();
				downloadTask.setFileDownSize((int)fileTotalSize);
				msg1.obj = downloadTask;
				msg1.what = MultiDownLoaderListener.BEGIN_MESSAGE;
				multiDownLoaderListener.sendMessage(msg1);
				
				//初始化工作完成后,任务处于RUNNING状态
				updateTaskRunning(downloadTask);
				
				
				//初始化本地文件,如果本地文件存在则断点续传,如果本地文件被删除则重新创建文件从头开始
				RandomAccessFile accessFile = null;
				if(isExistLocalFile(downloadTask)){
					accessFile = new RandomAccessFile(downloadTask.getFilePath(),"rwd");
					accessFile.seek(taskLogService.getTaskProgress(downloadTask));
					startposition = taskLogService.getTaskProgress(downloadTask);
					
					//由于setLength非常耗时,为了防止只提交一个任务时也会出现等待时间过长,需要在初始化文件之前发送Running状态
					Message msg = new Message();
					downloadTask.setFileDownSize(startposition);
					msg.obj = downloadTask;
					msg.what = MultiDownLoaderListener.RUNNING_MESSAGE;
					multiDownLoaderListener.sendMessage(msg);
				}else{
					accessFile = new RandomAccessFile(downloadTask.getFilePath(),"rwd");
					
					Message msg = new Message();
					downloadTask.setFileDownSize(startposition);
					msg.obj = downloadTask;
					msg.what = MultiDownLoaderListener.RUNNING_MESSAGE;
					multiDownLoaderListener.sendMessage(msg);
					
					//注意:setLength是一项非常耗时的工作...
					//accessFile.setLength(taskLogService.getTaskTotalSize(downloadTask));
					accessFile.seek(startposition);
				}
				
				
				//multiDownLoaderListener.sendEmptyMessage(MultiDownLoaderListener.PAUSE_MESSAGE);
				
				//从指定位置开始下载
				httpGet.addHeader("RANGE", "bytes=" +startposition+"-");
				httpResponse = httpClient.execute(httpGet);
				HttpEntity httpEntity = httpResponse.getEntity();
				if(httpEntity!=null){
					InputStream is = httpEntity.getContent();
					byte[] buffer = new byte[4096];
					int len = 0;
					
					while( (len=is.read(buffer)) !=-1 && downloadTask.getTaskState() == DownloadTask.TASK_RUNNING){
							accessFile.write(buffer,0,len);
							downloadTask.setFileDownSize(startposition+=len);
							taskLogService.updateTaskProgress(downloadTask);
							
							//即时发送任务进度消息
							Message msg = new Message();
							downloadTask.setFileDownSize(startposition);
							msg.obj = downloadTask;
							msg.what = MultiDownLoaderListener.RUNNING_MESSAGE;
							multiDownLoaderListener.sendMessage(msg);
					}
					//is.close();
					//accessFile.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				updateTaskException(downloadTask);
				multiDownLoaderListener.sendEmptyMessage(MultiDownLoaderListener.FAIL_MESSAGE);
			} 
			
			int taskState = downloadTask.getTaskState();
			//任务正常结束.
			if(taskState == DownloadTask.TASK_RUNNING || taskState == DownloadTask.TASK_OVER){
				updateTaskOver(downloadTask);
				multiDownLoaderListener.sendEmptyMessage(MultiDownLoaderListener.SUCCESS_MESSAGE);
			}
			//任务暂停.
			else if(taskState == DownloadTask.TASK_PAUSE){
				multiDownLoaderListener.sendEmptyMessage(MultiDownLoaderListener.PAUSE_MESSAGE);
			}
			//任务异常
			else if(taskState == DownloadTask.TASK_EXCEPTION){
				multiDownLoaderListener.sendEmptyMessage(MultiDownLoaderListener.FAIL_MESSAGE);
			}
		}
	}
	
	/**
	 * @author user
	 *	文件删除器
	 */
	private class FiledeleteDevice extends Thread{
		
		public Queue<DownloadTask> taskQueue = new LinkedList<DownloadTask>();
		private FiledeleteDevice mySelf;
		
		public FiledeleteDevice() {
			mySelf = this;
		}
		
		/**
		 * 添加一个删除文件任务
		 */
		public void addTask(DownloadTask task){
			taskQueue.add(task);
			//唤醒执行
			synchronized(mySelf){
				mySelf.notify();
			}
		}
		
		/**
		 * 开始执行任务
		 */
		public void doTask(DownloadTask task){
			deleteTaskFile1(task);
		}
		
		
		public void run() {
			while(true){
				DownloadTask lastTask = null;
				synchronized(taskQueue){
					if(!taskQueue.isEmpty()){
						//取出一个任务,且运行完成后将其移出任务队列.
						lastTask = taskQueue.poll();
						//执行任务
						doTask(lastTask);
					}
				}
				
				try {
					//等待执行.
					synchronized (this) {
						mySelf.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	/**
	 * 关闭当前线程池
	 */
	public void shutdown(){
		taskPool.shutdownNow();
	}

	/**
	 * 任务对应的本地文件是否存在.
	 * @param downloadTask
	 * @return
	 */
	private boolean isExistLocalFile(DownloadTask downloadTask){
		return downloadTask.getFilePath().exists();
	}
	
	/* 
	 * 暂停指定任务.当任务正在运行时,可以暂停该任务
	 */
	
	public boolean pauseTask(DownloadTask downloadTask) {
        int state = getTaskState(downloadTask);
		if(state != DownloadTask.TASK_RUNNING && state !=DownloadTask.TASK_WAIT){
			System.out.println("当前状态不为运行状态或等待,无法暂停执行!");
			return false;
		}
		updateTaskPause(downloadTask);
		return true;
	}

	/* 
	 * 取消指定任务.取消任务不会删除任务对应的本地文件.
	 */
	
	public void cancelTask(DownloadTask downloadTask) {
		updateTaskCancel(downloadTask);
		//由于任何状态下都能取消或者删除,所以此方法可直接返回调用该方法后任务的状态,而不需要由下载器分配状态
		MultiDownLoaderListener listener = (MultiDownLoaderListener)listenerList.get(downloadTask);
		if (listener != null) {
			listener.sendEmptyMessage(MultiDownLoaderListener.CANCEL_MESSAGE);
		}
	}
	
	public void deleteTaskFile(DownloadTask downloadTask){
		filedeleteDevice.addTask(downloadTask);
	}
	
	/* 
	 * 删除指定任务对应的本地文件.当任务已被取消或者删除时,可以删除其对应的本地文件.
	 * 此方法应该通过文件删除器来进行删除.
	 */
	private boolean deleteTaskFile1(DownloadTask downloadTask){
		//不能删除状态为RUNNING的任务对应的本地文件
		if(downloadTask.getTaskState() != DownloadTask.TASK_CANCEL){
			return false;
		}
		//删除本地文件对应的数据库记录日志.
		taskLogService.deleteTaskLog(downloadTask);
		
		
		//删除本地文件.
		File file = new File(downloadTask.getFilePath().getAbsolutePath());
		file.delete();
		return true;
	}
	
	/* 
	 *删除指定任务.删除任务时会将任务对应的本地文件一同删除
	 */
	
	public void deleteTask(DownloadTask downloadTask){
		cancelTask(downloadTask);
		deleteTaskFile(downloadTask);
	}
	
	
	/**
	 * 重启指定任务.当任务异常终止时,可重启该任务.
	 * @param downloadTask
	 * @return	true:重启成功	false:重启失败
	 */
	public boolean rebootTask(DownloadTask downloadTask){
		
		if(getTaskState(downloadTask) != DownloadTask.TASK_EXCEPTION){
			System.out.println("当前状态不为异常结束状态,无法重启执行!");
			return false;
		}
		
		MultiDownLoaderListener listener = (MultiDownLoaderListener)listenerList.get(downloadTask);
		if(listener == null){
			return false;
		}else{
			addTask(downloadTask,listener);
			return true;
		}
	}
	
	/* 
	 * 继续指定任务.当任务暂停时,可继续执行该任务
	 */
	public boolean continueTask(DownloadTask downloadTask){
		
		if(getTaskState(downloadTask) != DownloadTask.TASK_PAUSE){
			System.out.println("当前状态不为暂停状态,无法继续执行!");
			return false;
		}
		
		MultiDownLoaderListener listener = (MultiDownLoaderListener)listenerList.get(downloadTask);
		if(listener == null){
			return false;
		}else{
			addTask(downloadTask,listener);
			return true;
		}
	}
	
	/* 
	 * 重新执行指定任务.当任务执行完成时,可重新执行该任务
	 */
	public boolean restartTask(DownloadTask downloadTask){
		
		if(getTaskState(downloadTask) != DownloadTask.TASK_OVER){
			System.out.println("当前任务状态不为完成状态,无法重新执行!");
			return false;
		}
		
		cancelTask(downloadTask);
		
		MultiDownLoaderListener listener = (MultiDownLoaderListener)listenerList.get(downloadTask);
		if(listener == null){
			return false;
		}else{
			addTask(downloadTask,listener);
			return true;
		}
	}
	
	/**
	 * 根据任务状态初始化View状态
	 * @param view
	 * @param listener
	 */
	public void initTaskListener(DownloadTask task,MultiDownLoaderListener listener){
		listenerList.put(task, listener);
		
		if(task.getTaskState() == DownloadTask.TASK_PAUSE || getTaskState(task)==DownloadTask.TASK_PAUSE){
			listener.onPause();
		}else if(task.getTaskState() == DownloadTask.TASK_OVER || getTaskState(task)==DownloadTask.TASK_OVER){
			listener.onSuccessed();
		}
	}
	
	/**
	 * 查看任务是否已存在
	 * @return
	 */
	public boolean isExist(DownloadTask downloadTask){
		if(taskLogService.getTaskState(downloadTask)!=DownloadTask.TASK_NULL){
			return true;
		}return false;
	}
	
	/**
	 * 获取已存在任务对应的下载文件的总大小.单位:"字节"
	 * @param downloadTask
	 * @return	-1:任务不存在,或不能获取该任务对应本地文件的总大小.
	 */
	public int getTaskTotalSize(DownloadTask downloadTask){
		return taskLogService.getTaskTotalSize(downloadTask);
	}
	
	/**
	 * 获取已存在任务对应该的文件已下载的大小.单位:"字节 "
	 * @param downloadTask
	 * @return	-1:任务不存在.
	 */
	public int getTaskDownSize(DownloadTask downloadTask){
		return taskLogService.getTaskProgress(downloadTask);
	}

	
	/**
	 * 获取任务当前状态
	 * @param downloadTask
	 * @return
	 */
	private int getTaskState(DownloadTask downloadTask){
		return taskLogService.getTaskState(downloadTask);
	}
	
	/**
	 * 更新任务状态为"WAIT"
	 * @param downloadTask
	 */
	private void updateTaskWait(DownloadTask downloadTask){
		downloadTask.setTaskState(DownloadTask.TASK_WAIT);
		taskLogService.createTaskLog(downloadTask);
		taskLogService.updateTaskState(downloadTask);
	}
	
	/**
	 * 更新任务状态为"PAUSE"
	 * @param downloadTask
	 */
	private void updateTaskPause(DownloadTask downloadTask){
		downloadTask.setTaskState(DownloadTask.TASK_PAUSE);
		taskLogService.updateTaskState(downloadTask);
	}
	
	/**
	 * 更新任务状态为"RUNNING"
	 * @param downloadTask
	 */
	private void updateTaskRunning(DownloadTask downloadTask){
		downloadTask.setTaskState(DownloadTask.TASK_RUNNING);
		taskLogService.updateTaskState(downloadTask);
	}
	
	/**
	 * 更新任务状态为"EXCEPTION"
	 * @param downloadTask
	 */
	private void updateTaskException(DownloadTask downloadTask){
		downloadTask.setTaskState(DownloadTask.TASK_EXCEPTION);
		taskLogService.updateTaskState(downloadTask);
	}
	
	/**
	 * 更新任务状态为"CANCEL"
	 * @param downloadTask
	 */
	private void updateTaskCancel(DownloadTask downloadTask){
		downloadTask.setTaskState(DownloadTask.TASK_CANCEL);
        downloadTask.setFileDownSize(0);
		taskLogService.updateTaskState(downloadTask);
	}
	
	/**
	 * 更新任务状态为"OVER"
	 * @param downloadTask
	 */
	private void updateTaskOver(DownloadTask downloadTask){
		downloadTask.setTaskState(DownloadTask.TASK_OVER);
		taskLogService.updateTaskState(downloadTask);
		downloadTask.setFileDownSize(0);
		taskLogService.updateTaskProgress(downloadTask);
	}
	
	
	
	
	/**
	 * 删除任务.
	 */
	private void deleteTask(List<DownloadTask> taskList){
		for(int i=0;i<taskList.size();i++){
			//先取消任务
			cancelTask(taskList.get(i));
			//删除任务对应的本地文件.
			filedeleteDevice.addTask(taskList.get(i));
		}
	}
	
	/**
	 * 获取所有任务.不包括已被取消的任务
	 * @return
	 */
	public List<DownloadTask> getAllTask(){
		return taskLogService.getUnassignTask(DownloadTask.TASK_CANCEL);
	};
	
	/**
	 * 删除所有任务.不包括已被取消的任务
	 */
	public void deleteAllTask(){
		deleteTask(getAllTask());
		taskLogService.deleteUnassignTask(DownloadTask.TASK_CANCEL);
	};
	
	/**
	 * 获取所有已完成的任务
	 * @return
	 */
	public List<DownloadTask> getAllDoneTask(){
		return taskLogService.getAssignTask(DownloadTask.TASK_OVER);
	};
	
	/**
	 * 删除所有已完成的任务
	 */
	public void deleteAllDoneTask(boolean delLocalFile){
		deleteTask(getAllDoneTask());
		taskLogService.deleteAssignTask(DownloadTask.TASK_OVER);
	};
	
	
	/**
	 * 获取所有未完成的任务
	 * @return
	 */
	public List<DownloadTask> getAllnotDoneTask(){
		return taskLogService.getAllUnDoneTask();
	};
	
	/**
	 * 删除所有未完成的任务
	 */
	public void deleteAllnotDoneTask(){
		deleteTask(getAllnotDoneTask());
		taskLogService.deleteAllUndoneTask();
	};
	
	
	/**
	 * 获取所有正在等待的任务
	 * @return
	 */
	public List<DownloadTask> getAllWaitTask(){
		return taskLogService.getAssignTask(DownloadTask.TASK_WAIT);
	};
	
	/**
	 * 删除所有正在等待的任务
	 */
	public void deleteAllWaitTask(){
		deleteTask(getAllWaitTask());
		taskLogService.deleteAssignTask(DownloadTask.TASK_WAIT);
	};
	
	/**
	 * 获取所有正在运行的任务
	 * @return
	 */
	public List<DownloadTask> getAllRunningTask(){
		return taskLogService.getAssignTask(DownloadTask.TASK_RUNNING);
	};
	
	/**
	 * 删除所有正在运行的任务
	 */
	public void deleteAllRunningTask(){
		deleteTask(getAllRunningTask());
		taskLogService.deleteAssignTask(DownloadTask.TASK_RUNNING);
	};
	
	/**
	 * 获取所有已暂停的任务
	 * @return
	 */
	public List<DownloadTask> getAllPauseTask(){
		return taskLogService.getAssignTask(DownloadTask.TASK_PAUSE);
	};
	
	/**
	 * 删除所有已暂停的任务
	 */
	public void deleteAllPauseTask(){
		deleteTask(getAllPauseTask());
		taskLogService.deleteAssignTask(DownloadTask.TASK_PAUSE);
	};
	
	/**
	 * 获取所有异常终止的任务
	 * @return
	 */
	public List<DownloadTask> getAllExceptionTask(){
		return taskLogService.getAssignTask(DownloadTask.TASK_EXCEPTION);
	};
	
	
	/**
	 * 删除所有异常终止的任务
	 * @param delLocalFile
	 */
	public void deleteAllExceptionTask(){
		deleteTask(getAllExceptionTask());
		taskLogService.deleteAssignTask(DownloadTask.TASK_EXCEPTION);
	};
	
	/**
	 * 获取所有被取消的任务
	 * @return
	 */
	public List<DownloadTask> getAllCancelTask(){
		return taskLogService.getAssignTask(DownloadTask.TASK_CANCEL);
	};
	
	
	/**
	 * 删除所有被取消的任务
	 * @param delLocalFile
	 */
	public void deleteAllCancelTask(){
		deleteTask(getAllCancelTask());
		taskLogService.deleteAssignTask(DownloadTask.TASK_CANCEL);
	};
}
