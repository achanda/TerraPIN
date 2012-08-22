package com.apps.terrapin;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

/*import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

public class UploadActivity extends Activity {

	private Cursor cursor;
	private int columnIndex;
	private String currentSelection;

	public void uploadPic() {
		Log.e("@@@@@", "uploadpic starts");
		ThreadPolicy tp = ThreadPolicy.LAX;
		StrictMode.setThreadPolicy(tp);

		HttpURLConnection connection = null;
		DataOutputStream outputStream = null;

		try {
			String pathToOurFile = currentSelection;
			String urlServer = Utils.serverName + "savefile.php";
			String lineEnd = "\r\n";
			String twoHyphens = "--";
			String boundary = "*****";

			int bytesRead, bytesAvailable, bufferSize;
			byte[] buffer;
			int maxBufferSize = 1 * 1024 * 1024;

			FileInputStream fileInputStream = new FileInputStream(new File(
					pathToOurFile));
			URL url = new URL(urlServer);

			connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);

			outputStream = new DataOutputStream(connection.getOutputStream());
			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			outputStream
					.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
							+ pathToOurFile + "\"" + lineEnd);
			outputStream.writeBytes(lineEnd);

			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			bytesRead = fileInputStream.read(buffer, 0, bufferSize);

			while (bytesRead > 0) {
				outputStream.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}

			outputStream.writeBytes(lineEnd);
			outputStream.writeBytes(twoHyphens + boundary + twoHyphens
					+ lineEnd);

			String serverResponseMessage = connection.getResponseMessage();
			Log.e("TAG", serverResponseMessage);
			fileInputStream.close();
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.e("@@@@@", "uploadpic ends");
	}
	
	private void sendPicRequest(String toUser) {
		Log.e("TAG", "Sending a notification to user: " + toUser);
		
		String burl = Utils.serverName + "send.php?";
		String url = burl + "touser=" + toUser + "&fromuser=" + Utils.getUserName(this.getBaseContext())
				+ "flag=" + "0"; // flag = 1 for request to upload, 0 for request to download 
		
		Log.e("TAG", "trying to send pic request: " + url);
		
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upload);
		Log.e("TAG", "Uploadpic started");

		// Set up an array of the Thumbnail Image ID column we want
		String[] projection = { MediaStore.Images.Thumbnails._ID };
		// Create the cursor pointing to the SDCard
		cursor = managedQuery(
				MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, projection, // Which
																				// columns
																				// to
																				// return
				null, // Return all rows
				null, MediaStore.Images.Thumbnails.IMAGE_ID);
		// Get the column index of the Thumbnails Image ID
		columnIndex = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID);

		GridView sdcardImages = (GridView) findViewById(R.id.sdcard);
		sdcardImages.setAdapter(new ImageAdapter(this));

		Button button = (Button) findViewById(R.id.uploadButton);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.e("TAG", "Button cicked with: " + currentSelection);
				uploadPic();
				
				Bundle extras = getIntent().getExtras();
				String to = extras.getString("to");
				String from = extras.getString("from");
				sendPicRequest(from);
			}
		});

		// Set up a click listener
		sdcardImages.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView parent, View v, int position, long id) {
				// Get the data location of the image
				String[] projection = { MediaStore.Images.Media.DATA };
				cursor = managedQuery(
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
						projection, // Which columns to return
						null, // Return all rows
						null, null);
				columnIndex = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToPosition(position);
				// Get image filename
				String imagePath = cursor.getString(columnIndex);
				Log.e("TAG", "Path: " + imagePath);
				currentSelection = imagePath;
			}
		});
	}

	private class ImageAdapter extends BaseAdapter {

		private Context context;

		public ImageAdapter(Context localContext) {
			context = localContext;
		}

		public int getCount() {
			return cursor.getCount();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView picturesView;
			if (convertView == null) {
				picturesView = new ImageView(context);
				// Move cursor to current position
				cursor.moveToPosition(position);
				// Get the current value for the requested column
				int imageID = cursor.getInt(columnIndex);
				
				// Set the content of the image based on the provided URI
				picturesView.setImageURI(Uri.withAppendedPath(
						MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, ""
								+ imageID));
				picturesView.setScaleType(ImageView.ScaleType.FIT_CENTER);
				picturesView.setPadding(8, 8, 8, 8);
				picturesView
						.setLayoutParams(new GridView.LayoutParams(100, 100));
			} else {
				picturesView = (ImageView) convertView;
			}
			return picturesView;
		}
	}
}*/
public class UploadActivity extends Activity {
    final int PICK_IMAGE = 0;
    ImageView uploadImage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.upload);
		//uploadImage = (ImageView)findViewById(R.id.uploadimage);
		Log.e("TAG", "Uploadpic started totally");
		
		Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(intent, this.PICK_IMAGE);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	  super.onActivityResult(requestCode, resultCode, data);
	  if (requestCode == PICK_IMAGE)
	    if (resultCode == Activity.RESULT_OK) {
	      Uri selectedImage = data.getData();
	      File f = new File(selectedImage.toString());
	      uploadPic(f.getAbsolutePath());
	      sendPicRequest(Utils.getUserName(getApplicationContext()));
	    } 
	}
	
	private void sendPicRequest(String toUser) {
		Log.e("TAG", "Sending notification to user: " + toUser);
		
		String burl = Utils.serverName + "send.php?";
		String url = burl + "touser=" + toUser + "&fromuser=" + Utils.getUserName(this.getBaseContext())
				+ "flag=" + "0"; // flag = 1 for request to upload, 0 for request to download 
		
		Log.e("TAG", "Trying to send pic request: " + url);
		
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
	
	public void uploadPic(String currentSelection) {
		Log.e("@@@@@", "uploadpic starts");
		ThreadPolicy tp = ThreadPolicy.LAX;
		StrictMode.setThreadPolicy(tp);

		HttpURLConnection connection = null;
		DataOutputStream outputStream = null;

		try {
			String pathToOurFile = currentSelection;
			String urlServer = Utils.serverName + "savefile.php";
			String lineEnd = "\r\n";
			String twoHyphens = "--";
			String boundary = "*****";

			int bytesRead, bytesAvailable, bufferSize;
			byte[] buffer;
			int maxBufferSize = 1 * 1024 * 1024;

			FileInputStream fileInputStream = new FileInputStream(new File(
					pathToOurFile));
			URL url = new URL(urlServer);

			connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);

			outputStream = new DataOutputStream(connection.getOutputStream());
			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			outputStream
					.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
							+ pathToOurFile + "\"" + lineEnd);
			outputStream.writeBytes(lineEnd);

			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			bytesRead = fileInputStream.read(buffer, 0, bufferSize);

			while (bytesRead > 0) {
				outputStream.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}

			outputStream.writeBytes(lineEnd);
			outputStream.writeBytes(twoHyphens + boundary + twoHyphens
					+ lineEnd);

			String serverResponseMessage = connection.getResponseMessage();
			Log.e("TAG", serverResponseMessage);
			fileInputStream.close();
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.e("@@@@@", "uploadpic ends");
	}
	
	/*private Bitmap decodeFile(File f){
		Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		
	    try {
	        //Decode image size
	        BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;
	        BitmapFactory.decodeStream(new FileInputStream(f),null,o);

	        //The new size we want to scale to
	        final int REQUIRED_SIZE=20000;

	        //Find the correct scale value. It should be the power of 2.
	        int scale=1;
	        while(o.outWidth/scale/2>=REQUIRED_SIZE && o.outHeight/scale/2>=REQUIRED_SIZE)
	            scale*=2;

	        //Decode with inSampleSize
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        scale=1;
	        o2.inSampleSize=scale;
	        return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
	    } catch (FileNotFoundException e) {}
	    return null;
	}*/
}