package com.tessoft.nearhere;

import com.tessoft.common.TaxiMainArrayAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

public class TaxiPostDetailActivity extends BaseListActivity {

	TaxiMainArrayAdapter adapter = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try
		{
			super.onCreate(savedInstanceState);
			
			header = getLayoutInflater().inflate(R.layout.list_header_taxi_post_detail, null);
			
			listMain = (ListView) findViewById(R.id.listMain);
			listMain.addHeaderView(header);
			
			adapter = new TaxiMainArrayAdapter( getApplicationContext(), 0 );
			listMain.setAdapter(adapter);
			adapter.setDelegate(this);
			
			Button btnUserProfile = (Button) header.findViewById(R.id.btnUserProfile);
			btnUserProfile.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					goUserProfileActivity();
				}
			});
			Button btnMessage = (Button) header.findViewById(R.id.btnSendMessage);
			btnMessage.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					goUserChatActivity();
				}
			});
		}
		catch(Exception ex )
		{
			catchException(this, ex);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.taxi_post_detail, menu);
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
	
	public void goUserProfileActivity()
	{
		Intent intent = new Intent( this, UserProfileActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.stay);
	}
	
	public void goUserChatActivity()
	{
		Intent intent = new Intent( this, UserChatActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.stay);
	}
}
