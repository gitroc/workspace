package com.corebase.android.framework.http.download.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper{

	private static final String DBNAME = "chexiang.db";
	private static final String TASKLOG_TB = "taskLogTb";
	private static final int VERSION = 1;
	
	public static String initSQL = 
        "CREATE TABLE IF NOT EXISTS " + TASKLOG_TB + " (" +
        "    id INTEGER PRIMARY KEY, " 		+
        "    downloadUrl INT(255), 	 " 		+		
        "    savePath varchar(100),  "		+       
        "	 downloadLength INT(255),"		+	
        "	 totalLength INT(255),	 "		+
        "    taskState INT(1) " 			+			
        ");";
	
	
	public DBOpenHelper(Context context) {
		super(context, DBNAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(initSQL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
