package com.tessoft.nearhere;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.type.TypeReference;

import com.tessoft.common.MessageBoxListAdapter;
import com.tessoft.common.NoticeListAdapter;
import com.tessoft.common.SettingsAdapter;
import com.tessoft.domain.APIResponse;
import com.tessoft.domain.Notice;
import com.tessoft.domain.User;
import com.tessoft.domain.UserSetting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class SettingsFragment extends BaseListFragment {

	SettingsAdapter adapter = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		try
		{
			super.onCreateView(inflater, container, savedInstanceState);
			
			listMain = (ListView) rootView.findViewById(R.id.listMain);
			adapter = new SettingsAdapter(getActivity(), this, 0);
			listMain.setAdapter(adapter);

			User user = getLoginUser();
			
			getActivity().setProgressBarIndeterminateVisibility(true);
			sendHttp("/taxi/getUserSetting.do", mapper.writeValueAsString(user), 1 );
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
		
		return rootView;
	}
	
	@Override
	public void doPostTransaction(int requestCode, Object result) {
		// TODO Auto-generated method stub
		try
		{
			getActivity().setProgressBarIndeterminateVisibility(false);
			
			super.doPostTransaction(requestCode, result);
			
			APIResponse response = mapper.readValue(result.toString(), new TypeReference<APIResponse>(){});
			
			if ( requestCode == 1 && "0000".equals( response.getResCode() ) )
			{
				String settingString = mapper.writeValueAsString( response.getData() );
				List<UserSetting> settingsList = mapper.readValue( settingString , new TypeReference<List<UserSetting>>(){});
				adapter.setItemList(settingsList);
				adapter.notifyDataSetChanged();
			}
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}
	
	@Override
	public void doAction(String actionName, Object param) {
		// TODO Auto-generated method stub
		try
		{
			super.doAction(actionName, param);
			UserSetting setting = (UserSetting) param;
			getActivity().setProgressBarIndeterminateVisibility(true);
			sendHttp("/taxi/updateUserSetting.do", mapper.writeValueAsString( setting ), 2);
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
		
	}
}
