package com.vlad805.fmradio.service.fm.impl;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import com.vlad805.fmradio.BuildConfig;
import com.vlad805.fmradio.C;
import com.vlad805.fmradio.Utils;
import com.vlad805.fmradio.enums.Direction;
import com.vlad805.fmradio.enums.MuteState;
import com.vlad805.fmradio.service.FMRecordService;
import com.vlad805.fmradio.service.fm.*;
import com.vlad805.fmradio.service.fm.communications.Poll;
import com.vlad805.fmradio.service.fm.communications.Request;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * vlad805 (c) 2020
 */
public class QualCommLegacy extends FMController implements IFMEventListener, IFMRecordable {

	public static class Config extends LaunchConfig {
		@Override
		public int getClientPort() {
			return 2112;
		}

		@Override
		public int getServerPort() {
			return 2113;
		}
	}

	private FMEventListenerServer mServer;
	private FMEventCallback mEventCallback;
	private Poll mCommandPoll;

	public QualCommLegacy(final LaunchConfig config, final Context context) {
		super(config, context);

		mCommandPoll = new Poll(config);
	}

	public void setEventListener(final FMEventCallback callback) {
		mEventCallback = callback;
	}

	/**
	 * Returns filename of binary by architecture
	 * @return Filename of binary
	 */
	private String getBinaryName() {
		return "fmbin-" + Utils.determineArch();
	}

	private String getBinaryPath() {
		return getAppRootPath(getBinaryName());
	}

	@Override
	public boolean isInstalled() {
		return new File(getBinaryPath()).exists();
	}

