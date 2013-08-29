package com.sdouglas.android.commuteralert;

import java.util.HashMap;
import java.util.Stack;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/*
 * This is a utility class whose sole purpose is to be able to create a voice alert ... no matter what state the phone's in,
 * except powered off.
 * In order to do this, I tried many techniques.  You have to make this an Activity in order for it to work, which is why it
 * appears whenever the voice alert happens.  Then it goes away.
 * 
 *   Note that there is also code here for popping up a dialog box, too; but this isn't used in this application.  
 */

public class VoiceHelper extends Activity {
	private static final int MY_DATA_CHECK_CODE = 12229000;
	private static final String NOTIFICATIONUTERENCE = "322220001";
	private String _theText;
	private int _countDoing=0;
	private Boolean _imInited=false;
	private TextToSpeech mTts=null;
	private static WindowManager mWindowManager;
	private CountDownTimer _mCountDownTimer=null;
	private Stack<LinearLayout> _notificationPopups=new Stack<LinearLayout>();
	public static final String PREFS_NAME = "com.sdouglas.android.commuteralert_preferences";
	private SharedPreferences mSharedPreferences; 
    private static String ALERT_TEXT="Alert! Alert! You are arriving at your destination.";
	private WakeLock screenLock=null;
	
	/*
	 * When we're all done (after the TextToSpeech object informs me that its done speaking), then I can
	 * proceed to close things up.  It's important that we do so.  We don't want to close, though, if there's
	 * a popup window being shown too.  The whole point of the popup window is that the user sees it on his screen.
	 */
	private void doneCode() {
		try {
			if (_mCountDownTimer!= null) {
				_mCountDownTimer.cancel();
				_mCountDownTimer=null;
			}
		} catch (Exception ee33) {}
		if(mTts!=null) {
			try {
				mTts.stop();
				//getLogger().log("VoiceHelper: 10 ... done mTts.stop",100);
				mTts.shutdown();
				//getLogger().log("VoiceHelper: 11 ... done mTts.shutdown",100);
				mTts=null;
				
			} catch (Exception eeee) {}
		}		
		try {
			int jdcount=0;
			//getLogger().log("VoiceHelper: 12a (finish) ... _notificationPopups.count: " + String.valueOf(_notificationPopups.size()),100);
			while(!_notificationPopups.empty()) {
				removeView();
				jdcount++;
				if(jdcount>10) {
					break;
				}
			}
			mWindowManager=null;
		} catch(Exception e9) {}
		try {
			if (screenLock!=null) {
				screenLock.release();
				screenLock=null;
			}
		} catch (Exception e3) {}		
		
	}
	
	@Override
	public void finish() {
		//getLogger().log("VoiceHelper: 9 ... finish()",100);
		doneCode();
		super.finish();
	}

	@Override 
	public void onDestroy() {
		doneCode();
		super.onDestroy();
	}
	
