package com.diamondsoftware.android.commuterhelper;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.diamondsoftware.android.commuterhelper.Home2.WarningAndInitialDialog;
import com.diamondsoftware.android.commuterhelper.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Address;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public abstract class AbstractActivityForMenu extends Activity {

	private static String INSTRUCTIONS_MESSAGE = "To select a location\n\n-- Long press the screen\n   at the desired location. \n\n              or\n\n-- Press the Search button.";
	protected SettingsManager mSettingsManager;
	protected abstract void refreshHelp();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSettingsManager=new SettingsManager(this);
	}	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(mSettingsManager.getHelpOverlayStateOn()) {
			getMenuInflater().inflate(R.menu.home2_help_on, menu);
		} else {
			getMenuInflater().inflate(R.menu.home2, menu);
		}
		refreshHelp();
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_settings:
				Intent i2 = new Intent(this, Preferences.class);
				startActivity(i2);
				return true;
			case R.id.action_rateapp:
				String uri = "market://details?id="
						+ getPackageName();
				Intent ii3 = new Intent(Intent.ACTION_VIEW,
						Uri.parse(uri));
				startActivity(ii3);	
				return true;
			case R.id.action_help:
				mSettingsManager.setHelpOverlayStateOn(!mSettingsManager.getHelpOverlayStateOn());
				invalidateOptionsMenu();
				return true;
			case R.id.action_contactus:
				String[] mailto = {getString(R.string.emailto),""};
				Intent sendIntent = new Intent(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_EMAIL, mailto);
				sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Message from Commuter Alert user"
						.toString());
				sendIntent.putExtra(Intent.EXTRA_TEXT, ""
						.toString());
				sendIntent.setType("text/plain");
				startActivity(Intent.createChooser(sendIntent, "Send email..."));
				return true;
			case R.id.action_about:
				int mTank=0;
				if(!mSettingsManager.getBoughtASubscription() && !mSettingsManager.getBoughtPermanentLicense()) {
					Integer mTankInteger=mSettingsManager.getMTank();
					if(mTankInteger!=null){
						mTank=mTankInteger.intValue();
					} else {
						int usages=new SecurityManager(this).getCountUserArmed();
						mTank=Home2.TRIAL_ALLOWANCE-usages;
					}
				}
				GregorianCalendar gc=new GregorianCalendar(Locale.getDefault());
				gc.setTime(mSettingsManager.getSubscriptionEnds());
				AboutDialog ab=new AboutDialog(mTank,mSettingsManager.getBoughtASubscription(), gc, mSettingsManager.getBoughtPermanentLicense());
				ab.show();
				return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}
	public class AboutDialog {
		int mNumberOfUsagesLeft;
		boolean mIsSubscription;
		boolean mIsPermanent;
		GregorianCalendar mWhenSubscriptionEnds;

		@SuppressWarnings("unused")
		private AboutDialog() {
			super();
		}

		public AboutDialog(int numberOfUsagesLeft, boolean isSubscription, GregorianCalendar whenSubscriptionEnds, boolean isPermanent ) {
			mNumberOfUsagesLeft=numberOfUsagesLeft;
			mIsSubscription=isSubscription;
			mIsPermanent=isPermanent;
			mWhenSubscriptionEnds=whenSubscriptionEnds;
		}

		public void show() {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					new ContextThemeWrapper(AbstractActivityForMenu.this,
							R.style.AlertDialogCustomLight));
			LayoutInflater inflater = AbstractActivityForMenu.this.getLayoutInflater();
			setTitle("About");

			// Inflate and set the layout for the dialog
			// Pass null as the parent view because its going in the dialog
			// layout
			final View view=inflater.inflate(R.layout.dialog_about, null);
			final TextView version = (TextView) view.findViewById(R.id.about_version);
			try {
				version.setText("Version "+AbstractActivityForMenu.this.getPackageManager().getPackageInfo(AbstractActivityForMenu.this.getPackageName(), 0).versionName);
			} catch (NameNotFoundException e) {
				version.setText("1.0");
			}
			final TextView notice = (TextView) view.findViewById(R.id.about_notice);
			if(mIsPermanent) {
				notice.setText("Thank you for purchasing CommuterAlert!");
			} else {
				if(mIsSubscription) {
			        SimpleDateFormat fmt = new SimpleDateFormat("MMMM-dd-yyyy");
			        String dateFormatted = fmt.format(mWhenSubscriptionEnds);
					notice.setText("Your subscription ends on" + dateFormatted);
				} else {
					notice.setText("Number of usages left: " + String.valueOf(mNumberOfUsagesLeft));
				}
			}

			builder.setView(view);
			builder.setPositiveButton("Okay",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							
						}
					});
			builder.setNegativeButton("Contact Us",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							String[] mailto = {getString(R.string.emailto),""};
							Intent sendIntent = new Intent(Intent.ACTION_SEND);
							sendIntent.putExtra(Intent.EXTRA_EMAIL, mailto);
							sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Message from Commuter Alert user"
									.toString());
							sendIntent.putExtra(Intent.EXTRA_TEXT, ""
									.toString());
							sendIntent.setType("text/plain");
							startActivity(Intent.createChooser(sendIntent, "Send email..."));
						}
				
					});
			builder.setNeutralButton("Rate App",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							String uri = "market://details?id="
									+ getPackageName();
							Intent ii3 = new Intent(Intent.ACTION_VIEW,
									Uri.parse(uri));
							startActivity(ii3);	
						}
					});

			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}

}
