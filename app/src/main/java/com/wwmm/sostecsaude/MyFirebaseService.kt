package com.wwmm.sostecsaude

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*


class MyFirebaseService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        val editor = prefs.edit()

        editor.putString("FBtoken", token)

        editor.apply()

        Log.d(LOGTAG, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["Title"]
        val body = remoteMessage.data["Body"]
        val group = remoteMessage.data["Group"]

        if (title != null && body != null && group != null) {
            showNotification(title, body, group)
        }
    }

    private fun showNotification(title: String, body: String, group: String) {
        val manager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as
                NotificationManager

        val channelId = getString(R.string.notification_avisos_id)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0,
            intent, 0
        )

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(
            applicationContext,
            channelId
        )
            .setContentTitle(title)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setGroup(group)

        // 1586792321 is an arbitrary high number of seconds just to make the id small

        val notificationID = (Date().time / 1000L) - 1586792321

        println(notificationID)

        manager.notify(notificationID.toInt(), builder.build())
    }

    companion object {
        const val LOGTAG = "MyFirebaseService"
    }
}