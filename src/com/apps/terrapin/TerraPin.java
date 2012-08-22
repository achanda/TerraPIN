package com.apps.terrapin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.util.Log;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class TerraPin extends TabActivity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab);
		Log.e("TAG", "TerraPin onCraete called!");
		
		//Utils.getAuthorization(getApplicationContext());

		/* TabHost will have Tabs */
		TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);

		//TabSpec firstTabSpec = tabHost.newTabSpec("tid1");
		TabSpec secondTabSpec = tabHost.newTabSpec("tid1");
		TabSpec thirdTabSpec = tabHost.newTabSpec("tid1");

		//firstTabSpec.setIndicator("Snap").setContent(
		//		new Intent(this, TakePictureActivity.class));
		secondTabSpec.setIndicator("Search").setContent(
				new Intent(this, SearchActivity.class));
		thirdTabSpec.setIndicator("Settings").setContent(
				new Intent(this, SettingsActivity.class));

		/* Add tabSpec to the TabHost to display. */
		//tabHost.addTab(firstTabSpec);
		tabHost.addTab(secondTabSpec);
		tabHost.addTab(thirdTabSpec);

		//username not set, ask for one
		if (!Utils.isUsernameSet(getApplicationContext())) { 
			tabHost.setCurrentTab(2);
		} else {
			tabHost.setCurrentTab(0);
			String regid = registerC2DM();
			publishRegistration(regid,
					Utils.getUserName(getApplicationContext()));
		}
	}

	private String registerC2DM() {
		final String TAG = TerraPin.class.getSimpleName();
		C2DMSampleApplication app; // This is where our shared pref is
		IntentFilter filter; // Used to catch a new regId intent sent from
								// C2DMReceiver
		IdReceiver receiver; // The receiver to receiver intents from
								// C2DMReceiver

		app = (C2DMSampleApplication) getApplication();
		filter = new IntentFilter(C2DMSampleApplication.NEW_REGID_INTENT);
		receiver = new IdReceiver();

		// Create registration intent
		Intent regIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
		// Identify your app
		regIntent.putExtra("app",
				PendingIntent.getBroadcast(TerraPin.this, 0, new Intent(), 0));
		// Identify role account server will use to send
		regIntent.putExtra("sender", "terrapin358@gmail.com");
		// Start the registration process
		startService(regIntent);

		String regId = app.getRegId();
		if (regId != null) {
			Log.d(TAG, String.format("Reg Id: %s", regId));
			// Copy to clipboard
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			clipboard.setText(regId);
		}
		return regId;
	}

	private void publishRegistration(final String id, final String user) {
		Log.d("@@@@@ Server response @@@@@", "test");
		new Thread(new Runnable() {
			public void run() {
				String burl = Utils.serverName + "register.php?";
				String url = burl + "id=" + id + "&username=" + user;

				Log.e("TAG", "trying to publish registration: " + url);

				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet();
				try {
					request.setURI(new URI(url));
					HttpResponse response = client.execute(request);
					Log.d("@@@@@ Server response @@@@@", response.toString());
				} catch (URISyntaxException e) {
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	class IdReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("IdReceiver", "onReceived");
		}
	}
}
