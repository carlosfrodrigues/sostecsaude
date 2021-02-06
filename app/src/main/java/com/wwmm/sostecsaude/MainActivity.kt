package com.wwmm.sostecsaude

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val id = getString(R.string.notification_avisos_id)

            val name = getString(R.string.notification_avisos_name)

            val descriptionText = getString(R.string.notification_avisos_description)

            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(id, name, importance).apply {
                description = descriptionText
            }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(LOGTAG, "getInstanceId failed", task.exception)

                    return@OnCompleteListener
                }

                val token = task.result?.token

                if (token != null) {
                    val prefs = PreferenceManager
                        .getDefaultSharedPreferences(this)

                    val editor = prefs.edit()

                    editor.putString("FBtoken", token)

                    editor.apply()
                }
            })
    }

    companion object {
        const val LOGTAG = "MainActivity"
    }
}
