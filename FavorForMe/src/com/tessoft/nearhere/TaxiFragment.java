package com.tessoft.nearhere;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.type.TypeReference;

import com.google.android.gms.drive.internal.GetMetadataRequest;
import com.google.android.gms.maps.model.LatLng;
import com.tessoft.common.AdapterDelegate;
import com.tessoft.common.TaxiArrayAdapter;
import com.tessoft.domain.APIResponse;
import com.tessoft.domain.ListItemModel;
import com.tessoft.domain.Post;
import com.tessoft.domain.TaxiPost;
import com.tessoft.domain.User;

import android.content.Intent;
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

public class TaxiFragment extends BaseListFragment {

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
			super.onCreateView(inflater, container, savedInstanceState);
			
			//header = getActivity().getLayoutInflater().inflate(R.layout.taxi_main_list_header1, null);
			header3 = getActivity().getLayoutInflater().inflate(R.layout.taxi_main_list_header3, null);
			header2 = getActivity().getLayoutInflater().inflate(R.layout.taxi_main_list_header2, null);
			footer = getActivity().getLayoutInflater().inflate(R.layout.list_footer_taxi_main, null);
			
			listMain = (ListView) rootView.findViewById(R.id.listMain);
			//listMain.addHeaderView(header);
			listMain.addHeaderView(header3);
			listMain.addHeaderView(header2);
			listMain.addFooterView(footer);
			
			adapter = new TaxiArrayAdapter( getActivity().getApplicationContext(), 0 );
			listMain.setAdapter(adapter);
			adapter.setDelegate(this);
			
			initializeComponents();
			
			listMain.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					TaxiPost post = (TaxiPost) arg1.getTag();
				
					Intent intent = new Intent( getActivity(), TaxiPostDetailActivity.class);
					intent.putExtra("post", post);
					startActivity(intent);
					getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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
					getActivity().setTitle( txtView.getText() + " 내의 사용자");
					
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
				intent.putExtra("command", "departure");
				if ( departure != null )
					intent.putExtra("departure", departure);
				startActivityForResult(intent, 1);
				getActivity().overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.stay);
			}
		});
		
		Button btnAddPost = (Button) footer.findViewById(R.id.btnAddPost);
		btnAddPost.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent( getActivity(), SetDestinationActivity.class);
				intent.putExtra("command", "new");
				if ( departure != null )
					intent.putExtra("departure", departure);
				startActivityForResult(intent, 1);
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
			
			if ( requestCode == 1 )
			{
				departure = (LatLng) data.getExtras().get("location");
				setAddressText( String.valueOf(departure.latitude) );
				
				inquiryPosts();
			}
			else if ( requestCode == 2 )
			{
				if ( data.getExtras().getBoolean("reload") )
					inquiryPosts();
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
			getActivity().setProgressBarIndeterminateVisibility(false);
			super.doPostTransaction(requestCode, result);
			
			if ( requestCode == 1 )
			{
				APIResponse response = mapper.readValue(result.toString(), new TypeReference<APIResponse>(){});
				String postData = mapper.writeValueAsString( response.getData() );
				List<TaxiPost> postList = mapper.readValue( postData, new TypeReference<List<TaxiPost>>(){});
				adapter.setItemList(postList);
				adapter.notifyDataSetChanged();
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
		
		getActivity().setProgressBarIndeterminateVisibility(true);
		execTransReturningString("/taxi/getPostsNearHere.do", mapper.writeValueAsString(hash), 1);
	}
	
	boolean bUpdatedOnce = false;
	public void updateAddress( LatLng location )
	{
		try
		{
//			showToastMessage("lat:" + location.latitude + " lat(meta):" + getMetaInfoString("latitude"));
			if ( bUpdatedOnce == false )
			{
				inquiryPosts();
				bUpdatedOnce = true;
			}
			
			header3.findViewById(R.id.txtGuide1).setVisibility(ViewGroup.GONE);
			header3.findViewById(R.id.layoutDeparture).setVisibility(ViewGroup.VISIBLE);			
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}
	
	private void setAddressText( String address )
	{
		TextView txtDeparture = (TextView) header3.findViewById(R.id.txtDeparture);
		txtDeparture.setText(address);
	}
}
