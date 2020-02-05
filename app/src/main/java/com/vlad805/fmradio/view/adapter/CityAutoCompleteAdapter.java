package com.vlad805.fmradio.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.vlad805.fmradio.R;
import com.vlad805.fmradio.Utils;
import com.vlad805.fmradio.models.City;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * vlad805 (c) 2020
 */
public class CityAutoCompleteAdapter extends BaseAdapter implements Filterable {
	private final Context mContext;
	private List<City> mList;
	private List<City> mResults;

	public interface OnListLoaded {
		void onLoad();
	}


	public CityAutoCompleteAdapter(final Context context) {
		mContext = context;
		mResults = new ArrayList<>();
	}

	public void loadList(final OnListLoaded onLoad) {
		Utils.fetch("http://rfm.velu.ga/getAreas", new Utils.FetchCallback() {
			@Override
			public void onSuccess(final JSONObject result) {
				mList = new ArrayList<>();

				final JSONArray items = result.optJSONArray("items");

				for (int i = 0; i < items.length(); ++i) {
					mList.add(new City(items.optJSONObject(i)));
				}

				Collections.sort(mList, (a, b) -> b.getCount() - a.getCount());

				onLoad.onLoad();
			}

			@Override
			public void onError(Throwable ex) {
				ex.printStackTrace();

				Utils.uiThread(() -> Toast.makeText(mContext, "Error while receive list of area", Toast.LENGTH_SHORT).show());
			}
		});
	}

	@Override
	public int getCount() {
		return mResults.size();
	}

	@Override
	public City getItem(final int index) {
		return mResults.get(index);
	}

	@Override
	public long getItemId(final int position) {
		return position;
	}

	@Override
	public View getView(final int position, View view, final ViewGroup parent) {
		if (view == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			view = inflater.inflate(R.layout.layout_item_city, parent, false);
		}
		City city = getItem(position);
		((TextView) view.findViewById(R.id.city_item_title)).setText(city.getCity());
		((TextView) view.findViewById(R.id.city_item_area)).setText(city.getArea());
		((TextView) view.findViewById(R.id.city_item_count)).setText(String.valueOf(city.getCount()));

		return view;
	}

	@Override
	public Filter getFilter() {
		return new Filter() {
			@Override
			protected FilterResults performFiltering(final CharSequence constraint) {
				final FilterResults filterResults = new FilterResults();
				if (constraint != null) {
					final List<City> books = findCities(String.valueOf(constraint).toLowerCase());
					// Assign the data to the FilterResults
					filterResults.values = books;
					filterResults.count = books.size();
				}
				return filterResults;
			}

			@Override
			protected void publishResults(final CharSequence constraint, final FilterResults results) {
				if (results != null && results.count > 0) {
					mResults = (List<City>) results.values;
					notifyDataSetChanged();
				} else {
					notifyDataSetInvalidated();
				}
			}};
	}

	/**
	 * Returns a result for the cities.
	 */
	private List<City> findCities(final String constraint) {
		final List<City> result = new ArrayList<>();

		for (final City current : mList) {
			if (current.getFull().startsWith(constraint)) {
				result.add(current);
			}
		}

		return result;
	}
}