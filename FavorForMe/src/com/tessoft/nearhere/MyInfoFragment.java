package com.tessoft.nearhere;

import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.type.TypeReference;

import com.google.android.gms.maps.model.LatLng;
import com.tessoft.common.TaxiArrayAdapter;
import com.tessoft.common.TaxiPostReplyListAdapter;
import com.tessoft.common.Util;
import com.tessoft.domain.APIResponse;
import com.tessoft.domain.Post;
import com.tessoft.domain.User;
import com.tessoft.domain.UserLocation;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class MyInfoFragment extends BaseListFragment {

	TaxiArrayAdapter adapter = null;
	Spinner spSex = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		try
		{
			super.onCreateView(inflater, container, savedInstanceState);
			
			header = getActivity().getLayoutInflater().inflate(R.layout.user_profile_list_header1, null);
			
			listMain = (ListView) rootView.findViewById(R.id.listMain);
			listMain.addHeaderView(header);
			
			adapter = new TaxiArrayAdapter( getActivity(), 0 );
			listMain.setAdapter(adapter);
			adapter.setDelegate(this);
			
			TextView txtPickHomeLocation = (TextView) rootView.findViewById(R.id.txtPickHomeLocation);
			txtPickHomeLocation.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					try
					{
						Intent intent = new Intent( getActivity(), SetDestinationActivity.class);
						intent.putExtra("command", "address");
						startActivityForResult(intent, 1);
						getActivity().overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.stay);
					}
					catch( Exception ex )
					{
						catchException(MyInfoFragment.this, ex);
					}
				}
			});
			
			TextView txtPickOfficeLocation = (TextView) rootView.findViewById(R.id.txtPickOfficeLocation);
			txtPickOfficeLocation.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					try
					{
						Intent intent = new Intent( getActivity(), SetDestinationActivity.class);
						intent.putExtra("command", "address");
						startActivityForResult(intent, 2);
						getActivity().overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.stay);
					}
					catch( Exception ex )
					{
						catchException(MyInfoFragment.this, ex);
					}
				}
			});
			
			spSex = (Spinner) header.findViewById(R.id.spSex);
			ArrayAdapter<CharSequence> sexAdapter = ArrayAdapter.createFromResource( getActivity(),
			        R.array.sex_list, android.R.layout.simple_spinner_item);
			sexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spSex.setAdapter(sexAdapter);
			spSex.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					
					try
					{
						if ( bDataFirstLoaded )
						{
							// TODO Auto-generated method stub
							TextView txtView = (TextView) arg1;
							User user = getLoginUser();
							if ( "남".equals( txtView.getText() ))
								user.setSex("M");
							else if ( "여".equals( txtView.getText() ))
								user.setSex("F");
							else
								return;
							
							getActivity().setProgressBarIndeterminateVisibility(true);
							sendHttp("/taxi/updateUserSex.do", mapper.writeValueAsString(user), 1);							
						}
					}
					catch( Exception ex )
					{
						catchException(MyInfoFragment.this, ex);
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
					
				}
			});
			
			TextView txtUpdateJobTitle = (TextView) rootView.findViewById(R.id.txtUpdateJobTitle);
			txtUpdateJobTitle.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					try
					{
						EditText edtJobTitle = (EditText) header.findViewById(R.id.edtJobTitle);
						User user = getLoginUser();
						user.setJobTitle( edtJobTitle.getText().toString() );
						getActivity().setProgressBarIndeterminateVisibility(true);
						sendHttp("/taxi/updateUserJobTitle.do", mapper.writeValueAsString(user), 1);		
					}
					catch( Exception ex )
					{
						catchException(MyInfoFragment.this, ex);
					}
				}
			});
			
			User user = getLoginUser();
			getActivity().setProgressBarIndeterminateVisibility(true);
			sendHttp("/taxi/getUserInfo.do", mapper.writeValueAsString( user ), 1);
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
		
		return rootView;
	}
	
	public boolean bDataFirstLoaded = false;
	@Override
	public void doPostTransaction(int requestCode, Object result) {
		// TODO Auto-generated method stub
		try
		{
			getActivity().setProgressBarIndeterminateVisibility(false);
			
			super.doPostTransaction(requestCode, result);

			if ( requestCode == 1 )
			{
				APIResponse response = mapper.readValue(result.toString(), new TypeReference<APIResponse>(){});
				
				if ( "0000".equals( response.getResCode() ) )
				{
					bDataFirstLoaded = true;
					
					HashMap hash = (HashMap) response.getData();
							
					String userString = mapper.writeValueAsString( hash.get("user") );
					String locationListString = mapper.writeValueAsString( hash.get("locationList") );
					String userPostString = mapper.writeValueAsString( hash.get("userPost") );
					String postsUserRepliedString = mapper.writeValueAsString( hash.get("postsUserReplied") );
					
					User user = mapper.readValue(userString, new TypeReference<User>(){});
					List<UserLocation> locationList = mapper.readValue(locationListString, new TypeReference<List<UserLocation>>(){});
					List<Post> postList = mapper.readValue(userPostString, new TypeReference<List<Post>>(){});
					List<Post> userPostsReplied = mapper.readValue(postsUserRepliedString, new TypeReference<List<Post>>(){});
					postList.addAll( userPostsReplied );
					
					if ( user.getBirthday() != null && !"".equals( user.getBirthday() ) )
					{
						String birthday = Util.getFormattedDateString(user.getBirthday(),"yyyy-MM-dd", "yyyy.MM.dd");
						TextView txtBirthday = (TextView) header.findViewById(R.id.txtBirthday );
						txtBirthday.setText( birthday );
					}
					
					for ( int i = 0; i < locationList.size(); i++ )
					{
						UserLocation loc = locationList.get(i);
						if ( "집".equals( loc.getLocationName() ) )
						{
							TextView txtHomeLocation = (TextView) header.findViewById(R.id.txtHomeLocation);
							txtHomeLocation.setText( loc.getAddress() );
						}
						else if ( "직장".equals( loc.getLocationName() ))
						{
							TextView txtOfficeLocation = (TextView) header.findViewById(R.id.txtOfficeLocation);
							txtOfficeLocation.setText( loc.getAddress() );
						}
					}
					
					if ( "M".equals( user.getSex() ))
						spSex.setSelection(1);
					else if ( "F".equals( user.getSex() ))
						spSex.setSelection(2);
					
					if ( user.getJobTitle() != null && !"".equals( user.getJobTitle() ))
					{
						EditText edtJobTitle = (EditText) header.findViewById(R.id.edtJobTitle);
						edtJobTitle.setText( user.getJobTitle() );
					}
					
					adapter.setItemList(postList);
					adapter.notifyDataSetChanged();
				}
				else
					showToastMessage(response.getResMsg());
			}
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		try
		{
			super.onActivityResult(requestCode, resultCode, data);
			
			String selectedAddress = data.getExtras().get("selectedAddress").toString();
			LatLng location = (LatLng) data.getExtras().get("location");
			
			if ( requestCode == 1 )
			{
				TextView txtHomeLocation = (TextView) header.findViewById(R.id.txtHomeLocation);
				txtHomeLocation.setText( selectedAddress );
				
				User user = getLoginUser();
				UserLocation userLocation = new UserLocation();
				userLocation.setUser( user );
				userLocation.setLocationName("집");
				userLocation.setLatitude( String.valueOf( location.latitude ));
				userLocation.setLongitude( String.valueOf( location.longitude ));
				userLocation.setAddress(selectedAddress);
				getActivity().setProgressBarIndeterminateVisibility(true);
				sendHttp("/taxi/updateUserLocation.do", mapper.writeValueAsString( userLocation ), 2);
			}
			else if ( requestCode == 2 )
			{
				TextView txtOfficeLocation = (TextView) header.findViewById(R.id.txtOfficeLocation);
				txtOfficeLocation.setText( selectedAddress );

				User user = getLoginUser();
				UserLocation userLocation = new UserLocation();
				userLocation.setUser( user );
				userLocation.setLocationName("직장");
				userLocation.setLatitude( String.valueOf( location.latitude ));
				userLocation.setLongitude( String.valueOf( location.longitude ));
				userLocation.setAddress(selectedAddress);
				getActivity().setProgressBarIndeterminateVisibility(true);
				sendHttp("/taxi/updateUserLocation.do", mapper.writeValueAsString( userLocation ), 2);
			}
				
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}
}
