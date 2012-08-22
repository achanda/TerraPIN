package com.apps.terrapin;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class DownloadActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.download);
		Log.e("TAG", "Download activity called");

		Button button = (Button) findViewById(R.id.downloadButton);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					String rfilename = "/image.jpg";
					String imageUrl = Utils.serverName + Utils.uploadDirectory + rfilename;
				       URL url = new URL(imageUrl);
				       HttpGet httpRequest = null;

				       httpRequest = new HttpGet(url.toURI());

				       HttpClient httpclient = new DefaultHttpClient();
				       HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);

				       HttpEntity entity = response.getEntity();
				       BufferedHttpEntity b_entity = new BufferedHttpEntity(entity);
				       InputStream input = b_entity.getContent();

				       Bitmap bitmap = BitmapFactory.decodeStream(input);

				        ImageView i = (ImageView) findViewById(R.id.image);
				        i.setImageBitmap(bitmap);
				    } catch (MalformedURLException e) {
				        Log.e("log", "bad url");
				    } catch (IOException e) {
				        Log.e("log", "io error");
				    } catch (URISyntaxException e) {
						e.printStackTrace();
					}
			}
		});
	}
}