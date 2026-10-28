package com.example.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

object AlarmRingingManager {

    private const val TAG = "AlarmRingingManager"
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var isCurrentlyRinging = false

    fun isRinging(): Boolean = isCurrentlyRinging

    /**
     * Starts looping alarm sound and repetitive wake-up vibration.
     */
    @Synchronized
    fun startRinging(context: Context, playSound: Boolean = true, vibrate: Boolean = true) {
        if (isCurrentlyRinging) {
            Log.d(TAG, "Already ringing, ignoring start request")
            return
        }
        isCurrentlyRinging = true

        if (playSound) {
            try {
                var alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                if (alarmUri == null) {
                    alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                }
                if (alarmUri == null) {
                    alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                }

                mediaPlayer = MediaPlayer().apply {
                    setDataSource(context, alarmUri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    isLooping = true
                    prepare()
                    start()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to start MediaPlayer for alarm sound", e)
            }
        }

        if (vibrate) {
            try {
                vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    manager?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }

                val pattern = longArrayOf(0, 800, 400, 800, 400, 1200)
                if (vibrator?.hasVibrator() == true) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0)) // 0 means repeat
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(pattern, 0)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to start vibration pattern", e)
            }
        }
    }

    /**
     * Stops the alarm sound and vibration immediately.
     */
    @Synchronized
    fun stopRinging() {
        isCurrentlyRinging = false
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping MediaPlayer", e)
        }

        try {
            vibrator?.cancel()
            vibrator = null
        } catch (e: Exception) {
            Log.w(TAG, "Error cancelling vibrator", e)
        }
    }
}
