package com.tessoft.nearhere;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.type.TypeReference;

import com.google.android.gms.maps.model.LatLng;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tessoft.common.Constants;
import com.tessoft.common.TaxiArrayAdapter;
import com.tessoft.common.TaxiPostReplyListAdapter;
import com.tessoft.common.UploadTask;
import com.tessoft.common.Util;
import com.tessoft.domain.APIResponse;
import com.tessoft.domain.Post;
import com.tessoft.domain.User;
import com.tessoft.domain.UserLocation;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class MyInfoFragment extends BaseListFragment {

	TaxiArrayAdapter adapter = null;
	Spinner spSex = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		try
		{
			super.onCreateView(inflater, container, savedInstanceState);
			
			header = getActivity().getLayoutInflater().inflate(R.layout.user_profile_list_header1, null);
			
			listMain = (ListView) rootView.findViewById(R.id.listMain);
			listMain.addHeaderView(header);
			
			adapter = new TaxiArrayAdapter( getActivity(), 0 );
			listMain.setAdapter(adapter);
			adapter.setDelegate(this);
			
			initializeComponent();
			
			User user = getLoginUser();
			getActivity().setProgressBarIndeterminateVisibility(true);
			sendHttp("/taxi/getUserInfo.do", mapper.writeValueAsString( user ), 1);
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
		
		return rootView;
	}

	private void initializeComponent() {
		
		TextView txtPickHomeLocation = (TextView) rootView.findViewById(R.id.txtPickHomeLocation);
		txtPickHomeLocation.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try
				{
					Intent intent = new Intent( getActivity(), SetDestinationActivity.class);
					intent.putExtra("command", "address");
					startActivityForResult(intent, 1);
					getActivity().overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.stay);
				}
				catch( Exception ex )
				{
					catchException(MyInfoFragment.this, ex);
				}
			}
		});
		
		TextView txtPickOfficeLocation = (TextView) rootView.findViewById(R.id.txtPickOfficeLocation);
		txtPickOfficeLocation.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try
				{
					openGalery();
				}
				catch( Exception ex )
				{
					catchException(MyInfoFragment.this, ex);
				}
			}
		});
		
		spSex = (Spinner) header.findViewById(R.id.spSex);
		ArrayAdapter<CharSequence> sexAdapter = ArrayAdapter.createFromResource( getActivity(),
		        R.array.sex_list, android.R.layout.simple_spinner_item);
		sexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spSex.setAdapter(sexAdapter);
		spSex.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				
				try
				{
					if ( bDataFirstLoaded )
					{
						// TODO Auto-generated method stub
						TextView txtView = (TextView) arg1;
						User user = getLoginUser();
						if ( "남".equals( txtView.getText() ))
							user.setSex("M");
						else if ( "여".equals( txtView.getText() ))
							user.setSex("F");
						else
							return;
						
						getActivity().setProgressBarIndeterminateVisibility(true);
						sendHttp("/taxi/updateUserSex.do", mapper.writeValueAsString(user), 1);							
					}
				}
				catch( Exception ex )
				{
					catchException(MyInfoFragment.this, ex);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		TextView txtUpdateJobTitle = (TextView) rootView.findViewById(R.id.txtUpdateJobTitle);
		txtUpdateJobTitle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try
				{
					EditText edtJobTitle = (EditText) header.findViewById(R.id.edtJobTitle);
					User user = getLoginUser();
					user.setJobTitle( edtJobTitle.getText().toString() );
					getActivity().setProgressBarIndeterminateVisibility(true);
					sendHttp("/taxi/updateUserJobTitle.do", mapper.writeValueAsString(user), 1);		
				}
				catch( Exception ex )
				{
					catchException(MyInfoFragment.this, ex);
				}
			}
		});
		
		TextView txtChangeBirthday = (TextView) header.findViewById(R.id.txtChangeBirthday);
		txtChangeBirthday.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try
				{
					TextView txtView = (TextView) v;
					RelativeLayout layoutBirthday = (RelativeLayout) header.findViewById(R.id.layoutBirthday);
					
					if ("변경하기".equals(txtView.getText()))
					{
						txtView.setText("취소");
						layoutBirthday.setVisibility(ViewGroup.VISIBLE);
					}
					else
					{
						txtView.setText("변경하기");
						layoutBirthday.setVisibility(ViewGroup.GONE);
					}
				}
				catch( Exception ex )
				{
					catchException(MyInfoFragment.this, ex);
				}
			}
		});
		
		TextView txtSetDatePicker = (TextView) header.findViewById(R.id.txtSetDatePicker);
		txtSetDatePicker.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try
				{
					TextView txtChangeBirthday = (TextView) header.findViewById(R.id.txtChangeBirthday);
					RelativeLayout layoutBirthday = (RelativeLayout) header.findViewById(R.id.layoutBirthday);
					TextView txtBirthday = (TextView) header.findViewById(R.id.txtBirthday);
					layoutBirthday.setVisibility(ViewGroup.GONE);
					txtChangeBirthday.setText("변경하기");
					
					DatePicker dp = (DatePicker) header.findViewById(R.id.datepicker);
					Date date = new Date( dp.getYear()-1900, dp.getMonth(), dp.getDayOfMonth());

					User user = getLoginUser();
					user.setBirthday(Util.getDateStringFromDate(date, "yyyy-MM-dd"));
					txtBirthday.setText( user.getBirthday() );
					
					getActivity().setProgressBarIndeterminateVisibility(true);
					sendHttp("/taxi/updateUserBirthday.do", mapper.writeValueAsString( user ), 3 );
				}
				catch( Exception ex )
				{
					catchException(MyInfoFragment.this, ex);
				}
			}
		});
		
		listMain.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				Post post = (Post) arg1.getTag();
			
				Intent intent = new Intent( getActivity(), TaxiPostDetailActivity.class);
				intent.putExtra("postID", post.getPostID());
				startActivity(intent);
				getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});
		
		ImageView imgProfile = (ImageView) header.findViewById(R.id.imgProfile);
		imgProfile.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try
				{
					Intent intent = new Intent();
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(Intent.createChooser(intent, "Select Picture"), 3);
				}
				catch( Exception ex )
				{
					catchException(this, ex);
				}
			}
		});
	}
	
	public void openGalery()
	{
		Intent intent = new Intent( getActivity(), SetDestinationActivity.class);
		intent.putExtra("command", "address");
		startActivityForResult(intent, 1);
		getActivity().overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.stay);
	}
	
	
	
	private static final int PICK_IMAGE = 1;

	public boolean bDataFirstLoaded = false;
	@Override
	public void doPostTransaction(int requestCode, Object result) {
		// TODO Auto-generated method stub
		try
		{
			if ( Constants.FAIL.equals(result) )
			{
				getActivity().setProgressBarIndeterminateVisibility(false);
				showOKDialog("통신중 오류가 발생했습니다.\r\n다시 시도해 주십시오.", null);
				return;
			}
			
			getActivity().setProgressBarIndeterminateVisibility(false);
			
			super.doPostTransaction(requestCode, result);

			if ( requestCode == 1 )
			{
				APIResponse response = mapper.readValue(result.toString(), new TypeReference<APIResponse>(){});
				
				if ( "0000".equals( response.getResCode() ) )
				{
					bDataFirstLoaded = true;
					
					HashMap hash = (HashMap) response.getData();
							
					String userString = mapper.writeValueAsString( hash.get("user") );
					String locationListString = mapper.writeValueAsString( hash.get("locationList") );
					String userPostString = mapper.writeValueAsString( hash.get("userPost") );
					String postsUserRepliedString = mapper.writeValueAsString( hash.get("postsUserReplied") );
					
					User user = mapper.readValue(userString, new TypeReference<User>(){});
					List<UserLocation> locationList = mapper.readValue(locationListString, new TypeReference<List<UserLocation>>(){});
					List<Post> postList = mapper.readValue(userPostString, new TypeReference<List<Post>>(){});
					List<Post> userPostsReplied = mapper.readValue(postsUserRepliedString, new TypeReference<List<Post>>(){});
					postList.addAll( userPostsReplied );
					
					if ( user != null && user.getProfileImageURL() != null && user.getProfileImageURL().isEmpty() == false )
					{
						ImageView imgProfile = (ImageView) header.findViewById(R.id.imgProfile);
						ImageLoader.getInstance().displayImage( Constants.imageServerURL + 
								user.getProfileImageURL() , imgProfile);
					}
					
					TextView txtUserName = (TextView) header.findViewById(R.id.txtUserName);
					txtUserName.setText( user.getUserName() );
					
					if ( user.getBirthday() != null && !"".equals( user.getBirthday() ) )
					{
						String birthday = Util.getFormattedDateString(user.getBirthday(),"yyyy-MM-dd", "yyyy.MM.dd");
						TextView txtBirthday = (TextView) header.findViewById(R.id.txtBirthday );
						txtBirthday.setText( birthday );
					}
					
					for ( int i = 0; i < locationList.size(); i++ )
					{
						UserLocation loc = locationList.get(i);
						if ( "집".equals( loc.getLocationName() ) )
						{
							TextView txtHomeLocation = (TextView) header.findViewById(R.id.txtHomeLocation);
							txtHomeLocation.setText( loc.getAddress() );
						}
						else if ( "직장".equals( loc.getLocationName() ))
						{
							TextView txtOfficeLocation = (TextView) header.findViewById(R.id.txtOfficeLocation);
							txtOfficeLocation.setText( loc.getAddress() );
						}
					}
					
					if ( "M".equals( user.getSex() ))
						spSex.setSelection(1);
					else if ( "F".equals( user.getSex() ))
						spSex.setSelection(2);
					
					if ( user.getJobTitle() != null && !"".equals( user.getJobTitle() ))
					{
						EditText edtJobTitle = (EditText) header.findViewById(R.id.edtJobTitle);
						edtJobTitle.setText( user.getJobTitle() );
					}
					
					adapter.setItemList(postList);
					adapter.notifyDataSetChanged();
				}
				else
					showToastMessage(response.getResMsg());
			}
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		try
		{
			super.onActivityResult(requestCode, resultCode, data);
			
			if ( requestCode == 1 || requestCode ==2 )
			{
				String selectedAddress = data.getExtras().get("selectedAddress").toString();
				LatLng location = (LatLng) data.getExtras().get("location");
				
				if ( requestCode == 1 )
				{
					TextView txtHomeLocation = (TextView) header.findViewById(R.id.txtHomeLocation);
					txtHomeLocation.setText( selectedAddress );
					
					User user = getLoginUser();
					UserLocation userLocation = new UserLocation();
					userLocation.setUser( user );
					userLocation.setLocationName("집");
					userLocation.setLatitude( String.valueOf( location.latitude ));
					userLocation.setLongitude( String.valueOf( location.longitude ));
					userLocation.setAddress(selectedAddress);
					getActivity().setProgressBarIndeterminateVisibility(true);
					sendHttp("/taxi/updateUserLocation.do", mapper.writeValueAsString( userLocation ), 2);
				}
				else if ( requestCode == 2 )
				{
					TextView txtOfficeLocation = (TextView) header.findViewById(R.id.txtOfficeLocation);
					txtOfficeLocation.setText( selectedAddress );

					User user = getLoginUser();
					UserLocation userLocation = new UserLocation();
					userLocation.setUser( user );
					userLocation.setLocationName("직장");
					userLocation.setLatitude( String.valueOf( location.latitude ));
					userLocation.setLongitude( String.valueOf( location.longitude ));
					userLocation.setAddress(selectedAddress);
					getActivity().setProgressBarIndeterminateVisibility(true);
					sendHttp("/taxi/updateUserLocation.do", mapper.writeValueAsString( userLocation ), 2);
				}	
			}
			else if ( requestCode == 3 )
			{
			    if( data != null && data.getData() != null) {
			        Uri _uri = data.getData();

			        //User had pick an image.
			        Cursor cursor = getActivity().getContentResolver().query(_uri, 
			        		new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, 
			        		null, null, null);
			        cursor.moveToFirst();

			        //Link to the image
			        final String imageFilePath = cursor.getString(0);
			        cursor.close();
			        
			        Bitmap myBitmap = BitmapFactory.decodeFile( imageFilePath );
			        sendPhoto( myBitmap );
			    }

			}
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}
	
	private void sendPhoto(Bitmap f) throws Exception {
		new UploadTask( getActivity() ).execute(f);
	}
}
