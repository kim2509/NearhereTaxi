package com.tessoft.nearhere.fragment;

import com.tessoft.common.Constants;
import com.tessoft.nearhere.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class MainFragment extends BaseFragment {

	View rootView = null;
	WebView webView = null;
	
	WebViewClient webViewClient = new WebViewClient() {
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
		}
		
		public void onReceivedSslError(WebView view, android.webkit.SslErrorHandler handler, android.net.http.SslError error) {
			handler.proceed();
		};
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		try
		{
			super.onCreateView(inflater, container, savedInstanceState);
			rootView = inflater.inflate(R.layout.fragment_main, container, false);
			
			webView = (WebView) rootView.findViewById(R.id.webView);
			webView.getSettings().setJavaScriptEnabled(true);
			webView.clearView();
			webView.setWebViewClient( webViewClient );
			webView.setWebChromeClient(new WebChromeClient());
			webView.loadUrl( Constants.serverURL );
			
			Button btnRefresh = (Button) rootView.findViewById(R.id.btnRefresh);
			btnRefresh.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					try {
						webView.reload();
					} catch ( Exception ex ) {
						// TODO Auto-generated catch block
						catchException(this, ex);
					}
				}
			});
		}
		catch( Exception ex )
		{
			
		}
	
		return rootView;
	}
}
