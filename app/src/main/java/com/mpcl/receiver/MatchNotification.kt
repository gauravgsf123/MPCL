package com.mpcl.receiver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.mpcl.R.drawable


class MatchNotification: BroadcastReceiver() {
    var NOTIFICATION_ID = "notification-id"
    var NOTIFICATION_CHANNEL_ID = "10001";

    private lateinit var player: MediaPlayer;
    private lateinit var context: Context;

    // Construct the notification to push to the user given the teams in the match
    private fun getNotification(
        content: String
    ): Notification? {
        val builder = NotificationCompat.Builder(
            context,
            "default"
        )

        builder.setContentTitle("NBA Alarm")
        builder.setStyle(NotificationCompat.BigTextStyle().bigText(content))
        builder.setContentText(content)
        builder.setSmallIcon(drawable.ic_launcher_foreground)
        builder.setAutoCancel(true)
        builder.setChannelId(NOTIFICATION_CHANNEL_ID)

        return builder.build()
    }

    override fun onReceive(context: Context, intent: Intent) {
        System.out.println("Match Notification Activated.");

        this.context = context

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel("default", "Default", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val id = intent.getIntExtra(NOTIFICATION_ID, 0)
        notificationManager.notify(id, getNotification("Trigger Notification!"))

        // Retrieve the URI of the alarm the user has set
        var ringtoneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        player = MediaPlayer.create(context, ringtoneUri)

        player.start()
    }
}