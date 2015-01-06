package com.tessoft.favorforme;

import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tessoft.common.Constants;
import com.tessoft.common.MainArrayAdapter;
import com.tessoft.domain.ListItemModel;
import com.tessoft.domain.Post;
import com.tessoft.domain.User;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

public class PostDetailActivity extends BaseActivity implements OnMapReadyCallback{

	Post post = null;
	int ZoomLevel = 16;
	GoogleMap map = null;
	ScrollView mainScrollView = null;
	ListView listPostReplies = null;
	ObjectMapper mapper = null;
	View header = null;
	View footer = null;
	MainArrayAdapter adapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		try
		{
			super.onCreate(savedInstanceState);

			setContentView(R.layout.activity_post_detail);

			getActionBar().setTitle( "HELP 상세" );

			post = (Post) getIntent().getExtras().get("post");

			header = getLayoutInflater().inflate(R.layout.post_detail_header, null);
			listPostReplies = (ListView) findViewById(R.id.listPostReplies);
			listPostReplies.addHeaderView(header);
			footer = getLayoutInflater().inflate(R.layout.post_detail_footer, null);
			listPostReplies.addFooterView(footer);
			
			TextView txtSubTitle = (TextView) header.findViewById(R.id.txtSubtitle);
			txtSubTitle.setText( post.getMessage() );
			
			ImageView imageView = (ImageView) header.findViewById(R.id.imgProfile);
			ImageLoader.getInstance().displayImage( Constants.imageServerURL + "c.png", imageView);
			MapFragment mapFragment = (MapFragment) getFragmentManager()
					.findFragmentById(R.id.map);
			mapFragment.getMapAsync(this);
			makeMapScrollable();
			
			adapter = new MainArrayAdapter( getApplicationContext(), 0 );
			adapter.setDelegate(this);
			
			listPostReplies.setAdapter(adapter);
			
			mapper = new ObjectMapper();
			execTransReturningString("/getPostDetail.do", mapper.writeValueAsString(post), 1);
			
			ImageView imageProfile = (ImageView) footer.findViewById(R.id.imgProfile);
			ImageLoader.getInstance().displayImage(Constants.imageServerURL + "d.png", imageProfile);
		}
		catch( Exception ex )
		{
			showToastMessage(ex.getMessage());
		}
	}

	private void makeMapScrollable() {
		
		ImageView transparentImageView = (ImageView) header.findViewById(R.id.transparent_image);

		transparentImageView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				try
				{
					int action = event.getAction();
					switch (action) {
					case MotionEvent.ACTION_DOWN:
						// Disallow ScrollView to intercept touch events.
						listPostReplies.requestDisallowInterceptTouchEvent(true);
						// Disable touch on transparent view
						return false;

					case MotionEvent.ACTION_UP:
						// Allow ScrollView to intercept touch events.
						listPostReplies.requestDisallowInterceptTouchEvent(false);
						return true;

					case MotionEvent.ACTION_MOVE:
						listPostReplies.requestDisallowInterceptTouchEvent(true);
						return false;

					default: 
						return true;
					}   
				}
				catch( Exception ex )
				{
					Log.e("error", ex.getMessage());
				}
				
				return true;
			}
		});
	}

	@Override
	public void onMapReady(GoogleMap map) {
		// TODO Auto-generated method stub

		try
		{
			this.map = map;

			Marker marker = map.addMarker(new MarkerOptions()
			.position(new LatLng(Double.parseDouble( post.getLatitude() ), Double.parseDouble(post.getLongitude())))
			.title(post.getMessage()));
			marker.showInfoWindow();

			CameraUpdate center= CameraUpdateFactory.newLatLng( 
					new LatLng( Double.parseDouble(post.getLatitude()) , Double.parseDouble( post.getLongitude() ) ) );

			map.moveCamera(center);
			CameraUpdate zoom=CameraUpdateFactory.zoomTo(ZoomLevel);
			map.animateCamera(zoom);
			map.setPadding(0, 200, 0, 0);

			Log.i("debug", "lat:" + post.getLatitude() + " long:" + post.getLongitude() );
		}
		catch( Exception ex )
		{
			Log.e("error", ex.getMessage() );
		}

	}

	@Override
	public void doPostTransaction(int requestCode, Object result) {

		try
		{
			// TODO Auto-generated method stub
			super.doPostTransaction(requestCode, result);

			Log.i("postDetail", result.toString());

			if ( requestCode == 1 )
			{
				post = mapper.readValue(result.toString(), new TypeReference<Post>(){});

				ImageView imgProfile = (ImageView) header.findViewById(R.id.imgProfile);
				ImageLoader.getInstance().displayImage(
						Constants.imageServerURL + post.getUser().getProfileImageURL(), imgProfile);
				
				TextView txtUserName = (TextView) header.findViewById(R.id.txtUserName);
				txtUserName.setOnClickListener( new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						User user = (User) v.getTag();
						doAction(1, user);
					}
				});

				txtUserName.setText( post.getUser().getUserName() );
				txtUserName.setTag( post.getUser() );
				
				TextView txtReward = (TextView) header.findViewById(R.id.txtReward);
				txtReward.setText( "보상 " + post.getReward() + "원");

				TextView txtPostDetailMsg = (TextView) header.findViewById(R.id.txtPostDetailMsg);
				txtPostDetailMsg.setText( post.getContent() );

				List<ListItemModel> listReplies = (List<ListItemModel>) (Object) post.getPostReplies();
				
				TextView txtReplyNotice = (TextView) header.findViewById(R.id.txtReplyNotice);
				txtReplyNotice.setText( listReplies.size() + " 개의 댓글이 있습니다.");
				
				adapter.setItemList(listReplies);
				adapter.notifyDataSetChanged();
			}
		}
		catch( Exception ex )
		{
			Log.e("error", ex.getMessage() );
		}

	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
	
	@Override
	public void doAction(int actionCode, Object param) {
		
		try
		{
			// TODO Auto-generated method stub
			super.doAction(actionCode, param);
			
			User user = (User) param;
			Intent intent = new Intent( getApplicationContext(), UserProfileActivity.class);
			intent.putExtra("user", user );
			startActivity(intent);
			overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.stay);
		}
		catch( Exception ex )
		{
			Log.e("error", ex.getMessage());			
		}
	}
	
	public void addPostReply( View view )
	{
		if ( view.getId() == R.id.btnSendReply )
		{
			EditText edtReplyMessage = (EditText) findViewById(R.id.edtReplyMessage);
			String replyText = edtReplyMessage.getText().toString();
			edtReplyMessage.setText("");
			
		}
	}
}
