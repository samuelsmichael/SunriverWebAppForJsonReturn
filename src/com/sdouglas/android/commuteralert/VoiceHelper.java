package com.sdouglas.android.commuteralert;

import java.util.HashMap;
import java.util.Stack;

import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
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
	private Stack<LinearLayout> _notificationPopups=new Stack<LinearLayout>();
	public static final String PREFS_NAME = "com.sdouglas.android.commuteralert_preferences";
	private SharedPreferences mSharedPreferences; 
    private static String ALERT_TEXT="Alert! Alert! You are arriving at your destination.";
	private WakeLock screenLock=null;
	private Boolean closeRequestedSoStopLooping=false;
	private MediaPlayer mMediaPlayer=null;
	private int mOriginalVolumn=-100;
	private AudioManager mAudioManager=null;
	
	/*
	 * When we're all done (after the TextToSpeech object informs me that its done speaking), then I can
	 * proceed to close things up.  It's important that we do so.  We don't want to close, though, if there's
	 * a popup window being shown too.  The whole point of the popup window is that the user sees it on his screen.
	 */
	private void doneCode() {
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
			  if (mMediaPlayer != null){
				  mMediaPlayer.release();
			  }
			} catch(Exception ee) {
				
			}
		mMediaPlayer=null;
		if(mAudioManager!=null) {
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mOriginalVolumn, 0);
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
		return mSharedPreferences.getBoolean("voice", true);
	}
	private Boolean wereDoingPopupNotifications() {
		return true;
	}
	private Boolean wereDoingMusic() {
		return mSharedPreferences.getBoolean("sound", true);
	}
	private Boolean wereVibrating() {
		return mSharedPreferences.getBoolean("vibrate", true);
	}
	
	
	private String getVoiceAndPopupText() {
		return mSharedPreferences.getString("voicetext", ALERT_TEXT);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Commuter Alert");
		mSharedPreferences=getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		_theText=getVoiceAndPopupText();
		_countDoing=0;
		/* This makes it happen even if the system is sleeping or locked */
		screenLock = ((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(
			     PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
			screenLock.acquire();
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		
		if(wereDoingMusic()) {
			playMusic();
		}
		
		//getLogger().log("VoiceHelper: 1 ... in onCreate",100);
		if(wereDoingVoiceNotifications()) {
			_theText=getVoiceAndPopupText();
			if(mTts!=null && _imInited) {
				//getLogger().log("VoiceHelper: 1a ... mTts!=null && _imInited",100);
				speak();
			} else {
		//		getLogger().log("VoiceHelper: 1b ... mTts==null || !_imInited. _theTExt="+_theText,100);
				Intent checkIntent = new Intent();
				checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
				startActivityForResult(checkIntent,MY_DATA_CHECK_CODE);
			}
		} else {
			/*
			 * If I'm doing text-to-speech, then I need to wait until the mTts is initialized before
			 * popping up the window; otherwise, the dialog that may need to come up which asks 
			 * which provider ... that dialog doesn't shows (it's hidden).
			 */
			doPopupNotifications();
		}
	}
	private void playMusic() {
	    new Thread(new Runnable() {
	        public void run() {
				getMediaPlayer().start(); // no need to call prepare(); create() does that for you			
				mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				if(mOriginalVolumn!=-100) {
					mOriginalVolumn=mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
				}
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
/*				int result = mAudioManager.requestAudioFocus(new OnAudioFocusChangeListener() {

					@Override
					public void onAudioFocusChange(int focusChange) {
					    switch (focusChange) {
				        case AudioManager.AUDIOFOCUS_GAIN:
				        	if(!(getMediaPlayer().isPlaying())) mMediaPlayer.start();
				            mMediaPlayer.setVolume(1.0f, 1.0f);
				            break;
				        case AudioManager.AUDIOFOCUS_LOSS:
				            // Lost focus for an unbounded amount of time: stop playback and release media player
				        	
				            if (getMediaPlayer().isPlaying()) mMediaPlayer.stop();
				            mMediaPlayer.release();
				            mMediaPlayer=null;
				            break;

				        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
				            // Lost focus for a short time, but we have to stop
				            // playback. We don't release the media player because playback
				            // is likely to resume
				            if (getMediaPlayer().isPlaying()) mMediaPlayer.pause();
				            break;

				        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				            // Lost focus for a short time, but it's ok to keep playing
				            // at an attenuated level
				            if (getMediaPlayer().isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
				            break;
					    }						
					}
					
				}, AudioManager.STREAM_MUSIC,
				    AudioManager.AUDIOFOCUS_GAIN);
				    */
	        }	        
	    }).start();		
	    
	}
	private MediaPlayer getMediaPlayer() {
		if(mMediaPlayer==null) {
			mMediaPlayer = MediaPlayer.create(VoiceHelper.this,R.raw.rossini_william_tell);
			mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
			mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					if(!closeRequestedSoStopLooping) {
						mMediaPlayer.release();
						playMusic();
					}
				}
				
			});
		}
		return mMediaPlayer;
	}
	private synchronized void speak() {
	//	getLogger().log("VoiceHelper: 4 ... speak("+words+")",100);
		String words=_theText;
		HashMap<String,String> hm = new HashMap<String,String>();
		hm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, NOTIFICATIONUTERENCE);
		mTts.speak(words, TextToSpeech.QUEUE_ADD, hm);
		_countDoing++;
//		getLogger().log("VoiceHelper: 5 ... end speak(...) _countDoing="+String.valueOf(_countDoing),100);
	}
	private synchronized void doneSpeaking() {
		if(_countDoing>0) {
			_countDoing--;
		}

		if(!closeRequestedSoStopLooping) {
			speak();
		} else {
		
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
	}
	private void doPopupNotifications() {
		LinearLayout notificationPopup=null;
		
		LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		notificationPopup = (LinearLayout)vi.inflate(R.layout.notificationpopup, null);
		
		TextView needDescription = (TextView) notificationPopup.findViewById(R.id.needDescription);
		needDescription.setText(_theText);
		Button closeMe=(Button)notificationPopup.findViewById(R.id.closeMe);
		if(mWindowManager==null) {
		    mWindowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
		}
		closeMe.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				removeView();
				closeRequestedSoStopLooping=true;
			//	if(_countDoing<=0) { // Don't close the whole window if we're still in the middle of speaking  (why not?)
					finish();
					
					if(mMediaPlayer!=null) {
						mMediaPlayer.stop();
					}
			//	}
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
				if(wereDoingPopupNotifications()) {
					doPopupNotifications();
				}
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
								speak();
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
