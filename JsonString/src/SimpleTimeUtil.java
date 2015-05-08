import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


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
				timeStr = time + "��";
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
	 *            ʱ��εĿ�ʼ
	 * @param end
	 *            ʱ��εĽ���
	 * @return ���������Date��������֮���ʱ������* days * hours * minutes * seconds�ĸ�ʽչʾ
	 * @author fy.zhang
	 */
	public static String formatDuring(Date begin, Date end) {
		return formatDuring(end.getTime() - begin.getTime());
	}

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
	 * string����ת��Ϊlong���� strTimeҪת����String���͵�ʱ�� formatTypeʱ���ʽ
	 * strTime��ʱ���ʽ��formatType��ʱ���ʽ������ͬ
	 */
	public static long stringToLong(String strTime, String formatType) throws ParseException {
		Date date = stringToDate(strTime, formatType); // String����ת��date����
		if (date == null) {
			return 0;
		} else {
			long currentTime = dateToLong(date); // date����ת��long����
			return currentTime;
		}
	}

	/**
	 * string����ת��Ϊlong���� strTimeҪת����String���͵�ʱ�� formatTypeʱ���ʽ
	 * strTime��ʱ���ʽ��formatType��ʱ���ʽ������ͬ
	 */
	public static long stringToLong(String strTime) throws ParseException {
		Date date = stringToDate(strTime, "yyyy-MM-dd HH:mm:ss"); // String����ת��date����
		if (date == null) {
			return 0;
		} else {
			long currentTime = dateToLong(date); // date����ת��long����
			return currentTime;
		}
	}

	/**
	 * date����ת��ΪString���� formatType��ʽΪyyyy-MM-dd HH:mm:ss//yyyy��MM��dd�� HHʱmm��ss��
	 * data Date���͵�ʱ��
	 */
	public static String dateToString(Date data, String formatType) {
		return new SimpleDateFormat(formatType).format(data);
	}

	/**
	 * long����ת��ΪString���� currentTimeҪת����long���͵�ʱ�� formatTypeҪת����string���͵�ʱ���ʽ
	 */
	public static String longToString(long currentTime, String formatType) throws ParseException {
		Date date = longToDate(currentTime, formatType); // long����ת��Date����
		String strTime = dateToString(date, formatType); // date����ת��String
		return strTime;
	}

	/**
	 * long����ת��ΪString���� currentTimeҪת����long���͵�ʱ�� formatTypeҪת����string���͵�ʱ���ʽ
	 */
	public static String longToString(long currentTime) throws ParseException {
		Date date = longToDate(currentTime, "yyyy-MM-dd HH:mm:ss"); // long����ת��Date����
		String strTime = dateToString(date, "yyyy-MM-dd HH:mm:ss"); // date����ת��String
		return strTime;
	}

	/**
	 * string����ת��Ϊdate���� strTimeҪת����string���͵�ʱ�䣬formatTypeҪת���ĸ�ʽyyyy-MM-dd
	 * HH:mm:ss//yyyy��MM��dd�� HHʱmm��ss�룬 strTime��ʱ���ʽ����Ҫ��formatType��ʱ���ʽ��ͬ
	 */
	public static Date stringToDate(String strTime, String formatType) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat(formatType);
		Date date = null;
		date = formatter.parse(strTime);
		return date;
	}

	/**
	 * longת��ΪDate���� currentTimeҪת����long���͵�ʱ�� formatTypeҪת����ʱ���ʽyyyy-MM-dd
	 * HH:mm:ss//yyyy��MM��dd�� HHʱmm��ss��
	 */
	public static Date longToDate(long currentTime, String formatType) throws ParseException {
		Date dateOld = new Date(currentTime); // ����long���͵ĺ���������һ��date���͵�ʱ��
		String sDateTime = dateToString(dateOld, formatType); // ��date���͵�ʱ��ת��Ϊstring
		Date date = stringToDate(sDateTime, formatType); // ��String����ת��ΪDate����
		return date;
	}

	/**
	 * date����ת��Ϊlong���� dateҪת����date���͵�ʱ��
	 */
	public static long dateToLong(Date date) {
		return date.getTime();
	}
}
