package cn.com.aa.android.framework.cache;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import org.apache.http.entity.ByteArrayEntity;
import org.json.JSONException;
import org.json.JSONObject;

import cn.com.aa.android.framework.db.DBHelper;
import cn.com.aa.android.framework.http.client.JsonHttpResponseHandler;
import cn.com.aa.common.android.utils.FileUtils;
import cn.com.aa.common.android.utils.SDCardUtils;
import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

/***
 * 缓存管理器
 * 
 * @author 王博
 */
public class CacheManager {
	private final static String TAG = CacheManager.class.getSimpleName();

	public final static int TYPE_INTERNAL = 1; // 内部缓存
	public final static int TYPE_EXTERNAL = 2; // 外部缓存

	private static long maxInternalCacheSize = 20 * 1024 * 1024; // 最大内部缓存容量，单位：byte
	private static long maxExternalCacheSize = 500 * 1024 * 1024; // 最大外部缓存容量，单位：byte
	private static long minInternalStorageAvailableSize = 1 * 1024 * 1024; // 最小内部存储可用空间，单位：byte
	private static long minExternalStorageAvailableSize = 10 * 1024 * 1024; // 最小外部存储可用空间，单位：byte

	public static File cacheDirInternal; // 内存缓存目录
	public static File cacheDirExternal; // 外部缓存目录
	public static File tempCacheDirInternal; // 中转内存缓存目录
	public static File tempCacheDirExternal; // 中转外部缓存目录

	public static File logDir; // 日志文件
	public static File downloadDir; // 下载目录
	public static File externalFileDir; // 文件目录
	public static File userAvatar; // 用户头像
	public static File offlineZip; // 离线下载zip压缩文件目录
	public static File offlineUnZip; // 离线下载zip解压缩文件目录

	public final static int dataCacheExpire = 3600; // json数据缓存时间 单位 秒
	public final static int imageCacheExpire = 1209600; // image缓存时间 单位 秒
	public static SQLiteOpenHelper dbHelper = null;

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");// 缓存目录加入（日期）

	public static long getMaxInternalCacheSize() {
		return maxInternalCacheSize;
	}

	public static void setMaxInternalCacheSize(long maxInternalCacheSize) {
		CacheManager.maxInternalCacheSize = maxInternalCacheSize;
	}

	public static long getMaxExternalCacheSize() {
		return maxExternalCacheSize;
	}

	public static void setMaxExternalCacheSize(long maxExternalCacheSize) {
		CacheManager.maxExternalCacheSize = maxExternalCacheSize;
	}

	public static long getMinInternalStorageAvailableSize() {
		return minInternalStorageAvailableSize;
	}

	public static void setMinInternalStorageAvailableSize(long minInternalStorageAvailableSize) {
		CacheManager.minInternalStorageAvailableSize = minInternalStorageAvailableSize;
	}

	public static long getMinExternalStorageAvailableSize() {
		return minExternalStorageAvailableSize;
	}

	public static void setMinExternalStorageAvailableSize(long minExternalStorageAvailableSize) {
		CacheManager.minExternalStorageAvailableSize = minExternalStorageAvailableSize;
	}

