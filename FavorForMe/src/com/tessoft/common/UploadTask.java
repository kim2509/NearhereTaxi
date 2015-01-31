package com.tessoft.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.Toast;

public class UploadTask extends AsyncTask<Bitmap, Void, Void> {
	
	Context context = null;
	String fileName = "";
	
	public UploadTask( Context context, String fileName )
	{
		this.context = context;
		this.fileName = fileName;
	}
	
	protected Void doInBackground(Bitmap... bitmaps) {
		if (bitmaps[0] == null)
			return null;
//		setProgress(0);
		
		Bitmap bitmap = bitmaps[0];
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream); // convert Bitmap to ByteArrayOutputStream
		InputStream in = new ByteArrayInputStream(stream.toByteArray()); // convert ByteArrayOutputStream to ByteArrayInputStream

		DefaultHttpClient httpclient = new DefaultHttpClient();
		try {
			HttpPost httppost = new HttpPost(
					Constants.serverURL + "/taxi/uploadUserProfilePhoto.do"); // server

			MultipartEntity reqEntity = new MultipartEntity();
			reqEntity.addPart("file", this.fileName + ".png", in);
			httppost.setEntity(reqEntity);

//			Log.i(TAG, "request " + httppost.getRequestLine());
			HttpResponse response = null;
			try {
				response = httpclient.execute(httppost);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
//				if (response != null)
//					Log.i(TAG, "response " + response.getStatusLine().toString());
			} finally {

			}
		} finally {

		}

		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return null;
	}
	
	class HttpData {
		public String content;
		@SuppressWarnings("rawtypes")
		public Hashtable cookies = new Hashtable();
		@SuppressWarnings("rawtypes")
		public Hashtable headers = new Hashtable();
	}
	
	@Override
	protected void onProgressUpdate(Void... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
	}
	
	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		Toast.makeText( context, "uploaded successfully.", Toast.LENGTH_LONG).show();
	}
}