package com.tessoft.nearhere;

import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.type.TypeReference;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tessoft.common.Constants;
import com.tessoft.common.NoticeListAdapter;
import com.tessoft.common.TaxiArrayAdapter;
import com.tessoft.common.Util;
import com.tessoft.domain.APIResponse;
import com.tessoft.domain.Post;
import com.tessoft.domain.User;
import com.tessoft.domain.UserLocation;
import com.tessoft.nearhere.R;

public class UserProfileActivity extends BaseListActivity {

	TaxiArrayAdapter adapter = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		try
		{
			super.onCreate(savedInstanceState);
			
			header = getLayoutInflater().inflate(R.layout.user_profile_list_header, null);
			
			listMain = (ListView) findViewById(R.id.listMain);
			listMain.addHeaderView(header);
			
			adapter = new TaxiArrayAdapter( this , 0);
			listMain.setAdapter(adapter);
			
			listMain.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					Post post = (Post) arg1.getTag();
				
					Intent intent = new Intent( getApplicationContext(), TaxiPostDetailActivity.class);
					intent.putExtra("postID", post.getPostID());
					startActivity(intent);
					overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				}
			});
			
			User user = new User();
			user.setUserID(getIntent().getExtras().getString("userID"));
			setProgressBarIndeterminateVisibility(true);
			sendHttp("/taxi/getUserInfo.do", mapper.writeValueAsString( user ), 1);
		}
		catch( Exception ex )
		{
			Log.e("error", ex.getMessage());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.user_profile, menu);
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
	
	@Override
	public void doPostTransaction(int requestCode, Object result) {
		// TODO Auto-generated method stub
		try
		{
			setProgressBarIndeterminateVisibility(false);
			super.doPostTransaction(requestCode, result);		
			
			if ( requestCode == 1 )
			{
				APIResponse response = mapper.readValue(result.toString(), new TypeReference<APIResponse>(){});
				
				if ( "0000".equals( response.getResCode() ) )
				{
					
					HashMap hash = (HashMap) response.getData();
					
					String userString = mapper.writeValueAsString( hash.get("user") );
					String locationListString = mapper.writeValueAsString( hash.get("locationList") );
					String userPostString = mapper.writeValueAsString( hash.get("userPost") );
					String postsUserRepliedString = mapper.writeValueAsString( hash.get("postsUserReplied") );
					
					User user = mapper.readValue(userString, new TypeReference<User>(){});
					List<UserLocation> locationList = mapper.readValue(locationListString, new TypeReference<List<UserLocation>>(){});
					List<Post> postList = mapper.readValue(userPostString, new TypeReference<List<Post>>(){});
					List<Post> userPostsReplied = mapper.readValue(postsUserRepliedString, new TypeReference<List<Post>>(){});
					
					if ( user != null && user.getProfileImageURL() != null && user.getProfileImageURL().isEmpty() == false )
					{
						ImageView imgProfile = (ImageView) header.findViewById(R.id.imgProfile);
						ImageLoader.getInstance().displayImage( Constants.imageServerURL + 
								user.getProfileImageURL() , imgProfile);
					}
					
					TextView txtUserName = (TextView) header.findViewById(R.id.txtUserName);
					txtUserName.setText( user.getUserName() );
					
					if ( user.getBirthday() != null && !"".equals( user.getBirthday() ) )
					{
						String birthday = Util.getFormattedDateString(user.getBirthday(),"yyyy-MM-dd", "yyyy.MM.dd");
						TextView txtBirthday = (TextView) header.findViewById(R.id.txtBirthday );
						txtBirthday.setText( birthday );
					}
					
					for ( int i = 0; i < locationList.size(); i++ )
					{
						UserLocation loc = locationList.get(i);
						if ( "집".equals( loc.getLocationName() ) )
						{
							TextView txtHomeLocation = (TextView) header.findViewById(R.id.txtHomeLocation);
							txtHomeLocation.setText( loc.getAddress() );
						}
						else if ( "직장".equals( loc.getLocationName() ))
						{
							TextView txtOfficeLocation = (TextView) header.findViewById(R.id.txtOfficeLocation);
							txtOfficeLocation.setText( loc.getAddress() );
						}
					}
					
					TextView txtSex = (TextView) header.findViewById(R.id.txtSex);
					if ( "M".equals( user.getSex() ))
						txtSex.setText("남자");
					else if ( "F".equals( user.getSex() ))
						txtSex.setText("여자");
					
					if ( user.getJobTitle() != null && !"".equals( user.getJobTitle() ))
					{
						TextView txtJobTitle = (TextView) header.findViewById(R.id.txtJobTitle);
						txtJobTitle.setText( user.getJobTitle() );
					}
					
					adapter.setItemList(postList);
					adapter.notifyDataSetChanged();
				}
			}
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}
}
