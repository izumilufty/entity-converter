package jp.lufty.util.entity;

import java.math.BigDecimal;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * いろいろな型をパースするユーティリティクラス。
 * 
 * @author izumi
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ParseUtils {
    
	/**
	 * 長整数のパース
	 * 
	 * @param data
	 *            長整数を表すデータ
	 * @return パースされた長整数
	 */
	public static Long parseLong(Object data) {

		Long result = null;

		if (data instanceof String) {
			result = Long.parseLong((String) data);
		} else if (data instanceof Integer) {
			result = ((Integer) data).longValue();
		} else {
			result = (Long) data;
		}

		return result;
	}

	/**
	 * 整数のパース
	 * 
	 * @param data
	 *            整数を表すデータ
	 * @return パースされた整数
	 */
	public static Integer parseInteger(Object data) {

		Integer result = null;

		if (data instanceof String) {
			result = Integer.parseInt((String) data);
		} else {
			result = (Integer) data;
		}

		return result;
	}

	public static BigDecimal parseBigDecimal(Object data) {

		BigDecimal result = null;

		if (data instanceof String) {
			result = new BigDecimal((String) data);
		} else if (data instanceof Integer) {
			result = new BigDecimal((Integer) data);
		} else if (data instanceof Long) {
			result = new BigDecimal((Long) data);
		} else if (data instanceof Float) {
			// result = new BigDecimal((Float) data);
			// } else if (data instanceof Double) {
			// result = new BigDecimal((Double) data);
		} else {
			result = (BigDecimal) data;
		}

		return result;
	}

	/**
	 * 浮動小数点型のパース
	 * 
	 * @param data
	 *            浮動小数点型を表すデータ
	 * @return パースされた浮動小数点型の値
	 */
	public static Float parseFloat(Object data) {

		Float result = null;

		if (data instanceof String) {
			result = Float.parseFloat((String) data);
		} else if (data instanceof Integer) {
			result = ((Integer) data).floatValue();
		} else if (data instanceof Long) {
			result = ((Long) data).floatValue();
		} else if (data instanceof Double) {
			result = ((Double) data).floatValue();
		} else {
			result = (Float) data;
		}

		return result;
	}

	/**
	 * 浮動小数点型のパース
	 * 
	 * @param data
	 *            浮動小数点型を表すデータ
	 * @return パースされた浮動小数点型の値
	 */
	public static Double parseDouble(Object data) {

		Double result = null;

		if (data instanceof String) {
			result = Double.parseDouble((String) data);
		} else if (data instanceof Integer) {
			result = (double) ((Integer) data).intValue();
		} else if (data instanceof Long) {
			result = ((Long) data).doubleValue();
		} else if (data instanceof Float) {
			result = ((Float) data).doubleValue();
		} else {
			result = (Double) data;
		}

		return result;
	}

	/**
	 * 真偽のパース
	 * 
	 * @param data
	 *            真偽を表すデータ
	 * @return パースされた真偽
	 */
	public static Boolean parseBoolean(Object data) {

		Boolean result = null;

		if (data instanceof String) {
			result = Boolean.valueOf((String) data);
		} else {
			result = (Boolean) data;
		}

		return result;
	}

}
