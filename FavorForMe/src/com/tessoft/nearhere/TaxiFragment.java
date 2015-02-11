package com.tessoft.nearhere;

import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.google.android.gms.maps.model.LatLng;
import com.tessoft.common.AdapterDelegate;
import com.tessoft.common.AddressTaskDelegate;
import com.tessoft.common.Constants;
import com.tessoft.common.GetAddressTask;
import com.tessoft.common.TaxiArrayAdapter;
import com.tessoft.common.TransactionDelegate;
import com.tessoft.common.Util;
import com.tessoft.domain.APIResponse;
import com.tessoft.domain.Post;
import com.tessoft.domain.User;
import com.tessoft.domain.UserLocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class TaxiFragment extends BaseFragment 
	implements AddressTaskDelegate, AdapterDelegate, TransactionDelegate, OnItemSelectedListener, OnClickListener{

	protected static final int REQUEST_POST_DETAIL = 0;
	private static final int GET_POSTS = 1;
	protected static final int REQUEST_SET_DEPARTURE= 10;
	protected static final int REQUEST_SET_DESTINATION = 20;
	protected static final int REQUEST_NEW_POST = 2;
	private static final int UPDATE_LOCATION = 3;
	
	View rootView = null;
	ListView listMain = null;
	View header = null;
	View footer = null;
	ObjectMapper mapper = new ObjectMapper();
	
	TaxiArrayAdapter adapter = null;
	View header3 = null;
	LatLng departure = null;
	LatLng destination = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		try
		{
			getActivity().setTitle("이근처 합승");
			
			rootView = inflater.inflate(R.layout.fragment_taxi_main, container, false);

			//header = getActivity().getLayoutInflater().inflate(R.layout.taxi_main_list_header1, null);
			header3 = getActivity().getLayoutInflater().inflate(R.layout.taxi_main_list_header3, null);
//			header2 = getActivity().getLayoutInflater().inflate(R.layout.taxi_main_list_header2, null);
			footer = getActivity().getLayoutInflater().inflate(R.layout.list_footer_taxi_main, null);

			listMain = (ListView) rootView.findViewById(R.id.listMain);
			//listMain.addHeaderView(header);
			listMain.addHeaderView(header3, null, true );
//			listMain.addHeaderView(header2, null, false );
			listMain.addFooterView(footer, null, false );

			adapter = new TaxiArrayAdapter( getActivity().getApplicationContext(), this, 0 );
			listMain.setAdapter(adapter);

			initializeComponents();

			listMain.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					Post post = (Post) arg1.getTag();

					Intent intent = new Intent( getActivity(), TaxiPostDetailActivity.class);
					intent.putExtra("postID", post.getPostID() );
					
					if ( departure != null )
					{
						intent.putExtra("fromLatitude", String.valueOf(departure.latitude) );
						intent.putExtra("fromLongitude", String.valueOf(departure.longitude) );						
					}
					
					if ( destination != null )
					{
						intent.putExtra("toLatitude", String.valueOf(destination.latitude) );
						intent.putExtra("toLongitude", String.valueOf(destination.longitude) );	
					}
					
					startActivityForResult(intent, REQUEST_POST_DETAIL );
					getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
				}
			});
			
			getActivity().setProgressBarIndeterminateVisibility(true);
			inquiryPosts();
			
			updateMyLocation();
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
		
		return rootView;
	}

	public void initializeComponents()
	{
		Spinner spDepartureDistance = (Spinner) header3.findViewById(R.id.spDepartureDistance);
		
		ArrayAdapter<CharSequence> adapterDeparture = ArrayAdapter.createFromResource( getActivity(),
				R.array.distance_list, android.R.layout.simple_spinner_item);
		adapterDeparture.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spDepartureDistance.setAdapter(adapterDeparture);
		spDepartureDistance.setSelection(0, false);
		spDepartureDistance.setOnItemSelectedListener( this );
		
		Spinner spDestinationDistance = (Spinner) header3.findViewById(R.id.spDestinationDistance);
		ArrayAdapter<CharSequence> adapterDestination = ArrayAdapter.createFromResource( getActivity(),
				R.array.distance_list, android.R.layout.simple_spinner_item);
		adapterDestination.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spDestinationDistance.setAdapter(adapterDestination);

		spDestinationDistance.setSelection(0, false);
		spDestinationDistance.setOnItemSelectedListener( this );
		
		Spinner spStatus = (Spinner) rootView.findViewById(R.id.spStatus);
		ArrayAdapter<CharSequence> adapterStatus = ArrayAdapter.createFromResource( getActivity(),
				R.array.status_list, android.R.layout.simple_spinner_item);
		adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spStatus.setAdapter(adapterStatus);
		spStatus.setSelection(0, false);
		spStatus.setOnItemSelectedListener( this );
		
		Button btnSelectDeparture = (Button) header3.findViewById(R.id.btnSelectDeparture);
		btnSelectDeparture.setOnClickListener( this );
		Button btnSelectDestination = (Button) header3.findViewById(R.id.btnSelectDestination);
		btnSelectDestination.setOnClickListener( this );

		Button btnAddPost = (Button) rootView.findViewById(R.id.btnAddPost);
		btnAddPost.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent( getActivity(), NewTaxiPostActivity.class);
				if ( departure != null )
				{
					TextView txtDeparture = (TextView) header3.findViewById(R.id.txtDeparture);
					intent.putExtra("address", txtDeparture.getText().toString() );
					intent.putExtra("departure", departure);
				}

				startActivityForResult(intent, REQUEST_NEW_POST );
				getActivity().overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.stay);
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		try
		{
			super.onActivityResult(requestCode, resultCode, data);

			if ( resultCode == getActivity().RESULT_OK )
			{
				if ( requestCode == REQUEST_SET_DEPARTURE || requestCode == REQUEST_SET_DESTINATION )
				{
					if ( requestCode == REQUEST_SET_DEPARTURE )
					{
						departure = (LatLng) data.getExtras().get("location");
						setDepartureAddressText( data.getExtras().getString("address") );
					}
					else
					{
						destination = (LatLng) data.getExtras().get("location");
						setDestinationAddressText( data.getExtras().getString("address") );
					}
					
					inquiryPosts();
				}
				else if ( requestCode == REQUEST_NEW_POST || requestCode == REQUEST_POST_DETAIL )
				{
					if ( data.getExtras().getBoolean("reload") )
						inquiryPosts();
				}
			}
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
				getActivity().setProgressBarIndeterminateVisibility(false);
				showOKDialog("통신중 오류가 발생했습니다.\r\n다시 시도해 주십시오.", null);
				return;
			}

			super.doPostTransaction(requestCode, result);

			APIResponse response = mapper.readValue(result.toString(), new TypeReference<APIResponse>(){});
			
			if ( "0000".equals( response.getResCode() ) )
			{
				if ( requestCode == GET_POSTS )
				{
					getActivity().setProgressBarIndeterminateVisibility(false);
					
					String postData = mapper.writeValueAsString( response.getData() );
					List<Post> postList = mapper.readValue( postData, new TypeReference<List<Post>>(){});
					adapter.clear();
					adapter.addAll(postList);
					adapter.notifyDataSetChanged();

					int userCount = Integer.parseInt( response.getData2().toString() );
					
					if ( postList.size() == 0 )
					{
						listMain.removeFooterView(footer);
						listMain.addFooterView(footer, null, false );
						footer.findViewById(R.id.txtGuide1).setVisibility(ViewGroup.GONE);
						TextView txtView = (TextView) footer.findViewById(R.id.txtGuide);
						txtView.setVisibility(ViewGroup.VISIBLE);
						
						if ( userCount > 0 )
							txtView.setText("근처에 " + userCount + " 명의 회원이 있습니다\r\n"
									+ "새 글을 등록해서 합승을 제안해보세요.\r\n근처의 회원들에게는 푸쉬메시지가 전송됩니다.");
						else
							txtView.setText("현재 등록된 내역이 없습니다.\r\n새 글을 등록해서 합승을 제안해보세요.\r\n근처의 회원들에게는 푸쉬메시지가 전송됩니다.");
					}
					else
					{
						listMain.removeFooterView(footer);
					}
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

	public void inquiryPosts() throws Exception
	{
		HashMap hash = new HashMap();

		String fromLatitude = "";
		String fromLongitude = "";
		
		if ( departure != null )
		{
			fromLatitude = String.valueOf( departure.latitude ); 
			fromLongitude = String.valueOf( departure.longitude );
		}

		if ( !Util.isEmptyString( fromLatitude ) && !Util.isEmptyString( fromLongitude ) )
		{
			hash.put("fromLatitude", fromLatitude);
			hash.put("fromLongitude", fromLongitude);			
		}
		
		if ( destination != null && destination.latitude != 0 && destination.longitude != 0 )
		{
			hash.put("toLatitude", String.valueOf( destination.latitude ) );
			hash.put("toLongitude", String.valueOf( destination.longitude ) );
		}
		
		Spinner spDepartureDistance = (Spinner) header3.findViewById(R.id.spDepartureDistance);
		String departureDistance = spDepartureDistance.getSelectedItem().toString();
		departureDistance = Util.getDistanceDouble(departureDistance);
		
		Spinner spDestinationDistance = (Spinner) header3.findViewById(R.id.spDestinationDistance);
		String destinationDistance = spDestinationDistance.getSelectedItem().toString();
		destinationDistance = Util.getDistanceDouble(destinationDistance);
		
		if ( !Util.isEmptyString( departureDistance ) )
			hash.put("fromDistance", departureDistance );
		
		if ( !Util.isEmptyString( destinationDistance ) )
			hash.put("toDistance", destinationDistance );
		
		hash.put("userID", getLoginUser().getUserID() );
		
		Spinner spStatus = (Spinner) rootView.findViewById(R.id.spStatus);
		String status = spStatus.getSelectedItem().toString();
		if ( !"전체".equals( status ) )
			hash.put("status", status );

		getActivity().setProgressBarIndeterminateVisibility(true);
		sendHttp("/taxi/getPostsNearHere.do", mapper.writeValueAsString(hash), GET_POSTS );
	}

	boolean bUpdatedOnce = false;
	public void updateAddress( LatLng location )
	{
		try
		{
			if ( bUpdatedOnce == false )
			{
				updateMyLocation();
				
				bUpdatedOnce = true;
				
				departure = new LatLng( location.latitude , location.longitude );
				
				Location loc = new Location("taxiFragment");
				loc.setLatitude(departure.latitude);
				loc.setLongitude(departure.longitude);

				Location[] locs = new Location[1];
				locs[0] = loc;
				new GetAddressTask( getActivity(), this, 1 ).execute(locs);	
				
				inquiryPosts();
			}
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}
	
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
			sendHttp("/taxi/updateUserLocation.do", mapper.writeValueAsString( userLocation ), UPDATE_LOCATION );			
		}
	}
	
	private void setDepartureAddressText( String address )
	{
		TextView txtDeparture = (TextView) header3.findViewById(R.id.txtDeparture);
		txtDeparture.setText(address);
	}
	
	private void setDestinationAddressText( String address )
	{
		TextView txtDestination = (TextView) header3.findViewById(R.id.txtDestination);
		txtDestination.setText(address);
	}

	@Override
	public void onAddressTaskPostExecute(int requestCode, Object result) {
		// TODO Auto-generated method stub

		String address = Util.getDongAddressString( result );
		setDepartureAddressText( address );
	}
	
	@Override
	public void doAction(String actionName, Object param) {
		// TODO Auto-generated method stub
		super.doAction(actionName, param);
		
		if ( "userProfile".equals( actionName ) )
		{
			goUserProfileActivity(param.toString());
		}
	}

	public void goUserProfileActivity( String userID )
	{
		Intent intent = new Intent( getActivity(), UserProfileActivity.class);
		intent.putExtra("userID", userID );
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.stay);
	}
	
	//This is the handler that will manager to process the broadcast intent
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try
			{
				inquiryPosts();
			}
			catch( Exception ex )
			{
				catchException(this, ex);
			}
		}
	};
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		getActivity().registerReceiver(mMessageReceiver, new IntentFilter("refreshContents"));
	}
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		getActivity().unregisterReceiver(mMessageReceiver);
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onItemSelected(AdapterView<?> spinner , View selectedView, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		try
		{
			TextView txtView = (TextView) selectedView;
			
			if ( spinner.getId() == R.id.spDepartureDistance )
			{
				if ( departure == null || departure.latitude == 0 || departure.longitude == 0 )
				{
					if ( !"전체".equals( txtView.getText() ) )
					{
						showOKDialog("경고", "출발지를 먼저 선택해 주십시오.", null);
						return;	
					}
				}
			}
			else if ( spinner.getId() == R.id.spDestinationDistance )
			{
				if ( destination == null || destination.latitude == 0 || destination.longitude == 0 )
				{
					if ( !"전체".equals( txtView.getText() ) )
					{
						showOKDialog("경고", "도착지를 먼저 선택해 주십시오.", null);
						return;
					}
				}
			}
			
			inquiryPosts();
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		try
		{
			if ( v.getId() == R.id.btnSelectDeparture || v.getId() == R.id.btnSelectDestination )
			{
				Intent intent = new Intent( getActivity(), SetDestinationActivity.class);
				intent.putExtra("anim1", R.anim.stay );
				intent.putExtra("anim2", R.anim.slide_out_to_bottom );
				
				if ( v.getId() == R.id.btnSelectDeparture )
				{
					if ( departure != null )
						intent.putExtra("initLocation", departure);
					startActivityForResult(intent, REQUEST_SET_DEPARTURE );
				}
				else
				{
					if ( destination != null )
						intent.putExtra("initLocation", destination);
					startActivityForResult(intent, REQUEST_SET_DESTINATION );
				}
				
				getActivity().overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.stay);
			}
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}
}
