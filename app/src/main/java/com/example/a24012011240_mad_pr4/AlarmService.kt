package com.example.a24012011240_mad_pr4

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class AlarmService : Service() {

    private var mp: MediaPlayer? = null

    private val TAG = "AlarmService"

    private val CHANNEL_ID = "alarm_channel"
    private val NOTIFICATION_ID = 1001

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        Log.i(TAG, "AlarmService created")
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        Log.i(TAG, "AlarmService started")

        // Start foreground service immediately
        startForeground(
            NOTIFICATION_ID,
            createNotification()
        )

        try {

            if (mp == null) {

                mp = MediaPlayer.create(
                    this,
                    R.raw.alarm
                )

                if (mp == null) {
                    Log.e(
                        TAG,
                        "MediaPlayer.create() returned null. Could not load alarm.wav"
                    )
                    return START_NOT_STICKY
                }

                mp?.setVolume(
                    1.0f,
                    1.0f
                )

                mp?.isLooping = true

                Log.i(
                    TAG,
                    "alarm.wav loaded successfully"
                )
            }

            if (mp?.isPlaying == false) {

                mp?.start()

                Log.i(
                    TAG,
                    "Alarm sound started"
                )
            }

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Error playing alarm",
                e
            )
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm",
                NotificationManager.IMPORTANCE_HIGH
            )

            channel.description = "Alarm notification"

            // Sound is played by MediaPlayer
            channel.setSound(
                null,
                null
            )

            val notificationManager =
                getSystemService(
                    NotificationManager::class.java
                )

            notificationManager.createNotificationChannel(
                channel
            )
        }
    }

    private fun createNotification(): android.app.Notification {

        val intent = Intent(
            this,
            MainActivity::class.java
        )

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
            .setContentTitle("Alarm")
            .setContentText("Your alarm is ringing")
            .setSmallIcon(R.drawable.clock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(
                NotificationCompat.PRIORITY_HIGH
            )
            .build()
    }

    override fun onDestroy() {

        Log.i(
            TAG,
            "AlarmService destroyed"
        )

        if (mp != null) {

            if (mp?.isPlaying == true) {
                mp?.stop()
            }

            mp?.release()
            mp = null
        }

        stopForeground(
            STOP_FOREGROUND_REMOVE
        )

        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}