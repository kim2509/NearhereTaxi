package com.tessoft.nearhere;

import java.util.ArrayList;
import java.util.List;

import com.tessoft.common.AdapterDelegate;
import com.tessoft.common.TaxiArrayAdapter;
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
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		try
		{
			super.onCreateView(inflater, container, savedInstanceState);
			
			header = getActivity().getLayoutInflater().inflate(R.layout.taxi_main_list_header1, null);
			header2 = getActivity().getLayoutInflater().inflate(R.layout.taxi_main_list_header2, null);
			footer = getActivity().getLayoutInflater().inflate(R.layout.list_footer_taxi_main, null);
			
			listMain = (ListView) rootView.findViewById(R.id.listMain);
			listMain.addHeaderView(header);
			listMain.addHeaderView(header2);
			listMain.addFooterView(footer);
			
			adapter = new TaxiArrayAdapter( getActivity().getApplicationContext(), 0 );
			listMain.setAdapter(adapter);
			adapter.setDelegate(this);
			
			ArrayList<TaxiPost> postList = new ArrayList<TaxiPost>();
			TaxiPost post = new TaxiPost();
			User user = new User();
			user.setUserName("김광중");
			user.setProfileImageURL("k.png");
			user.setUserID("licenser");
			post.setUser(user);
			post.setDestination("강남구 역삼동");
			post.setDistance("0.01");
			post.setLatitude("37.474");
			post.setLongitude("126.963");
			postList.add(post);
			adapter.setItemList( postList );
			
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
//			getActivity().setProgressBarIndeterminateVisibility(true);
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
				// TODO Auto-generated method stub
				TextView txtView = (TextView) arg1;
				getActivity().setTitle( txtView.getText() + " 내의 사용자");
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		Button btnAddPost = (Button) footer.findViewById(R.id.btnAddPost);
		btnAddPost.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				goAddPostActivity();
			}
		});
	}

	public void goAddPostActivity()
	{
		Intent intent = new Intent( getActivity(), SetDestinationActivity.class);
		startActivityForResult(intent, 1);
		getActivity().overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.stay);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		showToastMessage("return");
		
		getActivity().setProgressBarIndeterminateVisibility(false);
	}
}
