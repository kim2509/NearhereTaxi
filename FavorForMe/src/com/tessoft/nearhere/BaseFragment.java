package com.tessoft.nearhere;

import android.app.Fragment;
import android.util.Log;
import android.widget.Toast;

public class BaseFragment extends Fragment{

	public void showToastMessage( String message )
	{
		Toast.makeText( getActivity() , message, Toast.LENGTH_LONG).show();
	}
	
	public void catchException ( Object target, Exception ex )
	{
		if ( ex == null )
			writeLog( "[" + target.getClass().getName() + "] NullPointerException!!!" );
		else
			writeLog( "[" + target.getClass().getName() + "]" + ex.getMessage() );
	}
	
	public void writeLog( String log )
	{
		Log.i("NearHereHelp", log );
	}
}
