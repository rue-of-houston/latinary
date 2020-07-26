package com.ameivasoft.latinary;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.ameivasoft.shortdroid.WebServicer;

public class Webservice extends IntentService {

	public Webservice() {
		super("Webservice");
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		
		Bundle extras = intent.getExtras();
		
		if (extras != null)
		{
			String urlLink = extras.getString("url");
			
			if (urlLink != null)
			{
				int requestCode	 = extras.getInt("requestCode");
				Messenger msngr  = (Messenger) extras.get("messenger");
				Message msg	     = Message.obtain();
				
				// default fail value
				msg.arg1 		 = msg.arg2 = 0;
				msg.obj 		 = null;

				// get the response data from the get request
				String result = WebServicer.get(urlLink, WebServicer.CONTENT_TEXT, null);
				//String response = result[result.length -1];
				
				if (result != null && result != "")
				{
					msg.arg1 = -1;
					msg.arg2 = requestCode;
					msg.obj = result;
				}
				else
				{
					Log.i("Webservice", "null return");
				}
				
					try {

						msngr.send(msg);
						
					} catch (RemoteException e) {
						e.printStackTrace();
					}
			}
		}
	}
	

}
