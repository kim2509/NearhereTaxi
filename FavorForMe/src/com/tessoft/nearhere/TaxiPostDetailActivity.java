package com.tessoft.nearhere;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.type.TypeReference;

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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tessoft.common.Constants;
import com.tessoft.common.TaxiArrayAdapter;
import com.tessoft.common.TaxiPostReplyListAdapter;
import com.tessoft.common.Util;
import com.tessoft.domain.APIResponse;
import com.tessoft.domain.Post;
import com.tessoft.domain.PostReply;
import com.tessoft.domain.User;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TaxiPostDetailActivity extends BaseListActivity implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener{

	TaxiPostReplyListAdapter adapter = null;
	Post post = null;
	View header2 = null;
	GoogleMap map = null;
	int ZoomLevel = 13;

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

			TextView txtUserName = (TextView) header.findViewById(R.id.txtUserName);
			txtUserName.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					goUserProfileActivity();
				}
			});

			setProgressBarIndeterminateVisibility(true);

			HashMap hash = new HashMap();
			hash.put("postID", getIntent().getExtras().getString("postID") );
			hash.put("latitude", getMetaInfoString("latitude"));
			hash.put("longitude", getMetaInfoString("longitude"));

			sendHttp("/taxi/getPostDetail.do", mapper.writeValueAsString(hash), 1);
		}
		catch(Exception ex )
		{
			catchException(this, ex);
		}
	}

	public void initializeComponent() throws Exception
	{
		MapFragment mapFragment = (MapFragment) getFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
		makeMapScrollable();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
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
		intent.putExtra("userID", post.getUser().getUserID());
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.stay);
	}

	public void goUserChatActivity()
	{
		Intent intent = new Intent( this, UserMessageActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.stay);
	}

	Marker departureMarker = null;
	Marker destinationMarker = null;

	@Override
	public void onMapReady(GoogleMap m ) {
		// TODO Auto-generated method stub
		try
		{
			this.map = m;
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	private void drawPostOnMap() {
		double fromLatitude = Double.parseDouble(post.getFromLatitude());
		double fromLongitude = Double.parseDouble(post.getFromLongitude());

		LatLng Fromlocation = new LatLng( fromLatitude, fromLongitude );

		double latitude = Double.parseDouble(post.getLatitude());
		double longitude = Double.parseDouble(post.getLongitude());

		LatLng location = new LatLng( latitude, longitude );

		departureMarker = map.addMarker(new MarkerOptions()
		.position( Fromlocation )
		.title("출발지"));

		destinationMarker = map.addMarker(new MarkerOptions()
		.position( location )
		.title("목적지"));

		PolylineOptions rectOptions = new PolylineOptions()
		.add( Fromlocation ) 
		.add( location );

		Polyline polyline = map.addPolyline(rectOptions);

		moveMap( location );

		destinationMarker.showInfoWindow();
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

	public void addPostReply( View v )
	{
		try
		{
			EditText edtPostReply = (EditText) footer.findViewById(R.id.edtPostReply);

			if ( TextUtils.isEmpty(edtPostReply.getText()) )
			{
				edtPostReply.setError("댓글을 입력해 주시기 바랍니다.");
				return;
			}

			setProgressBarIndeterminateVisibility(true);

			PostReply postReply = new PostReply();
			postReply.setPostID( post.getPostID() );
			postReply.setUser( getLoginUser() );
			postReply.setLatitude( getMetaInfoString("latitude"));
			postReply.setLongitude( getMetaInfoString("longitude"));
			postReply.setMessage( edtPostReply.getText().toString() );
			edtPostReply.setText("");

			sendHttp("/taxi/insertPostReply.do", mapper.writeValueAsString(postReply), 2);
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

			if ( requestCode == 1 )
			{
				APIResponse response = mapper.readValue(result.toString(), new TypeReference<APIResponse>(){});
				String postData = mapper.writeValueAsString( response.getData() );
				post = mapper.readValue( postData, new TypeReference<Post>(){});

				drawPostOnMap();

				ImageView imgProfile = (ImageView) header.findViewById(R.id.imgProfile);
				ImageLoader.getInstance().displayImage( Constants.imageServerURL + 
						post.getUser().getProfileImageURL() , imgProfile);

				String age = "";

				if ( post.getUser().getAge() != null && !"".equals( post.getUser().getAge() ) )
					age = " (" + post.getUser().getAge() + ")";

				TextView txtUserName = (TextView) header.findViewById(R.id.txtUserName);
				txtUserName.setText( post.getUser().getUserName() + age );

				TextView txtTitle = (TextView) header.findViewById(R.id.txtTitle);
				txtTitle.setText( post.getMessage() );

				TextView txtDeparture = (TextView) header.findViewById(R.id.txtDeparture);
				txtDeparture.setText( post.getFromAddress() );

				TextView txtDestination = (TextView) header.findViewById(R.id.txtDestination);
				txtDestination.setText( post.getToAddress() );

				TextView txtDistance = (TextView) header.findViewById(R.id.txtDistance);
				txtDistance.setText( Util.getDistance(post.getDistance()) );

				TextView txtCreatedDate = (TextView) header.findViewById(R.id.txtCreatedDate);
				txtCreatedDate.setText( Util.getFormattedDateString(post.getCreatedDate(), "yyyy-MM-dd HH:mm") );

				adapter.setItemList( post.getPostReplies() );
				adapter.notifyDataSetChanged();
			}
			else if ( requestCode == 2 )
			{
				setProgressBarIndeterminateVisibility(true);
				sendHttp("/taxi/getPostDetail.do", mapper.writeValueAsString(post), 1);
			}
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}
}
