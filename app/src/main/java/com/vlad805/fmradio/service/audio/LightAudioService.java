package com.vlad805.fmradio.service.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import com.vlad805.fmradio.service.IAudioRecordable;
import com.vlad805.fmradio.service.fm.IFMRecorder;
import com.vlad805.fmradio.service.fm.RecordError;

/**
 * vlad805 (c) 2019
 */
@SuppressWarnings("deprecation")
public class LightAudioService extends FMAudioService implements IAudioRecordable {

	private Thread mThread;

	private AudioTrack mAudioTrack;
	private AudioRecord mAudioRecorder;
	private IFMRecorder mRecordable;

	private boolean mIsActive = false;

	public LightAudioService(final Context context) {
		super(context);
	}

	@Override
	public void startAudio() {
		if (mIsActive) {
			return;
		}

		mIsActive = true;
		mThread = new Thread(mReadWrite);
		mThread.start();
	}

	@Override
	public void stopAudio() {
		if (!mIsActive) {
			return;
		}

		mIsActive = false;
		if (mThread != null) {
			mThread.interrupt();
		}
		closeAll();
	}

	private void closeAll() {
		mIsActive = false;
		if (mAudioTrack != null) {
			mAudioTrack.release();
			mAudioTrack = null;
		}

		if (mAudioRecorder != null) {
			if (mAudioRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
				mAudioRecorder.stop();
			}
			mAudioRecorder.release();
			mAudioRecorder = null;
		}
	}

	private Runnable mReadWrite = () -> {
		int bufferSizeInBytes = AudioTrack.getMinBufferSize(
				mSampleRate,
				AudioFormat.CHANNEL_IN_STEREO,
				AudioFormat.ENCODING_PCM_16BIT
		);

		mAudioTrack = new AudioTrack(
				AudioManager.STREAM_MUSIC,
				mSampleRate,
				AudioFormat.CHANNEL_OUT_STEREO,
				AudioFormat.ENCODING_PCM_16BIT,
				bufferSizeInBytes,
				AudioTrack.MODE_STREAM
		);

		mAudioRecorder = getAudioRecorder();
		mAudioRecorder.startRecording();
		mAudioTrack.play();

		int bytes;
		byte[] buffer = new byte[bufferSizeInBytes];

		while (mIsActive) {
			bytes = mAudioRecorder.read(buffer, 0, bufferSizeInBytes);
			if (mIsActive) {
				mAudioTrack.write(buffer, 0, bytes);
				if (mRecordable != null) {
					mRecordable.record(buffer, bytes);
				}
			}
		}
	};

	@Override
	public void startRecord(final IFMRecorder driver) throws RecordError {
		mRecordable = driver;
		driver.startRecord();
	}

	@Override
	public void stopRecord() {
		mRecordable.stopRecord();
		mRecordable = null;
	}
}
