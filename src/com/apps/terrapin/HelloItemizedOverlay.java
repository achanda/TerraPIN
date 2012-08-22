package com.apps.terrapin;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

@SuppressWarnings("rawtypes")
public class HelloItemizedOverlay extends ItemizedOverlay {
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private  Context mContext;

	public HelloItemizedOverlay(Drawable defaultMarker) {
		  super(boundCenterBottom(defaultMarker));
		}
	
	public HelloItemizedOverlay(Drawable defaultMarker, Context context) {
		  this(defaultMarker);
		  mContext = context;
		}
	
	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}
	
	@Override
	protected OverlayItem createItem(int i) {
	  return mOverlays.get(i);
	}
	
	@Override
	public int size() {
	  return mOverlays.size();
	}
	
	private void sendPicRequest(String toUser) {
		Log.e("TAG", "Sending a notification to user: " + toUser);
		
		String burl = "http://" + "192.168.1.110" + "/send.php?";
		String url = burl + "touser=" + toUser + "&fromuser=" + Utils.getUserName(this.mContext)
				+ "flag=" + "1"; // flag = 1 for request to upload, 0 for request to download 
		
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
	protected boolean onTap(int index) {
	  final OverlayItem item = mOverlays.get(index);
	  AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
	  dialog.setTitle(item.getTitle());
	  dialog.setMessage(item.getSnippet());
	  dialog.setPositiveButton("Request", new OnClickListener() {
          public void onClick(DialogInterface dialog, int arg1) {
              sendPicRequest(item.getTitle());
              dialog.dismiss();
          }
	  });
	  dialog.show();
	  return true;
	}
}

