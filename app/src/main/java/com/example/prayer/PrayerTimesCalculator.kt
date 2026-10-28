package com.example.prayer

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.*

data class DayPrayerTimes(
    val fajr: Calendar,
    val sunrise: Calendar,
    val dhuhr: Calendar,
    val asr: Calendar,
    val maghrib: Calendar,
    val isha: Calendar,
    val tahajjudReminder: Calendar // 30 mins before Fajr by default
)

object PrayerTimesCalculator {

    enum class CalculationMethod(val fajrAngle: Double, val ishaAngle: Double, val displayName: String) {
        KARACHI(18.0, 18.0, "University of Islamic Sciences, Karachi (South Asia/BD)"),
        MWL(18.0, 17.0, "Muslim World League"),
        ISNA(15.0, 15.0, "ISNA (North America)"),
        EGYPT(19.5, 17.5, "Egyptian General Authority of Survey"),
        MAKKAH(18.5, 0.0, "Umm Al-Qura, Makkah (90m Isha)")
    }

    /**
     * Calculates astronomical prayer times for the specified calendar date and coordinates.
     */
    fun calculateTimes(
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        method: CalculationMethod = CalculationMethod.KARACHI,
        tahajjudOffsetMinutes: Int = 30
    ): DayPrayerTimes {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val tz = calendar.timeZone.rawOffset / (1000.0 * 3600.0)

        // Julian Date
        val julianDate = julianDate(year, month, day) - (longitude / (15.0 * 24.0))

        // Sun coordinates
        val d = julianDate - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(toRadians(g)) + 0.020 * sin(toRadians(2 * g)))
        val e = 23.439 - 0.00000036 * d
        val ra = fixAngle(toDegrees(atan2(cos(toRadians(e)) * sin(toRadians(l)), cos(toRadians(l))))) / 15.0
        val declination = toDegrees(asin(sin(toRadians(e)) * sin(toRadians(l))))
        val eqt = q / 15.0 - fixHour(ra)

        // Solar noon
        val noon = fixHour(12.0 + tz - longitude / 15.0 - eqt)

        // Sunrise and Sunset (approx angle 0.833 degrees for depression + refraction)
        val sunAlt = -0.8333
        val sunriseHour = noon - sunAngleHour(sunAlt, latitude, declination)
        val sunsetHour = noon + sunAngleHour(sunAlt, latitude, declination)

        // Fajr
        val fajrHour = noon - sunAngleHour(-method.fajrAngle, latitude, declination)

        // Asr (Hanafi juristic: shadow length 2 + noon shadow)
        // Shafi: 1 + noon shadow; South Asia typically Hanafi, let's use Hanafi/Standard shadow
        val asrShadow = 1.0 // Standard / Shafi shadow
        val asrAlt = toDegrees(atan(1.0 / (asrShadow + tan(toRadians(abs(latitude - declination))))))
        val asrHour = noon + sunAngleHour(asrAlt, latitude, declination)

        // Maghrib is sunset
        val maghribHour = sunsetHour

        // Isha
        val ishaHour = if (method == CalculationMethod.MAKKAH) {
            sunsetHour + 1.5 // 90 mins after Maghrib
        } else {
            noon + sunAngleHour(-method.ishaAngle, latitude, declination)
        }

        val fajrCal = hourToCalendar(calendar, fajrHour)
        val sunriseCal = hourToCalendar(calendar, sunriseHour)
        val dhuhrCal = hourToCalendar(calendar, noon)
        val asrCal = hourToCalendar(calendar, asrHour)
        val maghribCal = hourToCalendar(calendar, maghribHour)
        val ishaCal = hourToCalendar(calendar, ishaHour)

        // Tahajjud reminder: 30 minutes before Fajr
        val tahajjudCal = (fajrCal.clone() as Calendar).apply {
            add(Calendar.MINUTE, -tahajjudOffsetMinutes)
        }

        return DayPrayerTimes(
            fajr = fajrCal,
            sunrise = sunriseCal,
            dhuhr = dhuhrCal,
            asr = asrCal,
            maghrib = maghribCal,
            isha = ishaCal,
            tahajjudReminder = tahajjudCal
        )
    }

    /**
     * Determines the next upcoming Fajr and its corresponding Tahajjud reminder.
     * If today's Fajr has already passed, computes tomorrow's Fajr and Tahajjud reminder.
     */
    fun getNextUpcomingTahajjud(
        latitude: Double,
        longitude: Double,
        method: CalculationMethod = CalculationMethod.KARACHI,
        offsetMinutes: Int = 30
    ): Pair<Calendar, Calendar> {
        val now = Calendar.getInstance()
        val todayTimes = calculateTimes(now, latitude, longitude, method, offsetMinutes)

        return if (todayTimes.fajr.after(now)) {
            // Today's Fajr is still upcoming
            Pair(todayTimes.fajr, todayTimes.tahajjudReminder)
        } else {
            // Tomorrow's Fajr
            val tomorrow = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
            val tomorrowTimes = calculateTimes(tomorrow, latitude, longitude, method, offsetMinutes)
            Pair(tomorrowTimes.fajr, tomorrowTimes.tahajjudReminder)
        }
    }

    fun formatTime(calendar: Calendar): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    private fun hourToCalendar(base: Calendar, decimalHour: Double): Calendar {
        val cal = base.clone() as Calendar
        val hour = decimalHour.toInt()
        val minDecimal = (decimalHour - hour) * 60.0
        val minute = minDecimal.toInt()
        val second = ((minDecimal - minute) * 60.0).toInt()
        cal.set(Calendar.HOUR_OF_DAY, (hour % 24 + 24) % 24)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, second)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }

    private fun sunAngleHour(angle: Double, lat: Double, dec: Double): Double {
        val cosH = (sin(toRadians(angle)) - sin(toRadians(lat)) * sin(toRadians(dec))) /
                (cos(toRadians(lat)) * cos(toRadians(dec)))
        return if (cosH > 1.0) 0.0 else if (cosH < -1.0) 12.0 else toDegrees(acos(cosH)) / 15.0
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun fixAngle(a: Double): Double {
        var ang = a - 360.0 * floor(a / 360.0)
        if (ang < 0) ang += 360.0
        return ang
    }

    private fun fixHour(h: Double): Double {
        var hour = h - 24.0 * floor(h / 24.0)
        if (hour < 0) hour += 24.0
        return hour
    }

    private fun toRadians(deg: Double): Double = deg * PI / 180.0
    private fun toDegrees(rad: Double): Double = rad * 180.0 / PI
}
