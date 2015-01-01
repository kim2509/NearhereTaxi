package com.tessoft.favorforme;

import android.os.Bundle;

public class PostDetailActivity extends BaseActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		try
		{
			super.onCreate(savedInstanceState);

			setContentView(R.layout.activity_post_detail);

		}
		catch( Exception ex )
		{
			showToastMessage(ex.getMessage());
		}
	}

}
