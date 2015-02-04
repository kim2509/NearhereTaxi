package com.tessoft.nearhere;

import java.util.HashMap;

import org.codehaus.jackson.type.TypeReference;

import com.tessoft.common.Constants;
import com.tessoft.domain.APIResponse;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class TermsAgreementActivity extends BaseActivity {

	String commonTermsContent = "";
	String locationTermsContent = "";
	String commonTermsVersion = "";
	String locationTermsVersion = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		try
		{
			super.onCreate(savedInstanceState);
			
			supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
			
			setContentView(R.layout.activity_terms_agreement);	
			
			setProgressBarIndeterminateVisibility(true);
			sendHttp("/taxi/getTermsContent.do", null, 1);
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.terms_agreement, menu);
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
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void updateTermsAgreement( View v )
	{
		try
		{
			setProgressBarIndeterminateVisibility(true);
			
			HashMap hash = new HashMap();
			hash.put("userID", getMetaInfoString("userID"));
			hash.put("common_ver", commonTermsVersion);
			hash.put("common", "Y");
			hash.put("location_ver", locationTermsVersion);
			hash.put("location", "Y");
			sendHttp("/taxi/insertTermsAgreement.do", mapper.writeValueAsString(hash), 2);	
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
				setProgressBarIndeterminateVisibility(false);
				showOKDialog("통신중 오류가 발생했습니다.\r\n다시 시도해 주십시오.", null);
				return;
			}
			
			setProgressBarIndeterminateVisibility(false);
			
			if ( requestCode == 1 )
			{
				super.doPostTransaction(requestCode, result);
				
				APIResponse response = mapper.readValue(result.toString(), new TypeReference<APIResponse>(){});
				
				HashMap hash = (HashMap) response.getData();
				
				commonTermsVersion = (hash.get("commonTermsVersion") == null ) ? "" : hash.get("commonTermsVersion").toString();
				commonTermsContent = (hash.get("COMMON") == null ) ? "" : hash.get("COMMON").toString();
				locationTermsVersion = (hash.get("locationTermsVersion") == null ) ? "" : hash.get("locationTermsVersion").toString();
				locationTermsContent = (hash.get("LOCATION") == null ) ? "" : hash.get("LOCATION").toString();
				
				TextView txtCommonTerms = (TextView) findViewById(R.id.txtCommonTerms);
				txtCommonTerms.setText( commonTermsContent );
				TextView txtLocationTerms = (TextView) findViewById(R.id.txtLocationTerms);
				txtLocationTerms.setText( locationTermsContent );
			}
			else
			{
				APIResponse response = mapper.readValue(result.toString(), new TypeReference<APIResponse>(){});
				
				if ( !"0000".equals( response.getResCode() ) )
				{
					showOKDialog("경고", response.getResMsg(), null );
					return;
				}

				Intent intent = new Intent( this, MoreUserInfoActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
				super.finish();
			}
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
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
		Intent intent = new Intent( getApplicationContext(), RegisterUserActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
}
