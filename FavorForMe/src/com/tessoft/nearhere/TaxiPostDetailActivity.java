package com.tessoft.nearhere;

import java.io.IOException;
import java.util.HashMap;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.type.TypeReference;

import uk.co.senab.photoview.PhotoViewAttacher;

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
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.tessoft.common.Constants;
import com.tessoft.common.TaxiPostReplyListAdapter;
import com.tessoft.common.Util;
import com.tessoft.domain.APIResponse;
import com.tessoft.domain.Post;
import com.tessoft.domain.PostReply;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class TaxiPostDetailActivity extends BaseListActivity 
implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener, OnClickListener{

	private static final int DELETE_POST = 3;
	private static final int POST_DETAIL = 1;
	private static final int INSERT_POST_REPLY = 2;
	private static final int REQUEST_MODIFY_POST = 0;
	private static final int DELETE_POST_REPLY = 4;
	private static final int HTTP_UPDATE_POST_AS_FINISHED = 10;
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
			listMain.setHeaderDividersEnabled( false );
			listMain.addFooterView(footer, null, false );
			listMain.setFooterDividersEnabled(false);

			listMain.setSelector(android.R.color.transparent);

			adapter = new TaxiPostReplyListAdapter( getApplicationContext(), getLoginUser(),0 );
			listMain.setAdapter(adapter);
			adapter.setDelegate(this);

			initializeComponent();

			inquiryPostDetail();
			
