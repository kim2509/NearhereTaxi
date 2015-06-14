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

import com.kakao.Session;
import com.kakao.SessionCallback;
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
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_register_user);
			
			// 세션을 초기화 한다.
	        Session.initialize(this);

	        loginButton = (LoginButton) findViewById(R.id.com_kakao_login);
	        
	        // 세션 콜백 추가
	        session = Session.getCurrentSession();
	        session.addCallback(mySessionCallback);
	        
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
	
	@Override
    protected void onResume() {
        super.onResume();
        // 세션이 완전 종료된 상태로 갱신도 할 수 없으므로 명시적 오픈을 위한 로그인 버튼을 보여준다.
        if (session.isClosed()){
            loginButton.setVisibility(View.VISIBLE);
        }
        // 세션을 가지고 있거나, 갱신할 수 있는 상태로 명시적 오픈을 위한 로그인 버튼을 보여주지 않는다.
        else {
            loginButton.setVisibility(View.GONE);

            // 갱신이 가능한 상태라면 갱신을 시켜준다.
            if (session.isOpenable()) {
                session.implicitOpen();
            }
        }
    }
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		session.removeCallback(mySessionCallback);
	}
	
	public class MySessionStatusCallback implements SessionCallback {
        /**
         * 세션이 오픈되었으면 가입페이지로 이동 한다.
         */
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

        /**
         * 세션이 삭제되었으니 로그인 화면이 보여야 한다.
         * @param exception  에러가 발생하여 close가 된 경우 해당 exception
         */
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
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
