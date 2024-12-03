package com.example.facebook.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.example.facebook.MainActivity
import com.example.facebook.R
import com.example.facebook.ui.FacebookScreen
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


const val NOTIFICATION_ID = 1
const val KEY_TEXT_REPLY = "key_text_reply"
const val CHANNEL_ID = "fcm_default_channel"

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class FirebaseService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        when (val type = remoteMessage.data["type"]) {
            "new_message" -> {
                showNewMessageNotification(
                    remoteMessage.data["title"] ?: "No Title",
                    remoteMessage.data["body"] ?: "No Body",
                    remoteMessage.data["chatgroup"] ?: ""
                )
            }

            else -> {
                Log.d("FirebaseService", "Unknown message type: $type")
            }
        }

    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FirebaseService", "Refreshed token: $token")
    }


    @SuppressLint("MissingPermission")
    private fun showNewMessageNotification(title: String, message: String, chatgroup: String) {
        Log.d("FirebaseService", "Showing notification for chatgroup: $chatgroup")

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        Log.d("FirebaseService", locationManager.toString())
        Log.d("FirebaseService", locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER).toString())
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d("FirebaseService", "Requesting location updates")
            val locationService = LocationService(this, "Tin nhắn mới từ $title: $message")
            locationService.requestSingleLocationUpdate()
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            "FCM Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "FCM Notification Channel"
        }
        notificationManager.createNotificationChannel(channel)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            Log.d("com.example.mobile.AlarmReceiver", "No permission to post notifications")
            return
        }


        val remoteInput: RemoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).run {
            setLabel("Enter your reply here")
            build()
        }

        val replyAction: NotificationCompat.Action = NotificationCompat.Action.Builder(
            R.drawable.ic_launcher_foreground,
            "Reply",
            PendingIntent.getBroadcast(
                this,
                0,
                Intent(this, NotificationReceiver::class.java).apply {
                    action = "REPLY"
                    putExtra("chatgroup", chatgroup)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        ).addRemoteInput(remoteInput).build()

        val likeAction: NotificationCompat.Action = NotificationCompat.Action.Builder(
            R.drawable.ic_launcher_foreground,
            "Like",
            PendingIntent.getBroadcast(
                this,
                0,
                Intent(this, NotificationReceiver::class.java).apply {
                    action = "LIKE"
                    putExtra("chatgroup", chatgroup)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        ).build()


        val listenAction: NotificationCompat.Action = NotificationCompat.Action.Builder(
            R.drawable.ic_launcher_foreground,
            "Listen",
            PendingIntent.getBroadcast(
                this,
                0,
                Intent(this, NotificationReceiver::class.java).apply {
                    action = "LISTEN"
                    putExtra("content", "$title $message")
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        ).build()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("startDestination", "${FacebookScreen.CHAT_GROUP.name}/$chatgroup")
        }

        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )


        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(likeAction)
            .addAction(replyAction)
            .addAction(listenAction)

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }
}
