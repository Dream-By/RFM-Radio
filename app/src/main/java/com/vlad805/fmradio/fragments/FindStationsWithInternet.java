package com.vlad805.fmradio.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import androidx.appcompat.app.AlertDialog;
import com.vlad805.fmradio.R;
import com.vlad805.fmradio.models.City;
import com.vlad805.fmradio.models.Station;
import com.vlad805.fmradio.view.DelayAutoCompleteTextView;
import com.vlad805.fmradio.view.adapter.CityAutoCompleteAdapter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.vlad805.fmradio.Utils.fetch;
import static com.vlad805.fmradio.Utils.uiThread;

/**
 * vlad805 (c) 2020
 */
public class FindStationsWithInternet {
	public interface OnCitySelected {
		void onCitySelect(final City city);
	}

	public interface OnStationsLoaded {
		void onStationsLoaded(final List<Station> stations);
	}

	public static void openDialogSelectCity(final Context context, final OnCitySelected listener) {
		final AlertDialog.Builder ab = new AlertDialog.Builder(context);

		View root = LayoutInflater.from(context).inflate(R.layout.layout_autocomplete_city, null);

		ab.setView(root);

		final CityAutoCompleteAdapter adapter = new CityAutoCompleteAdapter(context);

		final DelayAutoCompleteTextView editText = root.findViewById(R.id.city_title);
		final ProgressBar progress = root.findViewById(R.id.city_progress);
		editText.setThreshold(2);
		editText.setAdapter(adapter);
		editText.setLoadingIndicator(progress);

		ab.setTitle(R.string.station_list_net_find_city);
		AlertDialog dialog = ab.create();
		dialog.show();

		editText.setOnItemClickListener((adapterView, view, position, id) -> {
			final City city = (City) adapterView.getItemAtPosition(position);
			editText.setText(city.getCity());

			if (listener != null) {
				listener.onCitySelect(city);
				dialog.cancel();
			}
		});

		new Thread(() -> adapter.loadList(() -> uiThread(() -> {
			editText.setEnabled(true);
			editText.setHint(R.string.station_list_net_hint);
			progress.setVisibility(View.GONE);
		}))).start();
	}

	public static void loadStationsByAreaId(final int areaId, final OnStationsLoaded callback) {
		new Thread(() -> fetch("http://rfm.velu.ga/getArea?areaId=" + areaId, result -> {
			final List<Station> list = new ArrayList<>();
			final JSONArray items = result.optJSONArray("stations");

			for (int i = 0; i < items.length(); ++i) {
				list.add(convertStation(items.optJSONObject(i)));
			}

			uiThread(() -> callback.onStationsLoaded(list));
		})).start();
	}

	private static Station convertStation(final JSONObject item) {
		return new Station(item.optInt("frequency"), item.optString("title"));
	}

}
