package com.tessoft.nearhere;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.tessoft.nearhere.R;

public class IntroActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		try
		{
			super.onCreate(savedInstanceState);
			
			setContentView(R.layout.activity_intro);
			
			EditText edtUserID = (EditText) findViewById(R.id.edtUserID);
			edtUserID.setText( getMetaInfoString("userID"));
			
			checkPlayServices();
		}
		catch( Exception ex )
		{
			
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.intro, menu);
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
	
	public void goMainActivity( View v )
	{
		try
		{
			setMetaInfo("registrationID", "");
			
			EditText edtUserID = (EditText) findViewById(R.id.edtUserID);
			setMetaInfo("userID", edtUserID.getText().toString());
			EditText edtUserName = (EditText) findViewById(R.id.edtUserName);
			setMetaInfo("userName", edtUserName.getText().toString());
			
			Intent intent = new Intent( this, MainActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		}
		catch( Exception ex )
		{
			
		}
	}
	
	public void goRegisterActivity( View v )
	{
		Intent intent = new Intent( this, RegisterUserActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		finish();
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
