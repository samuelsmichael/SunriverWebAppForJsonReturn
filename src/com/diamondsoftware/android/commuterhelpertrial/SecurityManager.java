package com.diamondsoftware.android.commuterhelpertrial;

import com.diamondsoftware.android.commuterhelpertrial.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Timer;
import java.util.TimerTask;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.ServerManagedPolicy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.ContextThemeWrapper;

public class SecurityManager {
	Activity mActivity;
	public static final int START_TRIAL_WARNINGS = 15;
	public static final int TRIAL_ALLOWANCE = 30;
	private static boolean isNonTrialVersionAndIsRegistered = false;
	private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArcOle5ouVLsqrmYiUQkQAQsfyW4mGZhEAn7HY0uICPfCN60AUDOG8ooTT5UjyNP30Fbd7LRyHTYq7STOsi7C1+2+JwBbjcu7bgXl+nHz/XsxCOzALy4KJw9jvZFGBMWx8+17C98OEjiejw9AqU11PvLoZx+cPIqJNxeOX5P0htZiewe4VHrXeZGsitdZwy/uP7sYGxKgZowG/VdAj89KGFuM3+MifYT58dEGBItKs0Y+Eg2LEVddOLkbT+PAL1FJA/MM4nBQ65UR/rBxrgwEk7Ev6LmjgFS9lSaYHh7AYmHBl7H8/Vm6OV0IB3012B3e4l+2lSNzqxWDQKCM3q0PpwIDAQAB";
	private static boolean haveDoneRegistrationCheck = false;
	private Handler mHandler;	


	private String getDeviceId() {
		TelephonyManager tm = (TelephonyManager) mActivity
				.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getLine1Number();
	}

	public SecurityManager(Activity activity) {
		mActivity = activity;
		if (!haveDoneRegistrationCheck && !isTrialVersion()) {
			final Timer jdTimer = new Timer("Licensing");
			jdTimer.schedule(new TimerTask() {
				public void run() {
					Thread thread = new Thread(new Runnable() {
						public void run() {
							// Construct the LicenseCheckerCallback. The library
							// calls this when done.
							mLicenseCheckerCallback = new MyLicenseCheckerCallback();
							// Construct the LicenseChecker with a Policy.
							mChecker = new LicenseChecker(mActivity,
									new ServerManagedPolicy(mActivity,
											new AESObfuscator(SALT, mActivity
													.getPackageName(),
													getDeviceId())),
									BASE64_PUBLIC_KEY // Your public licensing
														// key.
							);
							mChecker.checkAccess(mLicenseCheckerCallback);
						}
					});
					thread.setPriority(Thread.MIN_PRIORITY);
					thread.run();
				}
			}, 3000, 1000 * 60 * 10);
		}
	}

	private boolean isUnregisteredLiveVersion() {
		if (haveDoneRegistrationCheck && !isNonTrialVersionAndIsRegistered) {
			return true;
		}
		return false;
	}

	public boolean doTrialCheck() {
		if (isTrialVersion() || isUnregisteredLiveVersion()) {
			if(isTrialVersion()) {
				if (hasExceededTrials()) {
					new TrialVersionDialog(
							"Trial Software",
							"Your trial period is over. In order to continue using Commuter Alert you will have to purchase it.",
							mActivity, true).show();
					return false;
				} else {
					incrementTrials();
					if (startWarnings()) {
						new TrialVersionDialog(
								"Trial Software Alert",
								"Your trial period is nearing its end. In order to continue using Commuter Alert without seeing this warning, you will have to purchase it.",
								mActivity, false).show();
					}
				}
			} else {
				new TrialVersionDialog(
						"Unregistered Version",
						"It appears that you are using an un-registered version. In order to continue using Commuter Alert you will have to purchase it. If you think that you have received this message in error, please try to load Commuter Alert again.",
						mActivity, true).show();
				return false;
				
			}
		}
		return true;
	}

	private boolean isTrialVersion() {
		String pkg=mActivity.getPackageName().toLowerCase();
		return pkg.indexOf("trial") != -1;
	}

	private boolean hasExceededTrials() {
		return getCountUserArmed() > TRIAL_ALLOWANCE;
	}

	private boolean startWarnings() {
		return getCountUserArmed() > START_TRIAL_WARNINGS;
	}

	private void incrementTrials() {
		stampVersion(getCountUserArmed() + 1);
	}

	public static class TrialVersionDialog {
		private String mTitle;
		private String mMessage;
		private Activity mActivity;
		private boolean mTrialPeriodIsOver;

		private TrialVersionDialog() {
			super();
		}

		public TrialVersionDialog(String title, String message,
				Activity activity, boolean trialPeriodIsOver) {
			super();
			mTitle = title;
			mMessage = message;
			mActivity = activity;
			mTrialPeriodIsOver = trialPeriodIsOver;
		}

