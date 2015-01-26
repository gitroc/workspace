package com.corebase.android.framework.http.download;

import java.util.List;

import com.corebase.android.framework.http.download.bean.DownloadTask;


/**
 * @author tony
 *	多任务多线程下载器接口
 */
public interface IMultiDownLoader {
	
	/**
	 * 添加指定任务
	 * @param downLoadTask				下载任务
	 * @param multiDownLoaderListener	下载监听器
	 */
	public void addTask(DownloadTask downLoadTask,MultiDownLoaderListener multiDownLoaderListener);
	
	/**
	 * 暂停指定任务
	 * @param downloadTask
	 */
	public boolean pauseTask(DownloadTask downloadTask);
	
	/**
	 * 取消指定任务.取消任务不会删除任务对应的本地文件.  
	 * @param downloadTask
	 */
	public void cancelTask(DownloadTask downloadTask);
	
	
	/**
	 * 删除指定任务对应的本地文件.当任务已被取消或者删除时,可以删除其对应的本地文件.
	 * @param downloadTask
	 */
	public void deleteTaskFile(DownloadTask downloadTask);
	
	/**
	 * 删除指定任务.删除任务时会将任务对应的本地文件一同删除
	 * @param downloadTask
	 */
	public void deleteTask(DownloadTask downloadTask);
	
	/**
	 * 重启指定任务
	 * @param downloadTask
	 * @return
	 */
	public boolean rebootTask(DownloadTask downloadTask);
	
	/**
	 * 继续指定任务
	 * @param downloadTask
	 * @return
	 */
	public boolean continueTask(DownloadTask downloadTask);
	
	/**
	 * 重新执行指定任务
	 * @param downloadTask
	 * @return
	 */
	public boolean restartTask(DownloadTask downloadTask);
	
	/**
	 * 查看任务是否已存在
	 * @param downloadTask
	 * @return
	 */
	public boolean isExist(DownloadTask downloadTask);
	
	/**
	 * 获取已存在任务对应的下载文件的总大小.单位:"字节"
	 * @param downloadTask
	 * @return	-1:任务不存在,或不能获取该任务对应本地文件的总大小.
	 */
	public int getTaskTotalSize(DownloadTask downloadTask);
	
	/**
	 * 获取已存在任务对应该的文件已下载的大小.单位:"字节 "
	 * @param downloadTask
	 * @return	-1:任务不存在.
	 */
	public int getTaskDownSize(DownloadTask downloadTask);
	
	
	/**
	 * 获取所有任务
	 * @return
	 */
	public List<DownloadTask> getAllTask();
	
	/**
	 * 删除所有任务
	 */
	public void deleteAllTask();
	
	/**
	 * 获取所有已完成的任务
	 * @return
	 */
	public List<DownloadTask> getAllDoneTask();
	
	/**
	 * 删除所有已完成的任务
	 */
	public void deleteAllDoneTask(boolean delLocalFile);
	
	
	/**
	 * 获取所有未完成的任务
	 * @return
	 */
	public List<DownloadTask> getAllnotDoneTask();
	
	/**
	 * 删除所有未完成的任务
	 */
	public void deleteAllnotDoneTask();
	
	
	/**
	 * 获取所有正在等待的任务
	 * @return
	 */
	public List<DownloadTask> getAllWaitTask();
	
	/**
	 * 删除所有正在等待的任务
	 */
	public void deleteAllWaitTask();
	
	/**
	 * 获取所有正在运行的任务
	 * @return
	 */
	public List<DownloadTask> getAllRunningTask();
	
	/**
	 * 删除所有正在运行的任务
	 */
	public void deleteAllRunningTask();
	
	/**
	 * 获取所有已暂停的任务
	 * @return
	 */
	public List<DownloadTask> getAllPauseTask();
	
	/**
	 * 删除所有已暂停的任务
	 */
	public void deleteAllPauseTask();
	
	/**
	 * 获取所有异常终止的任务
	 * @return
	 */
	public List<DownloadTask> getAllExceptionTask();
	
	
	/**
	 * 删除所有异常终止的任务
	 * @param delLocalFile
	 */
	public void deleteAllExceptionTask();
	
	/**
	 * 获取所有被取消的任务
	 * @return
	 */
	public List<DownloadTask> getAllCancelTask();
	
	
	/**
	 * 删除所有被取消的任务
	 * @param delLocalFile
	 */
	public void deleteAllCancelTask();
	
}
