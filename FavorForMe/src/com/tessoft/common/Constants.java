package com.tessoft.common;

public class Constants {

	public static boolean bReal = false;
	
	public static String protocol = "http://";
	
	public static String serverURL = bReal ? 
			"http://www.hereby.co.kr/nearhere" : "http://tessoft.synology.me:8080/nearhere";
	
	public static String serverSSLURL = bReal ? 
			"https://www.hereby.co.kr/nearhere" : "https://tessoft.synology.me:8080/nearhere";
	
	public static String imageServerURL = bReal ? 
			"http://www.hereby.co.kr/image/" : "http://tessoft.synology.me:8090/image/";
	
	public static String FAIL = "9999";
}
