package com.tessoft.nearhere;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.type.TypeReference;

import com.tessoft.common.Constants;
import com.tessoft.common.PushMessageListAdapter;
import com.tessoft.domain.APIResponse;
import com.tessoft.domain.User;
import com.tessoft.domain.UserMessage;
import com.tessoft.domain.UserPushMessage;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
			
			listMain.setAdapter(adapter);
			
			getActivity().setProgressBarIndeterminateVisibility(true);
			
			User user = getLoginUser();
			sendHttp("/taxi/getUserPushMessage.do", mapper.writeValueAsString(user), 1);
			
			listMain.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					try
					{
						if ( arg1.getTag() != null )
						{
							UserPushMessage message = (UserPushMessage) arg1.getTag();
							if ( "message".equals( message.getType() ) )
							{
								String fromUserID = message.getParam1();
								goUserChatActivity( fromUserID );								
							}
							else
							{
								Intent intent = new Intent( getActivity(), TaxiPostDetailActivity.class);
								intent.putExtra("postID", message.getParam1() );
								startActivity(intent);
								getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
							}
						}
					}
					catch( Exception ex )
					{
						catchException(this, ex);
					}
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
			if ( Constants.FAIL.equals(result) )
			{
				getActivity().setProgressBarIndeterminateVisibility(false);
				showOKDialog("통신중 오류가 발생했습니다.\r\n다시 시도해 주십시오.", null);
				return;
			}
			
			getActivity().setProgressBarIndeterminateVisibility(false);
			
			super.doPostTransaction(requestCode, result);
			
			APIResponse response = mapper.readValue(result.toString(), new TypeReference<APIResponse>(){});
			
			if ( "0000".equals( response.getResCode() ) )
			{
				String userPushMessageString = mapper.writeValueAsString( response.getData() );
				List<UserPushMessage> messageList = mapper.readValue( userPushMessageString , new TypeReference<List<UserPushMessage>>(){});
				adapter.setItemList(messageList);
				adapter.notifyDataSetChanged();
			}
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}
	
	public void goUserChatActivity( String fromUserID )
	{
		try
		{
			HashMap hash = new HashMap();
			hash.put("fromUserID",  fromUserID );
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
