package com.example.model

enum class SharingConsentScope(val code: String, val labelBn: String, val labelEn: String, val descriptionBn: String) {
    PERSONAL_ONLY(
        "PERSONAL_ONLY",
        "সম্পূর্ণ ব্যক্তিগত (Personal Only)",
        "Personal Only",
        "আপনার আমল ও সালাতের কোনো তথ্য কারো সাথে বা স্কোয়াডে শেয়ার হবে না।"
    ),
    AGGREGATE_ONLY(
        "AGGREGATE_ONLY",
        "শুধুমাত্র সামগ্রিক অগ্রগতি (Aggregate Only)",
        "Aggregate Only",
        "স্কোয়াডে শুধুমাত্র আপনার ধারাবাহিকতা ও স্কোরের শতকরা হার শেয়ার হবে; ব্যক্তিগত ওয়াক্ত ও নোট সম্পূর্ণ গোপন থাকবে।"
    ),
    DETAILED(
        "DETAILED",
        "অনুমোদিত বিস্তারিত আমল (Detailed Amal)",
        "Detailed",
        "দায়িত্বশীল দ্বীনি পর্যালোচনার জন্য স্কোয়াড লিডারের সাথে সালাত ও তাহাজ্জুদ আদায়ের সংখ্যা শেয়ার হবে।"
    )
}

enum class AmalCategory(val code: String, val labelBn: String, val labelEn: String) {
    QURAN("QURAN", "কুরআন তিলাওয়াত", "Quran Recitation"),
    MORNING_ADHKAR("MORNING_ADHKAR", "সকালের আযকার", "Morning Adhkar"),
    EVENING_ADHKAR("EVENING_ADHKAR", "সন্ধ্যার আযকার", "Evening Adhkar"),
    SADAQAH("SADAQAH", "দান ও সাদাকাহ", "Charity & Sadaqah"),
    ISLAMIC_STUDY("ISLAMIC_STUDY", "দ্বীনি ইলম অন্বেষণ", "Islamic Knowledge"),
    CUSTOM("CUSTOM", "ব্যক্তিগত আমল / অভ্যাস", "Custom Habit")
}

enum class AmalStatus(val code: String, val labelBn: String, val labelEn: String) {
    NOT_RECORDED("NOT_RECORDED", "অনির্ধারিত", "Not Recorded"),
    COMPLETED("COMPLETED", "সম্পন্ন", "Completed"),
    PARTIAL("PARTIAL", "আংশিক", "Partial"),
    MISSED("MISSED", "ছুটে গেছে", "Missed")
}

enum class SquadRole(val code: String, val labelBn: String, val labelEn: String) {
    MEMBER("MEMBER", "সদস্য", "Member"),
    SQUAD_LEADER("SQUAD_LEADER", "স্কোয়াড লিডার", "Squad Leader"),
    MODERATOR("MODERATOR", "মডারেটর", "Moderator"),
    SUPER_ADMIN("SUPER_ADMIN", "সুপার অ্যাডমিন", "Super Admin")
}

data class AmalEntry(
    val id: String = "",
    val userId: String = "",
    val localDate: String = "", // YYYY-MM-DD
    val category: AmalCategory = AmalCategory.CUSTOM,
    val title: String = "",
    val description: String = "",
    val targetCount: Int = 1,
    val currentCount: Int = 0,
    val unit: String = "বার", // e.g. "পৃষ্ঠা", "মিনিট", "বার", "টাকা"
    val status: AmalStatus = AmalStatus.NOT_RECORDED,
    val notes: String = "",
    val isSharedWithSquad: Boolean = false,
    val syncState: SyncState = SyncState.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class Squad(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val ownerUid: String = "",
    val inviteCode: String = "",
    val isPrivate: Boolean = true,
    val memberCount: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)

data class SquadMember(
    val uid: String = "",
    val squadId: String = "",
    val displayName: String = "",
    val role: SquadRole = SquadRole.MEMBER,
    val joinedAt: Long = System.currentTimeMillis(),
    val shareMode: SharingConsentScope = SharingConsentScope.AGGREGATE_ONLY
)

data class SquadAggregateSummary(
    val squadId: String = "",
    val squadName: String = "",
    val activeMembersCount: Int = 0,
    val averagePrayerConsistency: Int = 0, // 0-100%
    val totalTahajjudNights: Int = 0,
    val totalAmalCompleted: Int = 0
)

enum class JobStatus {
    PENDING,
    PROCESSING,
    SUCCEEDED,
    RETRY_SCHEDULED,
    DEAD_LETTER
}

data class DurableSyncJob(
    val jobId: String = "",
    val sourceType: String = "", // PRAYER, TAHAJJUD, DHIKR, AMAL, SQUAD
    val sourceId: String = "",
    val eventVersion: Int = 1,
    val status: JobStatus = JobStatus.PENDING,
    val attemptCount: Int = 0,
    val nextRetryAt: Long = 0L,
    val lastError: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class PrayerRecord(
    val id: String = "",
    val userId: String = "",
    val localDate: String = "", // YYYY-MM-DD
    val prayerName: String = "", // FAJR, DHUHR, etc.
    val status: PrayerStatus = PrayerStatus.NOT_RECORDED,
    val notes: String = "",
    val syncState: SyncState = SyncState.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class TahajjudRecord(
    val id: String = "",
    val userId: String = "",
    val localDate: String = "", // YYYY-MM-DD
    val performed: Boolean = false,
    val rakahs: Int = 0,
    val startTime: String = "", // e.g. "04:10 AM"
    val endTime: String = "",   // e.g. "04:50 AM"
    val notes: String = "",
    val syncState: SyncState = SyncState.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class DhikrPreset(
    val id: String,
    val arabicText: String,
    val bengaliTransliteration: String,
    val bengaliMeaning: String,
    val englishMeaning: String,
    val reference: String,
    val defaultTarget: Int = 33
)

data class DhikrRecord(
    val id: String = "",
    val userId: String = "",
    val localDate: String = "", // YYYY-MM-DD
    val dhikrId: String = "",
    val dhikrTitle: String = "",
    val count: Int = 0,
    val targetCount: Int = 33,
    val syncState: SyncState = SyncState.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)

data class UserProfile(
    val uid: String = "guest_user",
    val displayName: String = "মুসাফির / Musafir",
    val email: String = "",
    val photoUrl: String = "",
    val timezone: String = "Asia/Dhaka",
    val language: String = "bn", // "bn" or "en"
    val consentGiven: Boolean = true,
    val consentScope: SharingConsentScope = SharingConsentScope.AGGREGATE_ONLY,
    val consentTimestamp: Long = System.currentTimeMillis(),
    val tahajjudAlarmEnabled: Boolean = true,
    val tahajjudAlarmOffsetMinutes: Int = 30,
    val calculationCity: String = "Dhaka",
    val latitude: Double = 23.8103,
    val longitude: Double = 90.4125,
    val isSuperAdmin: Boolean = false,
    val activeSquadId: String = ""
)

data class AdminAuditLog(
    val id: String = "",
    val adminUid: String = "",
    val action: String = "",
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class SyncLogItem(
    val id: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val recordType: String = "",
    val recordCount: Int = 0,
    val status: String = "",
    val message: String = ""
)
