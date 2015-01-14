package com.tessoft.common;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;
import android.util.Log;

public class HttpTransactionReturningString extends AsyncTask<Object, Integer, String> {

	private String url = "";
	int requestCode = 0;
	private TransactionDelegate delegate;
	
	public HttpTransactionReturningString( TransactionDelegate delegate, String url, int requestCode )
	{
		this.delegate = delegate;
		this.url = url;
		this.requestCode = requestCode;
	}

	protected void onPreExecute() {

	}

	protected String doInBackground( Object... data ) {

		try
		{
			HttpClient client = new DefaultHttpClient();
			HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit
			HttpResponse response;
			
			Object json = ( data == null || data.length == 0 || data[0] == null ) ? "" : data[0];
			
			String serverURL = Constants.serverURL + url;
			
			Log.d("HTTPRequest", "Connecting to URL:" + serverURL );
			
			HttpPost post = new HttpPost( serverURL );
			
			Log.d("HTTPRequest", "Request String:" + json.toString() );
			
			StringEntity se = new StringEntity( json.toString(), "UTF-8");
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=utf-8"));
			post.setEntity(se);
			response = client.execute(post);

			String responseString = EntityUtils.toString(response.getEntity());

			Log.d("HTTPRequest", "Successfully got response!!" );
			
			Log.d("HTTPRequest", "ResponseString:" + json.toString() );
			
			return responseString;

		}
		catch(Exception e){
			e.printStackTrace();
			
			Log.e("HTTPRequest", "Error while connecting to server." );
			
			writeLog( e.getMessage() );

			return Constants.FAIL;
		}
	}

	protected void onProgressUpdate(Integer... progress) {

	}

	protected void onPostExecute(String result) {

		delegate.doPostTransaction( requestCode, result );
		
	}

	public void writeLog( String log )
	{
		Log.e("FavorForMe", log );
	}
}