			setTitle("합승상세");
		}
		catch(Exception ex )
		{
			catchException(this, ex);
		}
	}

	private void inquiryPostDetail() throws IOException,
	JsonGenerationException, JsonMappingException {
		HashMap hash = new HashMap();
		
		findViewById(R.id.marker_progress).setVisibility(ViewGroup.VISIBLE);
		listMain.setVisibility(ViewGroup.GONE);
		
		if ( getIntent().getExtras() != null )
		{
			hash.put("postID", getIntent().getExtras().getString("postID") );
			if ( getIntent().getExtras().containsKey("fromLatitude") )
				hash.put("fromLatitude", getIntent().getExtras().getString("fromLatitude") );
			if ( getIntent().getExtras().containsKey("fromLongitude") )
				hash.put("fromLongitude", getIntent().getExtras().getString("fromLongitude") );
			if ( getIntent().getExtras().containsKey("toLatitude") )
				hash.put("toLatitude", getIntent().getExtras().getString("toLatitude") );
			if ( getIntent().getExtras().containsKey("toLongitude") )
				hash.put("toLongitude", getIntent().getExtras().getString("toLongitude") );
		}
		
		hash.put("userID", getLoginUser().getUserID() );

		sendHttp("/taxi/getPostDetail.do", mapper.writeValueAsString(hash), POST_DETAIL );
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
		
		Button btnDelete = (Button) header.findViewById(R.id.btnDelete);
		btnDelete.setOnClickListener(this);
		
		Button btnFinish = (Button) header.findViewById(R.id.btnFinish);
		btnFinish.setOnClickListener(this);
		
		if ( "true".equals( getMetaInfoString("hideMapOnPostDetail") ) )
		{
			findViewById(R.id.map_layout).setVisibility(ViewGroup.GONE);
			Button btnHideMap = (Button)findViewById(R.id.btnHideMap);
			btnHideMap.setText("경로 보기");
		}
		else
		{
			findViewById(R.id.map_layout).setVisibility(ViewGroup.VISIBLE);
			Button btnHideMap = (Button)findViewById(R.id.btnHideMap);
			btnHideMap.setText("경로 숨기기");
		}
		
		Button btnRefresh = (Button) findViewById(R.id.btnRefresh);
		btnRefresh.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					inquiryPostDetail();
				} catch ( Exception ex ) {
					// TODO Auto-generated catch block
					catchException(this, ex);
				}
			}
		});
		
		options = new DisplayImageOptions.Builder()
		.resetViewBeforeLoading(true)
		.cacheInMemory(true)
		.showImageOnLoading(R.drawable.no_image)
		.showImageForEmptyUri(R.drawable.no_image)
		.showImageOnFail(R.drawable.no_image)
		.displayer(new RoundedBitmapDisplayer(20))
		.delayBeforeLoading(100)
		.build();
	}

	DisplayImageOptions options = null;
	
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

		try
		{
			if (id == R.id.action_refresh) {
				inquiryPostDetail();
				return true;
			}
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void yesClicked(Object param) {
		// TODO Auto-generated method stub
		super.yesClicked(param);


		try
		{
			if ( param instanceof String && "postDelete".equals( param ) )
			{
				sendHttp("/taxi/deletePost.do", mapper.writeValueAsString(post), DELETE_POST );				
			}
			else if ( param instanceof PostReply )
			{
				sendHttp("/taxi/deletePostReply.do", mapper.writeValueAsString(param), DELETE_POST_REPLY );	
			}
			else if ( param instanceof String && "DIALOG_FINISH_POST".equals(param) )
			{
				post.setStatus("종료됨");
				sendHttp("/taxi/modifyPost.do", mapper.writeValueAsString(post), HTTP_UPDATE_POST_AS_FINISHED );
				Intent data = new Intent();
				data.putExtra("reload", true);
				setResult(RESULT_OK, data);
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
			
			//강남역
			moveMap(new LatLng(37.497916, 127.027546));
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
		.title("도착지"));

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

			PostReply postReply = new PostReply();
			postReply.setPostID( post.getPostID() );
			postReply.setUser( getLoginUser() );
			postReply.setLatitude( MainActivity.latitude );
			postReply.setLongitude( MainActivity.longitude );
			postReply.setMessage( edtPostReply.getText().toString() );
			edtPostReply.setText("");

			sendHttp("/taxi/insertPostReply.do", mapper.writeValueAsString(postReply), INSERT_POST_REPLY );
			
			findViewById(R.id.marker_progress).setVisibility(ViewGroup.VISIBLE);
			listMain.setVisibility(ViewGroup.GONE);
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
			findViewById(R.id.marker_progress).setVisibility(ViewGroup.GONE);
			
			if ( Constants.FAIL.equals(result) )
			{
				showOKDialog("통신중 오류가 발생했습니다.\r\n다시 시도해 주십시오.", null);
				return;
			}

			super.doPostTransaction(requestCode, result);

			APIResponse response = mapper.readValue(result.toString(), new TypeReference<APIResponse>(){});

			if ( "0000".equals( response.getResCode() ) )
			{
				listMain.setVisibility(ViewGroup.VISIBLE);
				
				if ( requestCode == POST_DETAIL )
				{
					String postData = mapper.writeValueAsString( response.getData() );
					post = mapper.readValue( postData, new TypeReference<Post>(){});

					drawPostOnMap();
					
					if ( !Util.isEmptyString(post.getUser().getProfileImageURL()))
					{
						ImageLoader.getInstance().displayImage( Constants.thumbnailImageURL + 
								post.getUser().getProfileImageURL() , imgProfile, options );
					}

					String age = "";

					if ( post.getUser().getAge() != null && !"".equals( post.getUser().getAge() ) )
						age = " (" + post.getUser().getAge() + ")";

					if ( Util.isEmptyString( post.getUser().getUserName() ) )
						txtUserName.setText( post.getUser().getUserID() + age );
					else
						txtUserName.setText( post.getUser().getUserName() + age );

					ImageView imgSex = (ImageView) header.findViewById(R.id.imgSex);
					imgSex.setVisibility(ViewGroup.VISIBLE);
					
					if ( "M".equals( post.getUser().getSex() ))
					{
						imgSex.setImageResource(R.drawable.ic_male);
					}
					else if ( "F".equals( post.getUser().getSex() ))
					{
						imgSex.setImageResource(R.drawable.ic_female);
					}
					else
						imgSex.setVisibility(ViewGroup.GONE);

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
//
//					TextView txtFromDistance = (TextView) header.findViewById(R.id.txtFromDistance);
//					if ( Util.isEmptyString( post.getFromDistance() ) == false )
//					{
//						txtFromDistance.setText( Util.getDistance( post.getFromDistance() ) );
//						txtFromDistance.setVisibility(ViewGroup.VISIBLE);
//					}
//					else
//						txtFromDistance.setVisibility(ViewGroup.INVISIBLE);
					
//					TextView txtToDistance = (TextView) header.findViewById(R.id.txtToDistance);
//					if ( Util.isEmptyString( post.getToDistance() ) == false )
//					{
//						txtToDistance.setText( Util.getDistance( post.getToDistance() ) );
//						txtToDistance.setVisibility(ViewGroup.VISIBLE);
//					}
//					else
//						txtToDistance.setVisibility(ViewGroup.INVISIBLE);

					if ( post.getDepartureDate() != null )
					{
						TextView txtDepartureDateTime = (TextView) header.findViewById(R.id.txtDepartureDateTime);
						txtDepartureDateTime.setText( post.getDepartureDate() + " " + post.getDepartureTime());	
					}

					TextView txtCreatedDate = (TextView) header.findViewById(R.id.txtCreatedDate);
					txtCreatedDate.setText( Util.getFormattedDateString(post.getCreatedDate(), "MM-dd HH:mm") );

					adapter.setItemList( post.getPostReplies() );
					adapter.notifyDataSetChanged();

//					if ( "true".equals( getMetaInfoString("admin") ) || post.getUser().getUserID().equals( getLoginUser().getUserID() ))
//					{
//						menu.findItem(R.id.action_edit).setVisible(true);
//						menu.findItem(R.id.action_delete).setVisible(true);	
//					}
//					else
//					{
//						menu.findItem(R.id.action_edit).setVisible(false);
//						menu.findItem(R.id.action_delete).setVisible(false);
//					}
					
					ImageView imgStatus = (ImageView) header.findViewById(R.id.imgStatus);
					imgStatus.setVisibility(ViewGroup.VISIBLE);
					Button btnFinish = (Button) header.findViewById(R.id.btnFinish);
					
					if ( post.getUser().getUserID().equals( getLoginUser().getUserID() ) || Constants.bAdminMode )
						header.findViewById(R.id.layoutMyOption).setVisibility(ViewGroup.VISIBLE);
					else
						header.findViewById(R.id.layoutMyOption).setVisibility(ViewGroup.GONE );
					
					if ( "진행중".equals( post.getStatus() ) )
					{
						imgStatus.setImageResource(R.drawable.progressing);
						if ( post.getUser().getUserID().equals( getLoginUser().getUserID() ))
							btnFinish.setVisibility(ViewGroup.VISIBLE);	
					}
					else
					{
						imgStatus.setImageResource(R.drawable.finished);
						btnFinish.setVisibility(ViewGroup.GONE);
					}
				}
				else if ( requestCode == INSERT_POST_REPLY )
				{
					inquiryPostDetail();
				}
				else if ( requestCode == DELETE_POST )
				{
					Intent data = new Intent();
					data.putExtra("reload", true);
					setResult(RESULT_OK, data);
					finish();
				}
				else if ( requestCode == DELETE_POST_REPLY || requestCode == HTTP_UPDATE_POST_AS_FINISHED )
				{
					inquiryPostDetail();
				}
			}
			else
			{
				listMain.setVisibility(ViewGroup.GONE);
				showOKDialog("경고", response.getResMsg(), "error" );
				return;
			}
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}
	
	@Override
	public void okClicked(Object param) {
		// TODO Auto-generated method stub
		super.okClicked(param);
		
		if ( "error".equals( param ) )
			onBackPressed();
	}

	public void modifyPost( View v )
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
				inquiryPostDetail();

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
			else if ( id == R.id.btnDelete )
			{
				showYesNoDialog("확인", "정말 삭제하시겠습니까?", "postDelete");
				return;
			}
			else if ( id == R.id.btnFinish )
			{
				showYesNoDialog("확인", "정말 종료하시겠습니까?", "DIALOG_FINISH_POST");
				return;
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
		try
		{
			super.doAction(actionName, param);

			if ( "userProfile".equals( actionName ) )
				goUserProfileActivity( param.toString() );
			else if ( "deleteReply".equals( actionName ) )
			{
				showYesNoDialog("확인", "정말 삭제하시겠습니까?", param );
				return;
			}	
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	//This is the handler that will manager to process the broadcast intent
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try
			{
				if ( intent.getExtras() != null && intent.getExtras().containsKey("type"))
				{
					String type = intent.getExtras().getString("type");
					if ( "postReply".equals( type ) && intent.getExtras().containsKey("postID"))
					{
						String postID = intent.getExtras().getString("postID");
						if ( post.getPostID().equals( postID ) )
							inquiryPostDetail();
					}
				}
			}
			catch( Exception ex )
			{
				catchException(this, ex);
			}
		}
	};

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		try
		{
			registerReceiver(mMessageReceiver, new IntentFilter("updateUnreadCount"));
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		unregisterReceiver(mMessageReceiver);
	}
	
	@Override
	public void onBackPressed() {

		finish();
		
		if ( MainActivity.active == false )
		{
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);			
		}
	}
	
	public void toggleMapVisibility( View v )
	{
		Button btn = (Button) v;
		if ( findViewById(R.id.map_layout).getVisibility() == ViewGroup.GONE )
		{
			findViewById(R.id.map_layout).setVisibility(ViewGroup.VISIBLE);
			btn.setText("경로 숨기기");
			setMetaInfo("hideMapOnPostDetail", "false");
		}
		else
		{
			findViewById(R.id.map_layout).setVisibility(ViewGroup.GONE);
			btn.setText("경로 보기");
			setMetaInfo("hideMapOnPostDetail", "true");
		}
	}
}
