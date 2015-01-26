package com.corebase.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;

/**
 * 
 * @Title: TimeUtil.java
 * @Description: 换算時間
 * @Copyright: Copyright (c) 2014
 * @Company: 车享网
 * @author houjie
 * @date 2014年8月9日 下午8:17:14
 */
@SuppressLint("SimpleDateFormat")
public class SimpleTimeUtil {
	private static SimpleDateFormat sdf;
	private static SimpleDateFormat sdf2;

	/**
	 * 
	 * @param mss
	 * @return
	 */
	public static String formatDuring(long mss) {
		if (mss <= 0) {
			return "00:00:00";
		}
		StringBuffer sb = new StringBuffer();
		long days = mss / (1000 * 60 * 60 * 24);
		long hours = (mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
		long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
		long seconds = (mss % (1000 * 60)) / 1000;
		sb.append(addZero(days, false)).append(addZero(hours, true)).append(addZero(minutes, true)).append(addZero(seconds, true));
		String str = sb.toString();
		str = str.substring(0, str.length() - 1);
		return str;
	}

	private static String addZero(long time, boolean b) {
		String timeStr;
		if (time < 1 && !b) {
			timeStr = "";
		} else if (time >= 1 && time < 10 || time <= 0) {
			if (!b) {
				timeStr = time + "天";
			} else {
				timeStr = "0" + time + ":";
			}
		} else {
			timeStr = time + ":";
		}
		return timeStr;
	}

	/**
	 * 
	 * @param begin
	 *            时间段的开始
	 * @param end
	 *            时间段的结束
	 * @return 输入的两个Date类型数据之间的时间间格用* days * hours * minutes * seconds的格式展示
	 * @author fy.zhang
	 */
	public static String formatDuring(Date begin, Date end) {
		return formatDuring(end.getTime() - begin.getTime());
	}

	@SuppressLint("SimpleDateFormat")
	public static Date dateToLong(String formatDate, String date) throws ParseException {
		sdf = new SimpleDateFormat(formatDate);
		Date dt = sdf.parse(date);
		return dt;
	}

	public static Date dateToLong(String date) throws ParseException {
		sdf2 = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");
		Date dt = sdf2.parse(date);
		return dt;
	}

	public static String longToDate(String time) {
		Date date = new Date(Long.parseLong(time));
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		time = formatter.format(date);
		return time;
	}

	public static Long formatDuring(String start, String end) throws ParseException {
		Date date1 = dateToLong(start);
		Date date2 = dateToLong(end);
		return date2.getTime() - date1.getTime();
	}

	/**
	 * string类型转换为long类型 strTime要转换的String类型的时间 formatType时间格式
	 * strTime的时间格式和formatType的时间格式必须相同
	 */
	public static long stringToLong(String strTime, String formatType) throws ParseException {
		Date date = stringToDate(strTime, formatType); // String类型转成date类型
		if (date == null) {
			return 0;
		} else {
			long currentTime = dateToLong(date); // date类型转成long类型
			return currentTime;
		}
	}

	/**
	 * string类型转换为long类型 strTime要转换的String类型的时间 formatType时间格式
	 * strTime的时间格式和formatType的时间格式必须相同
	 */
	public static long stringToLong(String strTime) throws ParseException {
		Date date = stringToDate(strTime, "yyyy-MM-dd HH:mm:ss"); // String类型转成date类型
		if (date == null) {
			return 0;
		} else {
			long currentTime = dateToLong(date); // date类型转成long类型
			return currentTime;
		}
	}

	/**
	 * date类型转换为String类型 formatType格式为yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
	 * data Date类型的时间
	 */
	public static String dateToString(Date data, String formatType) {
		return new SimpleDateFormat(formatType).format(data);
	}

	/**
	 * long类型转换为String类型 currentTime要转换的long类型的时间 formatType要转换的string类型的时间格式
	 */
	public static String longToString(long currentTime, String formatType) throws ParseException {
		Date date = longToDate(currentTime, formatType); // long类型转成Date类型
		String strTime = dateToString(date, formatType); // date类型转成String
		return strTime;
	}

	/**
	 * long类型转换为String类型 currentTime要转换的long类型的时间 formatType要转换的string类型的时间格式
	 */
	public static String longToString(long currentTime) throws ParseException {
		Date date = longToDate(currentTime, "yyyy-MM-dd HH:mm:ss"); // long类型转成Date类型
		String strTime = dateToString(date, "yyyy-MM-dd HH:mm:ss"); // date类型转成String
		return strTime;
	}

	/**
	 * string类型转换为date类型 strTime要转换的string类型的时间，formatType要转换的格式yyyy-MM-dd
	 * HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒， strTime的时间格式必须要与formatType的时间格式相同
	 */
	public static Date stringToDate(String strTime, String formatType) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat(formatType);
		Date date = null;
		date = formatter.parse(strTime);
		return date;
	}

	/**
	 * long转换为Date类型 currentTime要转换的long类型的时间 formatType要转换的时间格式yyyy-MM-dd
	 * HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
	 */
	public static Date longToDate(long currentTime, String formatType) throws ParseException {
		Date dateOld = new Date(currentTime); // 根据long类型的毫秒数生命一个date类型的时间
		String sDateTime = dateToString(dateOld, formatType); // 把date类型的时间转换为string
		Date date = stringToDate(sDateTime, formatType); // 把String类型转换为Date类型
		return date;
	}

	/**
	 * date类型转换为long类型 date要转换的date类型的时间
	 */
	public static long dateToLong(Date date) {
		return date.getTime();
	}
}
