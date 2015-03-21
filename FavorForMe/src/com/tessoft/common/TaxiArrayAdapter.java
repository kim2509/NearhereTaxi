package com.tessoft.common;

import java.io.File;
import java.util.List;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;
import com.tessoft.domain.Post;
import com.tessoft.nearhere.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TaxiArrayAdapter extends ArrayAdapter<Post> implements OnClickListener, OnTouchListener{

	private AdapterDelegate delegate = null;

	LayoutInflater inflater = null;
	DisplayImageOptions options = null;
	
	public TaxiArrayAdapter(Context context, AdapterDelegate delegate, int textViewResourceId) {
		super(context, textViewResourceId);
		inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.delegate = delegate;
		options = new DisplayImageOptions.Builder()
		.resetViewBeforeLoading(true)
		.cacheInMemory(true)
		.showImageOnLoading(R.drawable.no_image)
		.showImageForEmptyUri(R.drawable.no_image)
		.showImageOnFail(R.drawable.no_image)
		.displayer(new RoundedBitmapDisplayer(20))
		.delayBeforeLoading(100)
		.build();
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		View row = convertView;

		try
		{
			Post item = getItem(position);
			ImageView imgStatus = null;
			ImageView imageView = null;
			
			if (row == null) {
				row = inflater.inflate(R.layout.list_taxi_post_item, parent, false);
				imgStatus = (ImageView) row.findViewById(R.id.imgStatus);
				imgStatus.setOnTouchListener( this );

				imageView = (ImageView) row.findViewById(R.id.imgProfile);
				imageView.setOnClickListener( this );
			}
			else
			{
				imageView = (ImageView) row.findViewById(R.id.imgProfile);
				imgStatus = (ImageView) row.findViewById(R.id.imgStatus);
			}
			
			String titleDummy = "";
			
			if ( item.getSexInfo() != null && !"상관없음".equals( item.getSexInfo() ) )
				titleDummy += item.getSexInfo();
			
			if ( item.getNumOfUsers() != null && !"상관없음".equals( item.getNumOfUsers() ) )
				titleDummy += " " + item.getNumOfUsers();
			
			if ( titleDummy.isEmpty() == false )
				titleDummy = "(" + titleDummy.trim() + ")";
			
			ImageView imgSex = (ImageView) row.findViewById(R.id.imgSex);
			imgSex.setVisibility(ViewGroup.VISIBLE);
			
			if("M".equals( item.getUser().getSex() ) )
				imgSex.setImageResource(R.drawable.ic_male);
			else if("F".equals( item.getUser().getSex() ) )
				imgSex.setImageResource(R.drawable.ic_female);
			else
				imgSex.setVisibility(ViewGroup.GONE);
			
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
			
			
			
//			TextView txtFromDistance = (TextView) row.findViewById(R.id.txtFromDistance);
//			if ( Util.isEmptyString( item.getFromDistance() ) == false )
//			{
//				txtFromDistance.setText( Util.getDistance( item.getFromDistance() ) );
//				txtFromDistance.setVisibility(ViewGroup.VISIBLE);
//			}
//			else
//				txtFromDistance.setVisibility(ViewGroup.INVISIBLE);
//			
//			TextView txtToDistance = (TextView) row.findViewById(R.id.txtToDistance);
//			if ( Util.isEmptyString( item.getToDistance() ) == false )
//			{
//				txtToDistance.setText( Util.getDistance( item.getToDistance() ) );
//				txtToDistance.setVisibility(ViewGroup.VISIBLE);
//			}
//			else
//				txtToDistance.setVisibility(ViewGroup.INVISIBLE);
			
			TextView txtCreatedDate = (TextView) row.findViewById(R.id.txtCreatedDate);
			txtCreatedDate.setText( Util.getFormattedDateString(item.getCreatedDate(), "MM-dd HH:mm"));	
			
			if ( item.getDepartureDate() != null )
			{
				TextView txtDepartureDateTime = (TextView) row.findViewById(R.id.txtDepartureDateTime);
				txtDepartureDateTime.setText( item.getDepartureDate() + " " + item.getDepartureTime() );	
			}
			
			if ( item.getUser() != null && !Util.isEmptyString( item.getUser().getProfileImageURL() ) )
			{
				ImageLoader.getInstance().displayImage( Constants.thumbnailImageURL + 
						item.getUser().getProfileImageURL() , imageView, options );
			}
			else
			{
				ImageLoader.getInstance().cancelDisplayTask(imageView);
				imageView.setImageResource(R.drawable.no_image);
			}

			imgStatus.setVisibility(ViewGroup.VISIBLE);
			
			if ( "진행중".equals( item.getStatus() ) )
				imgStatus.setImageResource(R.drawable.progressing);
			else
				imgStatus.setImageResource(R.drawable.finished);
			
			LinearLayout layoutComment = (LinearLayout) row.findViewById(R.id.layoutComment);
			if ( item.getReplyCount() > 0 )
			{
				layoutComment.setVisibility(ViewGroup.VISIBLE);
				TextView txtReplyCount = (TextView) row.findViewById(R.id.txtReplyCount);
				txtReplyCount.setText( String.valueOf( item.getReplyCount() ) );
			}
			else
				layoutComment.setVisibility(ViewGroup.GONE);
			
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

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		try
		{
			if ( v != null && v.getId() == R.id.imgProfile )
			{
				Post post = (Post) ( (View) v.getParent().getParent() ).getTag();
				delegate.doAction("userProfile", post.getUser().getUserID() );	
			}
		}
		catch( Exception ex )
		{
			delegate.doAction("logException", null);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
	
}