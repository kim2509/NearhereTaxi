package com.tessoft.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tessoft.domain.ListItemModel;
import com.tessoft.domain.Post;
import com.tessoft.domain.PostReply;
import com.tessoft.favorforme.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainArrayAdapter extends ArrayAdapter<ListItemModel> {

	private TextView txtUserID;
	private List<ListItemModel> itemList = new ArrayList<ListItemModel>();
	
	LayoutInflater inflater = null;

	@Override
	public void add(ListItemModel object) {
		itemList.add(object);
		super.add(object);
	}

	public MainArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return this.itemList.size();
	}

	public ListItemModel getItem(int index) {
		return this.itemList.get(index);
	}
	
	public void setItemList( List<ListItemModel> itemList )
	{
		this.itemList = itemList;
		notifyDataSetChanged();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		
		View row = convertView;
		
		try
		{
			ListItemModel item = getItem(position);
			
			if (row == null) {
				
				if ( item instanceof Post )
					row = inflater.inflate(R.layout.list_post_item, parent, false);
				else if ( item instanceof Map )
					row = inflater.inflate(R.layout.list_map_item, parent, false);
				else if ( item instanceof PostReply )
					row = inflater.inflate(R.layout.list_post_reply_item, parent, false);
			}
			else
			{
				if (  item instanceof Post && row.getTag() instanceof Post == false )
					row = inflater.inflate(R.layout.list_post_item, parent, false);
				else if ( item instanceof Map && row.getTag() instanceof Map == false )
					row = inflater.inflate(R.layout.list_map_item, parent, false);
				else if ( item instanceof PostReply && row.getTag() instanceof PostReply == false )
					row = inflater.inflate(R.layout.list_post_reply_item, parent, false);
			}

			if ( item instanceof Post )
			{
				Post post = (Post) item;
				txtUserID = (TextView) row.findViewById(R.id.txtDistance);
				txtUserID.setText( Util.getDistance( post.getDistance()) );
				TextView txtMsg = (TextView) row.findViewById(R.id.txtMessage);
				txtMsg.setText( post.getMessage() );
				ImageView imageView = (ImageView) row.findViewById(R.id.imgPic);
				ImageLoader.getInstance().displayImage("https://www.gravatar.com/avatar/e0b3e3d0b29b541cd13a60a9236f02ac?s=32&d=identicon&r=PG", imageView);
			}
			else if ( item instanceof PostReply )
			{
				PostReply postReply = (PostReply) item;
				TextView txtPostReply = (TextView) row.findViewById(R.id.txtPostReply);
				txtPostReply.setText( postReply.getMessage() );
				TextView txtUserName = (TextView) row.findViewById(R.id.txtUserName);
				txtUserName.setText( postReply.getUser().getUserName() );
				ImageView imageProfile = (ImageView) row.findViewById(R.id.imgProfile);
				ImageLoader.getInstance().displayImage(Constants.imageServerURL + "d.png", imageProfile);
			}
			
			row.setTag( item );
		}
		catch( Exception ex )
		{
			Log.e("error", ex.getMessage());
		}
		
		return row;
	}

	public Bitmap decodeToBitmap(byte[] decodedByte) {
		return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
	}

}