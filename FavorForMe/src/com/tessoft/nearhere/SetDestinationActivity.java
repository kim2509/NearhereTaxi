package com.tessoft.nearhere;

import org.codehaus.jackson.type.TypeReference;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tessoft.domain.APIResponse;
import com.tessoft.domain.User;
import com.tessoft.domain.UserLocation;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

public class SetDestinationActivity extends BaseActivity implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener{

	GoogleMap map = null;
	GoogleApiClient mGoogleApiClient = null;
	int ZoomLevel = 16;
	Marker marker = null;
	String from = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		try
		{
			super.onCreate(savedInstanceState);
			
			supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
			
			setContentView(R.layout.activity_set_destination);
			
			MapFragment mapFragment = (MapFragment) getFragmentManager()
					.findFragmentById(R.id.map);
			mapFragment.getMapAsync(this);
			
			buildGoogleApiClient();
			
			if ( getIntent().getExtras() != null && getIntent().getExtras().get("from") != null )
			{
				from = getIntent().getExtras().get("from").toString();
				
				if ( "약관동의".equals( from ) ) 
					setTitle("집 위치 선택");
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

		
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		mGoogleApiClient.disconnect();
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
		getMenuInflater().inflate(R.menu.set_destination, menu);
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
	
	public void setDestination( View v )
	{
		try
		{
			if ( marker == null )
			{
				showOKDialog("지점을 선택해 주십시오.", null);
				return;
			}
			
			if ( "약관동의".equals( from ) ) 
			{
				setProgressBarIndeterminateVisibility(true);
				UserLocation userLocation = new UserLocation();
				userLocation.setUser( getLoginUser() );
				userLocation.setLocationName("집");
				userLocation.setLatitude( String.valueOf( marker.getPosition().latitude ) );
				userLocation.setLongitude( String.valueOf( marker.getPosition().longitude ) );
				
				execTransReturningString("/taxi/updateUserLocation.do", mapper.writeValueAsString(userLocation), 1);
				
				return;
			}
			else
			{
				LatLng location = marker.getPosition();
				
				setProgressBarIndeterminateVisibility(false);
				
				Intent intent = new Intent();
				intent.putExtra("reload", true);
				setResult(1, intent );
				finish();	
			}	
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
		
		overridePendingTransition(R.anim.stay, R.anim.slide_out_to_bottom);
	}

	@Override
	public void onMapReady(GoogleMap map ) {
		// TODO Auto-generated method stub
		try
		{
			this.map = map;
			
			mGoogleApiClient.connect();
			
			map.setMyLocationEnabled(true);
			
			map.setOnMapLongClickListener(new OnMapLongClickListener() {
				
				@Override
				public void onMapLongClick(LatLng location) {
					// TODO Auto-generated method stub
					addMarker( location );
					showToastMessage("선택되었습니다.");
				}
			});
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	public void addMarker( LatLng location )
	{
		map.clear();
		marker = map.addMarker(new MarkerOptions()
		.position( location )
		.title("목적지"));
	}
	
	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		try
		{
			Log.i("onConnected", "onConnected");
			
			Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
					mGoogleApiClient);
			
			if (mLastLocation != null) {
				
				if ( map != null )
				{
					// 최초 위치 로딩시 지도 이동.
					moveMap( new LatLng( mLastLocation.getLatitude() , mLastLocation.getLongitude() ) );
				}
			}	
		}
		catch( Exception ex )
		{
			catchException(this, ex);
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
	
	private void moveMap( LatLng location )
	{
		CameraUpdate center=
				CameraUpdateFactory.newLatLng( location );
		map.moveCamera(center);
		CameraUpdate zoom=CameraUpdateFactory.zoomTo(ZoomLevel);
		map.animateCamera(zoom);
	}
	
	@Override
	public void doPostTransaction(int requestCode, Object result) {
		// TODO Auto-generated method stub
		try
		{
			super.doPostTransaction(requestCode, result);
			
			setProgressBarIndeterminateVisibility(false);
			
			APIResponse response = mapper.readValue(result.toString(), new TypeReference<APIResponse>(){});
			
			if ( "0000".equals( response.getResCode() ) )
			{
				goTaxiTutorialActivity();
			}
			else
			{
				showOKDialog("위치 등록도중 오류가 발생했습니다.\r\n다시 시도해 주십시오.", null);
			}
			
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}
	
	public void goTaxiTutorialActivity()
	{
		Intent intent = new Intent( this, TaxiTutorialActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
		finish();
	}
}
