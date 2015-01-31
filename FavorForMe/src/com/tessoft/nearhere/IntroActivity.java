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
import com.tessoft.domain.APIResponse;
import com.tessoft.domain.Post;
import com.tessoft.domain.User;
import com.tessoft.nearhere.R;

public class IntroActivity extends BaseActivity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		
		try
		{
			super.onCreate(savedInstanceState);

			boolean isRunIntro = getIntent().getBooleanExtra("intro", true);
			if(isRunIntro) {
				beforeIntro();
			} else {
				afterIntro(savedInstanceState);
			}
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
			Log.e("error", ex.getMessage());
		}
		
	}

	// 인트로 화면 
	private void beforeIntro() {
		// 약 2초간 인트로 화면을 출력.
		getWindow().getDecorView().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent intent = null;
				
				if ( "".equals( getMetaInfoString("userID") ) || !"true".equals( getMetaInfoString("registerUserFinished")) )
				{
//					intent = new Intent( getApplicationContext(), IntroActivity.class);
//					intent.putExtra("intro", false);
					intent = new Intent( getApplicationContext(), RegisterUserActivity.class);
				}
				else
				{
					intent = new Intent( getApplicationContext(), MainActivity.class);
				}
				
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
				// 액티비티 이동시 페이드인/아웃 효과를 보여준다. 즉, 인트로
				//    화면에 부드럽게 사라진다.
				overridePendingTransition(android.R.anim.fade_in, 
						android.R.anim.fade_out);
			}
		}, 1000);
	}

	// 인트로 화면 이후.
	private void afterIntro(Bundle savedInstanceState) {
		// 기본 테마를 지정한다.

		setTheme(R.style.AppBaseTheme);

		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		getActionBar().hide();

		setContentView(R.layout.activity_intro);

		getWindow().setBackgroundDrawableResource(R.drawable.intro);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.intro, menu);
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
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
			
			if ( requestCode == 1 )
			{
				APIResponse response = mapper.readValue(result.toString(), new TypeReference<APIResponse>(){});
				
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
