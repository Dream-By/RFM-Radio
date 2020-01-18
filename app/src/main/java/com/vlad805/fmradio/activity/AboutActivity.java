package com.vlad805.fmradio.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.vlad805.fmradio.R;
import com.vlad805.fmradio.Utils;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);


		((TextView) findViewById(R.id.about_version)).setText(getString(R.string.about_version, Utils.getApplicationVersion(this)));
	}
}
