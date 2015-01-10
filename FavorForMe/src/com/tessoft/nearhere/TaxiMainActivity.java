package com.tessoft.nearhere;

import java.util.ArrayList;
import java.util.List;

import com.tessoft.common.TaxiMainArrayAdapter;
import com.tessoft.domain.ListItemModel;
import com.tessoft.domain.Post;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

public class TaxiMainActivity extends BaseListActivity {

	TaxiMainArrayAdapter adapter = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		try
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_base_list);
			
			header = getLayoutInflater().inflate(R.layout.list_header_taxi_main, null);
			footer = getLayoutInflater().inflate(R.layout.list_footer_taxi_main, null);
			
			listMain = (ListView) findViewById(R.id.listMain);
			listMain.addHeaderView(header);
			listMain.addFooterView(footer);
			
			adapter = new TaxiMainArrayAdapter( getApplicationContext(), 0 );
			listMain.setAdapter(adapter);
			adapter.setDelegate(this);
			
			ArrayList<Post> postList = new ArrayList<Post>();
			Post post = new Post();
			post.setMessage("abc");
			post.setLatitude("37.4704213612");
			post.setLongitude("126.96732417061958");
			postList.add(post);
			adapter.setItemList( (List<ListItemModel>) (Object)postList);
			
			initializeComponents();
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.taxi_main, menu);
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
	
	public void initializeComponents()
	{
		Button btnAddPost = (Button) footer.findViewById(R.id.btnAddPost);
		btnAddPost.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				goAddPostActivity();
			}
		});
	}

	public void goAddPostActivity()
	{
		Intent intent = new Intent( this, SetDestinationActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.stay);
	}
	
	@Override
	public void doAction(String actionName, Object param) {
		// TODO Auto-generated method stub
		super.doAction(actionName, param);
		
		if ( "showDetail".equals(actionName))
		{
			Intent intent = new Intent( this, TaxiPostDetailActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		}
	}
}
