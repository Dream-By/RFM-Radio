package com.vlad805.fmradio.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.vlad805.fmradio.C;
import com.vlad805.fmradio.R;
import com.vlad805.fmradio.controller.RadioController;
import com.vlad805.fmradio.fragments.FindStationsWithInternet;
import com.vlad805.fmradio.helper.ProgressDialog;
import com.vlad805.fmradio.models.Station;
import com.vlad805.fmradio.view.adapter.StationListAdapter;
import com.vlad805.fmradio.view.holder.StationHolder;

import java.util.ArrayList;
import java.util.List;

public class StationListActivity extends AppCompatActivity {
	private ProgressDialog mProgress;
	private RadioController mRadioCtl;
	private RecyclerView mRecycler;
	private RecyclerView.Adapter<StationHolder> mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_station_list);

		setSupportActionBar(findViewById(R.id.toolbar_station_list));

		final ActionBar ab = getSupportActionBar();

		if (ab != null) {
			ab.setDisplayHomeAsUpEnabled(true);
		}

		mProgress = ProgressDialog.create(this);
		mRecycler = findViewById(R.id.station_list_recycler);

		mRadioCtl = new RadioController(this);
	}

	private void showFrequencies(List<Station> stations) {
		mAdapter = new StationListAdapter(stations);
		mRecycler.setAdapter(mAdapter);
	}

	private void showVariants() {
		final AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle(R.string.station_list_search_select_way_title)
				.setIcon(R.drawable.ic_search)
				.setItems(R.array.station_search_select_way, (dialog, which) -> {
					switch (which) {
						case 0: searchHW(); break;
						case 1: searchManual(); break;
						case 2: searchInternet(); break;
					}
				});

		ab.create().show();
	}

	private BroadcastReceiver mSearchDone = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final int[] frequencies = intent.getIntArrayExtra(C.Key.STATION_LIST);
			final List<Station> result = new ArrayList<>();

			if (frequencies == null) {
				Log.e("SLA", "frequencies in C.Key.STATION_LIST is null");
				return;
			}

			unregisterReceiver(mSearchDone);
			mProgress.hide();

			for (int frequency : frequencies) {
				result.add(new Station(frequency));
			}

			showFrequencies(result);
		}
	};

	private void searchHW() {
		mProgress.text(R.string.station_list_searching).show();

		mRadioCtl.hwSearch();

		registerReceiver(mSearchDone, new IntentFilter(C.Event.HW_SEARCH_DONE));
	}

	private void searchManual() {
		mProgress.text(R.string.station_list_searching).show();

		mRadioCtl.swSearch();

		registerReceiver(mSearchDone, new IntentFilter(C.Event.SW_SEARCH_DONE));
	}

	private void searchInternet() {
		FindStationsWithInternet.openDialogSelectCity(this, city -> {
			mProgress.text(getString(R.string.station_list_net_fetch_stations, city.getCity())).show();
			FindStationsWithInternet.loadStationsByAreaId(city.getId(), list -> {
				mProgress.hide();
			});
		});
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.station, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home: {
				finish();
				return true;
			}

			case R.id.menu_station_search: {
				showVariants();
				return true;
			}

			default: {
				return super.onOptionsItemSelected(item);
			}
		}
	}
}
