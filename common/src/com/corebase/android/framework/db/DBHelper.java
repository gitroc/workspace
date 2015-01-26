package com.corebase.android.framework.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	private final static String TAG = DBHelper.class.getSimpleName();
	// 通用数据库操作语句
	private static String dbName = "app.db";
	public final static String CACHE_TABLE = "app_cache";
	private static DBHelper dbHelper;
	private static int version=1;
	
	private static String initSQL = "CREATE TABLE IF NOT EXISTS "
			+ CACHE_TABLE 
			+ " (" 
			+ "    id INTEGER PRIMARY KEY, " 
			+ "    key NVARCHAR(255), " 
			+ "    file NVARCHAR(255), " 
			+ "    size NUMERIC, " 
			+ "    status INTEGER, "
			+ "    time NUMERIC, " 
			+ "    expire NUMERIC" + ");";
	private static String upgradeSQL = initSQL;

	// 同时使用通用和应用数据库的构造函数
	// 注意SQL语句必须要有完整的结束符“;”
	public static DBHelper getInstanc(Context context){
		if (dbHelper==null) {
			dbHelper=new DBHelper(context, version, dbName, initSQL, upgradeSQL);
		}
		return dbHelper;
	}
	public DBHelper(Context context, int version, String appDbName, String appInitSql, String appUpgradeSql) {
		super(context, appDbName == null || appDbName.trim().equals("") ? dbName : appDbName, null, version);
		if (appInitSql != null && !appInitSql.trim().equals("")) {
			initSQL += appInitSql;
			upgradeSQL += appInitSql;
		}
		if (appUpgradeSql != null && !appUpgradeSql.trim().equals("")) {
			upgradeSQL += appUpgradeSql;
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(TAG, "Initialize database");
		String[] initSqls = initSQL.split(";");
		for (String initSql : initSqls) {
			Log.i(TAG, "execSQL: " + initSql + ";");
			db.execSQL(initSql + ";");
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(TAG, "Upgrade database");
		String[] upgradeSqls = upgradeSQL.split(";");
		for (String upgradeSql : upgradeSqls) {
			Log.i(TAG, "execSQL: " + upgradeSql + ";");
			db.execSQL(upgradeSql + ";");
		}
		onCreate(db);
	}
}
