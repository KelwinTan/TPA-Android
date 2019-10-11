package com.bluejack18_2.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MicrophoneDirection;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.bluejack18_2.R;
import com.bluejack18_2.activity.HomeActivity;
import com.bluejack18_2.activity.MainActivity;
import com.bluejack18_2.activity.ReportDetailActivity;
import com.bluejack18_2.model.Report;
import com.google.firebase.database.core.Repo;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public MyFirebaseMessagingService(){ }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if(remoteMessage.getData().get("title") != null) {
            String title = remoteMessage.getData().get("title");
            String message = remoteMessage.getData().get("message");
            String reportId = remoteMessage.getData().get("reportId");
            String imageUrl = remoteMessage.getData().get("imageUrl");
            String placeId = remoteMessage.getData().get("placeId");
            String reportDescription = remoteMessage.getData().get("reportDescription");
            String reportStatus = remoteMessage.getData().get("reportStatus");
            String reportTitle = remoteMessage.getData().get("reportTitle");
            String userId = remoteMessage.getData().get("userId");
            Report report = new Report(placeId, userId, imageUrl, reportTitle, reportDescription, reportStatus);
            report.setId(reportId);
            sendNotification(title, message, report);
        }
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    private void sendNotification(String title, String messageBody, Report report) {
        Intent intent = new Intent(this, ReportDetailActivity.class);
        intent.putExtra("My_Report", report);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        String channelId = "My_Channel_ID";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }

}
