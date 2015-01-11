package com.tessoft.common;

import java.util.ArrayList;
import java.util.List;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tessoft.domain.TaxiPost;
import com.tessoft.nearhere.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TaxiArrayAdapter extends ArrayAdapter<TaxiPost> {

	private List<TaxiPost> itemList = new ArrayList<TaxiPost>();
	private AdapterDelegate delegate = null;

	LayoutInflater inflater = null;

	@Override
	public void add(TaxiPost object) {
		itemList.add(object);
		super.add(object);
	}

	public TaxiArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return this.itemList.size();
	}

	public TaxiPost getItem(int index) {
		return this.itemList.get(index);
	}

	public void setItemList( List<TaxiPost> itemList )
	{
		this.itemList = itemList;
		notifyDataSetChanged();
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		View row = convertView;

		try
		{
			TaxiPost item = getItem(position);

			if (row == null) {
				row = inflater.inflate(R.layout.list_taxi_post_item, parent, false);
			}

			TextView txtDestination = (TextView) row.findViewById(R.id.txtDestination);
			txtDestination.setText( item.getDestination() );
			
			TextView txtDistance = (TextView) row.findViewById(R.id.txtDistance);
			txtDistance.setText( Util.getDistance( item.getDistance()) );
			
			ImageView imageView = (ImageView) row.findViewById(R.id.imgProfile);
			ImageLoader.getInstance().displayImage( Constants.imageServerURL + 
					item.getUser().getProfileImageURL() , imageView);

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

	public AdapterDelegate getDelegate() {
		return delegate;
	}

	public void setDelegate(AdapterDelegate delegate) {
		this.delegate = delegate;
	}

}