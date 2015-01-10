package com.tessoft.nearhere;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.tessoft.common.MainArrayAdapter;
import com.tessoft.domain.ListItemModel;
import com.tessoft.domain.MainInfo;
import com.tessoft.domain.Post;
import com.tessoft.domain.User;
import com.tessoft.nearhere.R;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

public class HelpMainActivity extends BaseActivity implements OnMapReadyCallback, OnItemClickListener, 
OnCameraChangeListener, OnMarkerClickListener, OnInfoWindowClickListener, ConnectionCallbacks, OnConnectionFailedListener{ //, OnScrollListener{

	ListView listMain = null;
	GoogleMap map = null;
	LocationManager mLocationManager = null;
	int ZoomLevel = 16;
	private HashMap<Marker, Post> markersMap = null;
	MainArrayAdapter adapter = null;
	View header = null;
	GoogleApiClient mGoogleApiClient = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		try
		{
			super.onCreate(savedInstanceState);

			setContentView(R.layout.activity_help_main);

			header = getLayoutInflater().inflate(R.layout.list_main_header, null);

			listMain = (ListView) findViewById(R.id.listMain);
			listMain.addHeaderView(header);

			adapter = new MainArrayAdapter( getApplicationContext(), 0 );
			listMain.setAdapter(adapter);
			adapter.setDelegate(this);
			listMain.setOnItemClickListener(this);

			markersMap = new HashMap<Marker, Post>();
			
			initImageLoader();

			MapFragment mapFragment = (MapFragment) getFragmentManager()
					.findFragmentById(R.id.map);
			mapFragment.getMapAsync(this);

			makeMapScrollable();

			buildGoogleApiClient();
		}
		catch( Exception ex )
		{
			showToastMessage(ex.getMessage());
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		mGoogleApiClient.connect();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		//mGoogleApiClient.disconnect();
	}

	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
		.addConnectionCallbacks(this)
		.addOnConnectionFailedListener(this)
		.addApi(LocationServices.API)
		.build();
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

	@SuppressWarnings("unchecked")
	@Override
	public void doPostTransaction(int requestCode, Object result) {
		// TODO Auto-generated method stub
		try
		{
			super.doPostTransaction(requestCode, result);

			List<ListItemModel> postList = null;

			if ( requestCode == 1 )
			{
				MainInfo mainInfo = mapper.readValue(result.toString(), new TypeReference<MainInfo>(){});

				User user = mainInfo.getUser();
				
				Log.i("doPostTransaction", mapper.writeValueAsString( user ) );
				
				if ( user != null )
				{
					setMetaInfo("userID", user.getUserID());
					setMetaInfo("userName", user.getUserName());
					setMetaInfo("profileImageURL", user.getProfileImageURL());
					
					TextView txtWelcome = (TextView) header.findViewById(R.id.txtWelcome);
					txtWelcome.setText( "안녕하세요. " + user.getUserName() + "님.");
				}
				
				TextView txtNotice1 = (TextView) findViewById(R.id.txtNotice1);
				txtNotice1.setText("근처에 " + mainInfo.getPostCount() + " 개의 HELP들이 있습니다.");

				postList = (List<ListItemModel>) (Object) mainInfo.getPostList();
				adapter.setItemList( postList );
				adapter.notifyDataSetChanged();
				
				putMarkersOnMap( postList );

				if ( map != null && !"".equals( getMetaInfoString("latitude") ))
				{
					double latitude = Double.parseDouble(getMetaInfoString("latitude"));
					double longitude = Double.parseDouble(getMetaInfoString("longitude"));
					moveMap(new LatLng(latitude, longitude));
					CameraUpdate zoom=CameraUpdateFactory.zoomTo(ZoomLevel);
					map.animateCamera(zoom);
				}
			}
			else if ( requestCode == 2 )
			{
				postList = (List<ListItemModel>) mapper.readValue(result.toString(), new TypeReference<List<Post>>(){});
				adapter.setItemList( postList );
				adapter.notifyDataSetChanged();
				
				putMarkersOnMap( postList );
			}
		}
		catch(Exception ex )
		{
			Log.e("doPostTransaction", "exception" );
		}
	}

	private void putMarkersOnMap( List<ListItemModel> postList )
	{
		for ( int i = 0; i < postList.size(); i++ )
		{
			Post post = (Post) postList.get(i);

			Marker marker = map.addMarker(new MarkerOptions()
			.position(new LatLng(Double.parseDouble( post.getLatitude() ), Double.parseDouble(post.getLongitude())))
			.title(post.getMessage()));

			//post.setTag( marker );
			markersMap.put( marker, post);
		}
	}

	@Override
	public void onMapReady(GoogleMap map) {

		try
		{
			this.map = map;

			map.setMyLocationEnabled(true);
			
			Log.i("onMapReady", "onMapReady");

			if ( !"".equals( getMetaInfoString("latitude")))
			{
				double latitude = Double.parseDouble( getMetaInfoString("latitude"));
				double longitude = Double.parseDouble( getMetaInfoString("longitude"));
				moveMap( new LatLng( latitude , longitude ) );
				CameraUpdate zoom=CameraUpdateFactory.zoomTo(ZoomLevel);
				map.animateCamera(zoom);
			}

			map.setOnCameraChangeListener(this);
			map.setOnMarkerClickListener(this);
			map.setOnInfoWindowClickListener(this);

			ObjectMapper mapper = new ObjectMapper();

			User user = new User();
			user.setUserID( getMetaInfoString("userID") );
			user.setLatitude(getMetaInfoString("latitude"));
			user.setLongitude(getMetaInfoString("longitude"));

			execTransReturningString("/getMainInfo.do", mapper.writeValueAsString(user), 1);
		}
		catch( Exception ex )
		{
			Log.e("onMapReady", ex.getMessage());
		}
	}

	private void moveMap( LatLng location )
	{
		CameraUpdate center=
				CameraUpdateFactory.newLatLng( location );
		map.moveCamera(center);
		map.setPadding(0, 200, 0, 0);
	}

	private LatLng getPostLocation( Post post )
	{
		return new LatLng(Double.parseDouble(post.getLatitude()) , Double.parseDouble(post.getLongitude()) );
	}

	boolean bItemClicked = false;

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		if ( arg0.getId() == R.id.listMain )
		{
			bItemClicked = true;

			Post post = (Post) arg1.getTag();
			moveMap(getPostLocation(post) );

			showInfoWindowWithPost( post );
		}
	}

	public void initImageLoader()
	{
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
		.memoryCacheExtraOptions(100, 100) // default = device screen dimensions
		.diskCacheExtraOptions(100, 100, null)
		.threadPoolSize(3) // default
		.threadPriority(Thread.NORM_PRIORITY - 2) // default
		.tasksProcessingOrder(QueueProcessingType.FIFO) // default
		.denyCacheImageMultipleSizesInMemory()
		.memoryCache(new LruMemoryCache(2 * 1024 * 1024))
		.memoryCacheSize(2 * 1024 * 1024)
		.memoryCacheSizePercentage(13) // default
		.diskCacheSize(50 * 1024 * 1024)
		.diskCacheFileCount(100)
		.diskCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
		.defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
		.writeDebugLogs()
		.build();
		ImageLoader.getInstance().init(config);
	}

	boolean bSkipPostListLoading = true;

	@Override
	public void onCameraChange(CameraPosition arg0) {
		// TODO Auto-generated method stub
		double latitude = arg0.target.latitude;
		double longitude = arg0.target.longitude;

		Log.d("debug", "oncameraChange bItemClicked:" + bItemClicked + " position " + latitude + " " + longitude );

		if ( bItemClicked )
		{
			bItemClicked = false;
		}
		else
		{
			// 지도를 움직였을 때
			try
			{
				if ( bSkipPostListLoading == false )
				{
					User user = new User();
					user.setUserID("kim2509");
					user.setLatitude( String.valueOf( latitude ) );
					user.setLongitude( String.valueOf( longitude ) );
					execTransReturningString("/getPosts.do", mapper.writeValueAsString(user), 2);
				}

				bSkipPostListLoading = false;
			}
			catch( Exception ex )
			{
				Log.e("error", ex.getMessage());
			}
		}
	}

	@Override
	public boolean onMarkerClick(Marker arg0) {
		// TODO Auto-generated method stub
		bSkipPostListLoading = true;

		return false;
	}

	@Override
	public void onInfoWindowClick(Marker arg0) {
		// TODO Auto-generated method stub
		try
		{
			Post post = markersMap.get(arg0);
			Intent intent = new Intent( this, PostDetailActivity.class);
			intent.putExtra("post", post );
			startActivity(intent);	
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		}
		catch( Exception ex )
		{
			Log.e("error", ex.getMessage());
		}
	}

	private void showInfoWindowWithPost( Post post )
	{
		Iterator<Entry<Marker, Post>> it = markersMap.entrySet().iterator();
		while( it.hasNext() )
		{
			Entry<Marker,Post> entry = it.next();
			if ( entry.getValue() == post )
			{
				entry.getKey().showInfoWindow();
				break;
			}
		}
	}

	@Override
	public void doAction(String actionName, Object param) {
		try
		{
			// TODO Auto-generated method stub
			super.doAction(actionName, param);

			if ( "showUserInfo".equals( actionName ) && param != null && param instanceof User )
			{
				User user = (User) param;
				Intent intent = new Intent( getApplicationContext(), UserProfileActivity.class);
				intent.putExtra("user", user );
				startActivity(intent);
				overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.stay);
			}
			else if ( "showDetail".equals( actionName ) && param != null && param instanceof Post )
			{
				Post post = (Post) param;
				Intent intent = new Intent( this, PostDetailActivity.class);
				intent.putExtra("post", post );
				startActivity(intent);	
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		}
		catch( Exception ex )
		{
			Log.e("error", ex.getMessage());
		}
	}

	ScrollView mainScrollView = null;
	private void makeMapScrollable() {

		ImageView transparentImageView = (ImageView) findViewById(R.id.transparent_image);

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
	public void onConnected(Bundle arg0) {
		
		try
		{
			Log.i("onConnected", "onConnected");
			// TODO Auto-generated method stub
			Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
					mGoogleApiClient);
			if (mLastLocation != null) {
				
				if ( map != null )
				{
					// 최초 위치 로딩시 지도 이동.
					moveMap( new LatLng( mLastLocation.getLatitude() , mLastLocation.getLongitude() ) );
					CameraUpdate zoom=CameraUpdateFactory.zoomTo(ZoomLevel);
					map.animateCamera(zoom);
				}
				
				setMetaInfo("latitude", String.valueOf(mLastLocation.getLatitude()));
				setMetaInfo("longitude", String.valueOf(mLastLocation.getLongitude()));
				
				User user = new User();
				user.setUserID("kim2509");
				user.setLatitude( String.valueOf( mLastLocation.getLatitude() ) );
				user.setLongitude( String.valueOf( mLastLocation.getLongitude() ) );
				execTransReturningString("/updateUserLocation.do", mapper.writeValueAsString(user), 3);
			}			
		}
		catch( Exception ex )
		{
			Log.e("error", ex.getMessage());
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
	}
}
