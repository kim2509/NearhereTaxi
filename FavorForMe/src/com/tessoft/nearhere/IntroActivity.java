package com.tessoft.nearhere;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import org.codehaus.jackson.type.TypeReference;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.tessoft.common.Constants;
import com.tessoft.common.Util;
import com.tessoft.domain.APIResponse;
import com.tessoft.domain.Post;
import com.tessoft.domain.User;
import com.tessoft.nearhere.R;

public class IntroActivity extends BaseActivity {

	private static final int HTTP_LOGIN_BACKGROUND = 2;
	private static final int HTTP_APP_INFO = 3;
	private static final String UPDATE_NOTICE = "UPDATE_NOTICE";
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		
		try
		{
			super.onCreate(savedInstanceState);
			
			HashMap hash = getDefaultRequest();
			hash.put("os", "Android");
			sendHttp("/app/appInfo.do", mapper.writeValueAsString( hash ), HTTP_APP_INFO );
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
			Log.e("error", ex.getMessage());
		}
		
	}

	private void login( HashMap appInfo ) throws Exception{

		if ( Constants.bKakaoLogin == false )
			checkIfAdminUser();
		
		if ( Constants.bKakaoLogin )
		{
			Intent intent = new Intent( getApplicationContext(), KakaoLoginActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
			overridePendingTransition(android.R.anim.fade_in, 
					android.R.anim.fade_out);
		}
		else if ( getLoginUser() == null || "".equals( getLoginUser().getUserID() ) || !"true".equals( getMetaInfoString("registerUserFinished")) 
				|| "true".equals( getMetaInfoString("logout")) )
		{
			Intent intent = null;
			intent = new Intent( getApplicationContext(), RegisterUserActivity.class);
			
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
			overridePendingTransition(android.R.anim.fade_in, 
					android.R.anim.fade_out);
		}
		else
		{
			HashMap request = getDefaultRequest();
			request.put("user", getLoginUser());
			sendHttp("/taxi/login_bg.do", mapper.writeValueAsString(request), HTTP_LOGIN_BACKGROUND);
		}

	}
	
	/*
	public void login( View v )
	{
		try
		{
			EditText edtUserID = (EditText) findViewById(R.id.edtUserID);
			EditText edtPassword = (EditText) findViewById(R.id.edtPassword);
			
			if ( TextUtils.isEmpty( edtUserID.getText() ) )
			{
				edtUserID.setError("아이디를 입력해 주십시오.");
				return;
			}
			
			if ( TextUtils.isEmpty( edtPassword.getText() ) )
			{
				edtPassword.setError("비밀번호를 입력해 주십시오.");
				return;
			}
			
			edtUserID.setEnabled(false);
			edtPassword.setEnabled(false);
			findViewById(R.id.btnRegisterUser).setEnabled(false);
			findViewById(R.id.btnLogin).setEnabled(false);
			
			HashMap hash = new HashMap();
			hash.put("userID", edtUserID.getText().toString() );
			hash.put("password", edtPassword.getText().toString() );
			sendHttp("/taxi/login.do", mapper.writeValueAsString( hash ), 1);
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}
	*/

	@Override
	public void doPostTransaction(int requestCode, Object result) {
		// TODO Auto-generated method stub
		try
		{
			if ( Constants.FAIL.equals(result) )
			{
				showOKDialog("통신중 오류가 발생했습니다.\r\n다시 시도해 주십시오.", null);
				return;
			}
			
			super.doPostTransaction(requestCode, result);
			
			APIResponse response = mapper.readValue(result.toString(), new TypeReference<APIResponse>(){});
			
			if ( requestCode == 1 )
			{
				if ( "0000".equals( response.getResCode() ) )
				{
					String postData = mapper.writeValueAsString( response.getData() );
					User user = mapper.readValue( postData, new TypeReference<User>(){});
					setMetaInfo("userID", user.getUserID() );
					setMetaInfo("userName", user.getUserName() );
					goMainActivity();
					finish();
				}
				else
				{
					findViewById(R.id.edtUserID).setEnabled(true);
					findViewById(R.id.edtPassword).setEnabled(true);
					findViewById(R.id.btnRegisterUser).setEnabled(true);
					findViewById(R.id.btnLogin).setEnabled(true);
					
					showOKDialog("오류", response.getResMsg(), null);
					return;
				}
			}
			else if ( requestCode == HTTP_LOGIN_BACKGROUND )
			{
				String userString = mapper.writeValueAsString( response.getData() );
				User user = mapper.readValue( userString, new TypeReference<User>(){});
				setLoginUser(user);
				Intent intent = new Intent( getApplicationContext(), MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
			else if ( requestCode == HTTP_APP_INFO )
			{
				String appInfoString = mapper.writeValueAsString( response.getData() );
				HashMap appInfo = mapper.readValue( appInfoString, new TypeReference<HashMap>(){});
				
				if ( appInfo == null || !appInfo.containsKey("version") || !appInfo.containsKey("forceUpdate") ) return;
				
				if ( !getPackageVersion().equals( appInfo.get("version") ) )
				{
					if ("Y".equals( appInfo.get("forceUpdate") ) )
						showOKDialog("알림","이근처 합승이 업데이트 되었습니다.\r\n확인을 누르시면 업데이트 화면으로 이동합니다." , UPDATE_NOTICE );
					else
						showYesNoDialog("알림", "이근처 합승이 업데이트 되었습니다.\r\n지금 업데이트 하시겠습니까?", UPDATE_NOTICE );
					
					return;
				}
				
				if ( "Y".equals( appInfo.get("kakaoYN") ) )
					Constants.bKakaoLogin = true;
				else
					Constants.bKakaoLogin = false;
				
				login( appInfo );
			}
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}
	
	@Override
	public void okClicked(Object param) {
		// TODO Auto-generated method stub
		super.okClicked(param);
		
		if ( UPDATE_NOTICE.equals( param ) )
		{
			goUpdate();
		}
	}

	private void goUpdate() {
		final String appPackageName = getPackageName();
		try {
		    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
		} catch (android.content.ActivityNotFoundException anfe) {
		    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
		}
	}
	
	@Override
	public void yesClicked(Object param) {
		// TODO Auto-generated method stub
		super.yesClicked(param);
		
		if ( UPDATE_NOTICE.equals( param ) )
		{
			goUpdate();
			finish();
		}
	}
	
	@Override
	public void noClicked(Object param) {
		// TODO Auto-generated method stub
		super.noClicked(param);
		
		if ( UPDATE_NOTICE.equals( param ) )
		{
			finish();
		}
	}
	
	public void goMainActivity()
	{
		try
		{
			Intent intent = new Intent( this, MainActivity.class);
			startActivity(intent);
			overridePendingTransition(android.R.anim.fade_in, 
					android.R.anim.fade_out);
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	public void goRegisterActivity( View v )
	{
		Intent intent = new Intent( this, RegisterUserActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
	}

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i("intro", "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}
	
	public void checkIfAdminUser()
	{
		try
		{
			File sdcard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

			//Get the text file
			File file = new File(sdcard,"nearhere.txt");
			
			if ( !file.exists() ) return;

			//Read text from file
			StringBuilder text = new StringBuilder();

			BufferedReader br = new BufferedReader(new FileReader(file));
		    String line;

		    while ((line = br.readLine()) != null) {
		        text.append(line);
		        text.append('\n');
		    }
		    br.close();

		    String loginInfo = text.toString();
		    
		    if ( Util.isEmptyString( loginInfo ) ) return;
		    
		    String[] tokens = loginInfo.split("\\;");

		    String userNo = "";
		    String userID = "";
		    String pw = "";
		    String pushOffOnNewPost = "";
		    
		    for ( int i = 0; i < tokens.length; i++ )
		    {
		    	String key = tokens[i].split("\\=")[0];
		    	String value = tokens[i].split("\\=")[1];
		    	if ( "userNo".equals( key ) )
		    		userNo = value;
		    	else if ( "userID".equals( key ) )
		    		userID = value;
		    	else if ( "pw".equals( key ) )
		    		pw = value;
		    	else if ( "pushOffOnNewPost".equals( key ) )
		    		pushOffOnNewPost = value;
		    }
		    
		    if (!"이근처합승".equals(pw.trim())) 
		    {
		    	Constants.bAdminMode = false;
		    	return;
		    }
		    
		    if ( "Y".equals( pushOffOnNewPost.trim() ) ) Constants.bPushOffOnNewPost = true;
		    else Constants.bPushOffOnNewPost = false;
		    
		    Constants.bAdminMode = true;
		    
		    User user = getLoginUser();
		    user.setUserNo(userNo);
		    user.setUserID(userID);
		    setLoginUser(user);
		    
		    setMetaInfo("registerUserFinished", "true");
		    setMetaInfo("logout", "false");
		}
		catch( Exception ex )
		{
			
		}
	}
}
