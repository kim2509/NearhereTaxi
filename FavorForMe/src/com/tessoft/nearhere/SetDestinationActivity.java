package com.tessoft.nearhere;

import org.codehaus.jackson.type.TypeReference;

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
import com.tessoft.common.AddressTaskDelegate;
import com.tessoft.common.Constants;
import com.tessoft.common.GetAddressTask;
import com.tessoft.common.Util;
import com.tessoft.domain.APIResponse;
import com.tessoft.domain.Post;
import com.tessoft.domain.UserLocation;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

public class SetDestinationActivity extends BaseActivity 
implements OnMapReadyCallback, AddressTaskDelegate {

	GoogleMap map = null;
	int ZoomLevel = 14;
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

			if ( getIntent().getExtras().containsKey("departure"))
			{
				initLocation = (LatLng) getIntent().getExtras().get("departure");
			}
			else
			{
				initLocation = new LatLng( Double.parseDouble( getMetaInfoString("latitude") ),
						Double.parseDouble( getMetaInfoString("longitude") ));
			}
			
			if ( getIntent().getExtras().containsKey("title") )
			{
				setTitle( getIntent().getExtras().getString("title") ); 
			}
			
			if ( getIntent().getExtras().containsKey("subTitle") )
			{
				getActionBar().setSubtitle(getIntent().getExtras().getString("subTitle"));
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
	}

	@Override
	public void onMapReady(GoogleMap map ) {
		// TODO Auto-generated method stub
		try
		{
			this.map = map;

			moveMap(initLocation);
			addMarker( initLocation );
			
			selectedLocation = initLocation;
			getAddress(selectedLocation, 1);

			map.setMyLocationEnabled(true);

			map.setOnMapClickListener(new OnMapClickListener() {

				@Override
				public void onMapClick(LatLng location ) {
					// TODO Auto-generated method stub
					SetDestinationActivity.this.map.clear();
					
					selectedLocation = location;
					
					addMarker( location );
					getAddress(location, 1);
				}
			});
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	public void getAddress(LatLng location, int requestCode ) {
		Location loc = new Location("");
		loc.setLatitude(location.latitude);
		loc.setLongitude(location.longitude);
		
		Location[] locs = new Location[1];
		locs[0] = loc;
		new GetAddressTask( getApplicationContext(), this, requestCode ).execute(locs);
	}
	
	public void addMarker( LatLng location )
	{
		map.clear();
		marker = map.addMarker(new MarkerOptions()
		.position( location )
		.title("목적지"));
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
	public void onAddressTaskPostExecute(int requestCode, Object result) {
		// TODO Auto-generated method stub
		
		TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
		String title = "";
		
		selectedAddress = Util.getDongAddressString( result );
		
		title = "위치 : " + Util.getDongAddressString( result );
		txtTitle.setText( title );
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
			
			Intent data = new Intent();
			data.putExtra("address", selectedAddress);
			data.putExtra("location", selectedLocation);
			setResult( RESULT_OK, data);
			finish();
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	String selectedAddress = "";
	LatLng selectedLocation = null;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.set_destination, menu);
		
		// Associate searchable configuration with the SearchView
	    SearchManager searchManager =
	           (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    SearchView searchView =
	            (SearchView) menu.findItem(R.id.action_search).getActionView();
//	    searchView.setSearchableInfo(
//	            searchManager.getSearchableInfo(getComponentName()));
	    
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

		if ( getIntent().getExtras() != null && getIntent().getExtras().containsKey("anim1") )
		{
			overridePendingTransition( getIntent().getExtras().getInt("anim1"), getIntent().getExtras().getInt("anim2"));
		}
		else
			overridePendingTransition(R.anim.stay, R.anim.slide_out_to_right);
	}
}