		public void show() {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					new ContextThemeWrapper(mActivity,
							R.style.AlertDialogCustomDark));
			builder.setTitle(mTitle)
					.setPositiveButton("Purchase",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									String uri = "market://details?id="
											+ mActivity.getPackageName()
													.replace("trial", "");
									Intent ii3 = new Intent(Intent.ACTION_VIEW,
											Uri.parse(uri));
									mActivity.startActivity(ii3);
								}
							}).setMessage(mMessage);
			// Create the AlertDialog object and return it
			String negativeButtonVerbiage = "Not Now";
			if (mTrialPeriodIsOver) {
				negativeButtonVerbiage = "Not Now";
			}
			builder.setNegativeButton(negativeButtonVerbiage,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int id) {
							if (mTrialPeriodIsOver) {

							}
						}
					});
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}

	/*
	 * return true if file existed beforehand
	 */
	public boolean initializeVersion() {
		boolean retValue = false;
		if (!getExistsVersionFile()) {
			stampVersion(0);
			retValue = false;
		} else {
			retValue = true;
		}
		return retValue;
	}

	public int getCountUserArmed() {
		int countUsed = 0;
		FileInputStream fis = null;
		final char[] buffer = new char[10];
		final StringBuilder out = new StringBuilder();
		try {
			fis = getTrialCounterInputStream();
			final Reader in = new InputStreamReader(fis, "UTF-8");
			for (;;) {
				int rsz = in.read(buffer, 0, buffer.length);
				if (rsz < 0)
					break;
				out.append(buffer, 0, rsz);
			}
			String contents = out.toString();
			int index = contents.indexOf("~.");
			if (index >= 0) {
				countUsed = Integer.valueOf(contents.substring(index + 2));
			} else {
				countUsed = 0;
			}
			fis.close();
		} catch (Exception e) {

		}
		return countUsed;
	}

	private boolean getExistsVersionFile() {
		boolean retValue = false;
		File file = null;
		if (isSdPresent()) {
			file = new File("/sdcard/commuteralarm/version" + Home2.CURRENT_VERSION
					+ "a.txt");
			if (file.exists()) {
				retValue = true;
			}
		} else {
			file = new File("/data/data/" + mActivity.getPackageName()
					+ "/files/version" + Home2.CURRENT_VERSION + "a.txt");
			if (file.exists()) {
				retValue = true;
			}
		}
		return retValue;
	}

	public boolean isSdPresent() {
		String sdState = android.os.Environment.getExternalStorageState();
		return sdState.equals(android.os.Environment.MEDIA_MOUNTED);
	}

	public void stampVersion(int nbrOfAlertSets) {
		FileOutputStream fos = null;
		PrintWriter pw = null;
		try {
			fos = getVersionOutputStream();
			pw = new PrintWriter(fos);
			pw.write(Home2.CURRENT_VERSION);
			pw.write("~." + String.valueOf(nbrOfAlertSets));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				pw.close();
			} catch (Exception eee) {
			}
			try {
				fos.close();
			} catch (Exception eee) {
			}
		}
	}

	private FileOutputStream getVersionOutputStream()
			throws FileNotFoundException {
		FileOutputStream fileOutputStream_Version = null;
		File file = null;
		if (isSdPresent()) {
			file = new File("/sdcard/commuteralarm/version");
			if (!file.exists()) {
				file.mkdirs();
			}
			fileOutputStream_Version = new FileOutputStream(
					"/sdcard/commuteralarm/version" + Home2.CURRENT_VERSION + "a.txt",
					false);
		} else {
			fileOutputStream_Version = mActivity.openFileOutput("version"
					+ Home2.CURRENT_VERSION + "a.txt",
					Context.MODE_WORLD_READABLE);
		}
		return fileOutputStream_Version;
	}

	private FileInputStream getTrialCounterInputStream()
			throws FileNotFoundException {
		File file = null;
		if (isSdPresent()) {
			file = new File("/sdcard/commuteralarm/version");
			if (!file.exists()) {
				file.mkdirs();
			}
			return new FileInputStream("/sdcard/commuteralarm/version"
					+ Home2.CURRENT_VERSION + "a.txt");
		} else {
			return mActivity.openFileInput("version" + Home2.CURRENT_VERSION
					+ "a.txt");
		}
	}

	private LicenseCheckerCallback mLicenseCheckerCallback;
	private LicenseChecker mChecker;
	// Generate 20 random bytes, and put them here.
	private static final byte[] SALT = new byte[] { -46, 65, 30, -128, -113,
			-37, 74, -64, 51, 88, -95, -45, 77, -117, -36, -113, -21, 12, -64,
			19 };

	private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
		public void allow() {
			if (mActivity.isFinishing()) {
				// Don't update UI if Activity is finishing.
				return;
			}
			// Should allow user access.
			handleResultOfLicenseCheck(true);
		}

		public void dontAllow() {
			if (mActivity.isFinishing()) {
				// Don't update UI if Activity is finishing.
				return;
			}
			handleResultOfLicenseCheck(false);
		}

		@Override
		public void applicationError(int errorCode) {
			// Until we get it set up, assume it's licensed.
			if (errorCode==LicenseCheckerCallback.ERROR_NOT_MARKET_MANAGED) {
				handleResultOfLicenseCheck(false);
			}
		}

		@Override
		public void allow(int reason) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void dontAllow(int reason) {
			// TODO Auto-generated method stub
			
		}
	}

	private void handleResultOfLicenseCheck(Boolean allow) {
				isNonTrialVersionAndIsRegistered = allow;
				haveDoneRegistrationCheck = true;
	}
}
