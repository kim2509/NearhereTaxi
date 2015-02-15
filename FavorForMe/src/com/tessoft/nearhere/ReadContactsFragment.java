package com.tessoft.nearhere;

import java.util.HashMap;

import com.google.android.gms.maps.model.LatLng;
import com.tessoft.common.AdapterDelegate;
import com.tessoft.common.ReadContactArrayAdapter;
import com.tessoft.common.Util;
import com.tessoft.domain.Contact;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.database.Cursor;
import android.opengl.Visibility;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class ReadContactsFragment extends DialogFragment implements OnClickListener, AdapterDelegate
{
	View rootView = null;
	AdapterDelegate delegate;
	ListView listContacts;
	ReadContactArrayAdapter adapter = null;
	
	public ReadContactsFragment( AdapterDelegate delegate ) {
		// TODO Auto-generated constructor stub
		this.delegate = delegate;
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) 
    {
    	return super.onCreateView(inflater, container, savedInstanceState);
    }

    AlertDialog readContactsDialog = null;
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) 
    {
    	
    	try
    	{
    		LayoutInflater li = LayoutInflater.from( getActivity() );
    		rootView = li.inflate(R.layout.fragment_read_contacts, null);
    		
    		initializeComponent();
    		
            // Set a theme on the dialog builder constructor!
    		AlertDialog.Builder builder = 
                new AlertDialog.Builder( getActivity() );
            builder.setView( rootView );
            
            builder
    		.setCancelable(false)
    		.setTitle("연락처");
            
            readContactsDialog = builder.create();
            
    	}
    	catch( Exception ex )
    	{
    	}
        
        return readContactsDialog;
    }
    
    public void initializeComponent()
    {
    	adapter = new ReadContactArrayAdapter(getActivity(), 0, this);
    	listContacts = (ListView) rootView.findViewById(R.id.listConstacts);
    	listContacts.setAdapter(adapter);
    	
    	listContacts.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Contact item = (Contact) arg1.getTag();
				delegate.doAction("selectContact", item);
				readContactsDialog.dismiss();
			}
		});
    	
    	EditText edtName = (EditText) rootView.findViewById(R.id.edtName);
    	edtName.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
				adapter.clear();
				String where = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like '%" + s + "%'";
				Cursor phones = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, where ,null, null);
				while (phones.moveToNext())
				{
				  String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
				  String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

				  Contact contact = new Contact();
				  contact.setName( name );
				  contact.setNumber(phoneNumber);
				  adapter.add(contact);
				}
				phones.close();
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
    	
    	String where = null;
    	adapter.clear();
		Cursor phones = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, where ,null, null);
		while (phones.moveToNext())
		{
		  String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
		  String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

		  Contact contact = new Contact();
		  contact.setName( name );
		  contact.setNumber(phoneNumber);
		  adapter.add(contact);
		}
		phones.close();
    }
    
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		try
		{
			if ( v.getId() == R.id.btnSelectDeparture || v.getId() == R.id.btnSelectDestination )
			{
				
			}
		}
		catch( Exception ex )
		{
			
		}
	}

	@Override
	public void doAction(String actionName, Object param) {
		// TODO Auto-generated method stub
		
	}

}