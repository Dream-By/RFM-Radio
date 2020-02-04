package com.vlad805.fmradio.view.holder;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.vlad805.fmradio.R;
import com.vlad805.fmradio.Utils;
import com.vlad805.fmradio.models.Station;

/**
 * vlad805 (c) 2020
 * Station view holder in recycler view in station list manager
 */
public class StationHolder extends RecyclerView.ViewHolder {
	private TextView mFrequency;
	private TextView mTitle;

	public StationHolder(final View v) {
		super(v);

		mFrequency = v.findViewById(R.id.station_item_frequency);
		mTitle = v.findViewById(R.id.station_item_title);
	}

	@SuppressLint("ClickableViewAccessibility")
	public void set(Station s) {
		mFrequency.setText(Utils.getMHz(s.getFrequency()).trim());
		mTitle.setText(s.getTitle());
	}
}
