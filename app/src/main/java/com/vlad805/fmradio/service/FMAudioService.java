package com.vlad805.fmradio.service;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import com.vlad805.fmradio.C;
import com.vlad805.fmradio.Storage;

/**
 * vlad805 (c) 2019
 */
public abstract class FMAudioService {
	public static final int SERVICE_LIGHT = 0;
	public static final int SERVICE_SPIRIT3 = 1;

	protected AudioManager mAudioManager;

	protected int mSampleRate = 44100; // Default = 8000 (Max with AMR)
	protected int mBufferSize = 16384;
	protected int mAudioSource = 1998;

	public FMAudioService(Context context) {
		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		mAudioSource = Storage.getPrefInt(context, C.Key.AUDIO_SOURCE, mAudioSource);
	}

	/**
	 * Start audio
	 */
	public abstract void startAudio();

	/**
	 * Stop audio
	 */
	public abstract void stopAudio();

	/**
	 * AudioSource:
	 * DEFAULT              work, HQ, loud
	 * MIC                  work, HW
	 * VOICE_UPLINK         not work
	 * VOICE_DOWNLINK       not work
	 * VOICE_CALL           not work
	 * CAMCORDER            work, LQ, very sensitive
	 * VOICE_RECOGNITION    work, LQ, only left channel
	 * VOICE_COMMUNICATION  work, LQ, very sensitive
	 * REMOTE_SUBMIX        not work
	 * 1998 = FM            work (Mi A1) / not work (Xperia L)
	 */
	protected AudioRecord getAudioRecorder() {
		// AUDIO_CHANNEL_IN_FRONT_BACK?
		try {
			AudioRecord recorder = new AudioRecord(
					mAudioSource,
					mSampleRate,
					AudioFormat.CHANNEL_IN_STEREO,
					AudioFormat.ENCODING_PCM_16BIT,
					mBufferSize
			);

			if (recorder.getState() == AudioRecord.STATE_INITIALIZED) { // If works, then done
				return recorder;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@SuppressWarnings("deprecation")
	protected void requestForFocus(boolean needFocus) {
		if (needFocus) { // If focus desired...
			mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK); //AudioManager.AUDIOFOCUS_GAIN);
		} else { // If focus return...
			mAudioManager.abandonAudioFocus(null);
		}
	}

}
