package com.tessoft.favorforme;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.tessoft.common.HttpTransactionReturningString;
import com.tessoft.common.TransactionDelegate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class BaseActivity extends ActionBarActivity implements TransactionDelegate{

	LocationManager locationManager;
	Location location;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//location = getCurrentGPSLocation();
	}

	public String getOSVersion()
	{
		return Build.VERSION.RELEASE;
	}
	
	public String getPackageVersion()
	{
		PackageInfo pInfo;
		try {
			
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			
			return pInfo.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "";
	}
	
	public String getUniqueDeviceID()
	{
		final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

	    final String tmDevice, tmSerial, androidId;
	    tmDevice = "" + tm.getDeviceId();
	    tmSerial = "" + tm.getSimSerialNumber();
	    androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

	    UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
	    return deviceUuid.toString();
	}
	
	public void showOKDialog( String message, final Object param )
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage( message )
		       .setCancelable(false)
		       .setPositiveButton("Ȯ��", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                okClicked( param );
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	public void okClicked( Object param )
	{
		
	}
	
	public void showYesNoDialog( String message, final Object param )
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage( message )
		       .setCancelable(false)
		       .setPositiveButton("��", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   yesClicked( param );
		           }
		       })
		       .setNegativeButton("�ƴϿ�", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   noClicked( param );
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	public void yesClicked( Object param )
	{
		
	}
	
	public void noClicked( Object param )
	{
		
	}
	
	public void setMetaInfo( String key, String value )
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( this );
		SharedPreferences.Editor editor = settings.edit();
		editor.putString( key, value );
		editor.commit();
	}
	
	public String getMetaInfoString( String key )
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( this );
		
		String value = settings.getString(key, "");
		if ("".equals( value ))
			return "";
		
	    return value;
	}
	
	public void showToastMessage( String message )
	{
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
	
	public void writeLog( String log )
	{
		Log.i("publicChat", log );
	}
	
	public void execTransReturningString( String url, Object request, int requestCode )
	{
		new HttpTransactionReturningString( this, url, requestCode ).execute( request );
	}
	
	public void doPostTransaction( int requestCode, Object result )
	{
		
	}
	
	public void catchException ( Exception ex )
	{
		writeLog( ex.getMessage() );
	}
	
	public Location getCurrentGPSLocation()
	{
		String context = Context.LOCATION_SERVICE;
		locationManager = (LocationManager)getSystemService(context);
		
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);// ��Ȯ��
		criteria.setPowerRequirement(Criteria.POWER_LOW); // ��� �Һ�
		criteria.setAltitudeRequired(false); // �? ��뿩��
		criteria.setBearingRequired(false); //
		criteria.setSpeedRequired(false); // �ӵ�
		criteria.setCostAllowed(true); // ��������
		
		String provider = locationManager.getBestProvider(criteria, true);
		return locationManager.getLastKnownLocation(provider);
	}
	
	public String getCurrentAddress()
	{
		if ( location == null ) return "";
		
		double latitude = location.getLatitude(); // ����
		double longitude = location.getLongitude(); // �浵
		
		Geocoder gcK = new Geocoder(getApplicationContext(),Locale.KOREA);
		
		try {
            List<Address>  addresses = gcK.getFromLocation(latitude, longitude, 1);
            StringBuilder sb = new StringBuilder();

            if (addresses.size() > 0) {
                for (Address addr : addresses) {
                    sb.append(addr.getMaxAddressLineIndex()).append("********\n");
                    for (int i=0;i < addr.getMaxAddressLineIndex();i++)
                        sb.append(addr.getAddressLine(i)).append("<< \n\n");
                }
                sb.append("===========\n");
               
                Address address = addresses.get(0);
                sb.append(address.getCountryName()).append("\n");
                sb.append(address.getPostalCode()).append("\n");
                sb.append(address.getLocality()).append("\n");
                sb.append(address.getThoroughfare()).append("\n");
                sb.append(address.getFeatureName()).append("\n\n");
                sb.append(address.getAddressLine(0)).append("\n\n");
               
                sb.append("").append("\n");
                sb.append("").append("\n");
               
                return sb.toString();
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		
		return "";
	}
}