	@Override
	public boolean isObsolete() {
		return BuildConfig.DEBUG;
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Override
	protected void installImpl(final Callback<Void> callback) {
		File dir = new File(getAppRootPath());
		if (!dir.exists()) {
			dir.mkdirs();
		}

		try {
			// Copy binary
			boolean success = copyBinary();

			if (!success) {
				callback.onError(new Error("Error while copy binary"));
				return;
			}
		} catch (FileNotFoundException e) {
			// FileNotFoundException - Text file busy
			// Throws when binary is launched
			Utils.shell("killall " + getBinaryPath(), true);

			try {
				copyBinary();
			} catch (FileNotFoundException e2) {
				callback.onError(new Error("Error while copy binary after kill busy binary"));
				return;
			}
		}

		Utils.shell("chmod 777 " + getBinaryPath() + " 1>/dev/null 2>/dev/null", true);
		callback.onResult(null);
	}

	@Override
	protected void launchImpl(final Callback<Void> callback) {
		String command = String.format("%s 1>/dev/null 2>/dev/null &", getBinaryPath());
		Utils.shell(command, true);
		startServerListener();
		final Request request = new Request("init", 1500).onResponse(data -> callback.onResult(null));
		sendCommand(request);
	}

	@Override
	protected void killImpl(final Callback<Void> callback) {
		if (mServer != null) {
			mServer.closeServer();
		}
		String command = String.format("killall %1$s 1>/dev/null 2>/dev/null &", getBinaryName());
		Utils.shell(command, true);
		callback.onResult(null);
	}

	@Override
	protected void enableImpl(final Callback<Void> callback) {
		sendCommand(new Request("enable", 5000).onResponse(result -> callback.onResult(null)));
	}

	@Override
	protected void disableImpl(final Callback<Void> callback) {
		sendCommand(new Request("disable", 5000).onResponse(result -> callback.onResult(null)));
	}

	private int frequency;
	@Override
	protected void setFrequencyImpl(final int kHz, final Callback<Integer> callback) {
		sendCommand(new Request("setfreq " + kHz).onResponse(data -> callback.onResult(kHz)));
	}

	@Override
	protected void getSignalStretchImpl(final Callback<Integer> callback) {
		sendCommand(new Request("getrssi").onResponse(data -> callback.onResult(Utils.parseInt(-0xff + data))));
	}

	@Override
	protected void jumpImpl(final int direction, final Callback<Integer> callback) {
		sendCommand(new Request("jump " + direction, 1500).onResponse(data -> callback.onResult(Utils.parseInt(data))));
	}

	@Override
	protected void hwSeekImpl(final int direction, final Callback<Integer> callback) {
		sendCommand(new Request("seekhw " + direction, 4000).onResponse(data -> callback.onResult(Utils.parseInt(data))));
	}

	@Override
	public void setMute(final MuteState state, final Callback<Void> callback) {

	}

	@Override
	protected void hwSearchImpl(final Callback<List<Integer>> callback) {
		sendCommand(new Request("search", 60000).onResponse(data -> {
			List<Integer> res = new ArrayList<>();
			Log.e("QCL", "hwSearch " + data);
			callback.onResult(res);
		}));
	}

	private static final String TAG = "QCL_SW_S";
	@Override
	protected void swSearchImpl(final Callback<List<Integer>> callback) {
		int last = 87500;
		final List<Integer> list = new ArrayList<>();
		Log.d(TAG, "swSearchImpl: setup");

		final Callback<Void> onDone = _v -> {
			Log.d(TAG, "swSearchImpl: onDone");
			final Bundle bundle = new Bundle();
			bundle.putIntArray(C.Key.STATION_LIST, Utils.listIntegerToArrayInt(list));
			fireEvent(C.Event.SW_SEARCH_DONE, bundle);
		};

		setFrequencyImpl(last, _v -> {
			Log.d(TAG, "swSearchImpl: set 87500");
			frequency = last;
			stepSwSeek(list, onDone);
		});
	}

	private void stepSwSeek(final List<Integer> list, final Callback<Void> onDone) {
		Log.d(TAG, "stepSwSeek: stepSwSeek... with last ");
		hwSeekImpl(Direction.UP.getValue(), frequency -> {
			Log.d(TAG, "stepSwSeek: before sleep with v = " + frequency);
			Utils.sleep(1000);

			list.add(frequency);
			Log.d(TAG, "stepSwSeek: " + frequency + "; list size = " + list.size());
			if (frequency < list.get(list.size() - 1)) {
				onDone.onResult(null);
				return;
			}

			stepSwSeek(list, onDone);
		});
	}

	@Override
	public void newRecord(final Callback<IFMRecorder> callback) {
		callback.onResult(new FMRecordService(context, frequency));
	}

	/**
	 * Copy file from application assets
	 * @param context Context
	 * @param fromAssetPath Path in assets
	 * @param toPath Destination path
	 * @return True on success
	 */
	@SuppressWarnings("ResultOfMethodCallIgnored")
	private boolean copyAsset(Context context, String fromAssetPath, String toPath) throws FileNotFoundException {
		InputStream in;
		OutputStream out;
		try {
			in = context.getAssets().open(fromAssetPath);
			new File(toPath).createNewFile();
			out = new FileOutputStream(toPath);

			byte[] buffer = new byte[1024];
			int read;
			while((read = in.read(buffer)) != -1){
				out.write(buffer, 0, read);
			}

			in.close();
			out.flush();
			out.close();
			return true;
		} catch (IOException e) {
			if (e instanceof FileNotFoundException) {
				throw (FileNotFoundException) e;
			}
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Copy binary from assets
	 * @return True if success
	 */
	private boolean copyBinary() throws FileNotFoundException {
		return copyAsset(context, getBinaryName(), getBinaryPath());
	}

	/**
	 * Send command
	 * @param command Command
	 */
	private void sendCommand(final Request command) {
		mCommandPoll.send(command);
	}

	private void startServerListener() {
		try {
			if (mServer != null) {
				mServer.closeServer();
			}
			mServer = new FMEventListenerServer(config.getServerPort());
			mServer.setCallback(mEventCallback);
			mServer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
