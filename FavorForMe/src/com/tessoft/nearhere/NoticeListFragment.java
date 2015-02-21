package com.tessoft.nearhere;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.tessoft.common.Constants;
import com.tessoft.common.MessageBoxListAdapter;
import com.tessoft.common.NoticeListAdapter;
import com.tessoft.domain.APIResponse;
import com.tessoft.domain.Notice;
import com.tessoft.domain.User;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ListView;
import android.widget.TextView;

public class NoticeListFragment extends BaseFragment {

	View rootView = null;
	ExpandableListView listMain = null;
	View header = null;
	View footer = null;
	ObjectMapper mapper = new ObjectMapper();
	NoticeListAdapter adapter = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		try
		{
			super.onCreateView(inflater, container, savedInstanceState);

			rootView = inflater.inflate(R.layout.fragment_notice_list, container, false);

			footer = getActivity().getLayoutInflater().inflate(R.layout.fragment_messagebox_footer, null);

			listMain = (ExpandableListView) rootView.findViewById(R.id.listMain);

			listMain.setSelector(new ColorDrawable(0x0));
			listMain.addFooterView(footer, null, false );

			adapter = new NoticeListAdapter( getActivity() );

			listMain.setAdapter(adapter);

			listMain.setOnGroupExpandListener(new OnGroupExpandListener() {
				int previousGroup = -1;

				@Override
				public void onGroupExpand(int groupPosition) {
					if(groupPosition != previousGroup)
						listMain.collapseGroup(previousGroup);
					previousGroup = groupPosition;
				}
			});

			inquiryNotice();
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}

		return rootView;
	}

	private void inquiryNotice() throws IOException, JsonGenerationException,
	JsonMappingException {
		getActivity().setProgressBarIndeterminateVisibility(true);
		sendHttp("/taxi/getNoticeList.do", mapper.writeValueAsString( getLoginUser() ), 1 );
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
				List<Notice> noticeList = mapper.readValue( noticeListString , new TypeReference<List<Notice>>(){});

				// 공지사항 읽음처리
				updateLastNoticeID(noticeList);

				adapter.setGroupList(noticeList);
				adapter.notifyDataSetChanged();

				if ( noticeList.size() == 0 )
				{
					listMain.removeFooterView(footer);
					listMain.addFooterView(footer, null, false );
					TextView txtView = (TextView) footer.findViewById(R.id.txtGuide);
					txtView.setText("공지사항이 없습니다.");
				}
				else
					listMain.removeFooterView(footer);
				
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

	private void updateLastNoticeID(List<Notice> noticeList) {
		int maxNoticeID = 0;
		for ( int i = 0; i < noticeList.size(); i++ )
		{
			Notice noticeItem = noticeList.get(i);
			if ( maxNoticeID < Integer.parseInt( noticeItem.getNoticeID() ) )
				maxNoticeID = Integer.parseInt( noticeItem.getNoticeID() );
		}

		setMetaInfo("lastNoticeID", String.valueOf(maxNoticeID));
		getActivity().sendBroadcast( new Intent("updateUnreadCount") );
	}

	//This is the handler that will manager to process the broadcast intent
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try
			{
				inquiryNotice();
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
