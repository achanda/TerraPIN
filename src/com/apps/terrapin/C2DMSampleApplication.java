package com.apps.terrapin;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class C2DMSampleApplication extends Application {
	
	private SharedPreferences prefs;
	public static final String NEW_REGID_INTENT = "com.apps.terrapin.NEW_REGID";
	public static final String REGID_VAL_INTENT = "com.apps.terrapin.REGID_VAL";
	public static final String INTENT_SEND_PERMISSION = "com.apps.terrapin.SEND_NOTIFICATIONS";
	public static final String INTENT_RECEIVE_PERMISSION = "com.apps.terrapin.RECEIVE_NOTIFICATIONS";

	@Override
	public void onCreate() {
		super.onCreate();
		// get the shared preferences for the app
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
	}
	
	/**
	 * @return regId or null
	 * 
	 * This function returns the regId string if it's present, null if not
	 */
	public String getRegId() {
		return prefs.getString("regId", null);
	}

	/**
	 * @param regId - null or a String representing the registration id
	 * 
	 * This function can set or clear the regId preference. If null is received 
	 * then the preference is cleared, or else is it set
	 */
	public void setRegId(String regId) {
		SharedPreferences.Editor editor = prefs.edit();
		if (regId == null)
			editor.remove("regId");
		else 
			editor.putString("regId", regId);
		editor.commit();
	}
}
