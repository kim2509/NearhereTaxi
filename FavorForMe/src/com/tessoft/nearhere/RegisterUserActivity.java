package com.tessoft.nearhere;

import java.util.HashMap;

import org.codehaus.jackson.type.TypeReference;

import com.tessoft.common.Constants;
import com.tessoft.domain.APIResponse;
import com.tessoft.domain.User;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class RegisterUserActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		try
		{
			super.onCreate(savedInstanceState);

			supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

			setContentView(R.layout.activity_register_user);

			setProgressBarIndeterminateVisibility(true);
			User user = getLoginUser();
			sendHttp("/taxi/getRandomID.do", mapper.writeValueAsString(user), 2);

			//			Spinner spSex = (Spinner) findViewById(R.id.spSex);
			//			ArrayAdapter<CharSequence> sexAdapter = ArrayAdapter.createFromResource( this,
			//			        R.array.sex_list, android.R.layout.simple_spinner_item);
			//			sexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			//			spSex.setAdapter(sexAdapter);
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.register_user, menu);
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

	/*
	public void registerUser( View v )
	{
		try
		{
			EditText edtUserID = (EditText) findViewById(R.id.edtUserID);
			String userID = edtUserID.getText().toString();

			if ( TextUtils.isEmpty( userID ) )
			{
				edtUserID.setError("아이디를 입력해 주십시오.");
				return;
			}

			EditText edtUserName = (EditText) findViewById(R.id.edtUserName);
			String userName = edtUserName.getText().toString();

			if ( TextUtils.isEmpty( userName ) )
			{
				edtUserName.setError("이름을 입력해 주십시오.");
				return;
			}

			EditText edtPassword = (EditText) findViewById(R.id.edtPassword);
			String password = edtPassword.getText().toString();
			EditText edtRePassword = (EditText) findViewById(R.id.edtRePassword);
			String rePassword = edtRePassword.getText().toString();

			if ( TextUtils.isEmpty( password ) )
			{
				edtPassword.setError("비밀번호를 입력해 주십시오.");
				return;
			}

			if ( TextUtils.isEmpty( rePassword ) )
			{
				edtRePassword.setError("비밀번호를 입력해 주십시오.");
				return;
			}

			if ( !password.equals( rePassword ) )
			{
				edtRePassword.setError("비밀번호 재입력이 일치하지 않습니다.");
				return;
			}

			User user = new User();
			user.setUserID(userID);
			user.setUserName(userName);
			user.setPassword(password);

			setProgressBarIndeterminateVisibility(true);
			sendHttp("/taxi/registerUser.do", mapper.writeValueAsString(user), 1);
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
				setProgressBarIndeterminateVisibility(false);
				showOKDialog("통신중 오류가 발생했습니다.\r\n다시 시도해 주십시오.", null);
				return;
			}

			setProgressBarIndeterminateVisibility(false);

			super.doPostTransaction(requestCode, result);

			APIResponse response = mapper.readValue(result.toString(), new TypeReference<APIResponse>(){});

			if ( "0000".equals( response.getResCode() ) )
			{
				String temp = mapper.writeValueAsString( response.getData() );
				User user = mapper.readValue( temp, new TypeReference<User>(){});

				setMetaInfo("userNo", user.getUserNo());
				setMetaInfo("userID", user.getUserID());
				setMetaInfo("userName", user.getUserName());
				setMetaInfo("profileImageURL", user.getProfileImageURL());

				if ( requestCode == 1 )
				{
					goTermsAgreementActivity(null);	
				}
				else if ( requestCode == 2 )
				{
					Button btnRandomID = (Button) findViewById(R.id.btnRandomID);
					btnRandomID.setText("임시아이디 " + user.getUserID() + "로 시작하기");
					btnRandomID.setEnabled(true);
				}
			}
			else
			{
				showOKDialog("경고", response.getResMsg(), null);
				return;
			}
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	public void proceed( View v )
	{
		try
		{
			if ( "true".equals( getMetaInfoString("logout") ) )
			{
				setMetaInfo("logout", "false");
				goMainActivity();
				finish();
			}
			else
			{
				goTermsAgreementActivity(null);
			}
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}
	
	public void goTermsAgreementActivity( View v )
	{
		Intent intent = new Intent( this, TermsAgreementActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
		finish();
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

	boolean doubleBackToExitPressedOnce = false;
	@Override
	public void onBackPressed() {
		if (doubleBackToExitPressedOnce) {
			super.onBackPressed();
			return;
		}

		this.doubleBackToExitPressedOnce = true;
		Toast.makeText(this, "이전 버튼을 한번 더 누르면 종료합니다.", Toast.LENGTH_SHORT).show();

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				doubleBackToExitPressedOnce=false;                       
			}
		}, 2000);
	}
}
