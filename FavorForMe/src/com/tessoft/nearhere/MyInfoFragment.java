package com.tessoft.nearhere;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Bitmap.CompressFormat;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.DecelerateInterpolator;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class MyInfoFragment extends BaseFragment implements OnClickListener {

	TaxiArrayAdapter adapter = null;
	View rootView = null;
	ListView listMain = null;
	View header = null;
	View footer = null;
	ObjectMapper mapper = new ObjectMapper();
	int UPDATE_USER_MOBILE_NO = 10;
	protected static final int UPDATE_USER_NAME = 20;
	private static final int REQUEST_IMAGE_CROP = 40;
	User user = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		try
		{
			rootView = inflater.inflate(R.layout.activity_user_profile, container, false);

			header = getActivity().getLayoutInflater().inflate(R.layout.user_profile_list_header1, null);
			footer = getActivity().getLayoutInflater().inflate(R.layout.fragment_messagebox_footer, null);

			listMain = (ListView) rootView.findViewById(R.id.listMain);
			listMain.addHeaderView(header);
			listMain.addFooterView(footer, null, false );

			adapter = new TaxiArrayAdapter( getActivity(), this, 0 );
			listMain.setAdapter(adapter);

			initializeComponent();

			inquiryUserInfo();
			
			TextView txtTitle = (TextView) rootView.findViewById(R.id.txtTitle);
			txtTitle.setText("내 정보");
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}

		return rootView;
	}

	private void inquiryUserInfo() throws IOException, JsonGenerationException,
	JsonMappingException {
		User user = getLoginUser();
		getActivity().setProgressBarIndeterminateVisibility(true);
		sendHttp("/taxi/getUserInfo.do", mapper.writeValueAsString( user ), 1);
		
		listMain.setVisibility(ViewGroup.GONE);
		rootView.findViewById(R.id.marker_progress).setVisibility(ViewGroup.VISIBLE);
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
					intent.putExtra("title", "집 위치 선택");
					intent.putExtra("subTitle", "위치를 선택해 주십시오.");
					intent.putExtra("anim1", R.anim.stay );
					intent.putExtra("anim2", R.anim.slide_out_to_bottom );
					startActivityForResult(intent, 1 );
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
					Intent intent = new Intent( getActivity(), SetDestinationActivity.class);
					intent.putExtra("title", "직장 위치 선택");
					intent.putExtra("subTitle", "위치를 선택해 주십시오.");
					intent.putExtra("anim1", R.anim.stay );
					intent.putExtra("anim2", R.anim.slide_out_to_bottom );
					startActivityForResult(intent, 2 );
					getActivity().overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.stay);
				}
				catch( Exception ex )
				{
					catchException(MyInfoFragment.this, ex);
				}
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
				getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
			}
		});

		ImageView imgProfile = (ImageView) header.findViewById(R.id.imgProfile);
		imgProfile.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try
				{
					Intent intent = new Intent( getActivity(), PhotoViewer.class);
					intent.putExtra("imageURL", user.getProfileImageURL());
					startActivity(intent);
					getActivity().overridePendingTransition(R.anim.fade_in, R.anim.stay);
				}
				catch( Exception ex )
				{
					catchException(this, ex);
				}
			}
		});

		Button btnChangeProfileImage = (Button) header.findViewById(R.id.btnChangeProfileImage);
		btnChangeProfileImage.setOnClickListener(new OnClickListener() {

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

		Button btnChangeMobileNo = (Button) header.findViewById(R.id.btnChangeMobileNo);
		btnChangeMobileNo.setOnClickListener(this);

		Button btnChangeName = (Button) header.findViewById(R.id.btnChangeName);
		btnChangeName.setOnClickListener( this );
		
		Button btnRefresh = (Button) rootView.findViewById(R.id.btnRefresh);
		btnRefresh.setOnClickListener(this);
	}

	private static final int PICK_IMAGE = 1;

	private static final int PROFILE_IMAGE_UPLOAD = 6;

	public boolean bDataFirstLoaded = false;
	@Override
	public void doPostTransaction(int requestCode, Object result) {
		// TODO Auto-generated method stub
		try
		{
			rootView.findViewById(R.id.marker_progress).setVisibility(ViewGroup.GONE);
			
			if ( Constants.FAIL.equals(result) )
			{
				getActivity().setProgressBarIndeterminateVisibility(false);
				showOKDialog("통신중 오류가 발생했습니다.\r\n다시 시도해 주십시오.", null);
				return;
			}

			listMain.setVisibility(ViewGroup.VISIBLE);
			
			super.doPostTransaction(requestCode, result);

			APIResponse response = mapper.readValue(result.toString(), new TypeReference<APIResponse>(){});

			if ( "0000".equals( response.getResCode() ) )
			{
				if ( requestCode == 1 )
				{
					bDataFirstLoaded = true;

					HashMap hash = (HashMap) response.getData();

					String userString = mapper.writeValueAsString( hash.get("user") );
					String locationListString = mapper.writeValueAsString( hash.get("locationList") );
					String userPostString = mapper.writeValueAsString( hash.get("userPost") );
					String postsUserRepliedString = mapper.writeValueAsString( hash.get("postsUserReplied") );

					user = mapper.readValue(userString, new TypeReference<User>(){});
					List<UserLocation> locationList = mapper.readValue(locationListString, new TypeReference<List<UserLocation>>(){});
					List<Post> userPosts = mapper.readValue(userPostString, new TypeReference<List<Post>>(){});
					List<Post> userPostsReplied = mapper.readValue(postsUserRepliedString, new TypeReference<List<Post>>(){});

					ArrayList postList = new ArrayList();
					HashMap postKeys = new HashMap();

					for ( int i = 0; i < userPosts.size(); i++ )
					{
						if ( !postKeys.containsKey( userPosts.get(i).getPostID() ) )
						{
							postList.add( userPosts.get(i) );
							postKeys.put( userPosts.get(i).getPostID(), "exist" );
						}
					}

					for ( int i = 0; i < userPostsReplied.size(); i++ )
					{
						if ( !postKeys.containsKey( userPostsReplied.get(i).getPostID() ) )
						{
							postList.add( userPostsReplied.get(i) );
							postKeys.put( userPostsReplied.get(i).getPostID(), "exist" );
						}
					}

					ImageView imgProfile = (ImageView) header.findViewById(R.id.imgProfile);
					imgProfile.setImageResource(R.drawable.no_image);

					if ( user != null && user.getProfileImageURL() != null && user.getProfileImageURL().isEmpty() == false )
					{
						ImageLoader.getInstance().displayImage( Constants.thumbnailImageURL + 
								user.getProfileImageURL() , imgProfile);
						imgProfile.setTag( user.getProfileImageURL() );
					}

					TextView txtUserName = (TextView) header.findViewById(R.id.txtUserName);

					if ( Util.isEmptyString( user.getUserName() ) )
						txtUserName.setText( user.getUserID() );
					else
						txtUserName.setText( user.getUserName() + " (" + user.getUserID() + ")" );

					TextView txtCreditValue = (TextView) header.findViewById(R.id.txtCreditValue);
					txtCreditValue.setText( user.getProfilePoint() + "%");

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

					ImageView imgSex = (ImageView) header.findViewById(R.id.imgSex);
					TextView txtSex = (TextView) header.findViewById(R.id.txtSex);

					if ( "M".equals( user.getSex() ))
					{
						imgSex.setImageResource(R.drawable.male);
						txtSex.setText("남자");
					}
					else if ( "F".equals( user.getSex() ))
					{
						imgSex.setImageResource(R.drawable.female);
						txtSex.setText("여자");
					}
					else
						imgSex.setVisibility(ViewGroup.GONE);

					if ( user.getJobTitle() != null && !"".equals( user.getJobTitle() ))
					{
						EditText edtJobTitle = (EditText) header.findViewById(R.id.edtJobTitle);
						edtJobTitle.setText( user.getJobTitle() );
					}

					TextView txtMobileNo = (TextView) header.findViewById(R.id.txtMobileNo);
					if ( !Util.isEmptyString( user.getMobileNo() ))
					{
						txtMobileNo.setText( user.getMobileNo() );
					}
					else
						txtMobileNo.setText("");

					adapter.clear();
					adapter.addAll(postList);
					adapter.notifyDataSetChanged();

					if ( postList.size() == 0 )
					{
						listMain.removeFooterView(footer);
						listMain.addFooterView(footer, null, false );
						TextView txtView = (TextView) footer.findViewById(R.id.txtGuide);
						txtView.setText("합승내역이 없습니다.");
					}
					else
						listMain.removeFooterView(footer);
				}
				else
				{
					inquiryUserInfo();
				}
			}
			else
			{
				showOKDialog("경고", response.getResMsg(), null);
				return;
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
				String selectedAddress = data.getExtras().get("address").toString();
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

					String imageFilePath = _uri.getPath();

					ExifInterface exif = new ExifInterface(imageFilePath);

					int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);	    
					int exifDegree = exifOrientationToDegrees(exifOrientation);
					if(exifDegree != 0) {
						Bitmap bitmap = getBitmap( imageFilePath );			    	
						Bitmap rotatePhoto = rotate(bitmap, exifDegree);
						saveBitmap(rotatePhoto, imageFilePath );			    			    
					}	    			

					cropImage( _uri );
				}

			}
			else if ( requestCode == REQUEST_IMAGE_CROP )
			{
				Bundle extras = data.getExtras();
				if(extras != null) {
					Bitmap bitmap = (Bitmap)extras.get("data");
					bitmap = resizeBitmapImageFn( bitmap, 1024 );
					sendPhoto( bitmap );
				}
			}
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	public int exifOrientationToDegrees(int exifOrientation)
	{
		if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_90)
		{
			return 90;
		}
		else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_180)
		{
			return 180;
		}
		else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_270)
		{
			return 270;
		}
		return 0;
	}

	public Bitmap getBitmap( String imagePath ) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inInputShareable = true;
		options.inDither=false;
		options.inTempStorage=new byte[32 * 1024];
		options.inPurgeable = true;
		options.inJustDecodeBounds = false;

		File f = new File(imagePath);

		FileInputStream fs=null;
		try {
			fs = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			//TODO do something intelligent
			e.printStackTrace();
		}

		Bitmap bm = null;

		try {
			if(fs!=null) bm=BitmapFactory.decodeFileDescriptor(fs.getFD(), null, options);
		} catch (IOException e) {
			//TODO do something intelligent
			e.printStackTrace();
		} finally{ 
			if(fs!=null) {
				try {
					fs.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}		
		return bm;
	}

	public static Bitmap rotate(Bitmap image, int degrees)
	{
		if(degrees != 0 && image != null)
		{
			Matrix m = new Matrix();
			m.setRotate(degrees, (float)image.getWidth(), (float)image.getHeight());

			try
			{
				Bitmap b = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), m, true);

				if(image != b)
				{
					image.recycle();
					image = b;
				}

				image = b;
			} 
			catch(OutOfMemoryError ex)
			{
				ex.printStackTrace();
			}
		}
		return image;
	}

	public void saveBitmap(Bitmap bitmap, String mCurrentPhotoPath ) {
		File file = new File( mCurrentPhotoPath );
		OutputStream out = null;
		try {
			out = new FileOutputStream(file);
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bitmap.compress(CompressFormat.JPEG, 100, out) ;
		try {
			out.close();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	private void cropImage(Uri contentUri) {
		Intent cropIntent = new Intent("com.android.camera.action.CROP");
		//indicate image type and Uri of image
		cropIntent.setDataAndType(contentUri, "image/*");
		//set crop properties
		cropIntent.putExtra("crop", "true");
		//indicate aspect of desired crop
		cropIntent.putExtra("aspectX", 1);
		cropIntent.putExtra("aspectY", 1);
		//indicate output X and Y
		cropIntent.putExtra("outputX", 256);
		cropIntent.putExtra("outputY", 256);
		//retrieve data on return
		cropIntent.putExtra("return-data", true);
		startActivityForResult(cropIntent, REQUEST_IMAGE_CROP);		
	}

	private void sendPhoto(Bitmap f) throws Exception {
		User user = getLoginUser();
		new UploadTask( getActivity(), this, user.getUserID() , PROFILE_IMAGE_UPLOAD ).execute(f);
	}

	//This is the handler that will manager to process the broadcast intent
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try
			{
				inquiryUserInfo();
			}
			catch( Exception ex )
			{
				catchException(this, ex);
			}
		}
	};

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		getActivity().registerReceiver(mMessageReceiver, new IntentFilter("refreshContents"));
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		getActivity().unregisterReceiver(mMessageReceiver);
	}

	/*
	 * 비트맵(Bitmap) 이미지의 가로, 세로 이미지를 리사이징
	 * @param bmpSource 원본 Bitmap 객체
	 * @param maxResolution 제한 해상도
	 * @return 리사이즈된 이미지 Bitmap 객체
	 */
	public Bitmap resizeBitmapImageFn(
			Bitmap bmpSource, int maxResolution){ 
		int iWidth = bmpSource.getWidth();      //비트맵이미지의 넓이
		int iHeight = bmpSource.getHeight();     //비트맵이미지의 높이
		int newWidth = iWidth ;
		int newHeight = iHeight ;
		float rate = 0.0f;

		//이미지의 가로 세로 비율에 맞게 조절
		if(iWidth > iHeight ){
			if(maxResolution < iWidth ){ 
				rate = maxResolution / (float) iWidth ; 
				newHeight = (int) (iHeight * rate); 
				newWidth = maxResolution; 
			}
		}else{
			if(maxResolution < iHeight ){
				rate = maxResolution / (float) iHeight ; 
				newWidth = (int) (iWidth * rate);
				newHeight = maxResolution;
			}
		}

		return Bitmap.createScaledBitmap(
				bmpSource, newWidth, newHeight, true); 
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		try
		{
			if ( v.getId() == R.id.btnChangeMobileNo )
			{
				openMobileInputDialog();
			}
			else if ( v.getId() == R.id.btnChangeName )
			{
				openChangeNameDialog();
			}
			else if ( v.getId() == R.id.btnRefresh )
				inquiryUserInfo();
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	AlertDialog mobileInputDialog = null;

	public void openMobileInputDialog() throws Exception
	{
		LayoutInflater li = LayoutInflater.from( getActivity() );
		View promptsView = li.inflate(R.layout.fragment_user_mobile_input, null);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( getActivity() );
		alertDialogBuilder.setView(promptsView);

		final EditText userInput = (EditText) promptsView
				.findViewById(R.id.editTextDialogUserInput);

		// set dialog message
		alertDialogBuilder
		.setCancelable(false)
		.setTitle("휴대폰 번호 입력")
		.setPositiveButton("확인", null )
		.setNegativeButton("취소",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				dialog.cancel();
			}
		});

		// create alert dialog
		mobileInputDialog = alertDialogBuilder.create();

		mobileInputDialog.setOnShowListener( new OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				// TODO Auto-generated method stub

				Button b = mobileInputDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				b.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						try
						{
							if ( TextUtils.isEmpty( userInput.getText() ) )
							{
								userInput.setError("입력한 번호가 올바르지 않습니다.");
								return;
							}

							getActivity().setProgressBarIndeterminateVisibility(true);
							user.setMobileNo(userInput.getText().toString());
							sendHttp("/taxi/updateUserInfo.do", mapper.writeValueAsString( user ), UPDATE_USER_MOBILE_NO );
						}
						catch( Exception ex )
						{
							catchException(this, ex);
						}
						finally{
							mobileInputDialog.dismiss();
						}
					}
				});
			}
		});
		// show it
		mobileInputDialog.show();
	}

	public void openChangeNameDialog()
	{
		showSimpleInputDialog("이름 입력", "이름을 입력해 주세요.", user.getUserName(), "변경하고자 하는 이름을 입력해 주십시오.", 
				new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				AlertDialog dialog = null;

				try
				{
					if ( v.getTag() instanceof AlertDialog )
					{
						dialog = (AlertDialog) v.getTag();

						EditText edtSimpleInput = (EditText) dialog.findViewById(R.id.edtSimpleInput);
						if ( TextUtils.isEmpty( edtSimpleInput.getText() ) )
						{
							edtSimpleInput.setError("입력값이 올바르지 않습니다.");
							return;
						}

						if ( user == null )
						{
							showOKDialog("경고", "사용자 정보가 올바르지 않습니다.", null);
							return;
						}

						getActivity().setProgressBarIndeterminateVisibility(true);
						user.setUserName(edtSimpleInput.getText().toString());
						sendHttp("/taxi/updateUserInfo.do", mapper.writeValueAsString( user ), UPDATE_USER_NAME );
					}
				}
				catch( Exception ex )
				{
					catchException(this, ex);
				}

				if (dialog != null )
					dialog.dismiss();
			}
		});
	}
}
