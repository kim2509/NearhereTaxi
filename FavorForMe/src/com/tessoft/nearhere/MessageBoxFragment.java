package com.tessoft.nearhere;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.type.TypeReference;

import com.tessoft.common.Constants;
import com.tessoft.common.MessageBoxListAdapter;
import com.tessoft.domain.APIResponse;
import com.tessoft.domain.Notice;
import com.tessoft.domain.User;
import com.tessoft.domain.UserMessage;

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

public class MessageBoxFragment extends BaseListFragment {

	MessageBoxListAdapter adapter = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		try
		{
			super.onCreateView(inflater, container, savedInstanceState);

			footer = getActivity().getLayoutInflater().inflate(R.layout.fragment_messagebox_footer, null);

			listMain = (ListView) rootView.findViewById(R.id.listMain);
			adapter = new MessageBoxListAdapter(getActivity(), 0);
			listMain.addFooterView(footer, null, false );
			listMain.setAdapter(adapter);

			inquiryMessage();

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

	private void inquiryMessage() throws IOException, JsonGenerationException,
			JsonMappingException {
		User user = getLoginUser();

		getActivity().setProgressBarIndeterminateVisibility(true);
		sendHttp("/taxi/getUserMessageList.do", mapper.writeValueAsString(user), 1);
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
				String noticeListString = mapper.writeValueAsString( response.getData() );
				List<UserMessage> messageList = mapper.readValue( noticeListString , new TypeReference<List<UserMessage>>(){});
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
				inquiryMessage();
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
		getActivity().registerReceiver(mMessageReceiver, new IntentFilter("refreshContents"));
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		getActivity().unregisterReceiver(mMessageReceiver);
	}
}
