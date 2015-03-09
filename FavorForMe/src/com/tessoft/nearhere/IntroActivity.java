package com.tessoft.nearhere;

import java.util.HashMap;

import org.codehaus.jackson.type.TypeReference;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		
		try
		{
			super.onCreate(savedInstanceState);

			// 약 2초간 인트로 화면을 출력.
			getWindow().getDecorView().postDelayed(new Runnable() {
				@Override
				public void run() {
					
					try
					{

						if ( "".equals( getLoginUser().getUserID() ) || !"true".equals( getMetaInfoString("registerUserFinished")) 
								|| "true".equals( getMetaInfoString("logout")) )
						{
							Intent intent = null;
							intent = new Intent( getApplicationContext(), RegisterUserActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
							// 액티비티 이동시 페이드인/아웃 효과를 보여준다. 즉, 인트로
							//    화면에 부드럽게 사라진다.
							overridePendingTransition(android.R.anim.fade_in, 
									android.R.anim.fade_out);
						}
						else
						{
							HashMap request = getDefaultRequest();
							request.put("user", getLoginUser());
							sendHttp("/taxi/getRandomIDV2.do", mapper.writeValueAsString(request), HTTP_LOGIN_BACKGROUND);
						}
						
					}
					catch( Exception ex )
					{
						catchException(this, ex);
					}
				}
			}, 1000);
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
			Log.e("error", ex.getMessage());
		}
		
	}
	
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
				setMetaInfo("loginUserInfo", Util.encodeBase64( mapper.writeValueAsString( response.getData() ) ));
				Intent intent = new Intent( getApplicationContext(), MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		}
		catch( Exception ex )
		{
			catchException(this, ex);
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
}
