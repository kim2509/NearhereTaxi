package com.tessoft.nearhere;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
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
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.tessoft.common.AddressTaskDelegate;
import com.tessoft.common.Constants;
import com.tessoft.common.GetAddressTask;
import com.tessoft.common.MainMenuArrayAdapter;
import com.tessoft.common.Util;
import com.tessoft.domain.APIResponse;
import com.tessoft.domain.MainMenuItem;
import com.tessoft.domain.User;
import com.tessoft.domain.UserLocation;
import com.tessoft.nearhere.fragment.MainFragment;
import com.tessoft.nearhere.fragment.MessageBoxFragment;
import com.tessoft.nearhere.fragment.MyInfoFragment;
import com.tessoft.nearhere.fragment.NoticeListFragment;
import com.tessoft.nearhere.fragment.PushMessageListFragment;
import com.tessoft.nearhere.fragment.TaxiFragment;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.telephony.SmsManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends BaseActivity 
implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, AddressTaskDelegate{

	public static boolean active = false;
	DrawerLayout mDrawerLayout = null;
	ListView mDrawerList = null;
	String mTitle = "";
	private ActionBarDrawerToggle mDrawerToggle;
	private Fragment currentFragment = null;
	GoogleApiClient mGoogleApiClient = null;

	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	protected static final int GET_UNREAD_COUNT = 3;
	private static final int HTTP_LEAVE = 10;
	private static final int HTTP_UPDATE_LOCATION = 20;

	String SENDER_ID = "30113798803";
	GoogleCloudMessaging gcm;

	MainMenuArrayAdapter adapter = null;

	public static String latitude = "";
	public static String longitude = "";
	public static String fullAddress = "";

	public static String address = "";
	DisplayImageOptions options = null;
	
