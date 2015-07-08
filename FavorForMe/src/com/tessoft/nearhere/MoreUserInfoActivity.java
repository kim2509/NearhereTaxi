package com.tessoft.nearhere;

import org.apache.http.util.TextUtils;
import org.codehaus.jackson.type.TypeReference;

import com.tessoft.domain.APIResponse;
import com.tessoft.domain.User;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class MoreUserInfoActivity extends BaseActivity {

	private static final int UPDATE_USER_INFO = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		try
		{
			super.onCreate(savedInstanceState);
			
			supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
			
			setContentView(R.layout.activity_more_user_info);			
			
			Spinner spSex = (Spinner) findViewById(R.id.spSex);
			ArrayAdapter<CharSequence> sexAdapter = ArrayAdapter.createFromResource( this,
			        R.array.sex_list, android.R.layout.simple_spinner_item);
			sexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spSex.setAdapter(sexAdapter);
			
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.more_user_info, menu);
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
	
	public void start(View v )
	{
		try
		{
			Spinner spSex = (Spinner) findViewById(R.id.spSex);
			
			if ( "미지정".equals( spSex.getSelectedItem() ) )
			{
				showOKDialog("확인", "성별을 선택해 주십시오.", null);
				return;
			}
			
			EditText edtUserName = (EditText) findViewById(R.id.edtUserName);
			if ( TextUtils.isEmpty( edtUserName.getText() ) )
			{
				edtUserName.setError("이름을 입력해 주십시오.");
				return;
			}
			
			User user = getLoginUser();
			
			if ( "남".equals( spSex.getSelectedItem() ))
				user.setSex("M");
			else if ( "여".equals( spSex.getSelectedItem() ))
				user.setSex("F");
			
			user.setUserName( edtUserName.getText().toString() );
			
			setProgressBarIndeterminateVisibility(true);
			sendHttp("/taxi/updateUserInfo.do", mapper.writeValueAsString( user ), UPDATE_USER_INFO );
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
				application.setMetaInfo("logout", "false");
				application.setMetaInfo("registerUserFinished", "true");
				
				goTaxiTutorialActivity();
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
	
	public void goTaxiTutorialActivity()
	{
		Intent intent = new Intent( this, TaxiTutorialActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
		super.finish();
	}
	
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent intent = new Intent( getApplicationContext(), TermsAgreementActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
}
