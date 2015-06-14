package com.tessoft.nearhere;

import java.security.MessageDigest;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.kakao.APIErrorResult;
import com.kakao.KakaoTalkHttpResponseHandler;
import com.kakao.KakaoTalkProfile;
import com.kakao.KakaoTalkService;
import com.kakao.LogoutResponseCallback;
import com.kakao.Session;
import com.kakao.SessionCallback;
import com.kakao.UserManagement;
import com.kakao.exception.KakaoException;
import com.kakao.template.loginbase.SampleLoginActivity;
import com.kakao.widget.LoginButton;

public class KakaoLoginActivity extends SampleLoginActivity{

	private LoginButton loginButton;
	private final SessionCallback mySessionCallback = new MySessionStatusCallback();
	private Session session;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try
		{
			// 세션을 초기화 한다.
	        Session.initialize(this);
	        
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_register_user);
	        
	        // 카카오 로그인에 필요한 해쉬키 알아내기 위해
	     	getAppKeyHash();
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
		
		KakaoTalkService.requestProfile(new KakaoTalkHttpResponseHandler<KakaoTalkProfile>() {
            @Override
            protected void onHttpSuccess(final KakaoTalkProfile talkProfile) {
                Toast.makeText(getApplicationContext(), "success to get talk profile", Toast.LENGTH_SHORT).show();
//                applyTalkProfileToView(talkProfile);
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
