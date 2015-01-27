package com.tessoft.nearhere;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.type.TypeReference;

import com.tessoft.common.Constants;
import com.tessoft.common.MessageBoxListAdapter;
import com.tessoft.common.NoticeListAdapter;
import com.tessoft.domain.APIResponse;
import com.tessoft.domain.Notice;
import com.tessoft.domain.User;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class NoticeListFragment extends BaseListFragment {

	NoticeListAdapter adapter = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		try
		{
			super.onCreateView(inflater, container, savedInstanceState);
			
			listMain = (ListView) rootView.findViewById(R.id.listMain);
			adapter = new NoticeListAdapter(getActivity(), 0);
			listMain.setAdapter(adapter);
			
			getActivity().setProgressBarIndeterminateVisibility(true);
			sendHttp("/taxi/getNoticeList.do", null, 1 );
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
				String noticeListString = mapper.writeValueAsString( response.getData() );
				List<Notice> noticeList = mapper.readValue( noticeListString , new TypeReference<List<Notice>>(){});
				adapter.setItemList(noticeList);
				adapter.notifyDataSetChanged();
			}
			 
		}
		catch( Exception ex )
		{
			
		}
	}
}
