package com.tessoft.nearhere;

import java.util.ArrayList;

import com.tessoft.common.MessageBoxListAdapter;
import com.tessoft.domain.UserMessage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class MessageBoxFragment extends BaseListFragment {

	MessageBoxListAdapter adapter = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		try
		{
			super.onCreateView(inflater, container, savedInstanceState);
			
			listMain = (ListView) rootView.findViewById(R.id.listMain);
			adapter = new MessageBoxListAdapter(getActivity(), 0);
			
			ArrayList<UserMessage> itemList = new ArrayList<UserMessage>();
			
			UserMessage message = new UserMessage();
			message.setMessage("안녕하세요~~");
			message.setCreatedDate("1시간 전");
			itemList.add(message);
			message = new UserMessage();
			message.setMessage("신규 론치하였습니다. 많은 이용 바랍니다.");
			message.setCreatedDate("2시간 전");
			itemList.add(message);
			
			adapter.setItemList(itemList);
			listMain.setAdapter(adapter);
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
		
		return rootView;
	}
}
