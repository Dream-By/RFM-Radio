package com.vlad805.fmradio.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import com.vlad805.fmradio.R;

/**
 * vlad805 (c) 2019
 */
public class PresetListView extends HorizontalScrollView {

	private static final int PRESET_COUNT = 20;

	public PresetListView(Context context) {
		super(context);

		init();
	}

	public PresetListView(Context context, AttributeSet attrs) {
		super(context, attrs);

		init();
	}


	private void init() {

		LinearLayout v = new LinearLayout(getContext());

		v.setOrientation(LinearLayout.HORIZONTAL);

		for (int i = 0; i < PRESET_COUNT; ++i) {
			v.addView(new PresetView(getContext()));
		}

		addView(v);
	}


}
