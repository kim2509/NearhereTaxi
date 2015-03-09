package com.tessoft.common;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

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
