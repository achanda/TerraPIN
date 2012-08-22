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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class Utils {
	public static final String uploadDirectory = "uploads";
	public static final String serverName = "http://23.21.129.92/terrapin/"; 

	// returns the username saved in the application context
	// if nothing is found returns empty string
	public static String getUserName(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context.getApplicationContext());
		return pref.getString("username", "");
	}

	static public boolean hasStorage() {
		return android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
	}

	public static boolean isUsernameSet(Context context) {
		try {
			String str = getUserName(context);
			if (str.length() == 0)
				return false;
			else
				return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean getAuthorization(Context context) {
		boolean result;
		String baseUrl = Utils.serverName + "authorized.php?";
		String url = baseUrl + "user=" + Utils.getUserName(context);
		InputStream is = null;
		StringBuilder sb = new StringBuilder();
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
			if (sb.toString().contains("yes"))
				result = true;
			else
				result = false;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			result = false;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			result = false;
		} catch (IOException e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}
	
	public static boolean isAuthorized(Context context) {
		return getAuthorization(context);
	}
}
