//
//	Copyright (c) 2012 lenik terenin
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//		http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.

package com.lazydroid.autoupdateapk;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Button;

import static com.lazydroid.autoupdateapk.AutoUpdateApk.AUTOUPDATE_HAVE_UPDATE;
import static com.lazydroid.autoupdateapk.AutoUpdateApk.UPDATE_FILE;
import static com.lazydroid.autoupdateapk.AutoUpdateApk.preferences;
import static com.lazydroid.autoupdateapk.AutoUpdateApk.showUpdateAsPopup;

public class AutoUpdateApkActivity extends Activity implements Observer {

	// declare updater class member here (or in the Application)
	@SuppressWarnings("unused")
	private AutoUpdateApk aua;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		aua = new AutoUpdateApk(getApplicationContext());	// <-- don't forget to instantiate

		aua.addObserver(this);	// see the remark below, next to update() method

		Button notificationExample = findViewById(R.id.notification_example);
		notificationExample.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				raiseExampleNotification();
			}
		});
	}

	// you only need to use this method and specify "implements Observer" and use "addObserver()"
	// in case you want to closely monitor what's the AutoUpdateApk is doing, otherwise just ignore
	// "implements Observer" and "addObserver()" and skip implementing this method.
	//
	// There are three kinds of update messages sent from AutoUpdateApk (more may be added later):
	// AUTOUPDATE_CHECKING, AUTOUPDATE_NO_UPDATE and AUTOUPDATE_GOT_UPDATE, which denote the start
	// of update checking process, and two possible outcomes.
	//
	@Override
	public void update(Observable observable, Object data) {
		if( ((String)data).equalsIgnoreCase(AutoUpdateApk.AUTOUPDATE_GOT_UPDATE) ) {
			android.util.Log.i("AutoUpdateApkActivity", "Have just received update!");
		}
		if( ((String)data).equalsIgnoreCase(AUTOUPDATE_HAVE_UPDATE) ) {
			android.util.Log.i("AutoUpdateApkActivity", "There's an update available!");
		}
	}

	private void raiseExampleNotification() {
		ApplicationInfo appinfo = this.getApplicationInfo();
		String appName = this.getString(appinfo.labelRes);
		int appIcon = appinfo.icon;
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager nm = (NotificationManager) this.getSystemService(ns);

		// raise the notification
		CharSequence contentTitle = appName + " update available";
		CharSequence contentText = "Select to install";

		// Bugfix for Android 7 (Nougat)
		// Only Android 7's PackageManager can install from FileProvider content://
		// http://stackoverflow.com/a/39333203/2378095
		Intent notificationIntent;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			notificationIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
			notificationIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		} else {
			notificationIntent = new Intent(Intent.ACTION_VIEW);
		}

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		if (showUpdateAsPopup) {
			try {
				contentIntent.send();
			} catch (PendingIntent.CanceledException e) {
				e.printStackTrace();
			}
		} else {
			NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
			builder.setSmallIcon(appIcon);
			builder.setTicker(appName + " update");
			builder.setContentTitle(contentTitle);
			builder.setContentText(contentText);
			builder.setContentIntent(contentIntent);
			builder.setWhen(System.currentTimeMillis());
			builder.setAutoCancel(true);
			builder.setOngoing(true);

			nm.notify(0xBEEF, builder.build());
		}
	}

}
