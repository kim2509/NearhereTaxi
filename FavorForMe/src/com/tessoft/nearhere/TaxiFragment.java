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

public class TaxiFragment extends BaseFragment implements AddressTaskDelegate, AdapterDelegate, TransactionDelegate{

	protected static final int REQUEST_POST_DETAIL = 0;
	private static final int GET_POSTS = 1;
	protected static final int REQUEST_SET_DESTINATION = 1;
	protected static final int REQUEST_NEW_POST = 2;
	private static final int UPDATE_LOCATION = 3;
	
	View rootView = null;
	ListView listMain = null;
	View header = null;
	View footer = null;
	ObjectMapper mapper = new ObjectMapper();
	
	TaxiArrayAdapter adapter = null;
	View header2 = null;
	View header3 = null;
	LatLng departure = null;
	String distance = "0.5";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		try
		{
			rootView = inflater.inflate(R.layout.fragment_taxi_main, container, false);

			//header = getActivity().getLayoutInflater().inflate(R.layout.taxi_main_list_header1, null);
			header3 = getActivity().getLayoutInflater().inflate(R.layout.taxi_main_list_header3, null);
			header2 = getActivity().getLayoutInflater().inflate(R.layout.taxi_main_list_header2, null);
			footer = getActivity().getLayoutInflater().inflate(R.layout.list_footer_taxi_main, null);

			listMain = (ListView) rootView.findViewById(R.id.listMain);
			//listMain.addHeaderView(header);
			listMain.addHeaderView(header3, null, false );
			listMain.addHeaderView(header2, null, false );

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
					startActivityForResult(intent, REQUEST_POST_DETAIL );
					getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
				}
			});

			getActivity().setProgressBarIndeterminateVisibility(true);
		}
		catch( Exception ex )
		{

		}
		return rootView;
	}

	public void initializeComponents()
	{
		TextView txtNumOfUsers = (TextView) header2.findViewById(R.id.txtNumOfUsers);
		txtNumOfUsers.setText("대기승객 : 3");

		Spinner spDistance = (Spinner) header2.findViewById(R.id.spDistance);

		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource( getActivity(),
				R.array.distance_list, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spDistance.setAdapter(adapter);

		spDistance.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				try
				{
					// TODO Auto-generated method stub
					TextView txtView = (TextView) arg1;
					getActivity().setTitle( txtView.getText() + " 내의 합승내역");

					if ( "500m".equals( txtView.getText().toString()))
						distance = "0.5";
					else
						distance = txtView.getText().toString().replace("km", "");

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
		});

		Button btnSelectDeparture = (Button) header3.findViewById(R.id.btnSelectDeparture);
		btnSelectDeparture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent( getActivity(), SetDestinationActivity.class);
				intent.putExtra("title", "출발지 선택");
				intent.putExtra("subTitle", "출발지를 선택해 주십시오.");
				if ( departure != null )
					intent.putExtra("departure", departure);
				intent.putExtra("anim1", R.anim.stay );
				intent.putExtra("anim2", R.anim.slide_out_to_bottom );
				startActivityForResult(intent, REQUEST_SET_DESTINATION );
				getActivity().overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.stay);
			}
		});

		Button btnAddPost = (Button) rootView.findViewById(R.id.btnAddPost);
		btnAddPost.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent( getActivity(), NewTaxiPostActivity.class);
				if ( departure != null )
				{
					TextView txtDeparture = (TextView) header3.findViewById(R.id.txtDeparture);
					intent.putExtra("address", txtDeparture.getText().toString().replaceAll("출발지:", "") );
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
				if ( requestCode == REQUEST_SET_DESTINATION )
				{
					departure = (LatLng) data.getExtras().get("location");
					setAddressText( data.getExtras().getString("address") );

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

			getActivity().setProgressBarIndeterminateVisibility(false);

			super.doPostTransaction(requestCode, result);

			if ( requestCode == GET_POSTS )
			{
				APIResponse response = mapper.readValue(result.toString(), new TypeReference<APIResponse>(){});
				String postData = mapper.writeValueAsString( response.getData() );
				List<Post> postList = mapper.readValue( postData, new TypeReference<List<Post>>(){});
				adapter.setItemList(postList);
				adapter.notifyDataSetChanged();

				TextView txtNumOfUsers = (TextView) header2.findViewById(R.id.txtNumOfUsers);
				txtNumOfUsers.setText("합승내역 : " + postList.size() );
				
				int userCount = Integer.parseInt( response.getData2().toString() );
				
				if ( postList.size() == 0 )
				{
					listMain.removeFooterView(footer);
					listMain.addFooterView(footer, null, false );
					TextView txtView = (TextView) footer.findViewById(R.id.txtGuide);
					
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
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	public void inquiryPosts() throws Exception
	{
		HashMap hash = new HashMap();

		if ( departure != null )
		{
			hash.put("latitude", String.valueOf( departure.latitude )); 
			hash.put("longitude", String.valueOf( departure.longitude ));	
		}
		else
		{
			hash.put("latitude", getMetaInfoString("latitude")); 
			hash.put("longitude", getMetaInfoString("longitude"));
		}

		hash.put("distance", distance);
		hash.put("userID", getLoginUser().getUserID() );

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
				inquiryPosts();
				updateMyLocation();
				bUpdatedOnce = true;
			}

			if ( departure == null )
			{
				Location loc = new Location("taxiFragment");
				loc.setLatitude(location.latitude);
				loc.setLongitude(location.longitude);

				Location[] locs = new Location[1];
				locs[0] = loc;
				new GetAddressTask( getActivity(), this, 1 ).execute(locs);	
			}

			header3.findViewById(R.id.txtGuide1).setVisibility(ViewGroup.GONE);
			header3.findViewById(R.id.layoutDeparture).setVisibility(ViewGroup.VISIBLE);			
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	public void updateMyLocation() throws Exception
	{
		User user = getLoginUser();
		UserLocation userLocation = new UserLocation();
		userLocation.setUser( user );
		userLocation.setLocationName("현재위치");
		userLocation.setLatitude( getMetaInfoString("latitude"));
		userLocation.setLongitude( getMetaInfoString("longitude"));
		userLocation.setAddress( getMetaInfoString("address") );
		getActivity().setProgressBarIndeterminateVisibility(true);
		sendHttp("/taxi/updateUserLocation.do", mapper.writeValueAsString( userLocation ), UPDATE_LOCATION );
		
	}
	private void setAddressText( String address )
	{
		TextView txtDeparture = (TextView) header3.findViewById(R.id.txtDeparture);
		txtDeparture.setText("출발지:" + address);
	}

	@Override
	public void onAddressTaskPostExecute(int requestCode, Object result) {
		// TODO Auto-generated method stub

		String address = Util.getDongAddressString( result );

		setAddressText( address );
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
		
		try
		{
			updateMyLocation();
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}
}
