package com.ameivasoft.latinary;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.speech.tts.TextToSpeech;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import com.ameivasoft.shortdroid.AlertManager;
import com.ameivasoft.shortdroid.ApplicationDefaults;
import com.ameivasoft.shortdroid.FileSystem;
import com.ameivasoft.shortdroid.NetManager;
import com.ameivasoft.shortdroid.RegExManager;
import com.ameivasoft.shortdroid.UniArray;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint({ "HandlerLeak", "DefaultLocale" })
public class HomeScreen extends AppCompatActivity {

	private boolean 		FIRST_RUN 		= true;
	private UniArray 		DICTIONARY		= null;
	private ProgressDialog 	LOADING_DIALOG 	= null;
	private String[] 		SUGGESTIONS		= null;
	private int 	 		COUNTER			= 0;

	
	TextView      DESCRIPTION_FIELD 		= null;
	TextView      TITLE_FIELD 				= null;
	SearchView    SEARCH 					= null;
	DrawerLayout  DRAWER 					= null;
	ListView      LIST 						= null;
	TextToSpeech  SPEECH					= null;
	ImageButton   SPEECH_BTN				= null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home_screen);

		// Setting actionbar
		Toolbar toolbar = findViewById(R.id.toolbar);

		setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
		
		// start showing the loading dialog
		LOADING_DIALOG = AlertManager.showProgressDialog(this, getResources().getString(R.string.app_loading), getResources().getString(R.string.app_configuring), false, 0);
		
		loadDefaults();
		
		if (FIRST_RUN)
		{
			setupApplication();
		}
		else
		{
			// dismiss dialog
			LOADING_DIALOG.dismiss();
			
			appSetup();
			
		}

	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.home_screen, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Intent aboutScreen = new Intent(this, AboutScreen.class);

		if (aboutScreen != null)
		{
			startActivity(aboutScreen);
		}

		return true;
	}

	/**  Method for init app elements */
	public void appSetup()
	{
		// read the dictionary stored on device
		DICTIONARY = (UniArray) FileSystem.readObjectFile(this, getResources().getString(R.string.file_name), false);

		// get reference to the UI elements
		SEARCH = findViewById(R.id.searchView);
		TITLE_FIELD = findViewById(R.id.titleView);
		DESCRIPTION_FIELD = findViewById(R.id.descriptionView);
		DRAWER = findViewById(R.id.drawer);
		LIST = findViewById(R.id.drawerList);

		View header = getLayoutInflater().inflate(R.layout.header, null);
		LIST.addHeaderView(header);

		// setup the speech functionality
		setupSpeech();

		// setup the searching functionality
		setupSearch();
	}


	public void setupSpeech()
	{
		SPEECH = new TextToSpeech(this.getApplicationContext(), new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {

				if (status != TextToSpeech.ERROR)
				{
					SPEECH.setLanguage(Locale.US);
					SPEECH.setPitch(0.9f);
					SPEECH.setVoice(SPEECH.getVoice());
					SPEECH.setSpeechRate(0.85f);
				}
			}
		});

		SPEECH_BTN = findViewById(R.id.speechBtn);

		if (SPEECH_BTN != null)
		{
			SPEECH_BTN.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					doTextToSpeech();
				}
			});
		}
	}

	private void doTextToSpeech()
	{
		if (TITLE_FIELD != null)
		{
			if (SPEECH_BTN.getVisibility() == View.VISIBLE)
			{
				String text = TITLE_FIELD.getText().toString();

				doTextToSpeech(text);
			}
		}
	}

	/** Method for uttering text */
	public void doTextToSpeech(String text)
	{

		if (SPEECH != null)
		{
			SPEECH.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts");
		}
	}


	/** Method for setting up core functionality (searching) */
	public void setupSearch()
	{
		// setup search widget functionality
		if (SEARCH != null)
		{
			SEARCH.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
				
				@Override
				public boolean onQueryTextSubmit(String query) {

					if (DRAWER != null && DRAWER.isDrawerOpen(LIST))
					{
						DRAWER.closeDrawer(LIST);
					}
					
					// perform the search
					doSearch(query.toLowerCase());
					
					return false;
				}
				
				@Override
				public boolean onQueryTextChange(String newText) {

					if (newText.length() >= 2)
					{
						doSuggestionDrawer(newText.toLowerCase());
						
						if (DRAWER != null && DRAWER.isDrawerOpen(LIST) == false)
						{
							DRAWER.openDrawer(LIST);
						}
					}
					
					return false;
				}
			});
		}
	}
	
	/** Method for creating a suggestions list and displaying it in right drawer*/
	public void doSuggestionDrawer(String text)
	{
		String[] wordList = DICTIONARY.getAllStringKeys();
		UniArray tempList = new UniArray();
		
		if (wordList != null)
		{
			String pattern = text;


			// search each word for match
			for (int i = 0; i < wordList.length; i++)
			{
				int length = pattern.length();
				String wordPrefix;

				// return correctly abbr word or whole word for words shorter than query
				if (wordList[i].length() > length)
				{
					wordPrefix = wordList[i].substring(0, length);
				}
				else
				{
					wordPrefix = wordList[i];
				}
				
				if (RegExManager.checkPattern(wordPrefix, pattern))
				{
					tempList.putString("" + i, wordList[i]);
				}
			}
			
			// update the suggestions array
			SUGGESTIONS = tempList.getAllStrings();
			
			
			// setup listView functionality
			if (LIST != null && DRAWER != null)
			{
				LIST.clearChoices();
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.drawer_list, R.id.listItem, SUGGESTIONS);
				LIST.setAdapter(adapter);
				
				LIST.setOnItemClickListener(new DrawerItemClickListener(DICTIONARY, LIST, DRAWER, TITLE_FIELD, DESCRIPTION_FIELD, SPEECH_BTN));
			}
		}
	}
	
	/** Method for performing search queries on dictionary */
	public void doSearch(String query)
	{
		boolean doesExist = DICTIONARY.hasString(query);
		
		// verify the textViews are valid
		if (TITLE_FIELD != null && DESCRIPTION_FIELD != null)
		{
			if (doesExist)
			{
				// set the word and definition fields to match query
				TITLE_FIELD.setText(query);
				DESCRIPTION_FIELD.setText(DICTIONARY.getString(query));
				SPEECH_BTN.setVisibility(View.VISIBLE);
			}
			else
			{
				// set the word and definition fields for failed query
				TITLE_FIELD.setText(getResources().getString(R.string.query_failed));
				DESCRIPTION_FIELD.setText("");
				SPEECH_BTN.setVisibility(View.INVISIBLE);
			}
		}
	}
	
	/**  method for loading up default application data */
	public void loadDefaults() 
	{
		ApplicationDefaults defaults = new ApplicationDefaults(this);
		
		FIRST_RUN = defaults.getData().getBoolean(getResources().getString(R.string.runKey), true);
	}

	/** method for initial application setup */
	public void setupApplication()
	{
		final Context thisContext = this;
		
		// obtain the network status
		boolean isConnected = NetManager.getConnectionStatus(this);
		
		if (isConnected)
		{
			// create the callback
			Handler callback = new Handler()
			{
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					
					if (msg != null)
					{
						if (msg.arg1 == Activity.RESULT_OK)
						{
							String responseText = (String) msg.obj;
							
							if (responseText != null)
							{
								// decode the response 
								byte[] bytes = Base64.decode(responseText, Base64.DEFAULT);
								
								// recode decoded bytes as a string
								String decodedString = new String(bytes);

								String newline = System.lineSeparator();

								// split the decoded string into an array by newline char
								String[] newStrArr = decodedString.split(newline);
								
								// create a new object dictionary
								UniArray dictionary = new UniArray();
								
								// loop over the array and split word and definition / explanation
								for (int i = 0; i < newStrArr.length; i++)
								{
									// split the word from definition
									String[] wordDef = newStrArr[i].split(":");
									
									// assign word / definition as key / value in dicitionary
									dictionary.putString(wordDef[0].trim(), wordDef[1].trim());
								}
								
								// store the dictionary on device
								boolean result = FileSystem.writeObjectFile(thisContext, dictionary, getResources().getString(R.string.file_name), false);
								
								
								// verify if the dictionary saved 
								if (result == true)
								{
									// set first run var to false to prevent redundant re-downloading
									ApplicationDefaults defaults = new ApplicationDefaults(thisContext);
									
									if (defaults != null)
									{
										defaults.set(getResources().getString(R.string.runKey), false);

										// now setup the core app files
										appSetup();
									}
								}
								else
								{
									// show error message
									AlertManager.showToast(thisContext, getResources().getString(R.string.setup_failed), Toast.LENGTH_LONG);
									
									// close app
									finish();
								}
							}
							else
							{
								// show error message
								AlertManager.showToast(thisContext, getResources().getString(R.string.data_invalid), Toast.LENGTH_LONG);
								
								// close app
								finish();
							}
						}
						else
						{
							// show error message
							AlertManager.showToast(thisContext, getResources().getString(R.string.request_failed), Toast.LENGTH_LONG);
							
							// close app
							finish();
						}
					}
					else
					{
						// show error message
						AlertManager.showToast(thisContext, getResources().getString(R.string.request_null), Toast.LENGTH_LONG);
						
						// close app
						finish();
					}
					
					// dismiss the dialog
					LOADING_DIALOG.dismiss();
				}
			};
			
			//*****************************
			
			
			// setup the intent service to handle the webrequest
			Intent requestor = new Intent(this, Webservice.class);
			
			if (requestor != null)
			{
				final String urlLink = getResources().getString(R.string.request_url);
				
				// create the messenger with handler
				Messenger messenger = new Messenger(callback);
				
				requestor.putExtra(getResources().getString(R.string.urlKey), urlLink);
				requestor.putExtra(getResources().getString(R.string.messengerKey), messenger);
				requestor.putExtra(getResources().getString(R.string.requestKey), 500);

				// initiate the request
				startService(requestor);
			}
		}
		else
		{
			// show error message
			AlertManager.showToast(this, getResources().getString(R.string.network_err), Toast.LENGTH_LONG);
			
			// dismiss the dialog on network error
			LOADING_DIALOG.dismiss();
		}

	}
	
	@Override
	public void onBackPressed() {

		if (COUNTER >= 1)
		{
			finish();
		}

		AlertManager.showToast(getApplicationContext(), getResources().getString(R.string.back_out), Toast.LENGTH_SHORT);

		COUNTER++;

		final Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {

				COUNTER--;

				timer.cancel();
			}
		};

		timer.schedule(task, 2000);
	}
}