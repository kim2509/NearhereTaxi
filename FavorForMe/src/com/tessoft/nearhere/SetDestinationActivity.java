package com.tessoft.nearhere;

import org.codehaus.jackson.type.TypeReference;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tessoft.domain.APIResponse;
import com.tessoft.domain.TaxiPost;
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
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SetDestinationActivity extends BaseActivity 
implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener {

	GoogleMap map = null;
	GoogleApiClient mGoogleApiClient = null;
	int ZoomLevel = 16;
	Marker marker = null;
	String command = "";
	LatLng initLocation = null;

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

			if ( getIntent().getExtras() != null && getIntent().getExtras().get("command") != null )
			{
				command = getIntent().getExtras().get("command").toString();

				if ( "약관동의".equals( command ) ) 
					setTitle("집 위치 선택");
				else if ( "departure".equals( command ) || "new".equals( command ))
				{
					setTitle("출발지 선택");

					if ( getIntent().getExtras().containsKey("departure"))
					{
						initLocation = (LatLng) getIntent().getExtras().get("departure");
					}
					else
					{
						initLocation = new LatLng( Double.parseDouble( getMetaInfoString("latitude") ),
								Double.parseDouble( getMetaInfoString("longitude") ));
					}
				}
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

		if ( mGoogleApiClient.isConnected() )
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

			if ( ("new".equals( command ) || "departure".equals( command )) && initLocation != null )
			{
				moveMap(initLocation);
				if ( "departure".equals( command ) )
					addMarker( initLocation );
			}
			else
				mGoogleApiClient.connect();

			map.setMyLocationEnabled(true);

			map.setOnMapClickListener(new OnMapClickListener() {

				@Override
				public void onMapClick(LatLng arg0) {
					// TODO Auto-generated method stub
					showToastMessage( getResources().getString(R.string.set_destination_guide1) );
				}
			});

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

	public void goTaxiTutorialActivity()
	{
		Intent intent = new Intent( this, TaxiTutorialActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
		finish();
	}

	LatLng departure = null;
	LatLng destination = null;

	public void setDestination( View v )
	{
		try
		{
			if ( marker == null )
			{
				showOKDialog("지점을 선택해 주십시오.", null);
				return;
			}

			if ( "약관동의".equals( command ) ) 
			{
				setProgressBarIndeterminateVisibility(true);
				UserLocation userLocation = new UserLocation();
				userLocation.setUser( getLoginUser() );
				userLocation.setLocationName("집");
				userLocation.setLatitude( String.valueOf( marker.getPosition().latitude ) );
				userLocation.setLongitude( String.valueOf( marker.getPosition().longitude ) );

				sendHttp("/taxi/updateUserLocation.do", mapper.writeValueAsString(userLocation), 1);

				return;
			}
			else if ( "departure".equals( command ) )
			{
				LatLng location = marker.getPosition();
				Intent intent = new Intent();
				intent.putExtra("location", location);
				setResult(1, intent );
				finish();
			}
			else if ( "new".equals( command ) )
			{
				departure = marker.getPosition();
				map.clear();
				setTitle("목적지 선택");	
				showToastMessage("이제 목적지를 선택해 주세요.");
				TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
				txtTitle.setText("목적지를 선택해 주십시오.");
				command = "selectDestination";
			}	
			else if ( "selectDestination".equals( command ) )
			{
				destination = marker.getPosition();
				findViewById(R.id.layoutHeader).setVisibility(ViewGroup.GONE);
				findViewById(R.id.map).setVisibility(ViewGroup.GONE);
				findViewById(R.id.layoutFooter).setVisibility(ViewGroup.GONE);
				findViewById(R.id.layoutInput).setVisibility(ViewGroup.VISIBLE);
				command = "addPost";
			}
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	public void addPost( View v )
	{
		try
		{
			TaxiPost post = new TaxiPost();

			EditText edtMessage = (EditText) findViewById(R.id.edtMessage);

			if ( TextUtils.isEmpty( edtMessage.getText() ) )
			{
				edtMessage.setError("내용을 입력해 주십시오.");
				return;
			}

			post.setMessage( edtMessage.getText().toString() );
			post.setFromLatitude( String.valueOf( departure.latitude) );
			post.setFromLongitude( String.valueOf( departure.longitude) );
			post.setLatitude( String.valueOf( destination.latitude) );
			post.setLongitude( String.valueOf( destination.longitude) );
			post.setUser( getLoginUser() );

			setProgressBarIndeterminateVisibility(true);
			sendHttp("/taxi/insertPost.do", mapper.writeValueAsString(post), 1);
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

			setProgressBarIndeterminateVisibility(false);

			APIResponse response = mapper.readValue(result.toString(), new TypeReference<APIResponse>(){});

			if ( "0000".equals( response.getResCode() ) )
			{
				if ( "addPost".equals( command ) )
				{
					LatLng location = marker.getPosition();
					Intent intent = new Intent();
					intent.putExtra("reload", true);
					setResult(2, intent );
					finish();
				}
				else
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
}
