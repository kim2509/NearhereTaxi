package com.tessoft.common;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tessoft.domain.MainMenuItem;
import com.tessoft.nearhere.R;

public class MainMenuArrayAdapter extends ArrayAdapter<MainMenuItem>{

	LayoutInflater inflater = null;
	
	public MainMenuArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {

		View row = convertView;

		try
		{
			MainMenuItem item = getItem(position);

			if (row == null) {
				row = inflater.inflate(R.layout.list_main_menu_item, parent, false);
			}

			TextView txtTitle = (TextView) row.findViewById(R.id.txtTitle);
			txtTitle.setText( item.getMenuName() );
			
			TextView txtCount = (TextView) row.findViewById(R.id.txtCount);
			if ( item.getNotiCount() > 0 )
			{
				txtCount.setVisibility(ViewGroup.VISIBLE);
				txtCount.setText( String.valueOf(item.getNotiCount()) );
			}
			else
			{
				txtCount.setVisibility(ViewGroup.GONE);
				txtCount.setText( String.valueOf(item.getNotiCount()) );
			}
			
			row.setTag( item );
		}
		catch( Exception ex )
		{
			Log.e("error", ex.getMessage());
		}

		return row;
	}
}
