package com.tessoft.common;

public class Util {

	public static String getDistance( String dist )
	{
		if ( dist == null || "".equals( dist ) ) return "";
		
		if ( Double.parseDouble( dist ) < 1 )
			return (int) (Double.parseDouble( dist ) * 1000) + "m";
		else
			return Double.parseDouble(dist) + "km";
	}
}
