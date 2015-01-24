package com.tessoft.nearhere;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.type.TypeReference;

import com.tessoft.common.MessageBoxListAdapter;
import com.tessoft.domain.APIResponse;
import com.tessoft.domain.Notice;
import com.tessoft.domain.User;
import com.tessoft.domain.UserMessage;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
			listMain.setAdapter(adapter);
			
			User user = getLoginUser();
			
			getActivity().setProgressBarIndeterminateVisibility(true);
			sendHttp("/taxi/getUserMessageList.do", mapper.writeValueAsString(user), 1);
			
			listMain.setOnItemClickListener( new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					UserMessage um = (UserMessage) arg1.getTag();
					goUserChatActivity( um );
				}
			});
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
			
			if ( "0000".equals( response.getResCode() ) )
			{
				String noticeListString = mapper.writeValueAsString( response.getData() );
				List<UserMessage> messageList = mapper.readValue( noticeListString , new TypeReference<List<UserMessage>>(){});
				adapter.setItemList(messageList);
				adapter.notifyDataSetChanged();
			}
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}
	
	public void goUserChatActivity( UserMessage message )
	{
		try
		{
			HashMap hash = new HashMap();
			hash.put("fromUserID",  message.getUser().getUserID() );
			hash.put("userID",  getLoginUser().getUserID() );
			Intent intent = new Intent( getActivity(), UserMessageActivity.class);
			intent.putExtra("messageInfo", hash );
			startActivity(intent);
			getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);	
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}
}
