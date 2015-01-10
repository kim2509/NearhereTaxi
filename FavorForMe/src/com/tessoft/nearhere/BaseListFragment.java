package com.tessoft.nearhere;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class BaseListFragment extends BaseFragment{

	View rootView = null;
	ListView listMain = null;
	View header = null;
	View footer = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		rootView = inflater.inflate(R.layout.activity_base_list, container, false);
		
		return rootView;
	}
}
