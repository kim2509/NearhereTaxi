package com.tessoft.nearhere;

import java.io.IOException;
import java.util.HashMap;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.type.TypeReference;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
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
import com.tessoft.common.AddressTaskDelegate;
import com.tessoft.common.Constants;
import com.tessoft.common.GetAddressTask;
import com.tessoft.common.MainMenuArrayAdapter;
import com.tessoft.common.Util;
import com.tessoft.domain.APIResponse;
import com.tessoft.domain.MainMenuItem;
import com.tessoft.domain.User;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends BaseActivity 
implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, AddressTaskDelegate{

	public static boolean active = false;
	DrawerLayout mDrawerLayout = null;
	ListView mDrawerList = null;
	String mTitle = "";
	private ActionBarDrawerToggle mDrawerToggle;
	private Fragment mainFragment = null;
	GoogleApiClient mGoogleApiClient = null;

	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	protected static final int GET_UNREAD_COUNT = 3;
	String SENDER_ID = "30113798803";
	GoogleCloudMessaging gcm;

	MainMenuArrayAdapter adapter = null;
	
	public static String latitude = "";
	public static String longitude = "";
	public static String address = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		try
		{
			super.onCreate(savedInstanceState);

			getApplicationContext().registerReceiver(mMessageReceiver, new IntentFilter("updateUnreadCount"));

			supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

			setContentView(R.layout.activity_main);

			initImageLoader();

			mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
			mDrawerList = (ListView) findViewById(R.id.left_drawer);

			// Set the adapter for the list view
			adapter = new MainMenuArrayAdapter( getApplicationContext(), 0);
			adapter.add(new MainMenuItem("홈"));
			adapter.add(new MainMenuItem("내 정보"));
			adapter.add(new MainMenuItem("알림메시지"));
			adapter.add(new MainMenuItem("쪽지함"));
			adapter.add(new MainMenuItem("공지사항"));
			adapter.add(new MainMenuItem("설정"));
			adapter.add(new MainMenuItem("로그아웃"));

			mDrawerList.setAdapter( adapter );

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

			// 푸시 토큰을 생성한다.
			gcm = GoogleCloudMessaging.getInstance(this);
			regid = getMetaInfoString("registrationID");
			if ( Util.isEmptyString( regid ))
				registerInBackground();
			
			// google play sdk 설치 여부를 검사한다.
			checkPlayServices();
			
			getUnreadCount();
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		try
		{
			super.onStart();
			if ( mGoogleApiClient != null && mGoogleApiClient.isConnected() == false )
				mGoogleApiClient.connect();	
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		try
		{
			super.onStop();
			
			if ( mGoogleApiClient != null && mGoogleApiClient.isConnected() )
			{
				stopLocationUpdates();
				mGoogleApiClient.disconnect();
			}
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		try
		{
						
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	public  void getUnreadCount() throws IOException,
	JsonGenerationException, JsonMappingException {
		HashMap hash = getDefaultRequest();
		hash.put("userID", getLoginUser().getUserID() );
		hash.put("lastNoticeID", getMetaInfoString("lastNoticeID"));
		sendHttp("/taxi/getUnreadCount.do", mapper.writeValueAsString(hash), GET_UNREAD_COUNT );
	}

	//This is the handler that will manager to process the broadcast intent
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			try
			{
				if ( "updateUnreadCount".equals( intent.getAction() ) )
					getUnreadCount();
			}
			catch( Exception ex )
			{
				catchException(this, ex);
			}
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
		if (id == R.id.action_refresh) {

			Intent intent = new Intent("refreshContents");
			getApplicationContext().sendBroadcast(intent);
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

		MainMenuItem item = adapter.getItem( position );

		if ( "홈".equals( item.getMenuName() ) )
			mainFragment = new TaxiFragment();
		else if ( "내 정보".equals( item.getMenuName() ) )
			mainFragment = new MyInfoFragment();
		else if ( "알림메시지".equals( item.getMenuName() ) )
			mainFragment = new PushMessageListFragment();
		else if ( "쪽지함".equals( item.getMenuName() ) )
			mainFragment = new MessageBoxFragment();
		else if ( "공지사항".equals( item.getMenuName() ) )
			mainFragment = new NoticeListFragment();
		else if ( "설정".equals( item.getMenuName() ) )
			mainFragment = new SettingsFragment();
		else if ( "로그아웃".equals( item.getMenuName() ) )
		{
			showYesNoDialog("확인", "정말 로그아웃 하시겠습니까?", "logout" );
			mDrawerList.setItemChecked(position, false);
			mDrawerLayout.closeDrawer(mDrawerList);
			return;
		}

		if ( bFragment )
		{
			// Insert the fragment by replacing any existing fragment
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
			.replace(R.id.content_frame, mainFragment)
			.addToBackStack(null)
			.commit();

			setTitle( item.getMenuName() );
		}
		else
		{
			Intent intent = new Intent( this, SettingsActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
		}

		// Highlight the selected item, update the title, and close the drawer
		mDrawerList.setItemChecked(position, true);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void yesClicked(Object param) {
		// TODO Auto-generated method stub
		try
		{
			super.yesClicked(param);

			if ( "logout".equals( param ) )
			{
				setProgressBarIndeterminateVisibility(true);
				sendHttp("/taxi/logout.do", mapper.writeValueAsString( getLoginUser() ), 2 );
			}			
		}
		catch( Exception ex )
		{

		}
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
			MainActivity.latitude = String.valueOf( location.getLatitude() );
			MainActivity.longitude = String.valueOf( location.getLongitude() );

			new GetAddressTask( this, this, 1 ).execute(location);

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
	public void onAddressTaskPostExecute(int requestCode, Object result) {
		// TODO Auto-generated method stub
		MainActivity.address = Util.getDongAddressString( result );
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
			if ( Constants.FAIL.equals(result) )
			{
				setProgressBarIndeterminateVisibility(false);
				showOKDialog("통신중 오류가 발생했습니다.\r\n다시 시도해 주십시오.", null);
				return;
			}

			super.doPostTransaction(requestCode, result);

			APIResponse response = mapper.readValue(result.toString(), new TypeReference<APIResponse>(){});
			
			if ( "0000".equals( response.getResCode() ) )
			{
				if ( requestCode == 2 )
				{
					setProgressBarIndeterminateVisibility(false);
					
//					setMetaInfo("userNo", "");
//					setMetaInfo("registerUserFinished", "");
					setMetaInfo("logout", "true");
					setMetaInfo("userID", "");
					setMetaInfo("userName", "");
					setMetaInfo("profileImageURL", "");
					setMetaInfo("registrationID", "");

					Intent intent = null;
					intent = new Intent( getApplicationContext(), RegisterUserActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					finish();

					overridePendingTransition(android.R.anim.fade_in, 
							android.R.anim.fade_out);
				}
				else if ( requestCode == GET_UNREAD_COUNT )
				{
					HashMap<String, Integer> hash = (HashMap<String,Integer>) response.getData();
					
					int pushCount = hash.get("pushCount");
					int messageCount = hash.get("messageCount");
					
					for ( int i = 0; i < adapter.getCount(); i++ )
					{
						MainMenuItem item = adapter.getItem(i);
						if ( "알림메시지".equals( item.getMenuName() ) )
							item.setNotiCount( pushCount );
						else if ( "쪽지함".equals( item.getMenuName() ) )
							item.setNotiCount( messageCount );
					}
					
					if ( pushCount + messageCount > 0 )
					{
						mDrawerLayout.openDrawer(mDrawerList);
						getActionBar().setIcon(R.drawable.icon_new);
					}
					else
						getActionBar().setIcon(R.color.transparent);
					
					adapter.notifyDataSetChanged();
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

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		getApplicationContext().unregisterReceiver(mMessageReceiver);
	}

	boolean doubleBackToExitPressedOnce = false;
	@Override
	public void onBackPressed() {
		
		if ( mainFragment instanceof TaxiFragment == false )
		{
			selectItem(0);
			return;
		}
		
		if (doubleBackToExitPressedOnce) {
			super.onBackPressed();
			return;
		}

		this.doubleBackToExitPressedOnce = true;
		Toast.makeText(this, "이전 버튼을 한번 더 누르면 종료합니다.", Toast.LENGTH_SHORT).show();

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				doubleBackToExitPressedOnce=false;                       
			}
		}, 2000);
	}
	
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i("intro", "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}
}
