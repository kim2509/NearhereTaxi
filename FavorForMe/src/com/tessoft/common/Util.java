package com.tessoft.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {

	public static String getDistance( String dist )
	{
		if ( dist == null || "".equals( dist ) ) return "";
		
		if ( Double.parseDouble( dist ) < 1 )
			return (int) (Double.parseDouble( dist ) * 1000) + "m";
		else
			return Double.parseDouble(dist) + "km";
	}
	
	public static Date getDateFromString( String dateString, String format ) throws Exception
	{
		if ( dateString == null || "".equals( dateString ) ) return null;
		
		DateFormat sdf = new SimpleDateFormat( format );
		Date date = sdf.parse(dateString);
		return date;
	}
	
	public static String getDateStringFromDate( Date date, String format )
	{
		if ( date == null ) return "";
		
		DateFormat sdf = new SimpleDateFormat(format);
		String tempDate = sdf.format(date);
		return tempDate;
	}
	
	public static String getFormattedDateString( String origin, String format ) throws Exception
	{
		Date d = getDateFromString(origin, "yyyy-MM-dd hh:mm:ss");
		return getDateStringFromDate(d, format);
	}
}
