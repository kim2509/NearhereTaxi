package com.tessoft.favorforme;

import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.tessoft.common.MainArrayAdapter;
import com.tessoft.domain.ListItemModel;
import com.tessoft.domain.Post;
import com.tessoft.domain.User;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class MainActivity extends BaseActivity {

	ListView listMain = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		try
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);
			
			listMain = (ListView) findViewById(R.id.listMain);
			
			ObjectMapper mapper = new ObjectMapper();
			
			User user = new User();
			
			execTransReturningString("/getPosts.do", mapper.writeValueAsString(user), 1);
		}
		catch( Exception ex )
		{
			
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
	public void doPostTransaction(int requestCode, Object result) {
		// TODO Auto-generated method stub
		try
		{
			super.doPostTransaction(requestCode, result);
			
			ObjectMapper mapper = new ObjectMapper();
			List<ListItemModel> postList = mapper.readValue(result.toString(), new TypeReference<List<Post>>(){});
			
			MainArrayAdapter adapter = new MainArrayAdapter( getApplicationContext(), 0 );
			adapter.setItemList( postList );
			listMain.setAdapter( adapter );
		}
		catch(Exception ex )
		{
			showToastMessage(ex.getMessage());
		}
	}
}
