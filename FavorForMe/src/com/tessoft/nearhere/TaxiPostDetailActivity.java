package com.tessoft.nearhere;

import java.util.HashMap;

import org.codehaus.jackson.type.TypeReference;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tessoft.common.Constants;
import com.tessoft.common.TaxiPostReplyListAdapter;
import com.tessoft.common.Util;
import com.tessoft.domain.APIResponse;
import com.tessoft.domain.Post;
import com.tessoft.domain.PostReply;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TaxiPostDetailActivity extends BaseListActivity 
	implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener, OnClickListener{

	private static final int DELETE_POST = 3;
	private static final int POST_DETAIL = 1;
	private static final int INSERT_POST_REPLY = 2;
	private static final int REQUEST_MODIFY_POST = 0;
	TaxiPostReplyListAdapter adapter = null;
	Post post = null;
	View header2 = null;
	GoogleMap map = null;
	int ZoomLevel = 13;
	ImageView imgProfile = null;
	TextView txtUserName = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try
		{
			super.onCreate(savedInstanceState);

			header = getLayoutInflater().inflate(R.layout.taxi_post_detail_list_header1, null);
			header2 = getLayoutInflater().inflate(R.layout.taxi_post_detail_list_header2, null);
			footer = getLayoutInflater().inflate(R.layout.taxi_post_detail_list_footer, null);

			listMain = (ListView) findViewById(R.id.listMain);
			listMain.addHeaderView(header, null, false );
			listMain.addHeaderView(header2 );
			listMain.addFooterView(footer);

			adapter = new TaxiPostReplyListAdapter( getApplicationContext(), 0 );
			listMain.setAdapter(adapter);
			adapter.setDelegate(this);
			
			initializeComponent();

			setProgressBarIndeterminateVisibility(true);

			HashMap hash = new HashMap();
			hash.put("postID", getIntent().getExtras().getString("postID") );
			hash.put("latitude", getMetaInfoString("latitude"));
			hash.put("longitude", getMetaInfoString("longitude"));

			sendHttp("/taxi/getPostDetail.do", mapper.writeValueAsString(hash), POST_DETAIL );
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
		
		imgProfile = (ImageView) header.findViewById(R.id.imgProfile);
		imgProfile.setImageResource(R.drawable.no_image);
		imgProfile.setOnClickListener( this );
		
		txtUserName = (TextView) header.findViewById(R.id.txtUserName);
		txtUserName.setOnClickListener( this );
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

	private Menu menu = null;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.taxi_post_detail, menu);
		this.menu = menu;
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
		else if ( id == R.id.action_edit )
			modifyPost();
		else if (id == R.id.action_delete) {
			showYesNoDialog("확인", "정말 삭제하시겠습니까?", "postDelete");
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void yesClicked(Object param) {
		// TODO Auto-generated method stub
		super.yesClicked(param);
		
		
		try
		{
			if ( "postDelete".equals( param ) )
			{
				setProgressBarIndeterminateVisibility(true);
				sendHttp("/taxi/deletePost.do", mapper.writeValueAsString(post), DELETE_POST );				
			}
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	public void goUserProfileActivity( String userID )
	{
		Intent intent = new Intent( this, UserProfileActivity.class);
		intent.putExtra("userID", userID );
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
		overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
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

			sendHttp("/taxi/insertPostReply.do", mapper.writeValueAsString(postReply), INSERT_POST_REPLY );
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

			super.doPostTransaction(requestCode, result);

			if ( requestCode == POST_DETAIL )
			{
				APIResponse response = mapper.readValue(result.toString(), new TypeReference<APIResponse>(){});
				String postData = mapper.writeValueAsString( response.getData() );
				post = mapper.readValue( postData, new TypeReference<Post>(){});

				drawPostOnMap();

				if ( !Util.isEmptyString(post.getUser().getProfileImageURL()))
				{
					ImageLoader.getInstance().displayImage( Constants.imageServerURL + 
							post.getUser().getProfileImageURL() , imgProfile);			
				}

				String age = "";

				if ( post.getUser().getAge() != null && !"".equals( post.getUser().getAge() ) )
					age = " (" + post.getUser().getAge() + ")";

				if ( Util.isEmptyString( post.getUser().getUserName() ) )
					txtUserName.setText( post.getUser().getUserID() + age );
				else
					txtUserName.setText( post.getUser().getUserName() + age );

				String titleDummy = "";
				
				if ( post.getSexInfo() != null && !"상관없음".equals( post.getSexInfo() ) )
					titleDummy += post.getSexInfo();
				
				if ( post.getNumOfUsers() != null && !"상관없음".equals( post.getNumOfUsers() ) )
					titleDummy += " " + post.getNumOfUsers();
				
				if ( titleDummy.isEmpty() == false )
					titleDummy = "(" + titleDummy.trim() + ")";
				
				TextView txtTitle = (TextView) header.findViewById(R.id.txtTitle);
				txtTitle.setText( post.getMessage() + titleDummy );

				TextView txtDeparture = (TextView) header.findViewById(R.id.txtDeparture);
				txtDeparture.setText( post.getFromAddress() );

				TextView txtDestination = (TextView) header.findViewById(R.id.txtDestination);
				txtDestination.setText( post.getToAddress() );

				TextView txtDistance = (TextView) header.findViewById(R.id.txtDistance);
				txtDistance.setText( Util.getDistance(post.getDistance()) );

				if ( post.getDepartureDate() != null )
				{
					TextView txtDepartureDateTime = (TextView) header.findViewById(R.id.txtDepartureDateTime);
					txtDepartureDateTime.setText( post.getDepartureDate() + " " + post.getDepartureTime());	
				}
				
				TextView txtCreatedDate = (TextView) header.findViewById(R.id.txtCreatedDate);
				txtCreatedDate.setText( Util.getFormattedDateString(post.getCreatedDate(), "yyyy-MM-dd HH:mm") );

				adapter.setItemList( post.getPostReplies() );
				adapter.notifyDataSetChanged();
				
				if ( post.getUser().getUserID().equals( getLoginUser().getUserID() ))
				{
					menu.findItem(R.id.action_edit).setVisible(true);
					menu.findItem(R.id.action_delete).setVisible(true);	
				}
				else
				{
					menu.findItem(R.id.action_edit).setVisible(false);
					menu.findItem(R.id.action_delete).setVisible(false);
				}
			}
			else if ( requestCode == INSERT_POST_REPLY )
			{
				setProgressBarIndeterminateVisibility(true);
				sendHttp("/taxi/getPostDetail.do", mapper.writeValueAsString(post), POST_DETAIL );
			}
			else if ( requestCode == DELETE_POST )
			{
				Intent data = new Intent();
				data.putExtra("reload", true);
				setResult(RESULT_OK, data);
				finish();
			}
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}
	
	public void modifyPost()
	{
		Intent intent = new Intent( this, NewTaxiPostActivity.class);
		intent.putExtra("mode", "modify");
		intent.putExtra("post", post);
		startActivityForResult(intent, REQUEST_MODIFY_POST );
		overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.stay);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int responseCode, Intent data ) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, responseCode, data);
		
		try
		{
			if ( responseCode == RESULT_OK )
			{
				setProgressBarIndeterminateVisibility(true);
				sendHttp("/taxi/getPostDetail.do", mapper.writeValueAsString(post), POST_DETAIL );
				
				Intent result = new Intent();
				result.putExtra("reload", true);
				setResult(RESULT_OK, result);	
			}			
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		try
		{
			int id = v.getId();
			
			if ( id == R.id.txtUserName || id == R.id.imgProfile )
			{
				goUserProfileActivity( post.getUser().getUserID() );
			}
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}
	
	@Override
	public void doAction(String actionName, Object param) {
		// TODO Auto-generated method stub
		super.doAction(actionName, param);
		
		if ( "userProfile".equals( actionName ) )
			goUserProfileActivity( param.toString() );
	}
}
