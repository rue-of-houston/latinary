package com.ameivasoft.latinary;

import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.ameivasoft.shortdroid.UniArray;

public class DrawerItemClickListener implements ListView.OnItemClickListener {

	UniArray        DICTIONARY;
	ListView 		LIST;
	DrawerLayout 	DRAWER;
	TextView		TITLE;
	TextView		DESCRIPTION;
	ImageButton		SPEECH_BTN;
	
	public DrawerItemClickListener(UniArray dictionary, ListView list, DrawerLayout drawer, TextView title, TextView description, ImageButton imgBtn)
	{
		LIST		= list;
		TITLE 		= title;
		DRAWER  	= drawer;
		DICTIONARY 	= dictionary;
		DESCRIPTION = description;
		SPEECH_BTN  = imgBtn;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		// update list and close the drawer
		selectWord(view, position);
		DRAWER.closeDrawer(LIST);
		SPEECH_BTN.setVisibility(View.VISIBLE);
	}
	
	private void selectWord(View view, int position)
	{
		String text = null;
		
		// select the textView from the view
		TextView selectedText = view.findViewById(R.id.listItem);
		
		if (selectedText != null)
		{
			// get the text from the textView
			text = selectedText.getText().toString();
			
			// update the title and description fields
			if (DICTIONARY != null)
			{
				TITLE.setText(text);
				DESCRIPTION.setText(DICTIONARY.getString(text));
			}
		}
		
	}

}
