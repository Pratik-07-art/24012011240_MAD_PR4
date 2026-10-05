package com.example.a24012011240_mad_pr4

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    lateinit var textAlarm: TextView
    lateinit var cardSetAlarm: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        textAlarm = findViewById(R.id.textView6)
        cardSetAlarm = findViewById(R.id.materialCard2)

        cardSetAlarm.visibility = View.GONE

        findViewById<Button>(R.id.set_alarm_btn).setOnClickListener {
            showTimeDialog()
        }

        findViewById<Button>(R.id.cancel_alarm_btn).setOnClickListener {
            setAlarm(0, AlarmBroadcastReceiver.STOP_VAL)

            cardSetAlarm.visibility = View.GONE
            textAlarm.text = "--:--"

            Toast.makeText(
                this,
                "Alarm cancelled",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showTimeDialog() {

        val cldr = Calendar.getInstance()

        val hrs = cldr.get(Calendar.HOUR_OF_DAY)
        val mns = cldr.get(Calendar.MINUTE)

        val picker = TimePickerDialog(
            this,
            { _, sHour, sMinute ->
                sendDialogDataToActivity(sHour, sMinute)
            },
            hrs,
            mns,
            false
        )

        picker.show()
    }

    private fun sendDialogDataToActivity(hour: Int, minute: Int) {

        val alarmCalendar = Calendar.getInstance()

        alarmCalendar.set(
            Calendar.HOUR_OF_DAY,
            hour
        )

        alarmCalendar.set(
            Calendar.MINUTE,
            minute
        )

        alarmCalendar.set(
            Calendar.SECOND,
            0
        )

        alarmCalendar.set(
            Calendar.MILLISECOND,
            0
        )

        // If selected time has already passed today,
        // schedule it for tomorrow.
        if (alarmCalendar.timeInMillis <= System.currentTimeMillis()) {
            alarmCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        if (setAlarm(
                alarmCalendar.timeInMillis,
                AlarmBroadcastReceiver.START_VAL
            )
        ) {

            // Display time in 12-hour format with AM/PM
            val timeFormat = SimpleDateFormat(
                "hh:mm a",
                Locale.getDefault()
            )

            textAlarm.text = timeFormat.format(
                alarmCalendar.time
            )

            cardSetAlarm.visibility = View.VISIBLE

            Toast.makeText(
                this,
                "Alarm set for ${timeFormat.format(alarmCalendar.time)}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setAlarm(
        millisTime: Long,
        str: String
    ): Boolean {

        val intent = Intent(
            this,
            AlarmBroadcastReceiver::class.java
        )

        intent.putExtra(
            AlarmBroadcastReceiver.SERVICE_KEY,
            str
        )

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            23245,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager =
            getSystemService(ALARM_SERVICE) as AlarmManager

        if (str == AlarmBroadcastReceiver.START_VAL) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {

                if (alarmManager.canScheduleExactAlarms()) {

                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        millisTime,
                        pendingIntent
                    )

                    return true

                } else {

                    Toast.makeText(
                        this,
                        "Please allow exact alarms",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intentSettings = Intent(
                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        "package:$packageName".toUri()
                    )

                    startActivity(intentSettings)

                    return false
                }

            } else {

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    millisTime,
                    pendingIntent
                )

                return true
            }

        } else if (str == AlarmBroadcastReceiver.STOP_VAL) {

            alarmManager.cancel(pendingIntent)

            // Stop the alarm service
            val stopIntent = Intent(
                this,
                AlarmBroadcastReceiver::class.java
            )

            stopIntent.putExtra(
                AlarmBroadcastReceiver.SERVICE_KEY,
                AlarmBroadcastReceiver.STOP_VAL
            )

            sendBroadcast(stopIntent)

            return true
        }

        return false
    }
}