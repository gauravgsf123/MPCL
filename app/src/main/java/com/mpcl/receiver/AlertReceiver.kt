package com.mpcl.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

import com.gdsctsec.smartt.ui.notifications.NotificationHelper




class AlertReceiver() : BroadcastReceiver() {

    private val id = 1

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e("onReceive","onReceive")
        Toast.makeText(context,"onReceive",Toast.LENGTH_LONG).show()
        val notificationHelper = NotificationHelper(context)
        val key = intent?.getStringExtra("id")?.toInt()
        val title = intent?.getStringExtra("title")
        val time = intent?.getStringExtra("time")
        val nb = notificationHelper.getNotifications(
            "Alert", "$title","$time"
        )
        notificationHelper.notificationManager?.notify(id, nb.build())
    }
}