package com.apps.terrapin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.PendingIntent;
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
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends Activity {
	private EditText textBox;
	private CheckBox checkbox;
	private EditText firsttextBox;
	private EditText lasttextBox;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		textBox = (EditText) findViewById(R.id.uname);
		// textBox.setText(Utils.getUserName(getApplicationContext()));

		firsttextBox = (EditText) findViewById(R.id.first);
		lasttextBox = (EditText) findViewById(R.id.last);

		checkbox = (CheckBox) findViewById(R.id.fuzz);
	}

	public void saveClicked(View view) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		Editor editor = pref.edit();
		editor.putBoolean("fuzz", true);
		if ((textBox.getText().toString().length() != 0)
				&& (firsttextBox.getText().toString().length() != 0)
				&& (lasttextBox.getText().toString().length() != 0)) {
			editor.putString("username", textBox.getText().toString());
			editor.commit();

			String regid = registerC2DM();
			publishRegistration(regid,
					Utils.getUserName(getApplicationContext()));

			if (!publishdata())
				Toast.makeText(this,
						"The username exists, please select a different one",
						Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT)
						.show();
		}
	}

	public boolean publishdata() {
		StringBuilder sb = new StringBuilder();
		String baseUrl = Utils.serverName + "save_users.php?";
		String url = baseUrl + "first=" + firsttextBox.getText().toString()
				+ "&last=" + lasttextBox.getText().toString() + "&user="
				+ textBox.getText().toString();

		Log.e("settings", url);
		InputStream is = null;
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet();
		try {
			request.setURI(new URI(url));
			HttpResponse response = client.execute(request);
			is = response.getEntity().getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "iso-8859-1"), 8);
			sb.append(reader.readLine() + "\n");

			String line = "0";
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			if (sb.toString().contains("exists"))
				return false;
			else
				return true;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return false;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
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
				PendingIntent.getBroadcast(this, 0, new Intent(), 0));
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