	// 缓存任务集合
	private static ArrayList<CacheTask> cacheTasks = new ArrayList<CacheTask>();
	// 缓存线程
	private static Thread cacheThread = new Thread() {
		public void run() {
			while (true) {
				while (null != cacheTasks && !cacheTasks.isEmpty() && cacheTasks.size() > 0) {
					CacheTask task = cacheTasks.get(0);
					// 保存缓存
					saveCache(task);
					if (cacheTasks != null && !cacheTasks.isEmpty()) {
						// 清除任务
						cacheTasks.remove(0);
					}
				}
				try {
					synchronized (this) {
						// 等待通知
						wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	};
	// 启动缓存线程
	static {
		cacheThread.start();
	}

	/***
	 * 初始化缓存目录
	 * 
	 * @param sdName
	 *            最外层目录名字
	 * @param context
	 */
	public static void initCacheDir(String sdName, Context context, SQLiteOpenHelper dbHelper) {
		// 根据设备内存大小设置bitmap内存缓存的大小
		int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();

		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		Cache.memoryCacheSize = 1024 * 1024 * memClass / 10;

		CacheManager.dbHelper = dbHelper;
		// 初始化缓存目录
		CacheManager.cacheDirInternal = new File(context.getCacheDir(), "cpApp");
		CacheManager.cacheDirExternal = new File(Environment.getExternalStorageDirectory(), sdName + "/cache/app");
		if (!CacheManager.cacheDirInternal.exists() || !CacheManager.cacheDirInternal.isDirectory()) {
			CacheManager.cacheDirExternal.mkdirs();
		}

		if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			if (!CacheManager.cacheDirExternal.exists() || !CacheManager.cacheDirExternal.isDirectory()) {
				CacheManager.cacheDirExternal.mkdirs();
			}
		}

		// 初始化临时存储目录
		CacheManager.tempCacheDirInternal = new File(CacheManager.cacheDirInternal, "temp");
		CacheManager.tempCacheDirExternal = new File(CacheManager.cacheDirExternal, "temp");
		if (!CacheManager.tempCacheDirInternal.exists() || CacheManager.tempCacheDirInternal.isFile()) {
			CacheManager.tempCacheDirInternal.mkdirs();
		}
		if (!CacheManager.tempCacheDirExternal.exists() || CacheManager.tempCacheDirExternal.isFile()) {
			CacheManager.tempCacheDirExternal.mkdirs();
		}
	}

	/***
	 * 根据外部key获取缓存缓存系统标准key
	 * 
	 * @param key
	 *            外部key
	 * @return 缓存系统标准key
	 */
	private static String getCacheKey(String key) {
		String stdKey = null;
		if (key != null && key.trim().length() > 0) {
			stdKey = UUID.nameUUIDFromBytes(key.getBytes()).toString();
			if (key.contains("http://10.32.17.236:9200/")) {
				Log.i("urlMsg", "stdKey:" + stdKey);
			}
		}
		return stdKey;
	}

	/***
	 * 根据外部key获取缓存文件名
	 * 
	 * @param extKey
	 *            外部key
	 * @return 缓存文件名
	 */
	private static String getCacheFileName(String key) {
		return getCacheKey(key);
	}

	/***
	 * 写入缓存
	 * 
	 * @param key
	 *            外部key
	 * @param byte[] 缓存内容
	 * @param expire
	 *            有效时间
	 * @param type
	 *            缓存类型
	 */
	public static void setCache(String key, byte[] content, long expire, int type) {
		CacheTask task = new CacheTask();
		task.setKey(key);
		task.setContent(content);
		task.setExpire(expire);
		task.setType(type);
		cacheTasks.add(task);
		synchronized (cacheThread) {
			cacheThread.notify();
		}
	}

	/***
	 * 保存缓存(byte数组写入文件，并做数据库保存)
	 * 
	 * @param tempObject
	 */
	private synchronized static void saveCache(CacheTask task) {
		// 将缓存写入文件
		File cacheFile = saveByteToFile(task);
		long size = 0;
		if (cacheFile == null || !cacheFile.exists() || !cacheFile.isFile()) {
			return;
		} else {
			size = cacheFile.length();
		}
		// 缓存信息保存数据库
		String stdKey = getCacheKey(task.getKey());
		long time = System.currentTimeMillis();
		ContentValues dataValue = new ContentValues();
		dataValue.put("key", stdKey);
		dataValue.put("file", cacheFile.getAbsolutePath());
		dataValue.put("size", size);
		dataValue.put("status", task.getType());
		dataValue.put("time", time);
		if (task.getExpire() > 0) {
			// dataValue.put("expire", time + task.getExpire()*1000);
			dataValue.put("expire", task.getExpire());
		} else {
			dataValue.put("expire", time);
		}

		try {
			Cache cache = null;
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			while (db.isDbLockedByCurrentThread()) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Cursor cur = null;
			try {
				// 查询数据库中是否已经有相关cache
				cur = db.rawQuery("select * from " + DBHelper.CACHE_TABLE + " " + "where key = '" + stdKey + "'", null);
				if (cur != null && cur.getCount() > 0 && cur.moveToNext()) {
					cache = new Cache();
					cache.parse(cur);
				}
			} catch (Exception e) {
				throw e;
			} finally {
				if (cur != null) {
					cur.close();
				}
			}
			db = dbHelper.getWritableDatabase();
			while (db.isDbLockedByCurrentThread()) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (cache != null) {
				// 更新
				dataValue.put("id", cache.getId());
				String[] parms = new String[] { Long.toString(cache.getId()) };
				db.update(DBHelper.CACHE_TABLE, dataValue, "id=?", parms);
			} else {
				// 插入
				db.insert(DBHelper.CACHE_TABLE, null, dataValue);
			}
		} catch (Exception e) {
			Log.e(TAG, "set cache data failed: " + stdKey);
			e.printStackTrace();
		}
	}

	/***
	 * 判断存储卡是否可用
	 * 
	 * @return
	 */
	public static boolean isSDCardCanUse() {
		if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	/***
	 * 将byte数组保存到文件
	 * 
	 * @param tempObject
	 *            临时缓存对象
	 * @return 缓存文件
	 */
	private static File saveByteToFile(CacheTask task) {
		if (null == task) {
			return null;
		}
		// 如果外部缓存不能使用
		if (null != Environment.getExternalStorageState() && !Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED) && task.getType() == TYPE_EXTERNAL) {
			return null;
		}
		// 保存文件
		File cacheFile = null;// 缓存文件
		File tempFile = null;// 临时缓存文件
		File cacheDir = null;// 缓存目录
		if (null == task.getKey() || null == task.getContent() || task.getContent().length <= 0) {
			Log.i(TAG, "set cache data: cache task is null");
			return null;
		}
		String fileName = getCacheFileName(task.getKey());// 文件名
		if (null == fileName) {
			Log.i(TAG, "set cache data: cache name is null");
			return null;
		}
		String date = dateFormat.format(new Date());

		// 构造缓存文件名
		if (task.getType() == TYPE_INTERNAL) {// 内部缓存
			if (null == cacheDirInternal || !cacheDirInternal.exists() || null == tempCacheDirInternal || !tempCacheDirInternal.exists()) {
				Log.i(TAG, "set cache data: cache internal dir is not exists");
				return null;
			}
			cacheDir = new File(cacheDirInternal, date);
			cacheFile = new File(cacheDir, fileName);
			tempFile = new File(tempCacheDirInternal, fileName);
		} else if (task.getType() == TYPE_EXTERNAL) {// 外部缓存
			if (null == cacheDirExternal || !cacheDirExternal.exists() || null == tempCacheDirExternal || !tempCacheDirExternal.exists()) {
				Log.i(TAG, "set cache data: cache external dir is not exists");
				return null;
			}
			cacheDir = new File(cacheDirExternal, date);
			cacheFile = new File(cacheDir, fileName);
			tempFile = new File(tempCacheDirExternal, fileName);
		}
		if (cacheDir != null && !cacheDir.exists()) {
			cacheDir.mkdirs();
		}
		try {
			// 将byte数组写入文件
			FileUtils.writeFile(tempFile, task.getContent());
			// 将临时缓存搬到缓存文件
			if (tempFile != null && tempFile.exists() && tempFile.isFile()) {
				if (cacheFile != null && cacheFile.exists() && cacheFile.isFile()) {
					cacheFile.delete();
				}
				// 文件移动
				FileUtils.move(tempFile, cacheFile);
			}
		} catch (Exception e) {
			Log.i(TAG, "set cache data: move cache file exception " + e);
			e.printStackTrace();
		}
		return cacheFile;
	}

	/***
	 * 读取缓存（忽略过期时间）
	 * 
	 * @param key
	 *            外部key
	 * @return 缓存数据
	 */
	public static byte[] getCacheIgnoreExpire(String key) {
		Cache cache = null;
		cache = getCacheFile(key);
		byte[] content = null;
		if (cache != null) {
			try {
				content = FileUtils.readFileToByte(cache.getFile());
			} catch (IOException e) {
				Log.e(TAG, "get cache data ignore expire fail: " + key);
				e.printStackTrace();
			}
		}
		return content;
	}

	/***
	 * 
	 * @param key
	 * @return
	 */
	public static Cache getCacheFile(String key) {
		String stdKey = getCacheKey(key);
		Cache cache = null;
		if (null == dbHelper) {
			return null;
		}
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cur = null;
		try {
			cur = db.rawQuery("select * from " + DBHelper.CACHE_TABLE + " " + "where key = '" + stdKey + "'", null);
			if (cur != null && cur.getCount() > 0 && cur.moveToNext()) {
				try {
					cache = new Cache();
					cache.parse(cur);
				} catch (ParseException e) {
					Log.e(TAG, "get cache data ignore expire fail: " + stdKey);
					cache = null;
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
		return cache;
	}

	/***
	 * 读取缓存
	 * 
	 * @param key
	 *            外部key
	 * @return 缓存数据
	 */
	public static byte[] getCache(String key) {
		Cache cache = null;
		cache = getCacheFile(key);
		Log.i("urlMsg", "缓存文件路径:" + cache.getFile().getAbsolutePath());
		byte[] content = null;
		if (cache != null && cache.getExpire() < System.currentTimeMillis() && cache.getFile() != null && cache.getFile().exists() && cache.getFile().isFile()) {
			try {
				content = FileUtils.readFileToByte(cache.getFile());
				Log.i(TAG, "get cache data: " + key);
			} catch (IOException e) {
				content = null;
				Log.e(TAG, "get cache data ignore expire fail: " + key);
				e.printStackTrace();
			}
		} else {
			content = null;
			Log.i(TAG, "get cache data fail: " + key);
		}
		// try {
		// Log.i("msg", "缓存文件内容:"+FileUtils.readTextInputStream(new
		// ByteArrayInputStream(content)));
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		return content;
	}

	/***
	 * 读取url拿到缓存文件
	 * 
	 * @param key
	 *            外部key
	 */
	public static void getCacheToJson(final String key, final JsonHttpResponseHandler handler) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				Cache cache = null;
				JSONObject jsonObject;
				cache = getCacheFile(key);
				// Log.i("urlMsg", "缓存文件路径:" +
				// cache.getFile().getAbsolutePath());
				byte[] content = null;
				if (cache != null && cache.getExpire() < System.currentTimeMillis() && cache.getFile() != null && cache.getFile().exists() && cache.getFile().isFile()) {
					try {
						content = FileUtils.readFileToByte(cache.getFile());
						Log.i(TAG, "get cache data: " + key);
					} catch (IOException e) {
						content = null;
						Log.e(TAG, "get cache data ignore expire fail: " + key);
						e.printStackTrace();
					}
				} else {
					content = null;
					Log.i(TAG, "get cache data fail: " + key);
				}
				try {
					if (content != null) {
						String stream = FileUtils.readTextInputStream(new ByteArrayInputStream(content));
						if (!TextUtils.isEmpty(stream)) {
							jsonObject = new JSONObject(stream);
							handler.onSuccess(jsonObject);
						} else {
							handler.onFailure(new NullPointerException(), "数据不存在");
						}
					} else {
						handler.onFailure(new NullPointerException(), "数据不存在");
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	/***
	 * 清除所有缓存
	 * 
	 * @param cacheType
	 *            缓存类型
	 */
	public synchronized static void clearAllCache(int cacheType) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.delete(DBHelper.CACHE_TABLE, "status=" + cacheType, null);

		// 清除临时缓存
		clearTempCache(cacheType);

		// 清除应用缓存
		if (cacheType == TYPE_INTERNAL && cacheDirInternal.exists() && cacheDirInternal.isDirectory()) {
			FileUtils.deleteDirectory(cacheDirInternal, false);
			// 初始化临时存储目录
			tempCacheDirInternal = new File(cacheDirInternal, "temp");
			if (!tempCacheDirInternal.exists() || tempCacheDirInternal.isFile()) {
				tempCacheDirInternal.mkdirs();
			}
		} else if (cacheType == TYPE_EXTERNAL && cacheDirExternal.exists() && cacheDirExternal.isDirectory()) {
			FileUtils.deleteDirectory(cacheDirExternal, false);
			// 初始化临时存储目录
			tempCacheDirExternal = new File(cacheDirExternal, "temp");
			if (!tempCacheDirExternal.exists() || tempCacheDirExternal.isFile()) {
				tempCacheDirExternal.mkdirs();
			}
		}
	}

	/***
	 * 删除所有缓存
	 */
	public synchronized static void clearAllCache() {
		clearAllCache(TYPE_INTERNAL);
		clearAllCache(TYPE_EXTERNAL);
	}

	/***
	 * 删除过期缓存，并检测内部外部缓存是否有必须清除的，若需要，进行按比例删除
	 * 
	 */
	public synchronized static void clearNeedClearCache(int cacheType) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cur = null;
		Cache cache = null;
		try {
			cur = db.rawQuery("select * from " + DBHelper.CACHE_TABLE + " " + "where expire<=" + System.currentTimeMillis() + " and status = " + cacheType, null);
			if (cur != null && cur.getCount() > 0 && cur.moveToNext()) {
				cache = new Cache();
				cache.parse(cur);
				if (null != cache.getFile() && cache.getFile().exists() && cache.getFile().delete()) {
					db.delete(DBHelper.CACHE_TABLE, "id=" + cache.getId(), null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cur != null) {
				cur.close();
			}
		}

		if (needClear(TYPE_INTERNAL)) {
			Log.i(TAG, "The internal cache space is not enough, clear cache...");
			// clearAllCache(TYPE_INTERNAL);
			clearProportionCache(TYPE_INTERNAL);
		}
		if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED) && needClear(TYPE_EXTERNAL)) {
			Log.i(TAG, "The external cache space is not enough, clear cache...");
			// clearAllCache(TYPE_EXTERNAL);
			clearProportionCache(TYPE_EXTERNAL);
		}
	}

	// 删除长时间没被使用的文件目录
	public synchronized static void clearUnUserFile() {
		// 离线下载文章目录
		FileUtils.deleteDirectoryByTime(offlineUnZip, 10);
		boolean tag = FileUtils.deleteDirectoryByTime(cacheDirExternal, 30);
		if (tag) {// 重新初始化temp目录
			CacheManager.tempCacheDirExternal = new File(CacheManager.cacheDirExternal, "temp");
			if (!CacheManager.tempCacheDirExternal.exists() || CacheManager.tempCacheDirExternal.isFile()) {
				CacheManager.tempCacheDirExternal.mkdirs();
			}
		}

	}

	/***
	 * 按照比例删除缓存
	 * 
	 */
	public synchronized static void clearProportionCache(int cacheType) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cur = null;
		Cache cache = null;
		long totalSize = getUsedCacheSize(cacheType);// 总容量
		float proportion = (float) 0.5; // 删除比例50%
		long size = 0;
		try {
			cur = db.rawQuery("select * from " + DBHelper.CACHE_TABLE + " " + " where status = " + cacheType + " order by expire", null);
			if (cur != null && cur.getCount() > 0) {
				while (cur.moveToNext()) {
					cache = new Cache();
					cache.parse(cur);
					if (cache.getFile().delete()) {
						db.delete(DBHelper.CACHE_TABLE, "id=" + cache.getId(), null);
						size += cache.getSize();// 累计
					}
					if (size >= totalSize * proportion) {// 超过比例时，停止删除
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
	}

	/***
	 * 按比例删除(需要删除时)
	 */
	public synchronized static void clearProportionCache() {
		if (needClear(TYPE_INTERNAL)) {
			clearProportionCache(TYPE_INTERNAL);
		}
		if (needClear(TYPE_EXTERNAL)) {
			clearProportionCache(TYPE_EXTERNAL);
		}
	}

	/**
	 * 清除临时缓存数据
	 * 
	 */
	private synchronized static void clearTempCache(int cacheType) {
		// 清除中转缓存
		if (cacheType == TYPE_INTERNAL && tempCacheDirInternal.exists() && tempCacheDirInternal.isDirectory()) {
			FileUtils.deleteDirectory(tempCacheDirInternal, false);
		} else if (cacheType == TYPE_EXTERNAL && tempCacheDirExternal.exists() && tempCacheDirExternal.isDirectory()) {
			FileUtils.deleteDirectory(tempCacheDirExternal, false);
		}
	}

	public synchronized static void clearTempCache() {
		clearTempCache(TYPE_INTERNAL);
		clearTempCache(TYPE_EXTERNAL);
	}

	/***
	 * 获取可用缓存空间大小
	 * 
	 * @param cacheType
	 *            缓存类型
	 * @return 可用缓存空间大小 字节
	 */
	private synchronized static long getAvailableCacheSize(int cacheType) {
		File cacheDir = null;
		if (cacheType == TYPE_INTERNAL) {
			cacheDir = cacheDirInternal;
		} else {
			cacheDir = cacheDirExternal;
		}
		return FileUtils.getAvailableStorageSize(cacheDir);
	}

	/***
	 * 获取已经使用的缓存大小
	 * 
	 * @param cacheType
	 * @return 已经使用的缓存大小 字节
	 */
	public synchronized static long getUsedCacheSize(int cacheType) {
		File cacheDir = null;
		if (cacheType == TYPE_INTERNAL) {
			cacheDir = cacheDirInternal;
		} else {
			cacheDir = cacheDirExternal;
		}
		if (null == cacheDir || !cacheDir.exists()) {
			return 0;
		}
		return FileUtils.getDirSize(cacheDir);
	}

	/***
	 * 判断某一类型缓存是否需要清理
	 * 
	 * @param cacheType
	 * @return 是否必须执行缓存清理
	 */
	private synchronized static boolean needClear(int cacheType) {
		boolean need = false;
		if (cacheType == TYPE_INTERNAL) {
			// 内部存储
			// 可用空间小于最小存储可用空间 或者 存储空间大于最大缓存容量
			if (getAvailableCacheSize(TYPE_INTERNAL) <= minInternalStorageAvailableSize || getUsedCacheSize(TYPE_INTERNAL) > maxInternalCacheSize) {
				need = true;
			}
		} else {
			// 外部存储
			if (!Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED) || getAvailableCacheSize(TYPE_EXTERNAL) <= minExternalStorageAvailableSize || getUsedCacheSize(TYPE_EXTERNAL) > maxExternalCacheSize) {
				need = true;
			}
		}
		if (need) {
			Log.i(TAG, "The cache space [" + cacheType + "] need clear");
		}
		return need;
	}

	/***
	 * 缓存对象
	 * 
	 * @author user
	 * 
	 */
	private static class CacheTask {
		private String key; // 缓存key值
		private byte[] content; // 缓存内容
		private long expire; // 过期时间
		private int type; // 缓存类型

		public long getExpire() {
			return expire;
		}

		public void setExpire(long expire) {
			this.expire = expire;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public byte[] getContent() {
			return content;
		}

		public void setContent(byte[] content) {
			this.content = content;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

	}
}
