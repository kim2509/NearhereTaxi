package com.tessoft.nearhere;

import com.tessoft.common.TaxiPostReplyListAdapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class MyInfoFragment extends BaseListFragment {

	TaxiPostReplyListAdapter adapter = null;
	
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
			
			adapter = new TaxiPostReplyListAdapter( getActivity(), 0 );
			listMain.setAdapter(adapter);
			adapter.setDelegate(this);
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
		
		return rootView;
	}
}
