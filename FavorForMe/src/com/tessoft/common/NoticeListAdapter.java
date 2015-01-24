package com.tessoft.common;

import java.util.ArrayList;
import java.util.List;

import com.tessoft.domain.Notice;
import com.tessoft.nearhere.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class NoticeListAdapter extends ArrayAdapter<Notice> {

	private List<Notice> itemList = new ArrayList<Notice>();
	private AdapterDelegate delegate = null;

	LayoutInflater inflater = null;

	@Override
	public void add(Notice object) {
		itemList.add(object);
		super.add(object);
	}

	public NoticeListAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return this.itemList.size();
	}

	public Notice getItem(int index) {
		return this.itemList.get(index);
	}

	public void setItemList( List<Notice> itemList )
	{
		this.itemList = itemList;
		notifyDataSetChanged();
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		View row = convertView;

		try
		{
			Notice item = getItem(position);

			if (row == null) {
				row = inflater.inflate(R.layout.list_notice_item, parent, false);
			}

			TextView txtTitle = (TextView) row.findViewById(R.id.txtTitle);
			txtTitle.setText( item.getTitle() );
			TextView txtCreatedDate = (TextView) row.findViewById(R.id.txtCreatedDate);
			txtCreatedDate.setText( Util.getFormattedDateString(item.getCreatedDate(),"yyyy-MM-dd") );
			TextView txtContent = (TextView) row.findViewById(R.id.txtContent);
			txtContent.setText( item.getContent() );
			
			row.setTag( item );
		}
		catch( Exception ex )
		{
			Log.e("error", ex.getMessage());
		}

		return row;
	}

	public AdapterDelegate getDelegate() {
		return delegate;
	}

	public void setDelegate(AdapterDelegate delegate) {
		this.delegate = delegate;
	}

}