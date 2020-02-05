package com.vlad805.fmradio.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;

/**
 * vlad805 (c) 2020
 */
public class DelayAutoCompleteTextView extends androidx.appcompat.widget.AppCompatAutoCompleteTextView {

	private static final int MESSAGE_TEXT_CHANGED = 100;
	private static final int DEFAULT_AUTOCOMPLETE_DELAY = 750;

	private int mAutoCompleteDelay = DEFAULT_AUTOCOMPLETE_DELAY;
	private ProgressBar mLoadingIndicator;

	private static class FilterHandler extends Handler {
		private final DelayAutoCompleteTextView mView;

		public FilterHandler(final DelayAutoCompleteTextView view) {
			mView = view;
		}

		@Override
		public void handleMessage(Message msg) {
			mView.performFiltering((CharSequence) msg.obj, msg.arg1);
		}
	}

	private final Handler mHandler = new FilterHandler(this);

	public DelayAutoCompleteTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setLoadingIndicator(final ProgressBar progressBar) {
		mLoadingIndicator = progressBar;
	}

	@SuppressWarnings("unused")
	public void setAutoCompleteDelay(final int autoCompleteDelay) {
		mAutoCompleteDelay = autoCompleteDelay;
	}

	@Override
	protected void performFiltering(final CharSequence text, final int keyCode) {
		super.performFiltering(text, keyCode);
		if (mLoadingIndicator != null) {
			mLoadingIndicator.setVisibility(View.VISIBLE);
		}
		mHandler.removeMessages(MESSAGE_TEXT_CHANGED);
		mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_TEXT_CHANGED, text), mAutoCompleteDelay);
	}

	@Override
	public void onFilterComplete(final int count) {
		if (mLoadingIndicator != null) {
			mLoadingIndicator.setVisibility(View.GONE);
		}
		super.onFilterComplete(count);
	}
}
