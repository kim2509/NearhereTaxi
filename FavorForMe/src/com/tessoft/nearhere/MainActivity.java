package com.tessoft.nearhere;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends BaseActivity {

	DrawerLayout mDrawerLayout = null;
	ListView mDrawerList = null;
	String mTitle = "";
	String[] mMenuList = null;
	private ActionBarDrawerToggle mDrawerToggle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		try
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);

			mMenuList = getResources().getStringArray(R.array.menu_list);
			mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
			mDrawerList = (ListView) findViewById(R.id.left_drawer);

			// Set the adapter for the list view
			mDrawerList.setAdapter(new ArrayAdapter<String>(this,
					R.layout.drawer_list_item, mMenuList));

			// Set the list's click listener
			mDrawerList.setOnItemClickListener( new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView parent, View view, int position, long id) {
					selectItem(position);
				}
			});

			mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
					R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

				/** Called when a drawer has settled in a completely closed state. */
				public void onDrawerClosed(View view) {
					super.onDrawerClosed(view);
					invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
				}

				/** Called when a drawer has settled in a completely open state. */
				public void onDrawerOpened(View drawerView) {
					super.onDrawerOpened(drawerView);
					invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
				}
			};

			// Set the drawer toggle as the DrawerListener
			mDrawerLayout.setDrawerListener(mDrawerToggle);

			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setHomeButtonEnabled(true);
			
			Fragment fragment = new TaxiFragment();

			// Insert the fragment by replacing any existing fragment
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
			.add(R.id.content_frame, fragment)
			.commit();
		}
		catch( Exception ex )
		{
			catchException(this, ex);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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

		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
	
	/** Swaps fragments in the main content view */
	private void selectItem(int position) {
		// Create a new fragment and specify the planet to show based on position
		Fragment fragment = null;
		
		boolean bFragment = true;
		
		if ( "홈".equals( mMenuList[position]) )
			fragment = new TaxiFragment();
		else if ( "내 정보".equals( mMenuList[position]) )
			fragment = new MyInfoFragment();
		else if ( "알림메시지".equals( mMenuList[position]) )
			fragment = new PushMessageListFragment();
		else if ( "쪽지함".equals( mMenuList[position]) )
			fragment = new PushMessageListFragment();
		else if ( "공지사항".equals( mMenuList[position]) )
			fragment = new NoticeListFragment();
		else
			bFragment = false;

		if ( bFragment )
		{
			// Insert the fragment by replacing any existing fragment
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
			.replace(R.id.content_frame, fragment)
			.commit();
			
			setTitle( mMenuList[position] );
		}
		else
		{
			Intent intent = new Intent( this, SettingsActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		}

		// Highlight the selected item, update the title, and close the drawer
		mDrawerList.setItemChecked(position, true);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title.toString();
		getActionBar().setTitle(mTitle);
	}


	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

}
