package com.tessoft.common;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;
import android.util.Log;

public class Util {

	public static String getDistance( String dist )
	{
		if ( dist == null || "".equals( dist ) ) return "";

		if ( Double.parseDouble( dist ) < 1 )
			return (int) (Double.parseDouble( dist ) * 1000) + "m";
		else
			return Double.parseDouble(dist) + "km";
	}

	public static String getDistanceDouble( String dist )
	{
		if ( "전체".equals( dist ) || isEmptyString( dist )) return "";
		else if ( "500m".equals( dist ))
		{
			return "0.5";
		}
		else
			return dist.replaceAll("km", "");
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

	public static String getFormattedDateString( String origin, String fromFormat, String format ) throws Exception
	{
		Date d = getDateFromString(origin, fromFormat);
		return getDateStringFromDate(d, format);
	}

	public static String getFormattedDateString( String origin, String format ) throws Exception
	{
		Date d = getDateFromString(origin, "yyyy-MM-dd HH:mm:ss");
		return getDateStringFromDate(d, format);
	}

	public static String getNow( String format )
	{
		Date d = new Date();
		return getDateStringFromDate(d, format);
	}
	
	public static String getDepartureDateTime( String departureDate, String departureTime, String createdDateTime )
	{
		Date dCreatedDateTime = null;
		Date dDepartureDateTime = null;
		Date temp = null;
		Date now = new Date();
		
		String result = "";
		try
		{
			dCreatedDateTime = getDateFromString(createdDateTime, "yyyy-MM-dd HH:mm:ss");
			
			// 출발일자 설정
			if (departureDate.indexOf("오늘") >= 0)
				dDepartureDateTime = dCreatedDateTime;
			else
				dDepartureDateTime = getDateFromString(departureDate, "yyyy-MM-dd");
			
			if ( departureTime.indexOf( "지금" ) >= 0)
				temp = dCreatedDateTime;
			else
				temp = getDateFromString(departureTime, "HH:mm");
			
			// 출발시간 설정
			dDepartureDateTime.setHours(temp.getHours());
			dDepartureDateTime.setMinutes(temp.getMinutes());
			dDepartureDateTime.setSeconds(0);
			
			result = getDateStringFromDate(dDepartureDateTime,  "yyyy-MM-dd HH:mm");
			
			long diff = now.getTime() - dDepartureDateTime.getTime();
			long diffDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

			if ( diffDays > 0 )
			{
				if ( diffDays <= 5 )
					result = diffDays + " 일전 출발";
				else
					result = getDateStringFromDate(dDepartureDateTime,  "yyyy-MM-dd") + " 출발"; 
			}
			else if ( diffDays == 0 )
			{
				long diffHours = TimeUnit.HOURS.convert(diff, TimeUnit.MILLISECONDS);
				long diffMinutes = TimeUnit.MINUTES.convert(diff, TimeUnit.MILLISECONDS);
				
				diffMinutes -= diffHours * 60;
				
				if ( diffHours > 0 && diffMinutes < 10 )
					result = diffHours + " 시간전 출발예정";
				else if ( diffHours > 0 )
					result = diffHours + "시간 " + diffMinutes + "분전 출발예정";
				else if ( diffMinutes > 10 )
					result = diffMinutes + "분전 출발예정";
				else result = "곧 출발예정";
			}
			else if ( diffDays < 0 )
			{
				result = getDateStringFromDate(dDepartureDateTime,  "yyyy-MM-dd") + " 출발";
			}
		}
		catch( Exception ex )
		{
		}
		
		return result;
	}

	public static String getDongAddressString( Object fullAddress )
	{
		if ( fullAddress == null || "".equals( fullAddress )) return "";

		String[] tokens = fullAddress.toString().split("\\|");

		if ( tokens.length < 4 ) return "";

		return tokens[1] + " " + tokens[2] + " " + tokens[3];
	}

	public static boolean isEmptyString( String str )
	{
		if ( str == null ) return true;
		if ( "".equals(str.trim())) return true;
		return false;
	}

	public static double getDouble( String str )
	{
		if ( isEmptyString(str) ) return 0.0;

		return Double.parseDouble( str );
	}

	public static String getString( String str )
	{
		if ( isEmptyString(str) ) return "";
		return str;
	}

	public static int getInt( Object obj )
	{
		if ( obj == null || obj instanceof String == false ) return 0;

		if ( isEmptyString(obj.toString()) ) return 0;
		return Integer.parseInt(obj.toString());
	}

	public static String Decrypt(String text, String key) throws Exception
	{
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		byte[] keyBytes= new byte[16];
		byte[] b= key.getBytes("UTF-8");
		int len= b.length;
		if (len > keyBytes.length) len = keyBytes.length;
		System.arraycopy(b, 0, keyBytes, 0, len);
		SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
		cipher.init(Cipher.DECRYPT_MODE,keySpec,ivSpec);

		byte [] results = cipher.doFinal(Base64.decode(text, Base64.DEFAULT));
		return new String(results,"UTF-8");
	}

	public static String Encrypt(String text, String key) throws Exception
	{
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		byte[] keyBytes= new byte[16];
		byte[] b= key.getBytes("UTF-8");
		int len= b.length;
		if (len > keyBytes.length) len = keyBytes.length;
		System.arraycopy(b, 0, keyBytes, 0, len);
		SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
		cipher.init(Cipher.ENCRYPT_MODE,keySpec,ivSpec);

		byte[] results = cipher.doFinal(text.getBytes("UTF-8"));
		return Base64.encodeToString(results, Base64.DEFAULT);
	}
	
	public static String encodeBase64( String str )
	{
		return Base64.encodeToString( str.getBytes() , Base64.DEFAULT);
	}
	
	public static String decodeBase64( String str )
	{
		byte[] data = Base64.decode( str , Base64.DEFAULT);
		try {
			return new String( data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			return "";
		}
	}
}
