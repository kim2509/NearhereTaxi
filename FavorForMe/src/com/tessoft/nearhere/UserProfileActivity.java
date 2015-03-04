package com.tessoft.nearhere;

import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.type.TypeReference;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
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

public class UserProfileActivity extends BaseActivity {

	ListView listMain = null;
	View header = null;
	View footer = null;
	TaxiArrayAdapter adapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		try
		{
			super.onCreate(savedInstanceState);

			supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

			setContentView(R.layout.activity_user_profile);

			header = getLayoutInflater().inflate(R.layout.user_profile_list_header, null);
			footer = getLayoutInflater().inflate(R.layout.user_profile_list_footer, null);

			listMain = (ListView) findViewById(R.id.listMain);
			listMain.addHeaderView(header, null, false );

			adapter = new TaxiArrayAdapter( this , this, 0);
			listMain.setAdapter(adapter);

			listMain.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					if ( arg1.getTag() != null && arg1.getTag() instanceof Post )
					{
						Post post = (Post) arg1.getTag();

						Intent intent = new Intent( getApplicationContext(), TaxiPostDetailActivity.class);
						intent.putExtra("postID", post.getPostID());
						startActivity(intent);
						overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);						
					}
				}
			});

			footer.findViewById(R.id.txtNone).setVisibility(ViewGroup.GONE);

			User user = new User();
			user.setUserID(getIntent().getExtras().getString("userID"));
			setProgressBarIndeterminateVisibility(true);
			sendHttp("/taxi/getUserInfo.do", mapper.writeValueAsString( user ), 1);

			ImageView imgProfile = (ImageView) header.findViewById(R.id.imgProfile);
			imgProfile.setOnClickListener( new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if ( v.getTag() != null )
					{
						Intent intent = new Intent( getApplicationContext(), PhotoViewer.class);
						intent.putExtra("imageURL", v.getTag().toString());
						startActivity(intent);
						overridePendingTransition(R.anim.fade_in, R.anim.stay);						
					}

				}
			});
		}
		catch( Exception ex )
		{
			Log.e("error", ex.getMessage());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		try
		{
			getMenuInflater().inflate(R.menu.user_profile, menu);

			MenuItem item = menu.findItem(R.id.action_chat);

			if ( getIntent().getExtras().getString("userID").equals( getLoginUser().getUserID() ))
				item.setVisible(false);
			else
				item.setVisible(true);
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.

		try
		{
			int id = item.getItemId();
			if (id == R.id.action_chat) {

				HashMap hash = new HashMap();
				hash.put("fromUserID", getIntent().getExtras().getString("userID") );
				hash.put("userID",  getLoginUser().getUserID() );
				Intent intent = new Intent( this, UserMessageActivity.class);
				intent.putExtra("messageInfo", hash );
				startActivity(intent);
				overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);

				return true;
			}

		}
		catch( Exception ex )
		{

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
				if ( requestCode == 1 )
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
					postList.addAll( userPostsReplied );

					ImageView imgProfile = (ImageView) header.findViewById(R.id.imgProfile);
					imgProfile.setImageResource(R.drawable.no_image);

					if ( user != null && user.getProfileImageURL() != null && user.getProfileImageURL().isEmpty() == false )
					{
						ImageLoader.getInstance().displayImage( Constants.thumbnailImageURL + 
								user.getProfileImageURL() , imgProfile);
						imgProfile.setTag(user.getProfileImageURL());
					}

					TextView txtUserName = (TextView) header.findViewById(R.id.txtUserName);

					if ( Util.isEmptyString( user.getUserName() ) )
						txtUserName.setText( user.getUserID() );
					else
						txtUserName.setText( user.getUserName() + " (" + user.getUserID() + ")" );

					TextView txtCreditValue = (TextView) header.findViewById(R.id.txtCreditValue);
					txtCreditValue.setText( user.getProfilePoint() + "%" );

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

					ImageView imgSex = (ImageView) header.findViewById(R.id.imgSex);
					TextView txtSex = (TextView) header.findViewById(R.id.txtSex);
					imgSex.setVisibility(ViewGroup.VISIBLE);

					if ( "M".equals( user.getSex() ))
					{
						imgSex.setImageResource(R.drawable.male);
						txtSex.setText("남자");
					}
					else if ( "F".equals( user.getSex() ))
					{
						imgSex.setImageResource(R.drawable.female);
						txtSex.setText("여자");
					}
					else
						imgSex.setVisibility(ViewGroup.GONE);

					if ( user.getJobTitle() != null && !"".equals( user.getJobTitle() ))
					{
						TextView txtJobTitle = (TextView) header.findViewById(R.id.txtJobTitle);
						txtJobTitle.setText( user.getJobTitle() );
					}

					adapter.clear();
					adapter.addAll(postList);
					adapter.notifyDataSetChanged();

					if ( postList.size() == 0 )
						footer.findViewById(R.id.txtNone).setVisibility(ViewGroup.VISIBLE);
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
}
