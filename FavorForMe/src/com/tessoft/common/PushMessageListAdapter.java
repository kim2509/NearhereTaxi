package com.tessoft.common;

import java.util.ArrayList;
import java.util.List;

import com.tessoft.domain.PushMessage;
import com.tessoft.nearhere.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PushMessageListAdapter extends ArrayAdapter<PushMessage> {

	private List<PushMessage> itemList = new ArrayList<PushMessage>();
	private AdapterDelegate delegate = null;

	LayoutInflater inflater = null;

	@Override
	public void add(PushMessage object) {
		itemList.add(object);
		super.add(object);
	}

	public PushMessageListAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return this.itemList.size();
	}

	public PushMessage getItem(int index) {
		return this.itemList.get(index);
	}

	public void setItemList( List<PushMessage> itemList )
	{
		this.itemList = itemList;
		notifyDataSetChanged();
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		View row = convertView;

		try
		{
			PushMessage item = getItem(position);

			if (row == null) {
				row = inflater.inflate(R.layout.list_push_message_item, parent, false);
			}

			TextView txtTitle = (TextView) row.findViewById(R.id.txtTitle);
			txtTitle.setText( item.getTitle() );
			TextView txtCreatedDate = (TextView) row.findViewById(R.id.txtCreatedDate);
			txtCreatedDate.setText( item.getCreatedDate() );
			
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