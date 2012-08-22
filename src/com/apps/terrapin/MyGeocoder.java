package com.apps.terrapin;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.util.Log;

public class MyGeocoder {
	private static final String GEOCODER_REQUEST_PREFIX_FOR_XML = 
			"http://maps.google.com/maps/api/geocode/xml";
	private float latitude;
	private float longtitude;
	private String status;

	public float getLatitude() {
		return latitude;
	}

	public float getLongtitude() {
		return longtitude;
	}
	
	public boolean hasResults() {
		if (status.contains("ZERO_RESULTS"))
			return false;
		else
			return true;
	}

	public void getLocation(String address) {
		URL url = null;
		NodeList resultNodeList = null;
		HttpURLConnection conn = null;

		try {
			url = new URL(GEOCODER_REQUEST_PREFIX_FOR_XML + "?address="
					+ URLEncoder.encode(address, "UTF-8") + "&sensor=false");
			Log.e("@@@@@", url.toString());
			conn = (HttpURLConnection) url.openConnection();
			Document geocoderResultDocument = null;

			conn.connect();
			InputSource geocoderResultInputSource = new InputSource(
					conn.getInputStream());

			// read result and parse into XML Document
			geocoderResultDocument = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().parse(geocoderResultInputSource);
			XPath xpath = XPathFactory.newInstance().newXPath();
			resultNodeList = (NodeList) xpath.evaluate(
					"/GeocodeResponse/result[1]/geometry/location/*",
					geocoderResultDocument, XPathConstants.NODESET);
			status = (String) xpath.evaluate("/GeocodeResponse/status", 
					geocoderResultDocument, XPathConstants.STRING);
			Log.e("@@@@@", status);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.disconnect();
		}
		float lat = Float.NaN;
		float lng = Float.NaN;
		for (int i = 0; i < resultNodeList.getLength(); ++i) {
			Node node = resultNodeList.item(i);
			if ("lat".equals(node.getNodeName()))
				lat = Float.parseFloat(node.getTextContent());
			if ("lng".equals(node.getNodeName()))
				lng = Float.parseFloat(node.getTextContent());

			latitude = lat;
			longtitude = lng;
		}

	}
}
