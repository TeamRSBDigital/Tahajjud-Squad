package com.example.model

enum class PrayerType(val englishName: String, val bengaliName: String, val arabicName: String) {
    FAJR("Fajr", "ফজর", "الفجر"),
    DHUHR("Dhuhr", "যোহর", "الظهر"),
    ASR("Asr", "আসর", "العصر"),
    MAGHRIB("Maghrib", "মাগরিব", "المغرب"),
    ISHA("Isha", "ইশা", "العشاء")
}

enum class PrayerStatus(val code: String, val englishLabel: String, val bengaliLabel: String) {
    NOT_RECORDED("NOT_RECORDED", "Not Recorded", "অনির্ধারিত"),
    PERFORMED("PERFORMED", "Performed", "আদায়কৃত"),
    MISSED("MISSED", "Missed", "ছুটে গেছে"),
    QAZA("QAZA", "Qaza", "কাযা আদায়")
}

enum class SyncState {
    PENDING,
    SYNCED,
    FAILED
}
