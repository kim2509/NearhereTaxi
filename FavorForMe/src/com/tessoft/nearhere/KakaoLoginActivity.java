package com.tessoft.nearhere;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashMap;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import uk.co.senab.photoview.PhotoViewAttacher;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

import com.kakao.APIErrorResult;
import com.kakao.KakaoTalkHttpResponseHandler;
import com.kakao.KakaoTalkProfile;
import com.kakao.KakaoTalkService;
import com.kakao.LogoutResponseCallback;
import com.kakao.MeResponseCallback;
import com.kakao.Session;
import com.kakao.SessionCallback;
import com.kakao.UserManagement;
import com.kakao.UserProfile;
import com.kakao.exception.KakaoException;
import com.kakao.helper.Logger;
import com.kakao.template.loginbase.SampleLoginActivity;
import com.kakao.widget.LoginButton;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.tessoft.common.Constants;
import com.tessoft.common.HttpTransactionReturningString;
import com.tessoft.common.TransactionDelegate;
import com.tessoft.common.UploadTask;
import com.tessoft.domain.APIResponse;
import com.tessoft.domain.User;

public class KakaoLoginActivity extends SampleLoginActivity{

	private LoginButton loginButton;
	private final SessionCallback mySessionCallback = new MySessionStatusCallback();
	private Session session;
	NearhereApplication application = null;
	
	private final int HTTP_REQUEST_GET_RANDOM_ID_V2 = 2;
	
	private int PROFILE_IMAGE_UPLOAD = 1;
	ObjectMapper mapper = new ObjectMapper();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try
		{
			// 세션을 초기화 한다.
			Session.initialize(this);

			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_kakao_login);

			// 카카오 로그인에 필요한 해쉬키 알아내기 위해
			getAppKeyHash();
			
			application = (NearhereApplication) getApplication();
		}
		catch( Exception ex )
		{

		}
	}

	private void getAppKeyHash() {
		try {
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md;
				md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				String something = new String(Base64.encode(md.digest(), 0));
				Log.d("Hash key", something);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e("name not found", e.toString());
		}
	}

	public class MySessionStatusCallback implements SessionCallback {

		// 세션이 오픈되었으면 가입페이지로 이동 한다.
		@Override
		public void onSessionOpened() {
			// 뺑글이 종료
			try
			{
				// 프로그레스바를 보이고 있었다면 중지하고 세션 오픈후 보일 페이지로 이동
				onSessionOpened();
			}
			catch( Exception ex )
			{

			}
		}

		// 세션이 삭제되었으니 로그인 화면이 보여야 한다.
		@Override
		public void onSessionClosed(final KakaoException exception) {
			// 뺑글이 종료
			try
			{
				// 프로그레스바를 보이고 있었다면 중지하고 세션 오픈을 못했으니 다시 로그인 버튼 노출.
				loginButton.setVisibility(View.VISIBLE);
			}
			catch( Exception ex )
			{

			}
		}

		@Override
		public void onSessionOpening() {
			//뺑글이 시작
		}
	}

	protected void onSessionOpened(){

		application.showToastMessage("onSessionOpened");

		UserManagement.requestMe(new MeResponseCallback() {

            @Override
            protected void onSuccess(final UserProfile userProfile) {
            	
            	try
            	{
            		Logger.getInstance().d("UserProfile!!![ " + userProfile.getId() + "] : " + userProfile);
                    userProfile.saveUserToCache();
                    
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
                Logger.getInstance().d(message);
            }
        });
		
		/*
		KakaoTalkService.requestProfile(new KakaoTalkHttpResponseHandler<KakaoTalkProfile>() {
			@Override
			protected void onHttpSuccess(final KakaoTalkProfile talkProfile) {

				String profileImageURL = talkProfile.getProfileImageURL();

				Log.d("image", talkProfile.toString());
				
//				getRandomIDV2( talkProfile.)


				goTermsAgreementActivity(null);
			}

			@Override
			protected void onHttpSessionClosedFailure(APIErrorResult errorResult) {
				//                redirectLoginActivity();
			}

			@Override
			protected void onNotKakaoTalkUser() {
				Toast.makeText(getApplicationContext(), "not a KakaoTalk user", Toast.LENGTH_SHORT).show();
			}

			@Override
			protected void onFailure(APIErrorResult errorResult) {
				Toast.makeText(getApplicationContext(), "failure : " + errorResult, Toast.LENGTH_LONG).show();
			}
		});

		final Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
		*/
	}

	public static void onClickLogout() {
		UserManagement.requestLogout(new LogoutResponseCallback() {
			@Override
			protected void onSuccess(final long userId) {
			}

			@Override
			protected void onFailure(final APIErrorResult apiErrorResult) {
			}
		});
	}

	private void getRandomIDV2( String kakaoID, String nickName, String thumbnailImageURL, String profileImageURL ) throws IOException, JsonGenerationException,
	JsonMappingException {
		setProgressBarIndeterminateVisibility(true);
		HashMap request = getDefaultRequest();
		
		final User user = new User();
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
							
						}
					}).execute( loadedImage );
				}
			}
		});
	}

	public HashMap getDefaultRequest()
	{
		HashMap request = new HashMap();
		request.put("OSVersion", getOSVersion());
		request.put("AppVersion", getPackageVersion());
		return request;
	}
	
	public String getPackageVersion()
	{
		PackageInfo pInfo;
		try {
			
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			
			return pInfo.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "";
	}

	public String getOSVersion()
	{
		return Build.VERSION.RELEASE;
	}
	
	public void goTermsAgreementActivity( View v )
	{
		Intent intent = new Intent( this, TermsAgreementActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
		finish();
	}

}
