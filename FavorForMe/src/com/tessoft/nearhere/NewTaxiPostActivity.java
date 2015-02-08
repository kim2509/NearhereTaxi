package com.tessoft.nearhere;

import java.util.Date;

import org.codehaus.jackson.type.TypeReference;

import com.google.android.gms.maps.model.LatLng;
import com.tessoft.common.Constants;
import com.tessoft.common.Util;
import com.tessoft.domain.APIResponse;
import com.tessoft.domain.Post;

import android.app.DatePickerDialog.OnDateSetListener;
import android.app.DialogFragment;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

public class NewTaxiPostActivity extends BaseActivity implements OnClickListener, OnTimeSetListener, OnDateSetListener{

	private static final int MODIFY_POST = 2;
	TextView txtDeparture = null;
	TextView txtDestination = null;
	Post post = null;
	Spinner spSexInfo = null;
	Spinner spNumOfUsers = null;
	EditText edtMessage = null;
	String mode = "new";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		try
		{
			super.onCreate(savedInstanceState);
			
			supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
			
			setContentView(R.layout.activity_new_taxi_post);

			txtDeparture = (TextView) findViewById(R.id.txtDeparture);
			
			txtDestination = (TextView) findViewById(R.id.txtDestination);
			
			TextView txtChangeDeparture = (TextView) findViewById(R.id.txtChangeDeparture);
			txtChangeDeparture.setOnClickListener(this);
			TextView txtChangeDestination = (TextView) findViewById(R.id.txtChangeDestination);
			txtChangeDestination.setOnClickListener(this);
			TextView txtChangeDepartureTime = (TextView) findViewById(R.id.txtChangeDepartureTime);
			txtChangeDepartureTime.setOnClickListener(this);
			TextView txtChangeDepartureDate = (TextView) findViewById(R.id.txtChangeDepartureDate);
			txtChangeDepartureDate.setOnClickListener(this);
			
			spSexInfo = (Spinner) findViewById(R.id.spSex);
			ArrayAdapter<CharSequence> sexAdapter = ArrayAdapter.createFromResource( this ,
					R.array.sex_select_list, android.R.layout.simple_spinner_item);
			sexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spSexInfo.setAdapter(sexAdapter);

			spNumOfUsers = (Spinner) findViewById(R.id.spNumOfUsers);
			ArrayAdapter<CharSequence> nouAdapter = ArrayAdapter.createFromResource( this ,
					R.array.nou_list, android.R.layout.simple_spinner_item);
			nouAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spNumOfUsers.setAdapter(nouAdapter);
			
			txtDepartureDate = (TextView) findViewById(R.id.txtDepartureDate);
			txtDepartureTime = (TextView) findViewById(R.id.txtDepartureTime);
			
			edtMessage = (EditText) findViewById(R.id.edtMessage);
			
			Button btnSend = (Button) findViewById(R.id.btnSend);
			
			if ( getIntent().getExtras() != null && getIntent().getExtras().containsKey("mode"))
			{
				// 수정
				if ( "modify".equals( getIntent().getExtras().getString("mode") ))
				{
					setTitle("합승정보수정");
					post = (Post) getIntent().getExtras().get("post");
					btnSend.setText("수정하기");
					displayModifyInfo();
					mode = "modify";
				}
			}
			else
			{
				// 신규
				if ( getIntent().getExtras() != null && getIntent().getExtras().containsKey("departure") )
				{
					departure = (LatLng) getIntent().getExtras().get("departure");
					txtDeparture.setText( getIntent().getExtras().getString("address"));
				}
				else if ( Util.isEmptyString( MainActivity.address ) == false )
				{
					double latitude =  Util.getDouble( MainActivity.latitude );
					double longitude = Util.getDouble( MainActivity.longitude );
					departure = new LatLng(latitude, longitude);
					txtDeparture.setText( MainActivity.address );
				}	
				
				btnSend.setText("등록하기");
				
				mode = "new";
			}
			
			setProgressBarIndeterminateVisibility(false);
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_taxi_post, menu);
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
		overridePendingTransition(R.anim.stay, R.anim.slide_out_to_bottom);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if ( v.getId() == R.id.txtChangeDeparture )
			selectDeparture();
		else if ( v.getId() == R.id.txtChangeDestination )
			selectDestination();
		else if ( v.getId() == R.id.txtChangeDepartureDate )
			showDatePickerDialog(null);
		else if ( v.getId() == R.id.txtChangeDepartureTime )
			showTimePickerDialog(null);
	}
	
	public void showDatePickerDialog(View v) {
		DialogFragment newFragment = new DatePickerFragment( this );
		newFragment.show(getFragmentManager(), "datePicker");
	}
	
	public void showTimePickerDialog(View v) {
		DialogFragment newFragment = new TimePickerFragment( this );
		newFragment.show(getFragmentManager(), "timePicker");
	}

	TextView txtDepartureTime  = null;
	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		// TODO Auto-generated method stub
		Date date = new Date();
		date.setHours( hourOfDay );
		date.setMinutes( minute );
		
		String timeString = Util.getDateStringFromDate( date, "HH:mm" );
		txtDepartureTime.setText( timeString );
	}

	TextView txtDepartureDate = null;
	
	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		// TODO Auto-generated method stub
		String dateString = Util.getDateStringFromDate(new Date( year - 1900, monthOfYear, dayOfMonth), "yyyy-MM-dd"); 
		txtDepartureDate.setText( dateString );
	}
	
	public void selectDeparture()
	{
		Intent intent = new Intent( this, SetDestinationActivity.class);
		intent.putExtra("title", "출발지 선택");
		intent.putExtra("subTitle", "출발지를 선택해 주십시오.");
		intent.putExtra("departure", departure );
		startActivityForResult(intent, 1);
		overridePendingTransition(R.anim.slide_in_from_right, R.anim.stay);
	}
	
	public void selectDestination()
	{
		Intent intent = new Intent( this, SetDestinationActivity.class);
		intent.putExtra("title", "도착지 선택");
		intent.putExtra("subTitle", "도착지를 선택해 주십시오.");
		startActivityForResult(intent, 2);
		overridePendingTransition(R.anim.slide_in_from_right, R.anim.stay);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		try
		{
			if ( resultCode == RESULT_OK )
			{
				String address = data.getExtras().getString("address");
				LatLng location = (LatLng) data.getExtras().get("location");
			
				if ( requestCode == 1 )
				{
					txtDeparture.setText(address);
					departure = location;
				}
				else if ( requestCode == 2 )
				{
					txtDestination.setText(address);
					destination = location;
				}				
			}
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}
	
	LatLng departure = null;
	LatLng destination = null;
	
	public void addPost( View v )
	{
		try
		{
			// 신규등록일 경우
			if ( post == null )
				post = new Post();

			if ( TextUtils.isEmpty( edtMessage.getText() ) )
			{
				edtMessage.setError("내용을 입력해 주십시오.");
				return;
			}

			if ( departure == null )
			{
				showOKDialog("경고", "출발지를 지정해 주세요.", null);
				return;
			}
			
			if ( destination == null )
			{
				showOKDialog("경고", "도착지를 지정해 주세요.", null);
				return;
			}
			
			post.setMessage( edtMessage.getText().toString() );
			post.setFromLatitude( String.valueOf( departure.latitude) );
			post.setFromLongitude( String.valueOf( departure.longitude) );
			post.setFromAddress( txtDeparture.getText().toString() );
			post.setLatitude( String.valueOf( destination.latitude) );
			post.setLongitude( String.valueOf( destination.longitude) );
			post.setToAddress( txtDestination.getText().toString()  );
			post.setDepartureDate( txtDepartureDate.getText().toString() );
			post.setDepartureTime( txtDepartureTime.getText().toString() );
			
			Spinner spSex = (Spinner)findViewById(R.id.spSex);
			post.setSexInfo( spSex.getSelectedItem().toString() );
			
			Spinner spNumOfUsers = (Spinner)findViewById(R.id.spNumOfUsers);
			post.setNumOfUsers( spNumOfUsers.getSelectedItem().toString() );
			
			post.setUser( getLoginUser() );

			setProgressBarIndeterminateVisibility(true);
			
			if ( "new".equals( mode ) )
				sendHttp("/taxi/insertPost.do", mapper.writeValueAsString(post), 1);
			else
				sendHttp("/taxi/modifyPost.do", mapper.writeValueAsString(post), MODIFY_POST );
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	@Override
	public void doPostTransaction(int requestCode, Object result) {
		// TODO Auto-generated method stub
		try
		{
			setProgressBarIndeterminateVisibility(false);
		
			if ( Constants.FAIL.equals(result) )
			{
				
				showOKDialog("통신중 오류가 발생했습니다.\r\n다시 시도해 주십시오.", null);
				return;
			}
			
			super.doPostTransaction(requestCode, result);

			APIResponse response = mapper.readValue(result.toString(), new TypeReference<APIResponse>(){});

			if ( "0000".equals( response.getResCode() ) )
			{
				Intent intent = new Intent();
				intent.putExtra("reload", true);
				setResult( RESULT_OK , intent);
				finish();
			}
			else
			{
				showOKDialog("위치 등록도중 오류가 발생했습니다.\r\n다시 시도해 주십시오.", null);
			}

		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}
	
	public void displayModifyInfo()
	{
		if ( post == null ) return;
		
		departure = new LatLng( Double.parseDouble( post.getFromLatitude() ), 
				Double.parseDouble( post.getFromLongitude() ));
		txtDeparture.setText( post.getFromAddress());
		
		destination = new LatLng( Double.parseDouble( post.getLatitude() ), 
				Double.parseDouble( post.getLongitude() ));
		txtDestination.setText( post.getToAddress() );
	
		txtDepartureDate.setText( Util.getString( post.getDepartureDate() ) );
		txtDepartureTime.setText( Util.getString( post.getDepartureTime() ) );
		
		ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spSexInfo.getAdapter();
		spSexInfo.setSelection( adapter.getPosition( post.getSexInfo() ) );
		adapter = (ArrayAdapter<CharSequence>) spNumOfUsers.getAdapter();
		spNumOfUsers.setSelection( adapter.getPosition( post.getNumOfUsers() ) );
		
		edtMessage.setText( post.getMessage() );
	}
}
