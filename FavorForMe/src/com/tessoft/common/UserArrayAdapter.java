package com.tessoft.common;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tessoft.domain.User;
import com.tessoft.nearhere.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UserArrayAdapter extends ArrayAdapter<User>{

	private AdapterDelegate delegate = null;
	LayoutInflater inflater = null;
	
	public UserArrayAdapter(Context context, AdapterDelegate delegate, int textViewResourceId) {
		super(context, textViewResourceId);
		inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.delegate = delegate;
	}

	public static class ViewHolder
	{
		public User user = null;
		public ImageView imgProfile = null;
		public TextView txtUserName = null;
		public TextView txtCurrLocation = null;
		public TextView txtAge = null;
		public TextView txtProfilePoint = null;
		public TextView txtJobTitle = null;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {

		View row = convertView;

		try
		{
			User user = getItem(position);
			
			if ( user.isMoreFlag() )
			{
				row = inflater.inflate(R.layout.list_user_more_item, parent, false);
				this.delegate.doAction("loadMore", user);
				return row;
			}
			
			ImageView imageView = null;
			ViewHolder viewHolder = null;
			TextView txtUserName = null;
			TextView txtCurrLocation = null;
			TextView txtAge = null;
			TextView txtProfilePoint = null;
			TextView txtJobTitle = null;
			
			if (row == null || row.getTag() == null ) {
				row = inflater.inflate(R.layout.list_user_item, parent, false);
				
				viewHolder = new ViewHolder();
				
				viewHolder.imgProfile = (ImageView) row.findViewById(R.id.imgProfile);
				viewHolder.txtUserName = (TextView) row.findViewById(R.id.txtUserName);
				viewHolder.txtCurrLocation = (TextView) row.findViewById(R.id.txtCurrLocation);
				viewHolder.txtAge = (TextView) row.findViewById(R.id.txtAge);
				viewHolder.txtProfilePoint = (TextView) row.findViewById(R.id.txtProfilePoint);
				viewHolder.txtJobTitle = (TextView) row.findViewById(R.id.txtJobTitle);
				row.setTag(viewHolder);
			}
			else
			{
				viewHolder = (ViewHolder) row.getTag();
			}
			
			imageView = viewHolder.imgProfile;
			txtUserName = viewHolder.txtUserName;
			txtCurrLocation = viewHolder.txtCurrLocation;
			txtAge = viewHolder.txtAge;
			txtProfilePoint = viewHolder.txtProfilePoint;
			txtJobTitle = viewHolder.txtJobTitle;

			viewHolder.user = user;
			
			imageView.invalidate();
			imageView.setImageResource(R.drawable.no_image);
			
			if ( !Util.isEmptyString( user.getProfileImageURL() ))
			{
				ImageLoader.getInstance().displayImage( 
						Constants.thumbnailImageURL + user.getProfileImageURL() , imageView);	
			}
			
			txtUserName.setText( user.getUserName() );
			
			if ( Util.isEmptyString( user.getAddress() ) )
				row.findViewById(R.id.layoutCurrLocation).setVisibility(ViewGroup.GONE);
			else
				row.findViewById(R.id.layoutCurrLocation).setVisibility(ViewGroup.VISIBLE);
				
			txtCurrLocation.setText( user.getAddress() );
			
			if ( Util.isEmptyString( user.getAge() ))
				txtAge.setText( "미입력" );
			else
				txtAge.setText( user.getAge() + "세" );
			
			txtProfilePoint.setText( user.getProfilePoint() + "%" );
			
			if ( Util.isEmptyString( user.getJobTitle() ) )
				txtJobTitle.setText("미입력");
			else
				txtJobTitle.setText(user.getJobTitle());
		}
		catch( Exception ex )
		{
			delegate.doAction("logException", ex);
		}

		return row;
	}
}