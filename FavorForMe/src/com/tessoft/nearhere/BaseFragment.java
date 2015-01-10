package com.tessoft.nearhere;

import android.app.Fragment;
import android.util.Log;

public class BaseFragment extends Fragment{

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
