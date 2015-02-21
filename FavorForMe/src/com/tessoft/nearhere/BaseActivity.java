package com.tessoft.nearhere;

import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

import org.codehaus.jackson.map.ObjectMapper;

import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.tessoft.common.AdapterDelegate;
import com.tessoft.common.HttpTransactionReturningString;
import com.tessoft.common.TransactionDelegate;
import com.tessoft.domain.User;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class BaseActivity extends ActionBarActivity implements TransactionDelegate, AdapterDelegate {

	ObjectMapper mapper = new ObjectMapper();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initImageLoader();
	}

	public String getOSVersion()
	{
		return Build.VERSION.RELEASE;
	}
	
	public String getPackageVersion()
	{
		PackageInfo pInfo;
		try {
			
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			
			return pInfo.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "";
	}
	
	public String getUniqueDeviceID()
	{
		final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

	    final String tmDevice, tmSerial, androidId;
	    tmDevice = "" + tm.getDeviceId();
	    tmSerial = "" + tm.getSimSerialNumber();
	    androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

	    UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
	    return deviceUuid.toString();
	}
	
	public HashMap getDefaultRequest()
	{
		HashMap request = new HashMap();
		request.put("OSVersion", getOSVersion());
		request.put("AppVersion", getPackageVersion());
		request.put("UUID", getUniqueDeviceID());
		return request;
	}
	
	public void showOKDialog( String message, final Object param )
	{
		showOKDialog("확인", message, param);
	}
	
	public void showOKDialog( String title, String message, final Object param )
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setTitle( title )
			   .setMessage( message )
		       .setCancelable(false)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                okClicked( param );
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	public void okClicked( Object param )
	{
		
	}
	
	public void showYesNoDialog( String title, String message, final Object param )
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title)
			   .setMessage( message )
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   yesClicked( param );
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   noClicked( param );
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	public void yesClicked( Object param )
	{
		
	}
	
	public void noClicked( Object param )
	{
		
	}
	
	public void setMetaInfo( String key, String value )
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( this );
		SharedPreferences.Editor editor = settings.edit();
		editor.putString( key, value );
		editor.commit();
	}
	
	public String getMetaInfoString( String key )
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( this );
		if ( settings.contains(key) )
			return settings.getString(key, "");
		else return "";
	}
	
	public double getMetaInfoDouble( String key )
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( this );
		if ( settings.contains(key) )
			return Double.parseDouble( settings.getString(key, "0") );
		else
			return 0;
	}
	
	public void showToastMessage( String message )
	{
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
	
	public void writeLog( String log )
	{
		Log.e("NearHereHelp", log );
	}
	
	public void sendHttp( String url, Object request, int requestCode )
	{
		new HttpTransactionReturningString( this, url, requestCode ).execute( request );
	}
	
	public void doPostTransaction( int requestCode, Object result )
	{
		
	}
	
	public void catchException ( Object target, Exception ex )
	{
		if ( ex == null )
			writeLog( "[" + target.getClass().getName() + "] NullPointerException!!!" );
		else
			Log.e("이근처", "exception", ex);
	}
	
	public User getLoginUser()
	{
		User user = new User();
		user.setUserNo( getMetaInfoString("userNo") );
		user.setUserID( getMetaInfoString("userID") );
		user.setUserName( getMetaInfoString("userName") );
		user.setProfileImageURL( getMetaInfoString("profileImageURL"));
		user.setUuid( getUniqueDeviceID() );
		return user;
	}
	
	@Override
	public void doAction(String actionName, Object param) {
		// TODO Auto-generated method stub
		if ("logException".equals( actionName ) )
			Log.e("이근처", "exception", (Exception) param);
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		MainActivity.active = true;
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		MainActivity.active = false;
	}
	
	public static boolean bInitImageLoader = false;
	public void initImageLoader()
	{
		if ( BaseActivity.bInitImageLoader == false )
		{
			ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
			.memoryCacheExtraOptions(100, 100) // default = device screen dimensions
			.diskCacheExtraOptions(100, 100, null)
			.threadPoolSize(3) // default
			.threadPriority(Thread.NORM_PRIORITY - 2) // default
			.tasksProcessingOrder(QueueProcessingType.FIFO) // default
			.denyCacheImageMultipleSizesInMemory()
			.memoryCache(new LruMemoryCache(2 * 1024 * 1024))
			.memoryCacheSize(2 * 1024 * 1024)
			.memoryCacheSizePercentage(13) // default
			.diskCacheSize(50 * 1024 * 1024)
			.diskCacheFileCount(100)
			.diskCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
			.defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
			.writeDebugLogs()
			.build();
			ImageLoader.getInstance().init(config);
			BaseActivity.bInitImageLoader = true;
		}
	}
}
