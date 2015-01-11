package com.tessoft.nearhere;

import com.tessoft.common.AdapterDelegate;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListView;

public class BaseListFragment extends BaseFragment implements AdapterDelegate{

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
	
	@Override
	public void doAction(String actionName, Object param) {
		// TODO Auto-generated method stub
		
	}
}
