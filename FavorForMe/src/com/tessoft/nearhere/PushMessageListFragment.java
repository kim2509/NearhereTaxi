package com.tessoft.nearhere;

import java.util.ArrayList;

import com.tessoft.common.PushMessageListAdapter;
import com.tessoft.domain.PushMessage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class PushMessageListFragment extends BaseListFragment {

	PushMessageListAdapter adapter = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		try
		{
			super.onCreateView(inflater, container, savedInstanceState);
			
			listMain = (ListView) rootView.findViewById(R.id.listMain);
			adapter = new PushMessageListAdapter(getActivity(), 0);
			
			ArrayList<PushMessage> itemList = new ArrayList<PushMessage>();
			
			PushMessage push = new PushMessage();
			push.setTitle("쪽지가 왔습니다.");
			push.setCreatedDate("1시간 전");
			push.setType("message");
			itemList.add(push);
			push = new PushMessage();
			push.setTitle("댓글이 달렸습니다.");
			push.setCreatedDate("2시간 전");
			push.setType("post_reply");
			itemList.add(push);
			push = new PushMessage();
			push.setTitle("3키로 근처에서 낙성대로 등록되었습니다.");
			push.setCreatedDate("2시간 전");
			push.setType("home_recommend");
			itemList.add(push);
			
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