	View header = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		try
		{
			super.onCreate(savedInstanceState);

			supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

			setContentView(R.layout.activity_main);

			initLeftMenu();

//			currentFragment = new MainFragment();
			currentFragment = new TaxiFragment();

			// Insert the fragment by replacing any existing fragment
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
			.add(R.id.content_frame, currentFragment)
			.commit();

			buildGoogleApiClient();

			// 마지막 위치업데이트 시간 clear
			setMetaInfo("lastLocationUpdatedDt", "");

			createLocationRequest();

			// 푸시 토큰을 생성한다.
			gcm = GoogleCloudMessaging.getInstance(this);
			regid = getMetaInfoString("registrationID");
			if ( Util.isEmptyString( regid ))
				registerInBackground();

			// google play sdk 설치 여부를 검사한다.
			checkPlayServices();

			checkIfGPSEnabled();

			MainActivity.active = true;
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	private void initLeftMenu() throws Exception 
	{

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		header = getLayoutInflater().inflate(R.layout.list_my_info_header, null);
		header.setTag(new MainMenuItem("header"));
		mDrawerList.addHeaderView(header);

		options = new DisplayImageOptions.Builder()
		.resetViewBeforeLoading(true)
		.cacheInMemory(true)
		.showImageOnLoading(R.drawable.no_image)
		.showImageForEmptyUri(R.drawable.no_image)
		.showImageOnFail(R.drawable.no_image)
		.displayer(new RoundedBitmapDisplayer(20))
		.delayBeforeLoading(100)
		.build();

		// Set the adapter for the list view
		adapter = new MainMenuArrayAdapter( getApplicationContext(), 0);
		adapter.add(new MainMenuItem("홈"));
		//		adapter.add(new MainMenuItem("내 정보"));
		adapter.add(new MainMenuItem("알림메시지"));
		adapter.add(new MainMenuItem("쪽지함"));
		adapter.add(new MainMenuItem("공지사항"));
		adapter.add(new MainMenuItem("설정"));
		adapter.add(new MainMenuItem("로그아웃"));

		mDrawerList.setAdapter( adapter );
		
		reloadProfile();

		// Set the list's click listener
		mDrawerList.setOnItemClickListener( new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView parent, View view, int position, long id) {

				MainMenuItem item = (MainMenuItem) view.getTag();
				selectItem( item );
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

				mDrawerList.setAdapter( adapter );
				
				reloadProfile();

				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};

		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	private void reloadProfile() {
		ImageView imageView = (ImageView) header.findViewById(R.id.imgProfile);
		
		if ( Util.isEmptyString( getLoginUser().getProfileImageURL() ))
			imageView.setImageResource(R.drawable.no_image);
		else
		{
			ImageLoader.getInstance().displayImage( Constants.thumbnailImageURL + 
					getLoginUser().getProfileImageURL() , imageView, options );			
		}

		TextView txtUserName = (TextView) header.findViewById(R.id.txtUserName);
		txtUserName.setText( getLoginUser().getUserName() );
		
		TextView txtCreditValue = (TextView) header.findViewById(R.id.txtCreditValue);
		txtCreditValue.setText( getLoginUser().getProfilePoint() + "%");
		ProgressBar progressCreditValue = (ProgressBar) findViewById(R.id.progressCreditValue);
		progressCreditValue.setProgress( Integer.parseInt( getLoginUser().getProfilePoint() ) );
		
		imageView.setVisibility(ViewGroup.VISIBLE);
		imageView.invalidate();
		txtCreditValue.setVisibility(View.VISIBLE);
		txtCreditValue.invalidate();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		try
		{
			super.onStart();
			if ( mGoogleApiClient != null && mGoogleApiClient.isConnected() == false )
				mGoogleApiClient.connect();

			registerReceiver(mMessageReceiver, new IntentFilter("updateUnreadCount"));
			registerReceiver(mMessageReceiver, new IntentFilter("startLocationUpdate"));
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

			unregisterReceiver(mMessageReceiver);
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
			getUnreadCount();
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	public  void getUnreadCount() throws Exception 
	{
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
				else if ( "startLocationUpdate".equals( intent.getAction() ) )
					startLocationUpdates();
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
	private void selectItem( MainMenuItem item ) {
		// Create a new fragment and specify the planet to show based on position

		boolean bFragment = true;

		int position = adapter.getPosition(item);

		if ( "홈".equals( item.getMenuName() ) )
			currentFragment = new TaxiFragment();
		else if ( "header".equals( item.getMenuName() ) || "내 정보".equals( item.getMenuName() ) )
			currentFragment = new MyInfoFragment();
		else if ( "알림메시지".equals( item.getMenuName() ) )
			currentFragment = new PushMessageListFragment();
		else if ( "쪽지함".equals( item.getMenuName() ) )
			currentFragment = new MessageBoxFragment();
		else if ( "공지사항".equals( item.getMenuName() ) )
			currentFragment = new NoticeListFragment();
		else if ( "설정".equals( item.getMenuName() ) )
			currentFragment = new SettingsFragment();
		else if ( "로그아웃".equals( item.getMenuName() ) )
		{
			showYesNoDialog("확인", "정말 로그아웃 하시겠습니까?", "logout" );
			mDrawerList.setItemChecked(position, false);
			mDrawerLayout.closeDrawer(mDrawerList);
			return;
		}

		// Insert the fragment by replacing any existing fragment
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction()
		.replace(R.id.content_frame, currentFragment)
		.addToBackStack(null)
		.commit();

		setTitle( item.getMenuName() );

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
		//		getActionBar().setTitle(mTitle);
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

			if ( currentFragment instanceof TaxiFragment )
			{
				TaxiFragment f = (TaxiFragment) currentFragment;
				f.updateAddress( new LatLng( location.getLatitude(), location.getLongitude() ) );
			}

			stopLocationUpdates();
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	@Override
	public void onAddressTaskPostExecute(int requestCode, Object result) {
		// TODO Auto-generated method stub

		try
		{
			MainActivity.address = Util.getDongAddressString( result );
			MainActivity.fullAddress = result.toString();

			Intent intent = new Intent("currentLocationChanged");
			intent.putExtra("latitude", MainActivity.latitude );
			intent.putExtra("longitude", MainActivity.longitude );
			intent.putExtra("fullAddress", result.toString() );
			sendBroadcast(intent);

			if ( bMyLocationUpdated == false )
				updateMyLocation();			
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	boolean bMyLocationUpdated = false;

	public void updateMyLocation() throws Exception
	{
		if ( !Util.isEmptyString( MainActivity.latitude ) && !Util.isEmptyString( MainActivity.longitude ) )
		{
			User user = getLoginUser();
			UserLocation userLocation = new UserLocation();
			userLocation.setUser( user );
			userLocation.setLocationName("현재위치");
			userLocation.setLatitude( MainActivity.latitude );
			userLocation.setLongitude( MainActivity.longitude );
			userLocation.setAddress( MainActivity.address );
			sendHttp("/taxi/updateUserLocation.do", mapper.writeValueAsString( userLocation ), HTTP_UPDATE_LOCATION );			
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
		mLocationRequest.setInterval(1000);
		mLocationRequest.setFastestInterval(1000);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}

	protected void startLocationUpdates() {

		if ( mGoogleApiClient == null || mGoogleApiClient.isConnected() == false )
			return;

		// 2분마다 갱신하기 위해 추가한 로직.
		// 너무 자주 갱신하는 문제 수정
		Date now = new Date();
		Date lastLocationUpdatedDt = new Date();

		boolean bShouldUpdate = false;
		if ( Util.isEmptyString( getMetaInfoString("lastLocationUpdatedDt")))
			bShouldUpdate = true;
		else
		{
			lastLocationUpdatedDt.setTime( Long.parseLong( getMetaInfoString("lastLocationUpdatedDt") ) );

			Calendar c = Calendar.getInstance(); 
			c.setTime(lastLocationUpdatedDt); 
			c.add(Calendar.MINUTE, 2);
			lastLocationUpdatedDt = c.getTime();

			if(lastLocationUpdatedDt.before(now))
				bShouldUpdate = true;
		}

		if ( bShouldUpdate )
		{
			LocationServices.FusedLocationApi.requestLocationUpdates(
					mGoogleApiClient, mLocationRequest, this);

			setMetaInfo("lastLocationUpdatedDt", String.valueOf( now.getTime() ));			
		}
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
				showOKDialog("통신중 오류가 발생했습니다.\r\n다시 시도해 주십시오.", null);
				return;
			}

			super.doPostTransaction(requestCode, result);

			APIResponse response = mapper.readValue(result.toString(), new TypeReference<APIResponse>(){});

			if ( "0000".equals( response.getResCode() ) )
			{
				if ( requestCode == 2 )
				{
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

					int noticeCount = 0;
					if ( hash.containsKey("noticeCount") && hash.get("noticeCount") != null )
					{
						noticeCount = hash.get("noticeCount");
					}

					for ( int i = 0; i < adapter.getCount(); i++ )
					{
						MainMenuItem item = adapter.getItem(i);
						if ( "알림메시지".equals( item.getMenuName() ) )
							item.setNotiCount( pushCount );
						else if ( "쪽지함".equals( item.getMenuName() ) )
							item.setNotiCount( messageCount );
						else if ( "공지사항".equals( item.getMenuName() ) )
							item.setNotiCount( noticeCount );
					}

					if ( findViewById(R.id.btnLeftMenu) != null )
					{
						if ( pushCount + messageCount + noticeCount > 0 )
						{
							findViewById(R.id.btnLeftMenu).setBackgroundResource(R.drawable.top_ic_new);
						}
						else
							findViewById(R.id.btnLeftMenu).setBackgroundResource(R.drawable.top_ic_menu_off);	
					}

					adapter.notifyDataSetChanged();
				}
				else if ( requestCode == HTTP_UPDATE_LOCATION )
					bMyLocationUpdated = true;
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

		try
		{
			super.onDestroy();
			MainActivity.active = false;	
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	public void openDrawerMenu( View v )
	{
		mDrawerLayout.openDrawer(mDrawerList);
	}

	boolean doubleBackToExitPressedOnce = false;
	@Override
	public void onBackPressed() {

		if ( currentFragment instanceof TaxiFragment == false )
		{
			reloadProfile();
			selectItem(new MainMenuItem("홈"));
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

	public void checkIfGPSEnabled()
	{
		final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

		if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
			buildAlertMessageNoGps();
		}
	}

	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("경고")
		.setMessage("GPS 가 꺼져 있으면 원활한 서비스를 제공받을 수 없습니다.\n활성화시키겠습니까?")
		.setCancelable(false)
		.setPositiveButton("예", new DialogInterface.OnClickListener() {
			public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
				startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			}
		})
		.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
				dialog.cancel();
			}
		});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub

		try
		{
			sendHttp("/taxi/statistics.do?name=exit", mapper.writeValueAsString( getLoginUser() ), HTTP_LEAVE);			
		}
		catch( Exception ex )
		{

		}

		super.finish();
	}
}
