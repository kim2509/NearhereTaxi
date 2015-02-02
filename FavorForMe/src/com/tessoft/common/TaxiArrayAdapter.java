package com.tessoft.common;

import java.util.ArrayList;
import java.util.List;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tessoft.domain.Post;
import com.tessoft.nearhere.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TaxiArrayAdapter extends ArrayAdapter<Post> {

	private List<Post> itemList = new ArrayList<Post>();
	private AdapterDelegate delegate = null;

	LayoutInflater inflater = null;
	
	@Override
	public void add(Post object) {
		itemList.add(object);
		super.add(object);
	}

	public TaxiArrayAdapter(Context context, AdapterDelegate delegate, int textViewResourceId) {
		super(context, textViewResourceId);
		inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.delegate = delegate;
	}

	public int getCount() {
		return this.itemList.size();
	}

	public Post getItem(int index) {
		return this.itemList.get(index);
	}

	public void setItemList( List<Post> itemList )
	{
		this.itemList = itemList;
		notifyDataSetChanged();
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		View row = convertView;

		try
		{
			Post item = getItem(position);

			if (row == null) {
				row = inflater.inflate(R.layout.list_taxi_post_item, parent, false);
			}

			ImageView imageView = (ImageView) row.findViewById(R.id.imgProfile);
			imageView.invalidate();
			imageView.setImageResource(R.drawable.no_image);
			
			String titleDummy = "";
			
			if ( item.getSexInfo() != null && !"상관없음".equals( item.getSexInfo() ) )
				titleDummy += item.getSexInfo();
			
			if ( item.getNumOfUsers() != null && !"상관없음".equals( item.getNumOfUsers() ) )
				titleDummy += " " + item.getNumOfUsers();
			
			if ( titleDummy.isEmpty() == false )
				titleDummy = "(" + titleDummy.trim() + ")";
			
			TextView txtUserName = (TextView) row.findViewById(R.id.txtUserName);
			if ( Util.isEmptyString( item.getUser().getUserName() ) )
			{
				txtUserName.setText( item.getUser().getUserID() );
			}
			else
				txtUserName.setText( item.getUser().getUserName() );
			
			TextView txtTitle = (TextView) row.findViewById(R.id.txtTitle);
			txtTitle.setText( item.getMessage() + titleDummy );

			TextView txtDeparture = (TextView) row.findViewById(R.id.txtDeparture);
			txtDeparture.setText( item.getFromAddress() );
			
			TextView txtDestination = (TextView) row.findViewById(R.id.txtDestination);
			txtDestination.setText( item.getToAddress() );
			
			TextView txtDistance = (TextView) row.findViewById(R.id.txtDistance);
			txtDistance.setText( Util.getDistance( item.getDistance()) );
			
			TextView txtCreatedDate = (TextView) row.findViewById(R.id.txtCreatedDate);
			txtCreatedDate.setText( Util.getFormattedDateString(item.getCreatedDate(), "yyyy-MM-dd HH:mm"));	
			
			if ( item.getDepartureDate() != null )
			{
				TextView txtDepartureDateTime = (TextView) row.findViewById(R.id.txtDepartureDateTime);
				txtDepartureDateTime.setText( item.getDepartureDate() + " " + item.getDepartureTime() );	
			}
			
			if ( item.getUser() != null && !Util.isEmptyString( item.getUser().getProfileImageURL() ) )
			{
				ImageLoader.getInstance().displayImage( Constants.imageServerURL + 
						item.getUser().getProfileImageURL() , imageView);	
			}

			row.setTag( item );
		}
		catch( Exception ex )
		{
			delegate.doAction("logException", ex);
		}

		return row;
	}

	public Bitmap decodeToBitmap(byte[] decodedByte) {
		return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
	}

	public AdapterDelegate getDelegate() {
		return delegate;
	}
}