package com.example.a24012011240_mad_pr4

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class AlarmBroadcastReceiver : BroadcastReceiver() {

    companion object {

        const val SERVICE_KEY = "Service1"
        const val START_VAL = "start"
        const val STOP_VAL = "stop"
    }

    private val TAG = "AlarmBroadcastReceiver"

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        val str1 = intent.getStringExtra(SERVICE_KEY)

        if (str1 == START_VAL || str1 == STOP_VAL) {

            Log.i(
                TAG,
                "onReceive: Received value: $str1"
            )

            val intentService = Intent(
                context,
                AlarmService::class.java
            )

            if (str1 == START_VAL) {

                Log.i(
                    TAG,
                    "Starting alarm service"
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    context.startForegroundService(
                        intentService
                    )

                } else {

                    context.startService(
                        intentService
                    )
                }

            } else {

                Log.i(
                    TAG,
                    "Stopping alarm service"
                )

                context.stopService(
                    intentService
                )
            }
        }
    }
}