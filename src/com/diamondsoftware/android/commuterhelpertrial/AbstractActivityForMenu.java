package com.diamondsoftware.android.commuterhelpertrial;

import com.diamondsoftware.android.commuterhelpertrial.Home2.WarningAndInitialDialog;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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
				sendIntent.putExtra(Intent.EXTRA_SUBJECT, ""
						.toString());
				sendIntent.putExtra(Intent.EXTRA_TEXT, ""
						.toString());
				sendIntent.setType("text/plain");
				startActivity(Intent.createChooser(sendIntent, "Send email..."));
				return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}
}
