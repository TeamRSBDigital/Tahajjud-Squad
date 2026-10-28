package com.example.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class TahajjudAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        showTahajjudNotification(context, isTest = intent.getBooleanExtra("IS_TEST", false))
        // Start looping alarm audio and wake-up vibration
        AlarmRingingManager.startRinging(context)
        // Automatically reschedule next alarm
        TahajjudAlarmScheduler.scheduleNextAlarm(context)
    }

    companion object {
        const val CHANNEL_ID = "tahajjud_reminder_channel"
        const val NOTIFICATION_ID = 1001

        fun showTahajjudNotification(context: Context, isTest: Boolean = false) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "তাহাজ্জুদ রিমাইন্ডার / Tahajjud Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "তাহাজ্জুদ ও ফজরের পূর্ববর্তী স্মার্ট অ্যালার্ম ও মিশন রিমাইন্ডার"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 800, 400, 800)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val appIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("NAVIGATE_TO", "alarm_mission")
                putExtra("IS_ALARM_RINGING", true)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val title = if (isTest) {
                "স্মার্ট তাহাজ্জুদ অ্যালার্ম টেস্ট সফল!"
            } else {
                "তাহাজ্জুদের সময় — ফজর হতে আর ৩০ মিনিট বাকি!"
            }

            val content = if (isTest) {
                "তাহাজ্জুদ স্কোয়াড অ্যালার্ম ও মিশন সিস্টেম সক্রিয় আছে। ট্যাপ করে টেস্ট মিশন চালু করুন।"
            } else {
                "রাতের শেষ তৃতীয়াংশে জাগ্রত হোন। অ্যালার্ম বন্ধ করতে নির্ধারিত মিশন সম্পন্ন করুন।"
            }

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent, true)
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }
}
