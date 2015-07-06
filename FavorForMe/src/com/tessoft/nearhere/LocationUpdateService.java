package com.tessoft.nearhere;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.tessoft.common.AddressTaskDelegate;
import com.tessoft.common.GetAddressTask;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;

public class LocationUpdateService extends Service 
	implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, AddressTaskDelegate{

	GoogleApiClient mGoogleApiClient = null;
	String latitude = "";
	String longitude = "";
	public static String address = "";
	ObjectMapper mapper = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
        buildGoogleApiClient();
        
        createLocationRequest();
        
    	mapper = new ObjectMapper();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		
		try
		{
			// We want this service to continue running until it is explicitly
	        // stopped, so return sticky.
			if ( mGoogleApiClient != null && mGoogleApiClient.isConnected() == false )
				mGoogleApiClient.connect();
		}
		catch( Exception ex )
		{
		}
		
        return START_STICKY;
	}

	@Override
	public void onDestroy() { 

		try
		{
			if ( mGoogleApiClient != null && mGoogleApiClient.isConnected() )
			{
				stopLocationUpdates();
				mGoogleApiClient.disconnect();
			}
		}
		catch( Exception ex )
		{
			
		}
		
		super.onDestroy();
	}

    protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
		.addConnectionCallbacks(this)
		.addOnConnectionFailedListener(this)
		.addApi(LocationServices.API)
		.build();
	}
    
    LocationRequest mLocationRequest = null;
	protected void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(90000);
		mLocationRequest.setFastestInterval(90000);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
	
	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub

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
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		try
		{
			latitude = String.valueOf( location.getLatitude() );
			longitude = String.valueOf( location.getLongitude() );
			
			new GetAddressTask( this, this, 1 ).execute(location);
		}
		catch( Exception ex )
		{
		}
	}
	
	@Override
	public void onAddressTaskPostExecute(int requestCode, Object result) {
		// TODO Auto-generated method stub
		if ( result != null && result instanceof String )
		{
			address = result.toString();

//			User user = getLoginUser();
//			UserLocation userLocation = new UserLocation();
//			userLocation.setUser( user );
//			userLocation.setLocationName("현재위치");
//			userLocation.setLatitude( MainActivity.latitude );
//			userLocation.setLongitude( MainActivity.longitude );
//			userLocation.setAddress( MainActivity.address );
//			sendHttp("/taxi/updateUserLocation.do", mapper.writeValueAsString( userLocation ), HTTP_UPDATE_LOCATION );
			
			stopSelf();
			
		}
	}
}