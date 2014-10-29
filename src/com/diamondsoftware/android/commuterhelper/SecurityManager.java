package com.diamondsoftware.android.commuterhelper;

import com.diamondsoftware.android.commuterhelper.R;

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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

public class SecurityManager {
	Activity mActivity;
	public static final int START_TRIAL_WARNINGS = 5;
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

	boolean isUnregisteredLiveVersion() {
		if (haveDoneRegistrationCheck && !isNonTrialVersionAndIsRegistered) {
			return true;
		}
		return false;
	}

	boolean isTrialVersion() {
		boolean isTrial= 
				Home2.mIsPremium!=null &&
				Home2.mSubscribedToInfiniteGas!=null &&
				(Home2.mTank==null || Home2.mTank.intValue()==0) &&
				!Home2.mIsPremium.booleanValue() &&
				!Home2.mSubscribedToInfiniteGas.booleanValue();
		return isTrial;
	}

	boolean hasExceededTrials() {
		if(Home2.mTank == null) {
			return getCountUserArmed() > Home2.TRIAL_ALLOWANCE;
		} else {
			return Home2.mTank.intValue()==0;
		}
	}

	boolean startWarnings() {
		if(Home2.mTank == null) {
			return getCountUserArmed() > START_TRIAL_WARNINGS;
		} else {
			return false;
		}
	}

	void incrementTrials() {
		if(Home2.mTank==null) {
			stampVersion(getCountUserArmed() + 1);
		} else {
			Home2.mTank--;
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
			contents=decrypticate(contents);
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

	private static String decrypticate(String string) {
		StringBuffer sb=new StringBuffer();
		int length=(string.length());
		for (int c=0;c<length;c++) {
			char ch=string.charAt(c);
			ch+=c;
			sb.append(ch);
		}
		return sb.toString();
	}
	private static String encrypticate(String string) {
		StringBuffer sb=new StringBuffer();
		int length=(string.length());
		for (int c=0;c<length;c++) {
			char ch=string.charAt(c);
			ch-=c;
			sb.append(ch);
		}
		return sb.toString();
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
			String toWrite=Home2.CURRENT_VERSION+"~."+String.valueOf(nbrOfAlertSets);
			pw.write(encrypticate(toWrite));
/*			pw.write(Home2.CURRENT_VERSION);
			pw.write("~." + String.valueOf(nbrOfAlertSets));
*/
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
