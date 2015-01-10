package com.tessoft.nearhere;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class MessageBoxFragment extends BaseListFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		try
		{
			return super.onCreateView(inflater, container, savedInstanceState);	
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
		
		return rootView;
	}
}
