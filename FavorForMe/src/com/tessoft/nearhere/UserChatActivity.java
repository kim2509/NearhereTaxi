package com.tessoft.nearhere;

import com.tessoft.common.TaxiArrayAdapter;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

public class UserChatActivity extends BaseListActivity {

	TaxiArrayAdapter adapter = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try
		{
			super.onCreate(savedInstanceState);
			
			header = getLayoutInflater().inflate(R.layout.list_header_user_chat, null);
			
			listMain = (ListView) findViewById(R.id.listMain);
			listMain.addHeaderView(header);
			
			adapter = new TaxiArrayAdapter( getApplicationContext(), 0 );
			listMain.setAdapter(adapter);
			adapter.setDelegate(this);
		}
		catch(Exception ex )
		{
			catchException(this, ex);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.user_chat, menu);
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
	
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		this.overridePendingTransition(R.anim.stay, R.anim.slide_out_to_bottom);
	}
}
