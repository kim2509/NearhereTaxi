package com.tessoft.nearhere;

import java.io.IOException;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.tessoft.common.Util;
import com.tessoft.domain.User;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends BaseActivity 
	implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener{

	public static boolean active = false;
	DrawerLayout mDrawerLayout = null;
	ListView mDrawerList = null;
	String mTitle = "";
	String[] mMenuList = null;
	private ActionBarDrawerToggle mDrawerToggle;
	private Fragment mainFragment = null;
	GoogleApiClient mGoogleApiClient = null;

	public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    String SENDER_ID = "30113798803";
    GoogleCloudMessaging gcm;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		try
		{
			super.onCreate(savedInstanceState);
			
			supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
			
			setContentView(R.layout.activity_main);
			
			initImageLoader();

			mMenuList = getResources().getStringArray(R.array.menu_list);
			mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
			mDrawerList = (ListView) findViewById(R.id.left_drawer);

			// Set the adapter for the list view
			mDrawerList.setAdapter(new ArrayAdapter<String>(this,
					R.layout.drawer_list_item, mMenuList));

			// Set the list's click listener
			mDrawerList.setOnItemClickListener( new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView parent, View view, int position, long id) {
					selectItem(position);
				}
			});

			mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
					R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

				/** Called when a drawer has settled in a completely closed state. */
				public void onDrawerClosed(View view) {
					super.onDrawerClosed(view);
					invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
				}

				/** Called when a drawer has settled in a completely open state. */
				public void onDrawerOpened(View drawerView) {
					super.onDrawerOpened(drawerView);
					invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
				}
			};

			// Set the drawer toggle as the DrawerListener
			mDrawerLayout.setDrawerListener(mDrawerToggle);

			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setHomeButtonEnabled(true);
			
			mainFragment = new TaxiFragment();

			// Insert the fragment by replacing any existing fragment
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
			.add(R.id.content_frame, mainFragment)
			.commit();
		
			buildGoogleApiClient();
			createLocationRequest();
			
			gcm = GoogleCloudMessaging.getInstance(this);
			regid = getMetaInfoString("registrationID");
			if ( regid.isEmpty() )
			{
				registerInBackground();
			}
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}

	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		active = true;
		
		mGoogleApiClient.connect();
		
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		active = false;
		
		stopLocationUpdates();
		
		mGoogleApiClient.disconnect();
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
		if (id == R.id.action_refresh) {
			return true;
		}

		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
	
	/** Swaps fragments in the main content view */
	private void selectItem(int position) {
		// Create a new fragment and specify the planet to show based on position
		
		boolean bFragment = true;
		
		if ( "홈".equals( mMenuList[position]) )
			mainFragment = new TaxiFragment();
		else if ( "내 정보".equals( mMenuList[position]) )
			mainFragment = new MyInfoFragment();
		else if ( "알림메시지".equals( mMenuList[position]) )
			mainFragment = new PushMessageListFragment();
		else if ( "쪽지함".equals( mMenuList[position]) )
			mainFragment = new MessageBoxFragment();
		else if ( "공지사항".equals( mMenuList[position]) )
			mainFragment = new NoticeListFragment();
		else
			mainFragment = new SettingsFragment();

		if ( bFragment )
		{
			// Insert the fragment by replacing any existing fragment
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
			.replace(R.id.content_frame, mainFragment)
			.commit();
			
			setTitle( mMenuList[position] );
		}
		else
		{
			Intent intent = new Intent( this, SettingsActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		}

		// Highlight the selected item, update the title, and close the drawer
		mDrawerList.setItemChecked(position, true);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title.toString();
		getActionBar().setTitle(mTitle);
	}


	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
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
	
	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
		.addConnectionCallbacks(this)
		.addOnConnectionFailedListener(this)
		.addApi(LocationServices.API)
		.build();
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		try
		{
			setMetaInfo("latitude", String.valueOf( location.getLatitude()));
			setMetaInfo("longitude", String.valueOf( location.getLongitude()));
			
			if ( mainFragment instanceof TaxiFragment )
			{
				TaxiFragment f = (TaxiFragment) mainFragment;
				f.updateAddress( new LatLng( location.getLatitude(), location.getLongitude() ) );
			}
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		try
		{
			startLocationUpdates();
		}
		catch( Exception ex )
		{
			
		}
	}

	LocationRequest mLocationRequest = null;
	protected void createLocationRequest() {
		mLocationRequest = new LocationRequest();
	    mLocationRequest.setInterval(10000);
	    mLocationRequest.setFastestInterval(5000);
	    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}
	
	protected void startLocationUpdates() {
		LocationServices.FusedLocationApi.requestLocationUpdates(
	            mGoogleApiClient, mLocationRequest, this);
	}
	
	protected void stopLocationUpdates() {
	    LocationServices.FusedLocationApi.removeLocationUpdates(
	            mGoogleApiClient, this);
	}
	
	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		
	}

	static final String TAG = "Nearhere";
	
	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences(Context context) {
	    // This sample app persists the registration ID in shared preferences, but
	    // how you store the regID in your app is up to you.
	    return getSharedPreferences(MainActivity.class.getSimpleName(),
	            Context.MODE_PRIVATE);
	}
	
	private static int getAppVersion(Context context) {
	    try {
	        PackageInfo packageInfo = context.getPackageManager()
	                .getPackageInfo(context.getPackageName(), 0);
	        return packageInfo.versionCode;
	    } catch (NameNotFoundException e) {
	        // should never happen
	        throw new RuntimeException("Could not get package name: " + e);
	    }
	}
	
	String regid;
	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and the app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance( getApplicationContext() );
					}
					regid = gcm.register(SENDER_ID);
					msg = "Device registered, registration ID=" + regid;

					// You should send the registration ID to your server over HTTP, so it
					// can use GCM/HTTP or CCS to send messages to your app.
					sendRegistrationIdToBackend( regid );

					setMetaInfo("registrationID",  regid );
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				Log.i(TAG, msg);
			}
		}.execute(null, null, null);
	}

	public void sendRegistrationIdToBackend( String regid )
	{
		try
		{
			User user = getLoginUser();
			user.setRegID(regid);
			sendHttp("/taxi/updateUserRegID.do", mapper.writeValueAsString( user ), 1);
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
			super.doPostTransaction(requestCode, result);
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}
}
