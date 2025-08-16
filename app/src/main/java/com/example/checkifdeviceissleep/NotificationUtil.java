package com.example.checkifdeviceissleep;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;


/**
 * https://github.com/hilfritz/NotificationSample/
 * Created by hilfritz on 7/4/16.
 */
public class NotificationUtil {

    public static final String NOTIFICATION_ID = "notificationId";



    /**
     *
     * @param activity Activity
     * @param notificationIdTag int indicator id of notification, useful when getting a specific notification by id
     * @param title String
     * @param message String
     * @param notificationIconDrawable int ex. R.drawable.ic_launcher
     * @param pendingIntent PendingIntent
     */
    public static void createNotification(
            Activity activity,
            int notificationIdTag,
            String title,
            String message,
            int notificationIconDrawable,
            PendingIntent pendingIntent){

        //GET THE NOTIFICATION MANAGER
        NotificationManager notificationManager = (NotificationManager)activity.getSystemService(Activity.NOTIFICATION_SERVICE);

        // BUILD THE NOTIFICATION
        // Actions are just fake
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(activity)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(notificationIconDrawable)
                .setContentIntent(pendingIntent);
        notificationBuilder.setContentIntent(pendingIntent);
        Notification notification = notificationBuilder.build();
        // Hide the notification after its selected
        //noti.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |=Notification.DEFAULT_VIBRATE;
        notification.flags |=Notification.DEFAULT_SOUND;
        notification.flags |=Notification.DEFAULT_LIGHTS;
        notification.flags |=Notification.FLAG_AUTO_CANCEL;

        //SHOW THE NOTIFICATION
        //notificationManager.notify(notificationIdTag, notification);
    }

    /**
     *
     * @param activity Activity
     * @param notificationIdTag int indicator id of notification, useful when getting a specific notification by id
     */
    public static void removeNotification(Activity activity, int notificationIdTag){
        //GET THE NOTIFICATION MANAGER
        NotificationManager notificationManager = (NotificationManager)activity.getSystemService(Activity.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationIdTag);
    }
    public static PendingIntent getSamplePendingIntent2(Activity activity, Intent intent, int requestCode){
        //CREATE THE INTENT THAT WILL BE STARTED WHEN NOTIFICATION IS CLICKED
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pIntent = PendingIntent.getActivity(activity, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pIntent;
    }
    public static PendingIntent getSamplePendingIntent(Activity activity) {
        Intent intent = new Intent(activity, NotificationActivity.class);

        int flags = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                ? PendingIntent.FLAG_IMMUTABLE
                : 0;

        return PendingIntent.getActivity(activity, 0, intent, flags);
    }


}
