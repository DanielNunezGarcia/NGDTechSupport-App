package com.example.ngdtechsupport.ui.notifications

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {

        super.onNewToken(token)

        Log.d("FCM_TOKEN", token)

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title
        val body = remoteMessage.notification?.body

        showNotification(title, body)
    }

    private fun showNotification(title: String?, body: String?) {

        val builder = NotificationCompat.Builder(this, "updates_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.notify(1, builder.build())
    }
}