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
import com.tessoft.common.Util;
import com.tessoft.domain.ListItemModel;
import com.tessoft.domain.MainInfo;
import com.tessoft.domain.Post;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		try
		{
			super.onCreate(savedInstanceState);

			setContentView(R.layout.activity_post_detail);
			
			getActionBar().setTitle( "심부름 상세" );
			
			post = (Post) getIntent().getExtras().get("post");
			
			TextView txtSubTitle = (TextView) findViewById(R.id.txtSubtitle);
			txtSubTitle.setText( post.getMessage() );

			ImageView imageView = (ImageView) findViewById(R.id.imgProfile);
			ImageLoader.getInstance().displayImage( Constants.imageServerURL + "c.png", imageView);
			
			MapFragment mapFragment = (MapFragment) getFragmentManager()
					.findFragmentById(R.id.map);
			mapFragment.getMapAsync(this);
			
			makeMapScrollable();
			
			listPostReplies = (ListView) findViewById(R.id.listPostReplies);
			MainArrayAdapter adapter = new MainArrayAdapter( getApplicationContext(), 0 );
			listPostReplies.setAdapter(adapter);
			
			mapper = new ObjectMapper();
			execTransReturningString("/getPostDetail.do", mapper.writeValueAsString(post), 1);
		}
		catch( Exception ex )
		{
			showToastMessage(ex.getMessage());
		}
	}

	private void makeMapScrollable() {
		mainScrollView = (ScrollView) findViewById(R.id.scrollView);
		ImageView transparentImageView = (ImageView) findViewById(R.id.transparent_image);

		transparentImageView.setOnTouchListener(new View.OnTouchListener() {

		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        int action = event.getAction();
		        switch (action) {
		           case MotionEvent.ACTION_DOWN:
		                // Disallow ScrollView to intercept touch events.
		                mainScrollView.requestDisallowInterceptTouchEvent(true);
		                // Disable touch on transparent view
		                return false;

		           case MotionEvent.ACTION_UP:
		                // Allow ScrollView to intercept touch events.
		                mainScrollView.requestDisallowInterceptTouchEvent(false);
		                return true;

		           case MotionEvent.ACTION_MOVE:
		                mainScrollView.requestDisallowInterceptTouchEvent(true);
		                return false;

		           default: 
		                return true;
		        }   
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
				MainArrayAdapter adapter = (MainArrayAdapter) listPostReplies.getAdapter();
				post = mapper.readValue(result.toString(), new TypeReference<Post>(){});
				List<ListItemModel> listReplies = (List<ListItemModel>) (Object) post.getPostReplies();
				adapter.setItemList(listReplies);
				adapter.notifyDataSetChanged();
			}
			
			mainScrollView.fullScroll(ScrollView.FOCUS_UP);
		}
		catch( Exception ex )
		{
			Log.e("error", ex.getMessage() );
		}
		
	}
}