	private Boolean wereDoingVoiceNotifications() {
		return mSharedPreferences.getString("voice", "y").equals("y");
	}
	private Boolean wereDoingPopupNotifications() {
		return true;
	}
	
	
	private String getVoiceAndPopupText() {
		return mSharedPreferences.getString("voicetext", ALERT_TEXT);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Commuter Alert");
		mSharedPreferences=getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		_countDoing=0;
		/* This makes it happen even if the system is sleeping or locked */
		screenLock = ((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(
			     PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
			screenLock.acquire();
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		
		
		if(wereDoingPopupNotifications()) {
			LinearLayout notificationPopup=null;
			
			LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			notificationPopup = (LinearLayout)vi.inflate(R.layout.notificationpopup, null);
			
			TextView needDescription = (TextView) notificationPopup.findViewById(R.id.needDescription);
			needDescription.setText(getVoiceAndPopupText());
			Button closeMe=(Button)notificationPopup.findViewById(R.id.closeMe);
			if(mWindowManager==null) {
			    mWindowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
			}
			closeMe.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					removeView();
					if(_countDoing<=0) { // Don't close the whole window if we're still in the middle of speaking
						finish();
					}
				}
			});
			/*
			 * This is how you popup a dialog even if you're not the window at the front
			 */
		    WindowManager.LayoutParams lp=new WindowManager.LayoutParams(
					WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, 10, 10,
					WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY | WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
	//				WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG,
	//				WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,0
					WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
						| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,PixelFormat.OPAQUE);
	///				    lp.token=notificationPopup.getWindowToken();
		    mWindowManager.addView(notificationPopup, lp);		
		    _notificationPopups.push(notificationPopup);
		}
		//getLogger().log("VoiceHelper: 1 ... in onCreate",100);
		if(wereDoingVoiceNotifications()) {
			if(mTts!=null && _imInited) {
				//getLogger().log("VoiceHelper: 1a ... mTts!=null && _imInited",100);
				speak(getVoiceAndPopupText());
			} else {
				_theText=getVoiceAndPopupText();
		//		getLogger().log("VoiceHelper: 1b ... mTts==null || !_imInited. _theTExt="+_theText,100);
				Intent checkIntent = new Intent();
				checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
				startActivityForResult(checkIntent,MY_DATA_CHECK_CODE);
			}
		}
	}
	private synchronized void speak(String words) {
	//	getLogger().log("VoiceHelper: 4 ... speak("+words+")",100);
		_mCountDownTimer=new CountDownTimer(20000, 1000) { // ya got 30 seconds to get all this done
		     public void onTick(long millisUntilFinished) {}
		     public void onFinish() {
		    	 _mCountDownTimer=null;
		    	 doneSpeaking();
		     }
	    }.start();
		HashMap<String,String> hm = new HashMap<String,String>();
		hm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, NOTIFICATIONUTERENCE);
		mTts.speak(words, TextToSpeech.QUEUE_ADD, hm);
		_countDoing++;
//		getLogger().log("VoiceHelper: 5 ... end speak(...) _countDoing="+String.valueOf(_countDoing),100);
	}
	private synchronized void doneSpeaking() {
		try {
			if (_mCountDownTimer!= null) {
				_mCountDownTimer.cancel();
				_mCountDownTimer=null;
			}
		} catch (Exception ee33) {}
		if(_countDoing>0) {
			_countDoing--;
		}

//		getLogger().log("VoiceHelper: 7 ... doneSpeaking. Now _countDoing="+String.valueOf(_countDoing),100);
		
		if(_countDoing<=0) {
			try {
				mTts.stop();
				//getLogger().log("VoiceHelper: 10 ... done mTts.stop",100);
				mTts.shutdown();
				//getLogger().log("VoiceHelper: 11 ... done mTts.shutdown",100);
				mTts=null;
				
			} catch (Exception eeee) {}
			if(!wereDoingPopupNotifications()||_notificationPopups.empty()) {
				finish();
			}
		}
	}
	private synchronized void removeView() {
		if(!_notificationPopups.empty()) {
			try {
				VoiceHelper.mWindowManager.removeView(_notificationPopups.pop());
			} catch (Exception ee3) {}
		}
	//	getLogger().log("VoiceHelper: 8 ... end of removeView()",100);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode==MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
					//getLogger().log("VoiceHelper: 2 ... CHECK_VOICE_DATA_PASS",100);
					// success, create the TTS instance
					mTts = new TextToSpeech(this.getApplicationContext(), new OnInitListener(){
						@Override
						public void onInit(int arg0) {
							if(arg0==TextToSpeech.SUCCESS) {
								try {
									//getLogger().log("VoiceHelper: 3 ... onInit TextToSpeech.SUCCESS",100);
									_imInited=true;
									mTts.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
										@Override
										public void onUtteranceCompleted(String utteranceId) {
											//getLogger().log("VoiceHelper: 6 ... onUtteranceComplete()",100);
											doneSpeaking();
										}
									});
									speak(VoiceHelper.this._theText);
								} catch (Exception ee) {
									//getLogger().log("VoiceHelper: 3b ... onInit NOT TextToSpeech.SUCCESS",100);
									removeView();
									VoiceHelper.this.finish();
								}
							}
						}						
					});
			} else {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent.setAction(
				TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
				// set mSingleton back to null so we can start the whole process next time.
				if(!wereDoingPopupNotifications()||_notificationPopups.empty()) {
					finish();
				}
			}
		}		
		super.onActivityResult(requestCode, resultCode, data);
	}
}
