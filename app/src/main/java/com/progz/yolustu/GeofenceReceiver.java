package com.progz.yolustu;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceReceiver extends BroadcastReceiver {

    private static final String TAG = "GeofenceReceiver";
    private static final String CHANNEL_ID = "yolustu_geofence_channel";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent == null || geofencingEvent.hasError()) {
            Log.e(TAG, "Geofencing Error: " + (geofencingEvent != null ? geofencingEvent.getErrorCode() : "null event"));
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            // Get the triggered geofence ID (e.g. "BİM_0", "Şok_1")
            List<Geofence> triggeredGeofences = geofencingEvent.getTriggeringGeofences();
            if (triggeredGeofences == null || triggeredGeofences.isEmpty()) return;

            String rawId = triggeredGeofences.get(0).getRequestId();
            // Strip the "_N" branch index suffix (e.g. "BİM_0" → "BİM")
            String marketName = rawId.contains("_") ? rawId.substring(0, rawId.lastIndexOf('_')) : rawId;
            Log.d(TAG, "User entered: " + marketName + " (geofence: " + rawId + ")");

            // Load shopping items from Room DB on a background thread
            new Thread(() -> {
                ItemDatabase db = ItemDatabase.getInstance(context);
                List<ShoppingItem> allItems = db.shoppingItemDao().getAllItems();

                // Filter for this market's pending (not yet completed) items
                StringBuilder itemList = new StringBuilder();
                int count = 0;
                for (ShoppingItem item : allItems) {
                    if (!item.isCompleted() && item.getMarket().equalsIgnoreCase(marketName)) {
                        if (count > 0) itemList.append(", ");
                        itemList.append(item.getName());
                        count++;
                    }
                }

                String notificationBody;
                if (count > 0) {
                    notificationBody = "Alınacaklar: " + itemList.toString();
                } else {
                    notificationBody = "Alışveriş listenizi kontrol etmeyi unutmayın!";
                }

                sendNotification(context, marketName, notificationBody);
                Log.d(TAG, "Notification sent for " + marketName + " — " + notificationBody);
            }).start();
        }
    }

    private void sendNotification(Context context, String marketName, String body) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel (required for Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Yol Üstü Hatırlatıcı",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Yakınındaki market hatırlatmaları");
            notificationManager.createNotificationChannel(channel);
        }

        // Tap notification → open app
        Intent openAppIntent = new Intent(context, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("📍 " + marketName + " yakınındasınız!")
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
