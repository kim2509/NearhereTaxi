package com.tessoft.nearhere;

import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.type.TypeReference;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tessoft.common.Constants;
import com.tessoft.common.NoticeListAdapter;
import com.tessoft.common.TaxiArrayAdapter;
import com.tessoft.common.Util;
import com.tessoft.domain.APIResponse;
import com.tessoft.domain.Post;
import com.tessoft.domain.User;
import com.tessoft.domain.UserLocation;
import com.tessoft.nearhere.R;

public class UserProfileActivity extends BaseActivity {

	ListView listMain = null;
	View header = null;
	View footer = null;
	TaxiArrayAdapter adapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		try
		{
			super.onCreate(savedInstanceState);

			supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

			setContentView(R.layout.activity_user_profile);

			header = getLayoutInflater().inflate(R.layout.user_profile_list_header, null);
			footer = getLayoutInflater().inflate(R.layout.user_profile_list_footer, null);

			listMain = (ListView) findViewById(R.id.listMain);
			listMain.addHeaderView(header, null, false );

			adapter = new TaxiArrayAdapter( this , this, 0);
			listMain.setAdapter(adapter);

			listMain.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					if ( arg1.getTag() != null && arg1.getTag() instanceof Post )
					{
						Post post = (Post) arg1.getTag();

						Intent intent = new Intent( getApplicationContext(), TaxiPostDetailActivity.class);
						intent.putExtra("postID", post.getPostID());
						startActivity(intent);
						overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);						
					}
				}
			});

			footer.findViewById(R.id.txtNone).setVisibility(ViewGroup.GONE);

			User user = new User();
			user.setUserID(getIntent().getExtras().getString("userID"));
			setProgressBarIndeterminateVisibility(true);
			sendHttp("/taxi/getUserInfo.do", mapper.writeValueAsString( user ), 1);

			mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

			ImageView imgProfile = (ImageView) header.findViewById(R.id.imgProfile);
			imgProfile.setOnClickListener( new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					zoomImageFromThumb( v );
				}
			});
		}
		catch( Exception ex )
		{
			Log.e("error", ex.getMessage());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		try
		{
			getMenuInflater().inflate(R.menu.user_profile, menu);

			MenuItem item = menu.findItem(R.id.action_chat);

			if ( getIntent().getExtras().getString("userID").equals( getLoginUser().getUserID() ))
				item.setVisible(false);
			else
				item.setVisible(true);
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.

		try
		{
			int id = item.getItemId();
			if (id == R.id.action_chat) {

				HashMap hash = new HashMap();
				hash.put("fromUserID", getIntent().getExtras().getString("userID") );
				hash.put("userID",  getLoginUser().getUserID() );
				Intent intent = new Intent( this, UserMessageActivity.class);
				intent.putExtra("messageInfo", hash );
				startActivity(intent);
				overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);

				return true;
			}

		}
		catch( Exception ex )
		{

		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		this.overridePendingTransition(R.anim.stay, R.anim.slide_out_to_bottom);
	}

	@Override
	public void doPostTransaction(int requestCode, Object result) {
		// TODO Auto-generated method stub
		try
		{
			if ( Constants.FAIL.equals(result) )
			{
				setProgressBarIndeterminateVisibility(false);
				showOKDialog("통신중 오류가 발생했습니다.\r\n다시 시도해 주십시오.", null);
				return;
			}

			setProgressBarIndeterminateVisibility(false);
			super.doPostTransaction(requestCode, result);		


			APIResponse response = mapper.readValue(result.toString(), new TypeReference<APIResponse>(){});

			if ( "0000".equals( response.getResCode() ) )
			{
				if ( requestCode == 1 )
				{
					HashMap hash = (HashMap) response.getData();

					String userString = mapper.writeValueAsString( hash.get("user") );
					String locationListString = mapper.writeValueAsString( hash.get("locationList") );
					String userPostString = mapper.writeValueAsString( hash.get("userPost") );
					String postsUserRepliedString = mapper.writeValueAsString( hash.get("postsUserReplied") );

					User user = mapper.readValue(userString, new TypeReference<User>(){});
					List<UserLocation> locationList = mapper.readValue(locationListString, new TypeReference<List<UserLocation>>(){});
					List<Post> postList = mapper.readValue(userPostString, new TypeReference<List<Post>>(){});
					List<Post> userPostsReplied = mapper.readValue(postsUserRepliedString, new TypeReference<List<Post>>(){});
					postList.addAll( userPostsReplied );

					ImageView imgProfile = (ImageView) header.findViewById(R.id.imgProfile);
					imgProfile.setImageResource(R.drawable.no_image);

					if ( user != null && user.getProfileImageURL() != null && user.getProfileImageURL().isEmpty() == false )
					{
						ImageLoader.getInstance().displayImage( Constants.imageServerURL + 
								user.getProfileImageURL() , imgProfile);
						imgProfile.setTag(user.getProfileImageURL());
					}

					TextView txtUserName = (TextView) header.findViewById(R.id.txtUserName);

					if ( Util.isEmptyString( user.getUserName() ) )
						txtUserName.setText( user.getUserID() );
					else
						txtUserName.setText( user.getUserName() + " (" + user.getUserID() + ")" );

					TextView txtCreditValue = (TextView) header.findViewById(R.id.txtCreditValue);
					txtCreditValue.setText( user.getProfilePoint() + "%" );

					if ( user.getBirthday() != null && !"".equals( user.getBirthday() ) )
					{
						String birthday = Util.getFormattedDateString(user.getBirthday(),"yyyy-MM-dd", "yyyy.MM.dd");
						TextView txtBirthday = (TextView) header.findViewById(R.id.txtBirthday );
						txtBirthday.setText( birthday );
					}

					for ( int i = 0; i < locationList.size(); i++ )
					{
						UserLocation loc = locationList.get(i);
						if ( "집".equals( loc.getLocationName() ) )
						{
							TextView txtHomeLocation = (TextView) header.findViewById(R.id.txtHomeLocation);
							txtHomeLocation.setText( loc.getAddress() );
						}
						else if ( "직장".equals( loc.getLocationName() ))
						{
							TextView txtOfficeLocation = (TextView) header.findViewById(R.id.txtOfficeLocation);
							txtOfficeLocation.setText( loc.getAddress() );
						}
					}

					ImageView imgSex = (ImageView) header.findViewById(R.id.imgSex);
					TextView txtSex = (TextView) header.findViewById(R.id.txtSex);
					imgSex.setVisibility(ViewGroup.VISIBLE);

					if ( "M".equals( user.getSex() ))
					{
						imgSex.setImageResource(R.drawable.male);
						txtSex.setText("남자");
					}
					else if ( "F".equals( user.getSex() ))
					{
						imgSex.setImageResource(R.drawable.female);
						txtSex.setText("여자");
					}
					else
						imgSex.setVisibility(ViewGroup.GONE);

					if ( user.getJobTitle() != null && !"".equals( user.getJobTitle() ))
					{
						TextView txtJobTitle = (TextView) header.findViewById(R.id.txtJobTitle);
						txtJobTitle.setText( user.getJobTitle() );
					}

					adapter.clear();
					adapter.addAll(postList);
					adapter.notifyDataSetChanged();

					if ( postList.size() == 0 )
						footer.findViewById(R.id.txtNone).setVisibility(ViewGroup.VISIBLE);
				}
			}
			else
			{
				showOKDialog("경고", response.getResMsg(), null);
				return;
			}

		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}
	}

	/**
	 * "Zooms" in a thumbnail view by assigning the high resolution image to a hidden "zoomed-in"
	 * image view and animating its bounds to fit the entire activity content area. More
	 * specifically:
	 *
	 * <ol>
	 *   <li>Assign the high-res image to the hidden "zoomed-in" (expanded) image view.</li>
	 *   <li>Calculate the starting and ending bounds for the expanded view.</li>
	 *   <li>Animate each of four positioning/sizing properties (X, Y, SCALE_X, SCALE_Y)
	 *       simultaneously, from the starting bounds to the ending bounds.</li>
	 *   <li>Zoom back out by running the reverse animation on click.</li>
	 * </ol>
	 *
	 * @param thumbView  The thumbnail view to zoom in.
	 * @param imageResId The high-resolution version of the image represented by the thumbnail.
	 */

	private Animator mCurrentAnimator;
	private int mShortAnimationDuration;

	private void zoomImageFromThumb(final View thumbView ) {
		// If there's an animation in progress, cancel it immediately and proceed with this one.
		if (mCurrentAnimator != null) {
			mCurrentAnimator.cancel();
		}

		String imageURL = "";
		if ( thumbView.getTag() == null || thumbView.getTag() instanceof String == false ) return;

		imageURL = thumbView.getTag().toString();
		if ( Util.isEmptyString( imageURL ) ) return;

		// Load the high-resolution "zoomed-in" image.
		final ImageView expandedImageView = (ImageView) findViewById(R.id.expanded_image);
		expandedImageView.setImageResource(0);
		ImageLoader.getInstance().displayImage( Constants.imageServerURL + 
				imageURL, expandedImageView);

		// Calculate the starting and ending bounds for the zoomed-in image. This step
		// involves lots of math. Yay, math.
		final Rect startBounds = new Rect();
		final Rect finalBounds = new Rect();
		final Point globalOffset = new Point();

		// The start bounds are the global visible rectangle of the thumbnail, and the
		// final bounds are the global visible rectangle of the container view. Also
		// set the container view's offset as the origin for the bounds, since that's
		// the origin for the positioning animation properties (X, Y).
		thumbView.getGlobalVisibleRect(startBounds);
		findViewById(R.id.container).getGlobalVisibleRect(finalBounds, globalOffset);
		startBounds.offset(-globalOffset.x, -globalOffset.y);
		finalBounds.offset(-globalOffset.x, -globalOffset.y);

		// Adjust the start bounds to be the same aspect ratio as the final bounds using the
		// "center crop" technique. This prevents undesirable stretching during the animation.
		// Also calculate the start scaling factor (the end scaling factor is always 1.0).
		float startScale;
		if ((float) finalBounds.width() / finalBounds.height()
				> (float) startBounds.width() / startBounds.height()) {
			// Extend start bounds horizontally
			startScale = (float) startBounds.height() / finalBounds.height();
			float startWidth = startScale * finalBounds.width();
			float deltaWidth = (startWidth - startBounds.width()) / 2;
			startBounds.left -= deltaWidth;
			startBounds.right += deltaWidth;
		} else {
			// Extend start bounds vertically
			startScale = (float) startBounds.width() / finalBounds.width();
			float startHeight = startScale * finalBounds.height();
			float deltaHeight = (startHeight - startBounds.height()) / 2;
			startBounds.top -= deltaHeight;
			startBounds.bottom += deltaHeight;
		}

		// Hide the thumbnail and show the zoomed-in view. When the animation begins,
		// it will position the zoomed-in view in the place of the thumbnail.
		thumbView.setAlpha(0f);
		expandedImageView.setVisibility(View.VISIBLE);

		// Set the pivot point for SCALE_X and SCALE_Y transformations to the top-left corner of
		// the zoomed-in view (the default is the center of the view).
		expandedImageView.setPivotX(0f);
		expandedImageView.setPivotY(0f);

		// Construct and run the parallel animation of the four translation and scale properties
		// (X, Y, SCALE_X, and SCALE_Y).
		AnimatorSet set = new AnimatorSet();
		set
		.play(ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left,
				finalBounds.left))
				.with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top,
						finalBounds.top))
						.with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScale, 1f))
						.with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScale, 1f));
		set.setDuration(mShortAnimationDuration);
		set.setInterpolator(new DecelerateInterpolator());
		set.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mCurrentAnimator = null;
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				mCurrentAnimator = null;
			}
		});
		set.start();
		mCurrentAnimator = set;

		// Upon clicking the zoomed-in image, it should zoom back down to the original bounds
		// and show the thumbnail instead of the expanded image.
		final float startScaleFinal = startScale;
		expandedImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mCurrentAnimator != null) {
					mCurrentAnimator.cancel();
				}

				// Animate the four positioning/sizing properties in parallel, back to their
				// original values.
				AnimatorSet set = new AnimatorSet();
				set
				.play(ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left))
				.with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top))
				.with(ObjectAnimator
						.ofFloat(expandedImageView, View.SCALE_X, startScaleFinal))
						.with(ObjectAnimator
								.ofFloat(expandedImageView, View.SCALE_Y, startScaleFinal));
				set.setDuration(mShortAnimationDuration);
				set.setInterpolator(new DecelerateInterpolator());
				set.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						thumbView.setAlpha(1f);
						expandedImageView.setVisibility(View.GONE);
						mCurrentAnimator = null;
					}

					@Override
					public void onAnimationCancel(Animator animation) {
						thumbView.setAlpha(1f);
						expandedImageView.setVisibility(View.GONE);
						mCurrentAnimator = null;
					}
				});
				set.start();
				mCurrentAnimator = set;
			}
		});
	}
}
