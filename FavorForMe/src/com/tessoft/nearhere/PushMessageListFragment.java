package com.tessoft.nearhere;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.type.TypeReference;

import com.tessoft.common.Constants;
import com.tessoft.common.PushMessageListAdapter;
import com.tessoft.domain.APIResponse;
import com.tessoft.domain.User;
import com.tessoft.domain.UserMessage;
import com.tessoft.domain.UserPushMessage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class PushMessageListFragment extends BaseListFragment {

	protected static final int UPDATE_AS_READ = 2;
	PushMessageListAdapter adapter = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		try
		{
			super.onCreateView(inflater, container, savedInstanceState);

			footer = getActivity().getLayoutInflater().inflate(R.layout.fragment_messagebox_footer, null);
			
			listMain = (ListView) rootView.findViewById(R.id.listMain);
			listMain.addFooterView(footer, null, false );
			adapter = new PushMessageListAdapter(getActivity(), this, 0);

			listMain.setAdapter(adapter);

			inquiryPushMesage();

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
							else if ( "postReply".equals( message.getType() ) || "newPostByDistance".equals( message.getType() ) )
							{
								Intent intent = new Intent( getActivity(), TaxiPostDetailActivity.class);
								intent.putExtra("postID", message.getParam1() );
								startActivity(intent);
								getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
							}
							else if ( "event".equals( message.getType() ) )
							{
								Intent intent = new Intent( getActivity(), EventViewerActivity.class);
								intent.putExtra("eventSeq", message.getParam1() );
								intent.putExtra("pushNo", message.getPushNo() );
								startActivity(intent);
								getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
							}
							else if ( "eventssl".equals( message.getType() ) )
							{
								Intent intent = new Intent( getActivity(), EventViewerActivity.class);
								intent.putExtra("eventSeq", message.getParam1() );
								intent.putExtra("pushNo", message.getPushNo() );
								intent.putExtra("ssl", "true" );
								startActivity(intent);
								getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
							}
							
							message.setRead(true);
							sendHttp("/taxi/updatePushMessageAsRead.do", mapper.writeValueAsString( message ), UPDATE_AS_READ );
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

	private void inquiryPushMesage() throws IOException,
	JsonGenerationException, JsonMappingException {
		getActivity().setProgressBarIndeterminateVisibility(true);
		User user = getLoginUser();
		sendHttp("/taxi/getUserPushMessage.do", mapper.writeValueAsString(user), 1);
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
				if ( requestCode == 1 )
				{
					String userPushMessageString = mapper.writeValueAsString( response.getData() );
					List<UserPushMessage> messageList = mapper.readValue( userPushMessageString , new TypeReference<List<UserPushMessage>>(){});
					adapter.setItemList(messageList);
					adapter.notifyDataSetChanged();
					
					if ( messageList.size() == 0 )
					{
						listMain.removeFooterView(footer);
						listMain.addFooterView(footer, null, false );
						TextView txtView = (TextView) footer.findViewById(R.id.txtGuide);
						txtView.setText("메시지내역이 없습니다.");
					}
					else
						listMain.removeFooterView(footer);					
				}
				else if ( requestCode == UPDATE_AS_READ )
				{
					getActivity().sendBroadcast( new Intent("updateUnreadCount") );
				}
			}
			else
			{
				showOKDialog("경고", response.getResMsg(), null);
				return;
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
			getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);	
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	//This is the handler that will manager to process the broadcast intent
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try
			{
				inquiryPushMesage();
			}
			catch( Exception ex )
			{
				catchException(this, ex);
			}
		}
	};
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		try
		{
			getActivity().registerReceiver(mMessageReceiver, new IntentFilter("updateUnreadCount"));
			inquiryPushMesage();
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		getActivity().unregisterReceiver(mMessageReceiver);
	}
}
