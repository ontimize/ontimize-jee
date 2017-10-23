package com.ontimize.jee.server.rest;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import com.ontimize.util.ParseUtils;

public class ParseUtilsExt extends ParseUtils {

	protected final static Pattern ISO8601 = Pattern.compile(
	        "^([\\+-]?\\d{4}(?!\\d{2}\\b))((-?)((0[1-9]|1[0-2])(\\3([12]\\d|0[1-9]|3[01]))?|W([0-4]\\d|5[0-2])(-?[1-7])?|(00[1-9]|0[1-9]\\d|[12]\\d{2}|3([0-5]\\d|6[1-6])))([T\\s]((([01]\\d|2[0-3])((:?)[0-5]\\d)?|24\\:?00)([\\.,]\\d+(?!:))?)?(\\17[0-5]\\d([\\.,]\\d+)?)?([zZ]|([\\+-])([01]\\d|2[0-3]):?([0-5]\\d)?)?)?)?$");

	public static Object getValueForSQLType(Object object, int sqlType) {
		switch (sqlType) {
			case java.sql.Types.DATE:
			case java.sql.Types.TIME:
				return ParseUtilsExt.parseDate(object);
			case java.sql.Types.TIMESTAMP:
				return ParseUtilsExt.parseTimpestamp(object);
			default:
				return ParseUtils.getValueForSQLType(object, sqlType);
		}
	}

	public static Date parseDate(Object date) {
		if (date instanceof Long) {
			return new Date(((Long) date).longValue());
		} else if (date instanceof String) {
			String sDate = (String) date;
			if (ParseUtilsExt.ISO8601.matcher(sDate) != null) {
				Calendar calendar = DatatypeConverter.parseDate(sDate);
				return calendar.getTime();
			}
		} else if (date instanceof Date) {
			return (Date) date;
		}
		return null;
	}

	public static Timestamp parseTimpestamp(Object time) {
		if (time instanceof Long) {
			return new Timestamp((Long) time);
		} else if (time instanceof String) {
			String sTime = (String) time;
			Calendar calendar = DatatypeConverter.parseTime(sTime);
			return new Timestamp(calendar.getTimeInMillis());
		} else if (time instanceof Timestamp) {
			return (Timestamp) time;
		}
		return null;
	}
}
