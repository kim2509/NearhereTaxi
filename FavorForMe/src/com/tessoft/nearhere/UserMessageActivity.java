package com.tessoft.nearhere;

import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.type.TypeReference;

import com.tessoft.common.UserMessageArrayAdapter;
import com.tessoft.domain.APIResponse;
import com.tessoft.domain.User;
import com.tessoft.domain.UserMessage;

import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class UserMessageActivity extends BaseActivity {

	ListView listMain = null;
	View header = null;
	View footer = null;
	UserMessageArrayAdapter adapter = null;
	HashMap messageInfo = null;
	Runnable mRunnable = null;
	Handler mHandler = null; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try
		{
			super.onCreate(savedInstanceState);
			
			supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
			
			setContentView(R.layout.activity_user_message);
			
			header = getLayoutInflater().inflate(R.layout.list_header_user_message, null);
			
			listMain = (ListView) findViewById(R.id.listMain);
			//listMain.addHeaderView(header);
			
			adapter = new UserMessageArrayAdapter( getApplicationContext(), this, getLoginUser(), 0 );
			listMain.setAdapter(adapter);
			adapter.setDelegate(this);
			
			listMain.setDivider(null);
			
			listMain.setOnScrollListener( new OnScrollListener() {
				
				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {
					// TODO Auto-generated method stub
					bForceScrollDown = false;
				}
				
				@Override
				public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {
					// TODO Auto-generated method stub
					
				}
			});
			
			messageInfo = (HashMap) getIntent().getExtras().get("messageInfo");
			setProgressBarIndeterminateVisibility(true);
			sendHttp("/taxi/getUserMessage.do", mapper.writeValueAsString( messageInfo ), 1);
			
			mRunnable = new Runnable() {
	            @Override
	            public void run() {
	            	try
	            	{
	            		setProgressBarIndeterminateVisibility(true);
	            		sendHttp("/taxi/getUserMessage.do", mapper.writeValueAsString( messageInfo ), 1);	
	            	}
	            	catch( Exception ex )
	            	{
	            		catchException(this, ex);
	            	}
	            }
	        };
	        mHandler = new Handler();
			
			Button btnSend = (Button) findViewById(R.id.btnSend);
			btnSend.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					try
					{
						EditText edtMessage = (EditText) findViewById(R.id.edtMessage);
						String message = edtMessage.getText().toString();
						edtMessage.setText("");
						
						UserMessage userMessage = new UserMessage();
						userMessage.setFromUser( getLoginUser() );
						User toUser = new User();
						toUser.setUserID( messageInfo.get("fromUserID").toString() );
						userMessage.setToUser( toUser );
						userMessage.setMessage(message);
						setProgressBarIndeterminateVisibility(true);
						sendHttp("/taxi/sendUserMessage.do", mapper.writeValueAsString( userMessage ), 1);	
						bForceScrollDown = true;
					}
					catch( Exception ex )
					{
						catchException(this, ex);
					}
				}
			});
		}
		catch(Exception ex )
		{
			catchException(this, ex);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.user_chat, menu);
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
	
	private boolean bStopFetchMessage = false;
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		bStopFetchMessage = true;
	}
	
	boolean bForceScrollDown = true;
	
	@Override
	public void doPostTransaction(int requestCode, Object result) {

		try
		{
			setProgressBarIndeterminateVisibility(false);
			
			// TODO Auto-generated method stub
			super.doPostTransaction(requestCode, result);	
			
			APIResponse response = mapper.readValue(result.toString(), new TypeReference<APIResponse>(){});
			
			if ( requestCode == 1 && "0000".equals( response.getResCode() ) )
			{
				String userMessageString = mapper.writeValueAsString( response.getData() );
				List<UserMessage> messageList = mapper.readValue( userMessageString , new TypeReference<List<UserMessage>>(){});
				adapter.setItemList(messageList);
				adapter.notifyDataSetChanged();
				
				if ( bForceScrollDown )
					listMain.setSelection(adapter.getCount() - 1);
		        
				if ( bStopFetchMessage == false )
					mHandler.postDelayed(mRunnable, 5000);
			}
			
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}
	
	@Override
	public void doAction(String actionName, Object param) {
		
		try
		{
			// TODO Auto-generated method stub
			super.doAction(actionName, param);	
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

}