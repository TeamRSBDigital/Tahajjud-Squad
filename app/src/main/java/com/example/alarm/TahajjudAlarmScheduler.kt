package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.prayer.PrayerTimesCalculator
import java.util.Calendar

object TahajjudAlarmScheduler {

    private const val ALARM_REQUEST_CODE = 2002

    fun scheduleNextAlarm(
        context: Context,
        latitude: Double = 23.8103,
        longitude: Double = 90.4125,
        offsetMinutes: Int = 30
    ): Pair<Calendar, Calendar> {
        val (fajrCal, reminderCal) = PrayerTimesCalculator.getNextUpcomingTahajjud(
            latitude = latitude,
            longitude = longitude,
            offsetMinutes = offsetMinutes
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return Pair(fajrCal, reminderCal)

        val intent = Intent(context, TahajjudAlarmReceiver::class.java).apply {
            putExtra("IS_TEST", false)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = reminderCal.timeInMillis
        val now = System.currentTimeMillis()

        if (triggerTime > now) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
                Log.d("AmalAlarm", "Tahajjud alarm scheduled for ${reminderCal.time}")
            } catch (e: SecurityException) {
                Log.w("AmalAlarm", "SecurityException scheduling alarm, falling back", e)
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        }

        return Pair(fajrCal, reminderCal)
    }

    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, TahajjudAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun sendTestNotification(context: Context) {
        TahajjudAlarmReceiver.showTahajjudNotification(context, isTest = true)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED ||
            intent.action == Intent.ACTION_TIME_CHANGED
        ) {
            Log.d("AmalBoot", "Rescheduling Tahajjud alarms after system event: ${intent.action}")
            TahajjudAlarmScheduler.scheduleNextAlarm(context)
        }
    }
}
