package com.sdouglas.android.commuteralert;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.CharBuffer;

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
		if(!getExistsVersionFile()) {
			stampVersion(0);
			retValue=false;
		} else {
			retValue=true;
		}
		return retValue;
	}
	public int getCountUserArmed() {
		int countUsed=0;
		FileInputStream fis=null;
		final char[] buffer = new char[10];
		final StringBuilder out = new StringBuilder();		
		try {
			fis=getTrialCounterInputStream();
			final Reader in = new InputStreamReader(fis, "UTF-8");
		    for (;;) {
		    	int rsz = in.read(buffer, 0, buffer.length);
		        if (rsz < 0)
		          break;
		        out.append(buffer, 0, rsz);
		    }
		    String contents=out.toString(); 
		    int index=contents.indexOf("~.");
		    if(index>=0) {
		    	countUsed=Integer.valueOf(contents.substring(index+2));
		    } else {
		    	countUsed=0;
		    }
		    fis.close();
		} catch (Exception e) {
			
		}
		return countUsed;
	}

	private boolean getExistsVersionFile() {
		boolean retValue=false;
		File file = null;
		if (isSdPresent()) {
			file = new File("/sdcard/douglas/version"+Home2.CURRENT_VERSION+"a.txt");
			if (file.exists()) {
				retValue=true;
			}
		} else {
			file = new File("/data/data/"+mActivity.getPackageName()+"/files/version"+Home2.CURRENT_VERSION+"a.txt");
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
	public void stampVersion(int nbrOfAlertSets) {
		FileOutputStream fos = null;
		PrintWriter pw = null;
		try {
			fos = getVersionOutputStream();
			pw = new PrintWriter(fos);
			pw.write(Home2.CURRENT_VERSION);
			pw.write("~."+String.valueOf(nbrOfAlertSets));
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
	private FileInputStream getTrialCounterInputStream() throws FileNotFoundException {
		File file=null;
		if(isSdPresent()) {
			file=new File("/sdcard/douglas/version");
			if (!file.exists()) {
				file.mkdirs();
			}
			return new FileInputStream("/sdcard/douglas/version"+Home2.CURRENT_VERSION+"a.txt");
		} else {
			return mActivity.openFileInput("version"+Home2.CURRENT_VERSION+"a.txt");
		}
	}	
}
