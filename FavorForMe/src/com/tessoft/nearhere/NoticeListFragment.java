package com.tessoft.nearhere;

import java.util.ArrayList;

import com.tessoft.common.MessageBoxListAdapter;
import com.tessoft.common.NoticeListAdapter;
import com.tessoft.domain.Notice;

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
			
			ArrayList<Notice> itemList = new ArrayList<Notice>();
			
			Notice notice = new Notice();
			notice.setTitle("선착순 50명에게 아메리카노를 쏩니다!!!");
			notice.setCreatedDate("2015-01-11");
			notice.setContent("이번에 론치 기념으로 신규 고객들에게 아메리카노를 쏩니다.\r\n"
					+ "무조건 50명 선착순입니다.\r\n"
					+ "얼른 가입하시고, 주위에도 알려주세요~\r\n"
					+ "감사합니다.");
			itemList.add(notice);
			notice = new Notice();
			notice.setTitle("이근처 합승 에 오신것을 환영합니다.");
			notice.setContent("신규 론치하였습니다. 많은 이용 바랍니다.");
			notice.setCreatedDate("2015-01-1");
			itemList.add(notice);
			
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
