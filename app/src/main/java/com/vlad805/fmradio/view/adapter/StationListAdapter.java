package com.vlad805.fmradio.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vlad805.fmradio.R;
import com.vlad805.fmradio.models.Station;
import com.vlad805.fmradio.view.holder.StationHolder;

import java.util.List;

/**
 * vlad805 (c) 2020
 */
public class StationListAdapter extends RecyclerView.Adapter<StationHolder> {
	private List<Station> mDataset;

	public StationListAdapter(final List<Station> dataset) {
		setDataset(dataset);
	}

	public void setDataset(final List<Station> dataset) {
		mDataset = dataset;
	}

	@Override
	@NonNull
	public StationHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.station_list_item, parent, false);
		return new StationHolder(v);
	}

	@Override
	public void onBindViewHolder(final StationHolder holder, final int position) {
		holder.set(mDataset.get(position));
	}

	@Override
	public int getItemCount() {
		return mDataset.size();
	}
}