package com.database;

/**
 * 
 * @Title: SqlString.java
 * @Description:专门用来保存sql语句
 * @Copyright: Copyright (c) 2014
 * @Company: 车享网
 * @author houjie
 * @date 2014年8月14日 下午2:11:04
 */
public class SQLConstants {
	public static final String auctionBaseData = "auctionBaseData";
	public static final String name = "chexiangpai.db";
	private final static String CREATE_TABLE_NOT_EXISTS="CREATE TABLE NOT EXISTS ";
	private final static String DROP_TABLE_IF_EXISTS="DROP TABLE IF EXISTS ";
	
	/**创建车辆管理数据主表**/
	public static String initSQL = CREATE_TABLE_NOT_EXISTS 
			+auctionBaseData + "(" 
			+ "id INTEGER PRIMARY KEY,"// 主键
			+ "item_id INTEGER,"
			+ "pic TEXT,"// 车辆id
			+ "desc TEXT,"
			+ "level TEXT,"
			+ "licence TEXT,"
			+ "registDate TEXT,"
			+ "distince TEXT,"
			+ "startTime TEXT,"
			+ "endTime TEXT,"
			+ "updateTime TEXT,"
			+ "currentPrice FLOAT,"
			+ "isInterest NUMERIC,"
			+ "offerMin NUMERIC,"
			+ "offer NUMERIC,"
			+ "offerMax NUMERIC,"
			+ "myPrice FLOAT,"
			+ "startPrice FLOAT,"
			+ "status NUMERIC,"
			+ "md5 TEXT);";
	
	/**更新数据库需要的语句*/
	public static String upDateSQL=DROP_TABLE_IF_EXISTS+auctionBaseData+";";
}
