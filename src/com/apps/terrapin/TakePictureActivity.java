package com.apps.terrapin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

public class TakePictureActivity extends Activity {
	float latitude, longitude;
	String currentPictureName;
	CheckBox locationBox;
	CheckBox timeBox;
	ImageView img;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		setContentView(R.layout.takepicture);

		img = (ImageView) findViewById(R.id.preview);
		locationBox = (CheckBox) findViewById(R.id.location);
		timeBox = (CheckBox) findViewById(R.id.timestamp);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		String action = intent.getAction();

		if (Intent.ACTION_SEND.equals(action)) {
			if (extras.containsKey(Intent.EXTRA_STREAM)) {
				try {
					// Get resource path from intent callee
					Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
					// Query gallery for camera picture via
					// Android ContentResolver interface
					ContentResolver cr = getContentResolver();
					InputStream is = cr.openInputStream(uri);

					img.setImageBitmap(BitmapFactory.decodeStream(is));

					String path = getRealPathFromURI(uri);
					Log.e("TERRAPIN", path);
					String filename = path.split("/")[5];
					ExifInterface exif = new ExifInterface(path);

					float[] latlong = new float[2];
					if (exif.getLatLong(latlong)) {
						latitude = latlong[0];
						longitude = latlong[1];
						currentPictureName = filename;
					} else
						Toast.makeText(getApplicationContext(),
								"Please turn on geotagging", Toast.LENGTH_SHORT)
								.show();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(contentUri, proj, null, null, null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	public void uploadData() {
		Log.e("@@@@@", "uploadData starts");
		float lat = latitude;
		float lng = longitude;
		Log.e("$$$$$Lat", Double.toString(lat));
		Log.e("$$$$$Long", Double.toString(lng));

		ThreadPolicy tp = ThreadPolicy.LAX;
		StrictMode.setThreadPolicy(tp);

		String user = Utils.getUserName(getApplicationContext());

		String data = "&username=" + user;
		data = data + "&filename=" + currentPictureName;

		if (locationBox.isChecked() && timeBox.isChecked()) {
			SimpleDateFormat t = new SimpleDateFormat("ddMMyyyyHHmmss");
			String date = t.format(new Date());
			data = data + "&timestamp=" + date + "&latitude="
					+ Double.toString(lat) + "&longtitude="
					+ Double.toString(lng);
		}

		if (locationBox.isChecked() && !timeBox.isChecked()) {
			data = data + "&timestamp=0" + "&latitude=" + Double.toString(lat)
					+ "&longtitude=" + Double.toString(lng);
		}

		if (!locationBox.isChecked() && timeBox.isChecked()) {
			SimpleDateFormat t = new SimpleDateFormat("ddMMyyyyHHmmss");
			String date = t.format(new Date());

			data = data + "&timestamp=" + date + "&latitude=0"
					+ "&longtitude=0";
		}

		if (!locationBox.isChecked() && !timeBox.isChecked()) {
			data = data + "&timestamp=0" + "&latitude=0" + "&longtitude=0";
		}

		String baseUrl = Utils.serverName + "savedata.php?";

		Log.e("@@@@@", baseUrl + data);
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet();
		try {
			request.setURI(new URI(baseUrl + data));
			HttpResponse response = client.execute(request);
			Log.d("@@@@@ Server response @@@@@", response.toString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.e("@@@@@", "uploadData ends");
	}

	public void uploadClicked(View view) {
		Log.e("@@@@@@@@@", "uploadclicked called");
		if (Utils.isAuthorized(getApplicationContext())) {
			try {
				uploadData();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else
			Toast.makeText(this, "You are not authorized to use the service",
					Toast.LENGTH_SHORT).show();
	}
}