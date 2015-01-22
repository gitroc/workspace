package cn.com.aa.common.android.utils;
/**
 * 金额转换工具
 * @author Rex
 *
 */

import java.math.BigDecimal;


/**
 * com.util.AmountUtils
 * @description  金额元分之间转换工具类
 */
public class AmountUtils {
	
	/**金额的格式 */
	public static final String CURRENCY_FEN_REGEX = "\\-?[0-9]+";
	
	/**
	 * 将千为单位的转换为万 （除10000）
	 * 
	 * @param amount
	 * @return
	 * @throws Exception 
	 */
	public static String changeQ2W(String amount) throws Exception{
		if(!amount.matches(CURRENCY_FEN_REGEX)) {
			throw new Exception("金额格式有误");
		}
		return BigDecimal.valueOf(Long.valueOf(amount)).divide(new BigDecimal(10000)).toString();
	}
	
	/**
	 * 将百为单位的转换为万 （除10000）
	 * 
	 * @param amount
	 * @return
	 * @throws Exception 
	 */
	public static String changeB2W(String amount) throws Exception{
		if(!amount.matches(CURRENCY_FEN_REGEX)) {
			throw new Exception("金额格式有误");
		}
		return BigDecimal.valueOf(Long.valueOf(amount)).divide(new BigDecimal(10000)).toString();
	}
	
}


