package com.corebase.android.framework.http.download;

import java.io.File;
import java.util.List;

import com.corebase.android.framework.http.download.bean.DownloadTask;
import com.corebase.android.framework.http.download.utils.DBOpenHelper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TaskLogService {
	private DBOpenHelper openHelper;

	protected TaskLogService(Context context) {
		openHelper = new DBOpenHelper(context);
	}
	
	
	/**
	 * 初始化所有未结束未取消的任务状态为暂停状态.
	 */
	protected void initAllTaskState(){
		SQLiteDatabase db = openHelper.getWritableDatabase();
		String sql = "update taskLogTb set taskState = ? where taskState != ? and taskState != ?";
		db.execSQL(sql,new Object[]{DownloadTask.TASK_PAUSE,DownloadTask.TASK_OVER,DownloadTask.TASK_CANCEL});
	}
	
	/**
	 * 生成任务Log日志
	 * @param downloadTask
	 */
	protected void createTaskLog(DownloadTask downloadTask){
		if(getTaskState(downloadTask) == DownloadTask.TASK_NULL){
			//以读写方式打开数据库
			SQLiteDatabase db = openHelper.getWritableDatabase();
			String sql = "insert into taskLogTb(downloadUrl,savePath,taskState) values(?,?,?)";
			db.execSQL(sql,new Object[]{downloadTask.getUrl(),downloadTask.getFilePath().getAbsolutePath(),downloadTask.getTaskState()});
		}
	}
	
	protected boolean isExist(DownloadTask downloadTask){
		SQLiteDatabase db = openHelper.getWritableDatabase();
		String sql = "select * from taskLogTb where downloadUrl = ?";
		Cursor cursor = db.rawQuery(sql, new String[]{downloadTask.getUrl()});
		return cursor.moveToFirst();
	}
	
	/**
	 * 删除一条指定任务日志
	 * @param downloadTask
	 */
	protected boolean deleteTaskLog(DownloadTask downloadTask){
		//任务不存在时,不能删除
		if(getTaskState(downloadTask) == DownloadTask.TASK_NULL){
			return false;
		}
		
		//任务状态不为取消或者删除时,不能删除
		if(getTaskState(downloadTask) != DownloadTask.TASK_CANCEL){
			return false;
		}
		
		SQLiteDatabase db = openHelper.getWritableDatabase();
		String sql = "delete from taskLogTb where downloadUrl = ?";
		db.execSQL(sql, new Object[]{downloadTask.getUrl()});
		return true;
	}
	

	/**
	 * 获取任务的当前状态
	 * @param downloadTask
	 * @return
	 */
	protected int getTaskState(DownloadTask downloadTask){
		SQLiteDatabase db = openHelper.getWritableDatabase();
		String sql = "select * from taskLogTb where downloadUrl = ?";
		Cursor cursor = db.rawQuery(sql, new String[]{downloadTask.getUrl()});
		if(cursor.moveToFirst()){
			return cursor.getInt(cursor.getColumnIndex("taskState"));
		}else{
			return DownloadTask.TASK_NULL;
		}
	}
	
	/**
	 * 更新任务的当前状态
	 * @param downloadTask
	 */
	protected void updateTaskState(DownloadTask downloadTask){
		SQLiteDatabase db = openHelper.getWritableDatabase();
		String sql = "update taskLogTb set taskState = ? where downloadUrl = ?";
		db.execSQL(sql,new Object[]{downloadTask.getTaskState(),downloadTask.getUrl()});

        if (downloadTask.getTaskState() == DownloadTask.TASK_CANCEL){
            String sql1 = "update taskLogTb set downloadLength = 0 where downloadUrl = ?";
            db.execSQL(sql1,new Object[]{downloadTask.getUrl()});
        }
	}
	
	/**
	 * 获取任务下载进度
	 * @param downloadTask
	 * @return	-1:任务不存在.	>=0:任务已下载的进度
	 */
	protected int getTaskProgress(DownloadTask downloadTask){
		SQLiteDatabase db = openHelper.getWritableDatabase();
		String sql = "select * from taskLogTb where downloadUrl = ?";
		Cursor cursor = db.rawQuery(sql, new String[]{downloadTask.getUrl()});
		if(cursor.moveToNext()){
			return cursor.getInt(cursor.getColumnIndex("downloadLength"));
		}
		return -1;
	}
	
	/**
	 * 获取任务下载的文件总大小
	 * @return
	 */
	protected int getTaskTotalSize(DownloadTask downloadTask){
		SQLiteDatabase db = openHelper.getWritableDatabase();
		String sql = "select * from taskLogTb where downloadUrl = ?";
		Cursor cursor = db.rawQuery(sql, new String[]{downloadTask.getUrl()});
		if(cursor.moveToNext()){
			return cursor.getInt(cursor.getColumnIndex("totalLength"));
		}
		return -1;
	}
	
	/**
	 * 更新任务已下载进度
	 * @param downloadTask
	 * @param length
	 */
	protected void updateTaskProgress(DownloadTask downloadTask){
		SQLiteDatabase db = openHelper.getWritableDatabase();
		String sql = "update taskLogTb set downloadLength = ? where downloadUrl = ?";
		db.execSQL(sql,new Object[]{downloadTask.getFileDownSize(),downloadTask.getUrl()});
	}
	
	/**
	 * 更新任务下载文件的总大小
	 * @param downloadTask
	 */
	protected void updateTotalSize(DownloadTask downloadTask){
		SQLiteDatabase db = openHelper.getWritableDatabase();
		String sql = "update taskLogTb set totalLength = ? where downloadUrl = ?";
		db.execSQL(sql,new Object[]{downloadTask.getFileTotalSize(),downloadTask.getUrl()});
	}
	

	/**
	 * 获取所有指定状态的任务
	 * @param state
	 * @return
	 */
	protected  List<DownloadTask> getAssignTask(int taskState){
		List<DownloadTask> list = null;
		SQLiteDatabase db = openHelper.getWritableDatabase();
		String sql = "select * from taskLogTb where taskState = ?";
		Cursor cursor = db.rawQuery(sql, new String[]{taskState+""});
		if(cursor.moveToNext()){
			String url = cursor.getString(cursor.getColumnIndex("downloadUrl"));
			File filePath = new File(cursor.getString(cursor.getColumnIndex("savePath")));
			list.add(new DownloadTask(url, filePath));
		}
		return list;
	}
	
	/**
	 * 获取所有非指定状态的任务
	 * @return
	 */
	protected List<DownloadTask> getUnassignTask(int taskState){
		List<DownloadTask> list = null;
		SQLiteDatabase db = openHelper.getWritableDatabase();
		String sql = "select * from taskLogTb where taskState != ?";
		Cursor cursor = db.rawQuery(sql, new String[]{taskState+""});
		if(cursor.moveToNext()){
			String url = cursor.getString(cursor.getColumnIndex("downloadUrl"));
			File filePath = new File(cursor.getString(cursor.getColumnIndex("savePath")));
			list.add(new DownloadTask(url, filePath));
		}
		return list;
	}
	
	/**
	 * 获取所有未完成的任务,不包括已被取消的任务
	 * @return
	 */
	protected List<DownloadTask> getAllUnDoneTask() {
		List<DownloadTask> list = null;
		SQLiteDatabase db = openHelper.getWritableDatabase();
		String sql = "select * from taskLogTb where taskState != ? and taskState != ?";
		Cursor cursor = db.rawQuery(sql, new String[]{DownloadTask.TASK_OVER+"",DownloadTask.TASK_CANCEL+""});
		if(cursor.moveToNext()){
			String url = cursor.getString(cursor.getColumnIndex("downloadUrl"));
			File filePath = new File(cursor.getString(cursor.getColumnIndex("savePath")));
			list.add(new DownloadTask(url, filePath));
		}
		return list;
	}
	
	/**
	 * 删除所有指定状态的任务
	 * @param taskState
	 */
	protected void deleteAssignTask(int taskState){
		SQLiteDatabase db = openHelper.getWritableDatabase();
		String sql = "delete from taskLogTb where taskState = ?";
		db.execSQL(sql, new Object[]{taskState});
	}
	
	/**
	 * 删除所有非指定状态的任务
	 * @param taskState
	 */
	protected void deleteUnassignTask(int taskState){
		SQLiteDatabase db = openHelper.getWritableDatabase();
		String sql = "delete from taskLogTb where taskState != ?";
		db.execSQL(sql, new Object[]{taskState});
	}
	
	/**
	 * 删除所有未完成的任务
	 */
	protected void deleteAllUndoneTask() {
		SQLiteDatabase db = openHelper.getWritableDatabase();
		String sql = "delete from taskLogTb where taskState != ? android taskState != ?";
		db.execSQL(sql, new Object[]{DownloadTask.TASK_OVER+"",DownloadTask.TASK_CANCEL+""});
	}
}
