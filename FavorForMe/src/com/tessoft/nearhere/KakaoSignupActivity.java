package com.tessoft.nearhere;

import java.io.IOException;
import java.util.HashMap;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.kakao.APIErrorResult;
import com.kakao.MeResponseCallback;
import com.kakao.UserManagement;
import com.kakao.UserProfile;
import com.kakao.template.loginbase.SampleSignupActivity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.tessoft.common.HttpTransactionReturningString;
import com.tessoft.common.TransactionDelegate;
import com.tessoft.common.UploadTask;
import com.tessoft.domain.APIResponse;
import com.tessoft.domain.User;

public class KakaoSignupActivity extends SampleSignupActivity{

	NearhereApplication application = null;
	private final int HTTP_REQUEST_GET_RANDOM_ID_V2 = 2;
	private int PROFILE_IMAGE_UPLOAD = 1;
	ObjectMapper mapper = new ObjectMapper();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		application = (NearhereApplication) getApplication();
	}
	
	protected void redirectLoginActivity() {
        final Intent intent = new Intent(this, KakaoLoginActivity.class);
        startActivity(intent);
        finish();
    }

    protected void redirectMainActivity() {
//        final Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);
//        finish();
    	
    	UserManagement.requestMe(new MeResponseCallback() {

            @Override
            protected void onSuccess(final UserProfile userProfile) {
            	
            	try
            	{
                    getRandomIDV2( userProfile.getId() + "", userProfile.getNickname(),
                    		userProfile.getThumbnailImagePath(), userProfile.getProfileImagePath() );            		
            	}
            	catch( Exception ex )
            	{
            		
            	}
            }

            @Override
            protected void onNotSignedUp() {
            }

            @Override
            protected void onSessionClosedFailure(final APIErrorResult errorResult) {
            }

            @Override
            protected void onFailure(final APIErrorResult errorResult) {
                String message = "failed to get user info. msg=" + errorResult;
            }
        });
    }
    
    private void getRandomIDV2( String kakaoID, String nickName, String thumbnailImageURL, String profileImageURL ) throws IOException, JsonGenerationException,
	JsonMappingException {
		setProgressBarIndeterminateVisibility(true);
		HashMap request = application.getDefaultRequest();
		
		final User user = application.getLoginUser();
		user.setKakaoID(kakaoID);
		user.setUserName( nickName );
		user.setKakaoThumbnailImageURL(thumbnailImageURL);
		user.setKakaoProfileImageURL(profileImageURL);
		
		request.put("user", user );
		
		new HttpTransactionReturningString( new TransactionDelegate() {
			@Override
			public void doPostTransaction(int requestCode, Object result) {
				// TODO Auto-generated method stub
				try
				{
					uploadProfileImage( result.toString() );
				}
				catch( Exception ex )
				{
					
				}
			}
		}, "/taxi/getRandomIDV2.do", HTTP_REQUEST_GET_RANDOM_ID_V2 ).execute( mapper.writeValueAsString(request) );
	}
    
    public void uploadProfileImage( String result ) throws Exception 
	{
		APIResponse response = mapper.readValue(result, new TypeReference<APIResponse>(){}); 
		String userString = mapper.writeValueAsString( response.getData() );
		final User user = mapper.readValue(userString, new TypeReference<User>(){}); 
		application.setLoginUser(user);
		
		HashMap addInfo = null;
		if ( response.getData2() != null )
		{
			addInfo = (HashMap) response.getData2();
			
			Log.d("debug", "addInfo is not null." );
			Log.d("debug", addInfo.toString() );
			
			if ("Y".equals( addInfo.get("alreadyExistsYN") ) )
			{
				goMainActivity();
				return;
			}
		}
		else
		{
			Log.d("debug", "addInfo is null." );
		}
		
		ImageLoader imageLoader = ImageLoader.getInstance();

		imageLoader.loadImage( user.getKakaoProfileImageURL(), new SimpleImageLoadingListener() {
			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

				if (loadedImage != null )
				{
					Log.d("imageResult", "image loaded.");
					new UploadTask( getApplicationContext(), user.getUserID() , PROFILE_IMAGE_UPLOAD, 
							new TransactionDelegate() {
						
						@Override
						public void doPostTransaction(int requestCode, Object result) {
							// TODO Auto-generated method stub
						
							goTermsAgreementActivity( null );
							
						}
					}).execute( loadedImage );
				}
			}
		});
	}

    public void goMainActivity()
    {
    	Intent intent = new Intent( this, MainActivity.class);
    	startActivity(intent);
    	overridePendingTransition(android.R.anim.fade_in, 
    			android.R.anim.fade_out);
    	finish();
    }
    
    public void goTermsAgreementActivity( View v )
	{
		Intent intent = new Intent( this, TermsAgreementActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
		finish();
	}
}
