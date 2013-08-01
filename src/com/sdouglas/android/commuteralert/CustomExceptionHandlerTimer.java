package com.sdouglas.android.commuteralert;

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;

public class CustomExceptionHandlerTimer implements UncaughtExceptionHandler {
	private UncaughtExceptionHandler mDefaultUEH;
	private Context mContext;
	public CustomExceptionHandlerTimer(Context c) {
		mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
		mContext=c;
	}
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		try {
			Logger logger=new Logger(0,"ExceptionHandler",mContext);
			logger.log(e.getMessage(),999);
			String str="";
			StackTraceElement[] stea=e.getStackTrace();
			for(int c=0;c<stea.length;c++) {
				StackTraceElement ste=stea[c];
				str=ste.toString();
			}
			logger.log(str,99);
		} catch (Exception ee) {}
		mDefaultUEH.uncaughtException(t, e);
	}

}
