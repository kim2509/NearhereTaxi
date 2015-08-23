package com.tessoft.nearhere;

import java.security.MessageDigest;

import org.codehaus.jackson.map.ObjectMapper;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.kakao.APIErrorResult;
import com.kakao.LogoutResponseCallback;
import com.kakao.Session;
import com.kakao.UserManagement;
import com.kakao.template.loginbase.SampleLoginActivity;
import com.tessoft.domain.User;

public class KakaoLoginActivity extends SampleLoginActivity{

	static NearhereApplication application = null;
	
	ObjectMapper mapper = new ObjectMapper();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try
		{
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
				Log.d("debug", "hash key:[" + something + "]");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e("name not found", e.toString());
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	protected void onSessionOpened(){
		
		Log.d("debug", "[KakaoLoginActivity] onSessionOpened. starting KakaoSignupActivity.");
		
		final Intent intent = new Intent( this, KakaoSignupActivity.class);
        startActivity(intent);
        finish();
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
	
	boolean doubleBackToExitPressedOnce = false;
	@Override
	public void onBackPressed() {

		try {
			application.debug( this, mapper.writeValueAsString( application.getLoginUser() ) );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (doubleBackToExitPressedOnce) {
			super.onBackPressed();
			return;
		}

		this.doubleBackToExitPressedOnce = true;
		Toast.makeText(this, "이전 버튼을 한번 더 누르면 종료합니다.", Toast.LENGTH_SHORT).show();

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				doubleBackToExitPressedOnce=false;                       
			}
		}, 2000);
	}
}
