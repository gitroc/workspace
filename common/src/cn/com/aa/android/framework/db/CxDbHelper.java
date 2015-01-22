package cn.com.aa.android.framework.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.databaseFreamwork.orm.AbSDDBHelper;

/**
 * 
 * @Title: CxDbHelper.java
 * @Description: 数据库帮助类
 * @Copyright: Copyright (c) 2014
 * @Company: 车享网
 * @author houjie
 * @date 2014年8月17日 上午3:16:19
 */
public class CxDbHelper extends AbSDDBHelper {
	private static String dbName = "chexiangpai.db";// 数据库名称
	private static String path = "chexiangpaiDB";// 数据库保存在sd卡的路径
	private static int databaseVersion = 1;

	public CxDbHelper(Context context, Class<?>[] modelClasses) {
		super(context, path, dbName, null, databaseVersion, modelClasses);
	}

	/**
	 * 数据库构造器
	 * 
	 * @param databaseVersion
	 * @param context
	 * @param modelClasses
	 */
	public CxDbHelper(int databaseVersion, Context context, Class<?>[] modelClasses) {
		super(context, path, dbName, null, databaseVersion, modelClasses);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		super.onUpgrade(db, oldVersion, newVersion);
	}
}
