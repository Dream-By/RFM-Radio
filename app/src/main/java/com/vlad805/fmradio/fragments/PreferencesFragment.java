package com.vlad805.fmradio.fragments;

import android.os.Bundle;
import android.text.InputType;
import android.util.SparseArray;
import androidx.annotation.StringRes;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.vlad805.fmradio.R;
import com.vlad805.fmradio.Utils;
import com.vlad805.fmradio.service.audio.FMAudioService;
import com.vlad805.fmradio.service.fm.FMController;

import java.util.ArrayList;
import java.util.List;

/**
 * vlad805 (c) 2020
 */
public class PreferencesFragment extends PreferenceFragmentCompat {

	private static SparseArray<String> mAudioSource;

	static {
		mAudioSource = new SparseArray<>();
		mAudioSource.put(0, "0, DEFAULT");
	 	mAudioSource.put(1, "1, MIC");
	 	mAudioSource.put(2, "2, VOICE_UPLINK");
		mAudioSource.put(3, "3, VOICE_DOWNLINK");
	 	mAudioSource.put(4, "6, VOICE_CALL");
		mAudioSource.put(5, "5, CAMCORDER");
		mAudioSource.put(6, "6, VOICE_RECOGNITION");
	 	mAudioSource.put(7, "7, VOICE_COMMUNICATION");
		mAudioSource.put(8, "8, REMOTE_SUBMIX");
	 	mAudioSource.put(1998, "1998, FM");
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.preferences_main, rootKey);

		setNumberMessageAndProvider("tuner_antenna", R.string.pref_tuner_antenna_message);

		setListProviderAndEntries("tuner_driver", FMController.sDrivers);
		setListProviderAndEntries("audio_service", FMAudioService.sService);
		setListProviderAndEntries("audio_source", mAudioSource);
	}

	private void setNumberMessageAndProvider(final String key, final @StringRes int resId) {
		EditTextPreference preference = findPreference(key);

		if (preference != null) {
			preference.setSummaryProvider((Preference.SummaryProvider<EditTextPreference>) EditTextPreference::getText);
			preference.setDialogMessage(getString(resId));
			preference.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
		}
	}

	private void setListProviderAndEntries(final String key, final SparseArray<String> entries) {
		ListPreference lp = findPreference(key);
		if (lp != null) {
			final List<String> keys = new ArrayList<>();
			final List<String> values = new ArrayList<>();

			for (int i = 0; i < entries.size(); ++i) {
				final int k = entries.keyAt(i);

				keys.add(String.valueOf(k));
				values.add(entries.get(k));
			}

			lp.setSummaryProvider((Preference.SummaryProvider<ListPreference>) s -> {
				final int id = entries.indexOfKey(Utils.parseInt(s.getValue()));
				return entries.valueAt(id);
			});
			lp.setEntries(values.toArray(new String[0]));
			lp.setEntryValues(keys.toArray(new String[0]));
		}
	}
}
