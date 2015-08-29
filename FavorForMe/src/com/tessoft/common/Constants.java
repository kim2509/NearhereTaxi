package com.tessoft.common;

public class Constants {

	public static boolean bReal = false;
	
	public static String protocol = "http://";
	
	public static String serverURL = bReal ? 
			"http://www.hereby.co.kr/nearhere" : "http://192.168.43.137:8080/nearhere";
	
	public static String serverSSLURL = bReal ? 
			"https://www.hereby.co.kr/nearhere" : "http://192.168.43.137:8080/nearhere";
	
	public static String thumbnailImageURL = bReal ? 
			"http://www.hereby.co.kr/thumbnail/" : "http://192.168.43.137/thumbnail/";
	
	public static String imageURL = bReal ? 
			"http://www.hereby.co.kr/image/" : "http://192.168.43.137/image/";
	
	public static String FAIL = "9999";
	
	public static boolean bPushOffOnNewPost = false;
	
	public static boolean bAdminMode = false;
	
	public static boolean bKakaoLogin = false;
	public static final int HTTP_UPDATE_LOCATION = 20;
	public static final int HTTP_APP_INFO = 1010;
	public static final int HTTP_LOGIN_BACKGROUND2 = 1020;
}
