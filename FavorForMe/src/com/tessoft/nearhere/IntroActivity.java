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
import com.kakao.Session;
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
			
			// 카카오톡 세션을 초기화 한다.
			Session.initialize(this);
						
			HashMap hash = application.getDefaultRequest();
			hash.put("os", "Android");
			sendHttp("/app/appInfo.do", mapper.writeValueAsString( hash ), HTTP_APP_INFO );
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
			Log.e("error", ex.getMessage());
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
			application.debug(this, "doPostTransaction[" + requestCode + "]:" + result );
			
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
					application.setMetaInfo("userID", user.getUserID() );
					application.setMetaInfo("userName", user.getUserName() );
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
				
				application.setLoginUser(user);
				application.setMetaInfo("logout", "false");
				
				Intent intent = new Intent( getApplicationContext(), MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
			else if ( requestCode == Constants.HTTP_LOGIN_BACKGROUND2 )
			{
				String userString = mapper.writeValueAsString( response.getData() );
				User user = mapper.readValue( userString, new TypeReference<User>(){});
				
				HashMap addInfo = (HashMap) response.getData2();
				
				if ( "Y".equals( addInfo.get("registerUserFinished") ) )
				{
					application.setLoginUser(user);
					application.setMetaInfo("logout", "false");
					
					Intent intent = new Intent( getApplicationContext(), MainActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					finish();
				}
				else
					goKaKaoLoginActivity();
			}
			else if ( requestCode == HTTP_APP_INFO )
			{
				String appInfoString = mapper.writeValueAsString( response.getData() );
				HashMap appInfo = mapper.readValue( appInfoString, new TypeReference<HashMap>(){});
				
				if ( appInfo != null && appInfo.containsKey("version") && appInfo.containsKey("forceUpdate") )
				{
					if ( !application.getPackageVersion().equals( appInfo.get("version") ) )
					{
						if ("Y".equals( appInfo.get("forceUpdate") ) )
							showOKDialog("알림","이근처 합승이 업데이트 되었습니다.\r\n확인을 누르시면 업데이트 화면으로 이동합니다." , UPDATE_NOTICE );
						else
							showYesNoDialog("알림", "이근처 합승이 업데이트 되었습니다.\r\n지금 업데이트 하시겠습니까?", UPDATE_NOTICE );
						
						return;
					}					
				}
				
				login();
			}
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}
	
	private void login() throws Exception{
		
//		User u = new User();
//		u.setUserID("user27");
//		u.setUserNo("27");
//		application.setLoginUser(u);

//		checkIfAdminUser();
		
		Log.d("debug", "login");
		Log.d("debug", "login user : " + mapper.writeValueAsString( application.getLoginUser() ) );
		
		if ( "true".equals( application.getMetaInfoString("logout") ) || 
				"".equals( application.getLoginUser().getUserID() ) || !Util.isEmptyString( application.getLoginUser().getKakaoID() ) )
			Constants.bKakaoLogin = true;
		else
		{
			Constants.bKakaoLogin = false;
		}
		
		Log.d("debug", "bKakaoLogin : " + Constants.bKakaoLogin );
			
		if ( Util.isEmptyString( application.getLoginUser().getKakaoID() ) && Constants.bKakaoLogin )
		{
			// 회원가입
			goKaKaoLoginActivity();
		}
		else if ( Constants.bKakaoLogin )
		{
			// 로그아웃 했을 경우 다시 로그인 하기 위함
			HashMap request = application.getDefaultRequest();
			request.put("userID", application.getLoginUser().getUserID());
			request.put("hash", application.getMetaInfoString("hash") );
			
			Log.d("debug", "login_bg2 : " + mapper.writeValueAsString( request ) );
			
			sendHttp("/taxi/login_bg2.do", mapper.writeValueAsString(request), Constants.HTTP_LOGIN_BACKGROUND2 );
		}
		else
		{
			// 로그아웃 했을 경우 다시 로그인 하기 위함
			HashMap request = application.getDefaultRequest();
			request.put("user", application.getLoginUser());

			Log.d("debug", "login_bg : " + mapper.writeValueAsString( request ) );

			sendHttp("/taxi/login_bg.do", mapper.writeValueAsString(request), HTTP_LOGIN_BACKGROUND );
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
		    
		    User user = application.getLoginUser();
		    user.setUserNo(userNo);
		    user.setUserID(userID);
		    application.setLoginUser(user);
		    
		    application.setMetaInfo("registerUserFinished", "true");
		    application.setMetaInfo("logout", "false");
		}
		catch( Exception ex )
		{
			
		}
	}
}
