package com.tessoft.common;

public class Constants {

	public static boolean bReal = true;
	public static String serverURL = bReal ? 
			"http://tessoft.synology.me:8080/nearhere" : "http://192.168.10.105:8080/nearhere";
	
	public static String imageServerURL = bReal ? 
			"http://tessoft.synology.me:8090/image/" : "http://192.168.10.105/image/";
	
	public static String FAIL = "9999";
}
