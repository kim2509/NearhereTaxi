package com.tessoft.favorforme;

import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tessoft.common.MainArrayAdapter;
import com.tessoft.domain.ListItemModel;
import com.tessoft.domain.Post;
import com.tessoft.domain.User;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;

public class MainActivity extends BaseActivity implements OnMapReadyCallback{

	ListView listMain = null;
	GoogleMap map = null;
	LocationManager mLocationManager = null;
	
	private final LocationListener mLocationListener = new LocationListener() {
	    @Override
	    public void onLocationChanged(final Location location) {
	        //your code here
	    	map.addMarker(new MarkerOptions()
			.position(new LatLng(location.getLongitude(), location.getLatitude()))
			.title("Marker"));
	    	
	    	CameraUpdate center=
					CameraUpdateFactory.newLatLng(new LatLng(location.getLongitude()
							, location.getLatitude()));
			CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);

			map.moveCamera(center);
			map.animateCamera(zoom);
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
	protected void onCreate(Bundle savedInstanceState) {

		try
		{
			super.onCreate(savedInstanceState);

			//getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
			getActionBar().hide();

			setContentView(R.layout.activity_main);
			
			mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, mLocationListener);

			MapFragment mapFragment = (MapFragment) getFragmentManager()
					.findFragmentById(R.id.map);
			mapFragment.getMapAsync(this);

			listMain = (ListView) findViewById(R.id.listMain);

			ObjectMapper mapper = new ObjectMapper();

			User user = new User();
			user.setUserID("kim2509");
			user.setLatitude("126.968209");
			user.setLongitude("37.470763");

			execTransReturningString("/getPosts.do", mapper.writeValueAsString(user), 1);
		}
		catch( Exception ex )
		{

		}
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

	@Override
	public void doPostTransaction(int requestCode, Object result) {
		// TODO Auto-generated method stub
		try
		{
			super.doPostTransaction(requestCode, result);

			ObjectMapper mapper = new ObjectMapper();
			List<ListItemModel> postList = mapper.readValue(result.toString(), new TypeReference<List<Post>>(){});

			MainArrayAdapter adapter = new MainArrayAdapter( getApplicationContext(), 0 );
			adapter.setItemList( postList );
			listMain.setAdapter( adapter );
			adapter.notifyDataSetChanged();
		}
		catch(Exception ex )
		{
			showToastMessage(ex.getMessage());
		}
	}

	@Override
	public void onMapReady(GoogleMap map) {
		this.map = map;
		
		
		map.setMyLocationEnabled(true);

		
	}
}
