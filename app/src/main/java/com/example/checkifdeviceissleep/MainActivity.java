package com.example.checkifdeviceissleep;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import androidx.core.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;
/**
 * @see https://github.com/hilfritz/NotificationSample/
 * @see http://stackoverflow.com/questions/6391870/how-exactly-to-use-notification-builder
 * @author hilfritz
 *
 */
public class MainActivity extends Activity {
	public static final String TAG = "MainActivity";
	public static final String CHANNEL_ID = "my_channel_id"; // ✅ channel id
	int counter = 0;
	Handler handler;
	Timer timer;
	TimerTask checkTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate() ");
		setContentView(R.layout.activity_main);

		// ✅ Create the notification channel here
		createNotificationChannel();

		// Android 13+ needs runtime permission
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
					!= PackageManager.PERMISSION_GRANTED) {
				requestPermissions(new String[]{ Manifest.permission.POST_NOTIFICATIONS }, 1001);
			}
		}


		timer = new Timer();
		handler = new Handler();
		checkDeviceStatus();
	}

	// Create channel for Android O+
	private void createNotificationChannel2() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = "Default Channel";
			String description = "Channel for app notifications";
			int importance = NotificationManager.IMPORTANCE_HIGH;
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
			channel.setDescription(description);

			NotificationManager notificationManager = getSystemService(NotificationManager.class);
			notificationManager.createNotificationChannel(channel);
		}
	}
	private void createNotificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = "Default Channel";
			String description = "Channel for app notifications";
			int importance = NotificationManager.IMPORTANCE_HIGH; // This is good ✅

			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
			channel.setDescription(description);
			channel.enableLights(true);
			channel.enableVibration(true);
			channel.setVibrationPattern(new long[]{100, 200, 300, 400}); // optional

			NotificationManager notificationManager = getSystemService(NotificationManager.class);
			if (notificationManager != null) {
				notificationManager.createNotificationChannel(channel);
				Log.d(TAG, "Notification channel created: " + CHANNEL_ID);
			} else {
				Log.e(TAG, "Failed to get NotificationManager");
			}
		}
	}

	String str = "";
	private void checkDeviceStatus() {
		timer = new Timer();
		handler = new Handler();

		checkTask = new TimerTask() {
			public void run() {
				handler.post(() -> {
					str = "";
					if (isScreenOn())
						str += " screen is on";
					else
						str += " screen is off";
					if (isKeyguardLocked())
						str += " on lockscreen";
					else
						str += " not on lockscreen";
					Log.d(TAG, "checkDeviceStatus() " + str);
				});
			}
		};
		timer.schedule(checkTask, 500, 5000);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		timer.cancel();
		checkTask.cancel();
		Log.d(TAG, "onDestroy()");
	}

	public void createNotificationOnClick(View v) {
		Log.d(TAG, "createNotificationOnClick()");
		if (counter < 0) {
			counter = 0;
		}
		createNotification("title:" + counter, "description:" + counter, counter, R.drawable.ic_launcher);
		counter++;
	}

	public void removeNotificationOnClick(View v) {
		Log.d(TAG, "removeNotificationOnClick()");
		counter--;
		removeNotification(counter);
	}

	private void createNotification(String title, String description, int notificationId, int smallIcon) {
		Log.d(TAG, "createNotification() creating notification [notificationId:" + notificationId + "]");
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// Intent for click
		Intent intent = new Intent(this, NotificationActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.putExtra(NotificationActivity.EXTRA_KEY1, "today is the day (" + notificationId + ")");
		intent.putExtra(NotificationUtil.NOTIFICATION_ID, notificationId);

		int flags = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
				? PendingIntent.FLAG_IMMUTABLE
				: 0;

		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);

		// ✅ Builder with channel ID
		NotificationCompat.Builder notificationBuilder =
				new NotificationCompat.Builder(this, CHANNEL_ID)
						.setContentTitle(title)
						.setContentText(description)
						.setSmallIcon(smallIcon)
						.setContentIntent(pendingIntent)
						.setAutoCancel(true)
						.setPriority(NotificationCompat.PRIORITY_HIGH)
						.setDefaults(Notification.DEFAULT_ALL);

		Notification notification = notificationBuilder.build();
		notificationManager.notify(notificationId, notification);

		// Verify active
		if (isNotificationActive(notificationId)) {
			Log.d(TAG, "✅ Notification " + notificationId + " is active.");
		} else {
			Log.d(TAG, "❌ Notification " + notificationId + " not found.");
		}
	}

	public void removeNotification(int notificationId) {
		Log.d(TAG, "removeNotification()");
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(notificationId);
	}

	private boolean isNotificationActive(int notificationId) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			StatusBarNotification[] notifications = manager.getActiveNotifications();
			for (StatusBarNotification sbn : notifications) {
				if (sbn.getId() == notificationId) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isScreenOn() {
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
			return pm.isInteractive();
		} else {
			return pm.isScreenOn();
		}
	}

	private boolean isKeyguardLocked() {
		KeyguardManager kgMgr = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		return kgMgr.inKeyguardRestrictedInputMode();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1001) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				Log.d(TAG, "POST_NOTIFICATIONS permission granted ✅");
			} else {
				Log.w(TAG, "POST_NOTIFICATIONS permission denied ❌ — notifications won’t show");
			}
		}
	}
}
