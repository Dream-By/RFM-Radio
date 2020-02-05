package com.vlad805.fmradio.models;

import org.json.JSONObject;

/**
 * vlad805 (c) 2020
 */
public class City {
	private final int id;
	private final String area;
	private final String city;
	private final int count;
	private final String full;

	public City(final int id, final String area, final String city, final int count) {
		this.id = id;
		this.area = area;
		this.city = city;
		this.count = count;
		this.full = (city + " " + area).toLowerCase();
	}

	public City(final JSONObject obj) {
		this(
				obj.optInt("id"),
				obj.optString("area"),
				obj.optString("city"),
				obj.optInt("count")
		);
	}

	public int getId() {
		return id;
	}

	public String getArea() {
		return area;
	}

	public String getCity() {
		return city;
	}

	public int getCount() {
		return count;
	}

	public String getFull() {
		return full;
	}
}
