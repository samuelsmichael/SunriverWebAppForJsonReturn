package com.sdouglas.android.commuteralert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import android.app.Activity;
import android.content.Context;

public class SecurityManager {
	Activity mActivity;
	public SecurityManager(Activity activity) {
		mActivity=activity;
	}
	/*
	 * return true if file existed beforehand
	 */
	public boolean initializeVersion() {
		boolean retValue=false;
		if(!getVersionFile()) {
			stampVersion();
			retValue=false;
		} else {
			retValue=true;
		}
		return retValue;
	}
	private boolean getVersionFile() {
		boolean retValue=false;
		File file = null;
		if (isSdPresent()) {
			file = new File("/sdcard/douglas/version"+Home2.CURRENT_VERSION+"a.txt");
			if (file.exists()) {
				retValue=true;
			}
		} else {
			/*TRIAL_VS_REAL*/
			file = new File("/data/data/com.sdouglas.android.commuteralert/files/version"+Home2.CURRENT_VERSION+"a.txt");
			if (file.exists()) {
				retValue=true;
			}
		}
		return retValue;
	}
	public boolean isSdPresent() {
		String sdState=android.os.Environment.getExternalStorageState();
		return sdState.equals(
				android.os.Environment.MEDIA_MOUNTED) ;
	}	
	private void stampVersion() {
		FileOutputStream fos = null;
		PrintWriter pw = null;
		try {
			fos = getVersionOutputStream();
			pw = new PrintWriter(fos);
			pw.write(Home2.CURRENT_VERSION + "\n");
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
	private FileOutputStream getVersionOutputStream() throws FileNotFoundException {
		FileOutputStream fileOutputStream_Version = null;
		File file = null;
		if (isSdPresent()) {
			file = new File("/sdcard/douglas/version");
			if (!file.exists()) {
				file.mkdirs();
			}
			fileOutputStream_Version = new FileOutputStream("/sdcard/douglas/version"+Home2.CURRENT_VERSION+"a.txt",
					false);
		} else {
			fileOutputStream_Version = mActivity.openFileOutput("version"+Home2.CURRENT_VERSION+"a.txt",
					Context.MODE_WORLD_READABLE);
		}
		return fileOutputStream_Version;
	}	
}
