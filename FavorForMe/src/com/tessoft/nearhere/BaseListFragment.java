package com.tessoft.nearhere;

import org.codehaus.jackson.map.ObjectMapper;

import com.tessoft.common.AdapterDelegate;
import com.tessoft.common.HttpTransactionReturningString;
import com.tessoft.common.TransactionDelegate;
import com.tessoft.domain.User;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

public class BaseListFragment extends BaseFragment{

	View rootView = null;
	ListView listMain = null;
	View header = null;
	View footer = null;
	ObjectMapper mapper = new ObjectMapper();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		
		rootView = inflater.inflate(R.layout.fragment_base_list, container, false);
		
		return rootView;
	}
	
	protected void setTitle( String title ) {
		TextView txtTitle = (TextView) rootView.findViewById(R.id.txtTitle);
		txtTitle.setVisibility(ViewGroup.VISIBLE);
		rootView.findViewById(R.id.imgTitle).setVisibility(ViewGroup.GONE);
		txtTitle.setText( title );
	}
}
