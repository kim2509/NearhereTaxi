package com.tessoft.common;

public class Constants {

	public static boolean bReal = false;
	public static String serverURL = bReal ? 
			"http://tessoft.synology.me/randomMsgServer" : "http://192.168.10.107:8080/randomMsgServer";
	
	public static String imageServerURL = bReal ? 
			"http://tessoft.synology.me/randomMsgServer" : "http://192.168.10.107/image/";
	
	public static String FAIL = "9999";
}
