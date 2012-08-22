package com.apps.terrapin;

import com.google.android.maps.GeoPoint;

public class TerrapinType {
	private GeoPoint gp;
	private String mUserName;
	
	public TerrapinType(GeoPoint p, String name)
	{
		gp = p;
		mUserName = name;
	}
	
	public GeoPoint getPoint()
	{
		return gp;
	}
	
	public String getUserName()
	{
		return mUserName;
	}
	
	public void setUserName(String name)
	{
		mUserName = name;
	}
	
	public void setGeoPoint(GeoPoint p)
	{
		gp = p;
	}
}
