package com.tessoft.nearhere;

import java.util.ArrayList;
import java.util.List;

import com.tessoft.common.AdapterDelegate;
import com.tessoft.common.TaxiMainArrayAdapter;
import com.tessoft.domain.ListItemModel;
import com.tessoft.domain.Post;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

public class TaxiFragment extends BaseListFragment implements AdapterDelegate{

	TaxiMainArrayAdapter adapter = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		try
		{
			super.onCreateView(inflater, container, savedInstanceState);
			
			header = getActivity().getLayoutInflater().inflate(R.layout.list_header_taxi_main, null);
			footer = getActivity().getLayoutInflater().inflate(R.layout.list_footer_taxi_main, null);
			
			listMain = (ListView) rootView.findViewById(R.id.listMain);
			listMain.addHeaderView(header);
			listMain.addFooterView(footer);
			
			adapter = new TaxiMainArrayAdapter( getActivity().getApplicationContext(), 0 );
			listMain.setAdapter(adapter);
			adapter.setDelegate(this);
			
			ArrayList<Post> postList = new ArrayList<Post>();
			Post post = new Post();
			post.setMessage("abc");
			post.setLatitude("37.4704213612");
			post.setLongitude("126.96732417061958");
			postList.add(post);
			adapter.setItemList( (List<ListItemModel>) (Object)postList);
			
			initializeComponents();
		}
		catch( Exception ex )
		{
			
		}
		return rootView;
	}

	@Override
	public void doAction(String actionName, Object param) {
		// TODO Auto-generated method stub
		if ( "showDetail".equals(actionName))
		{
			Intent intent = new Intent( getActivity(), TaxiPostDetailActivity.class);
			startActivity(intent);
			getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		}
	}
	
	public void initializeComponents()
	{
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
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.stay);
	}
}
