package com.tessoft.nearhere;

import java.util.ArrayList;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tessoft.common.Constants;
import com.tessoft.common.TaxiArrayAdapter;
import com.tessoft.common.TaxiPostReplyListAdapter;
import com.tessoft.domain.PostReply;
import com.tessoft.domain.TaxiPost;
import com.tessoft.domain.User;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TaxiPostDetailActivity extends BaseListActivity implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener{

	TaxiPostReplyListAdapter adapter = null;
	TaxiPost post = null;
	View header2 = null;
	GoogleMap map = null;
	int ZoomLevel = 16;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try
		{
			super.onCreate(savedInstanceState);

			header = getLayoutInflater().inflate(R.layout.taxi_post_detail_list_header1, null);
			header2 = getLayoutInflater().inflate(R.layout.taxi_post_detail_list_header2, null);
			footer = getLayoutInflater().inflate(R.layout.taxi_post_detail_list_footer, null);

			listMain = (ListView) findViewById(R.id.listMain);
			listMain.addHeaderView(header);
			listMain.addHeaderView(header2);
			listMain.addFooterView(footer);

			adapter = new TaxiPostReplyListAdapter( getApplicationContext(), 0 );
			listMain.setAdapter(adapter);
			adapter.setDelegate(this);

			initializeComponent();

			Button btnUserProfile = (Button) header2.findViewById(R.id.btnUserProfile);
			btnUserProfile.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					goUserProfileActivity();
				}
			});
			Button btnMessage = (Button) header2.findViewById(R.id.btnSendMessage);
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

	public void initializeComponent() throws Exception
	{
		if ( getIntent().getExtras().get("post") != null )
		{
			post = (TaxiPost) getIntent().getExtras().get("post");

			ImageView imgProfile = (ImageView) header2.findViewById(R.id.imgProfile);
			ImageLoader.getInstance().displayImage( Constants.imageServerURL + 
					post.getUser().getProfileImageURL() , imgProfile);

			TextView txtUserName = (TextView) header2.findViewById(R.id.txtUserName);
			txtUserName.setText( post.getUser().getUserName() );

			MapFragment mapFragment = (MapFragment) getFragmentManager()
					.findFragmentById(R.id.map);
			mapFragment.getMapAsync(this);
			
			makeMapScrollable();
			
			ArrayList<PostReply> replyList = new ArrayList<PostReply>();
			PostReply reply = new PostReply();
			User user = new User();
			user.setUserID("kim2509");
			user.setUserName("김대용");
			user.setLatitude("37.4746367");
			user.setLongitude("126.96298599");
			user.setProfileImageURL("d.png");
			reply.setDistance("0.01");
			reply.setMessage("언제쯤 출발하실건가요?");
			reply.setCreateDate("오후 6:30");
			reply.setUser(user);
			replyList.add(reply);
			reply = new PostReply();
			reply.setDistance("0.03");
			reply.setMessage("언제쯤 출발하실건가요2?");
			reply.setCreateDate("오후 6:25");
			reply.setUser(user);
			replyList.add(reply);
			
			adapter.setItemList(replyList);
			adapter.notifyDataSetChanged();
		}
	}

	private void makeMapScrollable() {

		ImageView transparentImageView = (ImageView) header2.findViewById(R.id.transparent_image);

		transparentImageView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				try
				{
					int action = event.getAction();
					switch (action) {
					case MotionEvent.ACTION_DOWN:
						// Disallow ScrollView to intercept touch events.
						listMain.requestDisallowInterceptTouchEvent(true);
						// Disable touch on transparent view
						return false;

					case MotionEvent.ACTION_UP:
						// Allow ScrollView to intercept touch events.
						listMain.requestDisallowInterceptTouchEvent(false);
						return true;

					case MotionEvent.ACTION_MOVE:
						listMain.requestDisallowInterceptTouchEvent(true);
						return false;

					default: 
						return true;
					}   
				}
				catch( Exception ex )
				{
					Log.e("error", ex.getMessage());
				}

				return true;
			}
		});
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

	@Override
	public void onMapReady(GoogleMap map ) {
		// TODO Auto-generated method stub
		try
		{
			this.map = map;

			if ( post != null )
			{
				double latitude = Double.parseDouble(post.getLatitude());
				double longitude = Double.parseDouble(post.getLongitude());

				LatLng location = new LatLng( latitude, longitude );
				moveMap( location );
			}
			else
				showToastMessage("onmapReady but null");
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
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub

	}

	private void moveMap( LatLng location )
	{
		CameraUpdate center=
				CameraUpdateFactory.newLatLng( location );
		map.moveCamera(center);
		CameraUpdate zoom=CameraUpdateFactory.zoomTo(ZoomLevel);
		map.animateCamera(zoom);
	}
}
