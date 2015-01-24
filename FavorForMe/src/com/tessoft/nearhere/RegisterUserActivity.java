package com.tessoft.nearhere;

import java.util.HashMap;

import org.codehaus.jackson.type.TypeReference;

import com.tessoft.domain.APIResponse;
import com.tessoft.domain.User;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

public class RegisterUserActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		try
		{
			super.onCreate(savedInstanceState);
			
			supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
			
			setContentView(R.layout.activity_register_user);	
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
		return true;
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
	
	@Override
	public void doPostTransaction(int requestCode, Object result) {
		// TODO Auto-generated method stub
		try
		{
			setProgressBarIndeterminateVisibility(false);
			
			super.doPostTransaction(requestCode, result);
			
			APIResponse response = mapper.readValue(result.toString(), new TypeReference<APIResponse>(){});
			
			if ( "0000".equals( response.getResCode() ) )
			{
				String temp = mapper.writeValueAsString( response.getData() );
				User user = mapper.readValue( temp, new TypeReference<User>(){});
				
				setMetaInfo("userID", user.getUserID());
				setMetaInfo("userName", user.getUserName());
				setMetaInfo("profileImageURL", user.getProfileImageURL());
				
				goTermsAgreementActivity(null);
			}
			else
			{
				showOKDialog( "경고", response.getResMsg(), null);
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
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		finish();
	}
}
