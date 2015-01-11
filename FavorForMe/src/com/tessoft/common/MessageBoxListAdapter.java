package com.tessoft.common;

import java.util.ArrayList;
import java.util.List;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tessoft.domain.PushMessage;
import com.tessoft.domain.UserMessage;
import com.tessoft.nearhere.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MessageBoxListAdapter extends ArrayAdapter<UserMessage> {

	private List<UserMessage> itemList = new ArrayList<UserMessage>();
	private AdapterDelegate delegate = null;

	LayoutInflater inflater = null;

	@Override
	public void add(UserMessage object) {
		itemList.add(object);
		super.add(object);
	}

	public MessageBoxListAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return this.itemList.size();
	}

	public UserMessage getItem(int index) {
		return this.itemList.get(index);
	}

	public void setItemList( List<UserMessage> itemList )
	{
		this.itemList = itemList;
		notifyDataSetChanged();
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		View row = convertView;

		try
		{
			UserMessage item = getItem(position);

			if (row == null) {
				row = inflater.inflate(R.layout.list_message_box_item, parent, false);
			}

			ImageView imgProfile = (ImageView) row.findViewById(R.id.imgProfile);
			if ( item.getFromUser() != null && item.getFromUser().getProfileImageURL() != null && 
					!"".equals( item.getFromUser().getProfileImageURL() ) )
			{
				ImageLoader.getInstance().displayImage( Constants.imageServerURL + 
						item.getFromUser().getProfileImageURL() , imgProfile);
			}
			
			TextView txtMessage = (TextView) row.findViewById(R.id.txtMessage);
			txtMessage.setText( item.getMessage() );
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