package com.tessoft.common;

import java.util.ArrayList;
import java.util.List;

import com.tessoft.domain.ListItemModel;
import com.tessoft.domain.Post;
import com.tessoft.favorforme.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainArrayAdapter extends ArrayAdapter<ListItemModel> {

	private TextView txtUserID;
	private List<ListItemModel> postList = new ArrayList<ListItemModel>();
	private LinearLayout wrapper;

	@Override
	public void add(ListItemModel object) {
		postList.add(object);
		super.add(object);
	}

	public MainArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

	public int getCount() {
		return this.postList.size();
	}

	public ListItemModel getItem(int index) {
		return this.postList.get(index);
	}
	
	public void setItemList( List<ListItemModel> chatList )
	{
		this.postList = chatList;
		notifyDataSetChanged();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.list_post_item, parent, false);
		}

		Post post = (Post) getItem(position);
		txtUserID = (TextView) row.findViewById(R.id.txtUserID);
		txtUserID.setText(post.getUserID());
		TextView txtMsg = (TextView) row.findViewById(R.id.txtMessage);
		txtMsg.setText( post.getMessage() );
		
		return row;
	}

	public Bitmap decodeToBitmap(byte[] decodedByte) {
		return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
	}

}