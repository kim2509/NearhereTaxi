package com.tessoft.nearhere;

import java.security.MessageDigest;

import org.codehaus.jackson.map.ObjectMapper;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.kakao.APIErrorResult;
import com.kakao.LogoutResponseCallback;
import com.kakao.Session;
import com.kakao.UserManagement;
import com.kakao.template.loginbase.SampleLoginActivity;

public class KakaoLoginActivity extends SampleLoginActivity{

	NearhereApplication application = null;
	
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

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	protected void onSessionOpened(){
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
}
