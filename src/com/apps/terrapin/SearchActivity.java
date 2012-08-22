package com.apps.terrapin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class SearchActivity extends MapActivity {
	private double latitude;
	private double longtitude;
	private String radiusString;

	static final String LAT = "latitude";
	static final String LON = "longtitude";
	static final String NAME = "username";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);
	}

	public void searchClicked(View view) {
		Log.e("#####", "Search clicked");

		if (Utils.isAuthorized(getApplicationContext())) {

			EditText textBox = (EditText) findViewById(R.id.entry);
			String addressString = textBox.getText().toString();

			EditText rtextBox = (EditText) findViewById(R.id.rentry);
			radiusString = rtextBox.getText().toString();

			MyGeocoder gc = new MyGeocoder();
			gc.getLocation(addressString);
			if (gc.hasResults()) {
				latitude = gc.getLatitude();
				longtitude = gc.getLongtitude();
				searchData();
			} else {
				Toast.makeText(this,
						"Cannot convert the given address to location",
						Toast.LENGTH_LONG).show();
				textBox.setText("");
			}
		} else
			Toast.makeText(this, "You are not authorized to use the service",
					Toast.LENGTH_SHORT).show();
	}

	private void searchData() {
		String baseUrl = Utils.serverName + "search.php?";
		String url = baseUrl + "latitude=" + Double.toString(latitude)
				+ "&longtitude=" + Double.toString(longtitude) + "&radius="
				+ radiusString + "&username="
				+ Utils.getUserName(getApplicationContext());

		Log.e("@@@@@", "SearchData called");
		Log.e("@@@@@", url.toString());

		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet();
		try {
			request.setURI(new URI(url));
			HttpResponse response = client.execute(request);

			String savedFileName = DownloadFile();

			Log.e("@@##@@", "saved data file at: " + savedFileName);
			ArrayList<TerrapinType> gp = parseFile("file:///" + savedFileName);
			showMap(gp);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void showMap(ArrayList<TerrapinType> gp) {
		setContentView(R.layout.map);

		MapView mapView;
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.setScrollbarFadingEnabled(true);

		List<Overlay> mapOverlays = mapView.getOverlays();
		Drawable drawable = this.getResources().getDrawable(
				R.drawable.androidmarker);
		HelloItemizedOverlay itemizedoverlay = new HelloItemizedOverlay(
				drawable, this);

		ArrayList<OverlayItem> oi = new ArrayList<OverlayItem>();
		Log.e("@@##", Integer.toString(gp.size()));
		for (int i = 0; i < gp.size(); i++)
			oi.add(new OverlayItem(gp.get(i).getPoint(), gp.get(i)
					.getUserName(), "Send a request"));
		for (int i = 0; i < oi.size(); i++)
			itemizedoverlay.addOverlay(oi.get(i));

		mapOverlays.add(itemizedoverlay);
	}

	public String DownloadFile() {
		String rfilename = Utils.getUserName(getApplicationContext())
				+ "data.txt";
		String lfilename = "data.txt";
		String surl = Utils.serverName + rfilename;
		File outputFile = null;
		try {
			URL url = new URL(surl);
			Log.e("@@@@@Remote: ", surl);
			HttpURLConnection c = (HttpURLConnection) url.openConnection();
			c.setRequestMethod("GET");
			c.setDoOutput(true);
			c.connect();

			String PATH = Environment.getExternalStorageDirectory()
					+ "/download/";
			Log.v("@@@@@", "PATH: " + PATH);
			File file = new File(PATH);
			file.mkdirs();

			outputFile = new File(file, lfilename);
			Log.e("@@@@@Remote: ", outputFile.getPath());
			FileOutputStream fos = new FileOutputStream(outputFile);

			InputStream is = c.getInputStream();

			byte[] buffer = new byte[1024];
			int len1 = 0;
			while ((len1 = is.read(buffer)) != -1) {

				fos.write(buffer, 0, len1);

			}
			fos.close();
			is.close();
		} catch (IOException e) {
			Log.d("@@@@@@@", "Error: " + e);
			Toast.makeText(this, "error " + e.toString(), Toast.LENGTH_LONG)
					.show();
		}
		Log.e("TAG", "downloaded data file");
		return outputFile.getPath();
	}

	private String getTextValue(Element ele, String tagName) {
		String textVal = "";
		NodeList nl = ele.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			Element el = (Element) nl.item(0);
			Node n = el.getFirstChild();
			if (n instanceof CharacterData) {
				CharacterData cd = (CharacterData) n;
				textVal = cd.getData();
			}
		}
		return textVal;
	}

	private float getFloatValue(Element ele, String tagName) {
		return Float.parseFloat(getTextValue(ele, tagName));
	}

	public ArrayList<TerrapinType> parseFile(String path) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document dom = null;
		ArrayList<TerrapinType> gp = new ArrayList<TerrapinType>();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			dom = db.parse(path);

			org.w3c.dom.Element eroot = dom.getDocumentElement();
			eroot.normalize();
			NodeList nl = eroot.getElementsByTagName("entry");

			if (nl != null && nl.getLength() > 0) {
				for (int i = 0; i < nl.getLength(); i++) {
					org.w3c.dom.Element el = (org.w3c.dom.Element) nl.item(i);
					float lat = (float) (getFloatValue(el, LAT) * 1E6);
					float lon = (float) (getFloatValue(el, LON) * 1E6);
					String name = (String) (getTextValue(el, NAME));
					GeoPoint temp = new GeoPoint((int) lat, (int) lon);
					gp.add(new TerrapinType(temp, name));
				}
			}
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return gp;
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			setContentView(R.layout.search);
			return true;
		}
		return false;
	}
}