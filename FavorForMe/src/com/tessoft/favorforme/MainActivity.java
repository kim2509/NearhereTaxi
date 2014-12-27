package com.tessoft.favorforme;

import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
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

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends BaseActivity implements OnMapReadyCallback, OnItemClickListener, 
	OnCameraChangeListener, OnMarkerClickListener{ //, OnScrollListener{

	ListView listMain = null;
	GoogleMap map = null;
	LocationManager mLocationManager = null;
	//	ScrollView mainScrollView = null;
	int ZoomLevel = 16;
	ObjectMapper mapper = new ObjectMapper();

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		try
		{
			super.onCreate(savedInstanceState);

			//getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
			//getActionBar().hide();

			setContentView(R.layout.activity_main);

			mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, mLocationListener);

			MapFragment mapFragment = (MapFragment) getFragmentManager()
					.findFragmentById(R.id.map);
			mapFragment.getMapAsync(this);

			listMain = (ListView) findViewById(R.id.listMain);
			//listMain.setOnScrollListener(this);
			listMain.setOnItemClickListener(this);
			
			initImageLoader();
			//mainScrollView = (ScrollView) findViewById(R.id.mainScrollView);
		}
		catch( Exception ex )
		{
			showToastMessage(ex.getMessage());
		}
	}

	private final LocationListener mLocationListener = new LocationListener() {
		@Override
		public void onLocationChanged(final Location location) {
			//your code here

		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}
	};


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

			MainArrayAdapter adapter = new MainArrayAdapter( getApplicationContext(), 0 );
			List<ListItemModel> postList = null;
			
			if ( requestCode == 1 )
			{
				MainInfo mainInfo = mapper.readValue(result.toString(), new TypeReference<MainInfo>(){});

				TextView txtNotice1 = (TextView) findViewById(R.id.txtNotice1);
				txtNotice1.setText("근처에 " + mainInfo.getPostCount() + "개의 심부름이 있습니다.");

				postList = (List<ListItemModel>) (Object) mainInfo.getPostList();
				adapter.setItemList( postList );
				listMain.setAdapter( adapter );
			}
			else if ( requestCode == 2 )
			{
				postList = (List<ListItemModel>) mapper.readValue(result.toString(), new TypeReference<List<Post>>(){});
				adapter.setItemList( postList );
				listMain.setAdapter( adapter );
			}
				
			putMarkersOnMap( postList );
			
			Post post = (Post) postList.get(0);
			Marker marker = (Marker) post.getTag();
			marker.showInfoWindow();	
		}
		catch(Exception ex )
		{
			showToastMessage(ex.getMessage());
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
			
			post.setTag( marker );
		}
	}

	@Override
	public void onMapReady(GoogleMap map) {

		try
		{
			this.map = map;

			map.setMyLocationEnabled(true);

			moveMap( new LatLng(37.470763 , 126.968209 ) );
			
			CameraUpdate zoom=CameraUpdateFactory.zoomTo(ZoomLevel);
			map.animateCamera(zoom);
			
			map.setOnCameraChangeListener(this);
			map.setOnMarkerClickListener(this);

			ObjectMapper mapper = new ObjectMapper();

			User user = new User();
			user.setUserID("kim2509");
			user.setLatitude("37.470763");
			user.setLongitude("126.968209");

			execTransReturningString("/getMainInfo.do", mapper.writeValueAsString(user), 1);
		}
		catch( Exception ex )
		{
			showToastMessage(ex.getMessage());
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
			Marker marker = (Marker) post.getTag();
			moveMap(getPostLocation(post) );
			marker.showInfoWindow();
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

	/*
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

		if (scrollState == SCROLL_STATE_IDLE )
		{
			int first = view.getFirstVisiblePosition();
			MainArrayAdapter adapter = (MainArrayAdapter) view.getAdapter();
			Post post = (Post) adapter.getItem(first);
			moveMap(getPostLocation(post), ZoomLevel );
			Marker marker = (Marker) post.getTag();
			marker.showInfoWindow();
		}
		
	}
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		switch(view.getId()) {
		case R.id.listMain:  

			break;
		}
	}
	
	*/
}
