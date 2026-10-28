package com.example.data.repository

import android.content.Context
import androidx.room.Room
import com.example.data.local.*
import com.example.model.*
import com.example.sync.GoogleSheetsSyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class AmalRepository(private val context: Context) {

    val database: AmalDatabase = Room.databaseBuilder(
        context.applicationContext,
        AmalDatabase::class.java,
        "amal_tracker.db"
    ).fallbackToDestructiveMigration().build()

    val syncManager = GoogleSheetsSyncManager(context, database)

    // Current User Profile
    private val _currentUserProfile = MutableStateFlow(
        UserProfile(
            uid = "user_" + UUID.randomUUID().toString().take(8),
            displayName = "আব্দুল্লাহ / Abdullah",
            email = "abdullah@example.com",
            timezone = "Asia/Dhaka",
            language = "bn",
            consentGiven = true,
            consentScope = SharingConsentScope.AGGREGATE_ONLY,
            isSuperAdmin = false,
            activeSquadId = "squad_fajr_champions"
        )
    )
    val currentUserProfile: StateFlow<UserProfile> = _currentUserProfile.asStateFlow()

    // Apps Script & Super Admin Settings
    private val _appsScriptUrl = MutableStateFlow("https://script.google.com/macros/s/AKfycbyExampleAmalTrackerDeploy/exec")
    val appsScriptUrl: StateFlow<String> = _appsScriptUrl.asStateFlow()

    private val _syncSecretToken = MutableStateFlow("AMAL_SECURE_TOKEN_2026")
    val syncSecretToken: StateFlow<String> = _syncSecretToken.asStateFlow()

    // Admin Audit Logs
    private val _auditLogs = MutableStateFlow<List<AdminAuditLog>>(
        listOf(
            AdminAuditLog(
                id = "log_1",
                adminUid = "system",
                action = "SYSTEM_INITIALIZED",
                details = "Amal Tracker & Tahajjud Squad platform initialized with high-precision prayer calculation engine."
            )
        )
    )
    val auditLogs: StateFlow<List<AdminAuditLog>> = _auditLogs.asStateFlow()

    fun updateAppsScriptSettings(url: String, token: String) {
        _appsScriptUrl.value = url
        _syncSecretToken.value = token
        recordAudit("CONFIG_UPDATE", "Google Sheets Web App URL or Token updated.")
    }

    fun updateConsent(given: Boolean) {
        _currentUserProfile.value = _currentUserProfile.value.copy(
            consentGiven = given,
            consentTimestamp = System.currentTimeMillis()
        )
        recordAudit("USER_CONSENT", "Consent status updated to: $given")
    }

    fun updateSharingConsentScope(scope: SharingConsentScope) {
        _currentUserProfile.value = _currentUserProfile.value.copy(
            consentScope = scope,
            consentTimestamp = System.currentTimeMillis()
        )
        recordAudit("CONSENT_SCOPE_CHANGED", "Sharing consent scope set to: ${scope.name}")
    }

    fun updateLanguage(lang: String) {
        _currentUserProfile.value = _currentUserProfile.value.copy(language = lang)
    }

    fun updateSuperAdminStatus(isAdmin: Boolean) {
        _currentUserProfile.value = _currentUserProfile.value.copy(isSuperAdmin = isAdmin)
        recordAudit("ROLE_UPDATE", "Super Admin role set to: $isAdmin")
    }

    fun updateAlarmPreferences(enabled: Boolean, offsetMinutes: Int) {
        _currentUserProfile.value = _currentUserProfile.value.copy(
            tahajjudAlarmEnabled = enabled,
            tahajjudAlarmOffsetMinutes = offsetMinutes
        )
    }

    // Smart Tahajjud Alarm Mission Config & Persistence
    private val alarmPrefs = context.getSharedPreferences("amal_smart_alarm_prefs", Context.MODE_PRIVATE)

    private val _smartAlarmConfig = MutableStateFlow(loadSmartAlarmConfig())
    val smartAlarmConfig: StateFlow<SmartAlarmConfig> = _smartAlarmConfig.asStateFlow()

    private fun loadSmartAlarmConfig(): SmartAlarmConfig {
        val missionTypeCode = alarmPrefs.getString("mission_type", AlarmMissionType.BRAIN_MATH.name) ?: AlarmMissionType.BRAIN_MATH.name
        val diffCode = alarmPrefs.getString("difficulty", MissionDifficulty.EASY.name) ?: MissionDifficulty.EASY.name
        val missionType = try { AlarmMissionType.valueOf(missionTypeCode) } catch (_: Exception) { AlarmMissionType.BRAIN_MATH }
        val difficulty = try { MissionDifficulty.valueOf(diffCode) } catch (_: Exception) { MissionDifficulty.EASY }

        var qrToken = alarmPrefs.getString("qr_token", "") ?: ""
        if (qrToken.isBlank()) {
            qrToken = com.example.mission.QrMissionEngine.generateSecureQrToken()
            alarmPrefs.edit().putString("qr_token", qrToken).apply()
        }
        val savedAllowlist = alarmPrefs.getStringSet("qr_allowlist", emptySet()) ?: emptySet()
        val allowlist = if (qrToken.isNotBlank()) (savedAllowlist + qrToken).toList() else savedAllowlist.toList()

        return SmartAlarmConfig(
            enabled = alarmPrefs.getBoolean("enabled", true),
            missionType = missionType,
            difficulty = difficulty,
            taskCount = alarmPrefs.getInt("task_count", 2),
            registeredQrToken = qrToken,
            qrAllowlist = allowlist,
            registeredTargetSignature = alarmPrefs.getString("target_signature", "") ?: "",
            registeredTargetName = alarmPrefs.getString("target_name", "") ?: "",
            emergencyFallbackAllowed = alarmPrefs.getBoolean("emergency_fallback", true),
            emergencyPressCountRequired = alarmPrefs.getInt("emergency_presses", 8),
            soundVolume = alarmPrefs.getFloat("sound_volume", 1.0f),
            vibrate = alarmPrefs.getBoolean("vibrate", true)
        )
    }

    fun updateSmartAlarmConfig(config: SmartAlarmConfig) {
        _smartAlarmConfig.value = config
        val allowlistToSave = (config.qrAllowlist + config.registeredQrToken).filter { it.isNotBlank() }.toSet()
        alarmPrefs.edit()
            .putBoolean("enabled", config.enabled)
            .putString("mission_type", config.missionType.name)
            .putString("difficulty", config.difficulty.name)
            .putInt("task_count", config.taskCount)
            .putString("qr_token", config.registeredQrToken)
            .putStringSet("qr_allowlist", allowlistToSave)
            .putString("target_signature", config.registeredTargetSignature)
            .putString("target_name", config.registeredTargetName)
            .putBoolean("emergency_fallback", config.emergencyFallbackAllowed)
            .putInt("emergency_presses", config.emergencyPressCountRequired)
            .putFloat("sound_volume", config.soundVolume)
            .putBoolean("vibrate", config.vibrate)
            .apply()
        recordAudit("SMART_ALARM_CONFIG", "Updated mission: ${config.missionType.name}, difficulty: ${config.difficulty.name}")
    }

    fun regenerateQrToken(): String {
        val newToken = com.example.mission.QrMissionEngine.generateSecureQrToken()
        val currentAllowlist = _smartAlarmConfig.value.qrAllowlist
        val updated = _smartAlarmConfig.value.copy(
            registeredQrToken = newToken,
            qrAllowlist = (currentAllowlist + newToken).distinct()
        )
        updateSmartAlarmConfig(updated)
        return newToken
    }

    fun registerPhotoTarget(signature: String, targetName: String) {
        val updated = _smartAlarmConfig.value.copy(
            registeredTargetSignature = signature,
            registeredTargetName = targetName
        )
        updateSmartAlarmConfig(updated)
    }

    fun clearPhotoTarget() {
        val updated = _smartAlarmConfig.value.copy(
            registeredTargetSignature = "",
            registeredTargetName = ""
        )
        updateSmartAlarmConfig(updated)
    }

    fun recordAudit(action: String, details: String) {
        val newLog = AdminAuditLog(
            id = "log_" + System.currentTimeMillis(),
            adminUid = _currentUserProfile.value.uid,
            action = action,
            details = details,
            timestamp = System.currentTimeMillis()
        )
        _auditLogs.value = listOf(newLog) + _auditLogs.value.take(49)
    }

    // --- PRAYER OPERATIONS ---
    fun observePrayersForDate(date: String): Flow<List<PrayerRecord>> {
        val uid = _currentUserProfile.value.uid
        return database.prayerDao().getPrayersByDate(uid, date).map { entities ->
            val mapped = entities.map { it.toDomain() }
            // Ensure all 5 daily prayers exist in memory representation
            PrayerType.values().map { prayerType ->
                mapped.find { it.prayerName == prayerType.name } ?: PrayerRecord(
                    id = "${uid}_${date}_${prayerType.name}",
                    userId = uid,
                    localDate = date,
                    prayerName = prayerType.name,
                    status = PrayerStatus.NOT_RECORDED
                )
            }
        }
    }

    fun observeAllPrayers(): Flow<List<PrayerRecord>> {
        val uid = _currentUserProfile.value.uid
        return database.prayerDao().getAllPrayers(uid).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun setPrayerStatus(date: String, prayerType: PrayerType, status: PrayerStatus, notes: String = "") {
        val uid = _currentUserProfile.value.uid
        val id = "${uid}_${date}_${prayerType.name}"
        val record = PrayerRecord(
            id = id,
            userId = uid,
            localDate = date,
            prayerName = prayerType.name,
            status = status,
            notes = notes,
            syncState = SyncState.PENDING,
            updatedAt = System.currentTimeMillis()
        )
        database.prayerDao().insertOrUpdatePrayer(PrayerEntity.fromDomain(record))
    }

    // --- TAHAJJUD OPERATIONS ---
    fun observeTahajjudForDate(date: String): Flow<TahajjudRecord?> {
        val uid = _currentUserProfile.value.uid
        return database.tahajjudDao().getTahajjudByDate(uid, date).map { it?.toDomain() }
    }

    fun observeAllTahajjud(): Flow<List<TahajjudRecord>> {
        val uid = _currentUserProfile.value.uid
        return database.tahajjudDao().getAllTahajjud(uid).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun saveTahajjud(
        date: String,
        performed: Boolean,
        rakahs: Int,
        startTime: String = "",
        endTime: String = "",
        notes: String = ""
    ) {
        val uid = _currentUserProfile.value.uid
        val id = "${uid}_${date}_TAHAJJUD"
        val record = TahajjudRecord(
            id = id,
            userId = uid,
            localDate = date,
            performed = performed,
            rakahs = rakahs,
            startTime = startTime,
            endTime = endTime,
            notes = notes,
            syncState = SyncState.PENDING,
            updatedAt = System.currentTimeMillis()
        )
        database.tahajjudDao().insertOrUpdateTahajjud(TahajjudEntity.fromDomain(record))
    }

    // --- DHIKR OPERATIONS ---
    fun observeDhikrForDate(date: String): Flow<List<DhikrRecord>> {
        val uid = _currentUserProfile.value.uid
        return database.dhikrDao().getDhikrByDate(uid, date).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun observeAllDhikr(): Flow<List<DhikrRecord>> {
        val uid = _currentUserProfile.value.uid
        return database.dhikrDao().getAllDhikr(uid).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun recordDhikrSession(
        date: String,
        dhikrId: String,
        dhikrTitle: String,
        count: Int,
        targetCount: Int
    ) {
        val uid = _currentUserProfile.value.uid
        val id = "${uid}_${date}_${dhikrId}_${System.currentTimeMillis()}"
        val record = DhikrRecord(
            id = id,
            userId = uid,
            localDate = date,
            dhikrId = dhikrId,
            dhikrTitle = dhikrTitle,
            count = count,
            targetCount = targetCount,
            syncState = SyncState.PENDING
        )
        database.dhikrDao().insertOrUpdateDhikr(DhikrEntity.fromDomain(record))
    }

    // --- EXTENDED AMAL TRACKER OPERATIONS ---
    fun observeAmalForDate(date: String): Flow<List<AmalEntry>> {
        val uid = _currentUserProfile.value.uid
        return database.amalDao().getAmalByDate(uid, date).map { entities ->
            if (entities.isEmpty()) {
                // Return default verified templates for the day
                getDefaultAmalTemplates(uid, date)
            } else {
                entities.map { it.toDomain() }
            }
        }
    }

    fun observeAllAmal(): Flow<List<AmalEntry>> {
        val uid = _currentUserProfile.value.uid
        return database.amalDao().getAllAmal(uid).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun saveAmalEntry(entry: AmalEntry) {
        val uid = _currentUserProfile.value.uid
        val safeEntry = if (entry.id.isBlank()) {
            entry.copy(
                id = "${uid}_${entry.localDate}_${UUID.randomUUID().toString().take(6)}",
                userId = uid,
                updatedAt = System.currentTimeMillis()
            )
        } else {
            entry.copy(userId = uid, updatedAt = System.currentTimeMillis())
        }
        database.amalDao().insertOrUpdateAmal(AmalEntity.fromDomain(safeEntry))
    }

    suspend fun deleteAmalEntry(id: String) {
        database.amalDao().deleteAmalById(id)
    }

    private fun getDefaultAmalTemplates(uid: String, date: String): List<AmalEntry> {
        return listOf(
            AmalEntry(
                id = "${uid}_${date}_quran",
                userId = uid,
                localDate = date,
                category = AmalCategory.QURAN,
                title = "কুরআন তিলাওয়াত (Quran Recitation)",
                description = "দৈনন্দিন কুরআন কারীমের অর্থসহ তিলাওয়াত ও তাদাব্বুর",
                targetCount = 5,
                unit = "পৃষ্ঠা",
                status = AmalStatus.NOT_RECORDED
            ),
            AmalEntry(
                id = "${uid}_${date}_morning_adhkar",
                userId = uid,
                localDate = date,
                category = AmalCategory.MORNING_ADHKAR,
                title = "সকালের আযকার (Morning Adhkar)",
                description = "সহীহ হাদীস বর্ণিত মাসনূন সকালের দু'আ ও যিকিরসমূহ",
                targetCount = 1,
                unit = "সেশন",
                status = AmalStatus.NOT_RECORDED
            ),
            AmalEntry(
                id = "${uid}_${date}_evening_adhkar",
                userId = uid,
                localDate = date,
                category = AmalCategory.EVENING_ADHKAR,
                title = "সন্ধ্যার আযকার (Evening Adhkar)",
                description = "আসর ও মাগরিবের পর পঠিতব্য মাসনূন সুরক্ষামূলক আযকার",
                targetCount = 1,
                unit = "সেশন",
                status = AmalStatus.NOT_RECORDED
            ),
            AmalEntry(
                id = "${uid}_${date}_sadaqah",
                userId = uid,
                localDate = date,
                category = AmalCategory.SADAQAH,
                title = "দান ও সাদাকাহ (Daily Charity)",
                description = "আল্লাহর সন্তুষ্টির উদ্দেশ্যে গোপনে বা প্রকাশ্যে দান",
                targetCount = 1,
                unit = "বার",
                status = AmalStatus.NOT_RECORDED
            ),
            AmalEntry(
                id = "${uid}_${date}_knowledge",
                userId = uid,
                localDate = date,
                category = AmalCategory.ISLAMIC_STUDY,
                title = "দ্বীনি ইলম অন্বেষণ (Islamic Learning)",
                description = "হাদীস, তাফসীর বা ফিক্বহ অধ্যয়ন ও চর্চা",
                targetCount = 20,
                unit = "মিনিট",
                status = AmalStatus.NOT_RECORDED
            )
        )
    }

    // --- SQUAD & COMMUNITY OPERATIONS ---
    fun observeAllSquads(): Flow<List<Squad>> {
        return database.squadDao().getAllSquads().map { list ->
            if (list.isEmpty()) {
                listOf(
                    Squad(
                        id = "squad_fajr_champions",
                        name = "তাহাজ্জুদ ও ফজর স্কোয়াড (Tahajjud Squad Alpha)",
                        description = "রাতের শেষ প্রহরে তাহাজ্জুদ ও জামাতে ফজরের অঙ্গীকারবদ্ধ দ্বীনি ভ্রাতৃত্ব কাফেলা।",
                        ownerUid = "admin_lead_01",
                        inviteCode = "FAJR2026",
                        isPrivate = true,
                        memberCount = 18
                    ),
                    Squad(
                        id = "squad_dhikr_lovers",
                        name = "যিকির ও ইস্তিগফার সার্কেল (Zikr & Dua Circle)",
                        description = "দৈনিক ১০০০+ আযকার ও সুন্নাত সালাত সুরক্ষার পরিবার।",
                        ownerUid = "admin_lead_02",
                        inviteCode = "ZIKR786",
                        isPrivate = false,
                        memberCount = 34
                    )
                )
            } else {
                list.map { it.toDomain() }
            }
        }
    }

    suspend fun createSquad(name: String, description: String, isPrivate: Boolean): Squad {
        val uid = _currentUserProfile.value.uid
        val squadId = "squad_" + UUID.randomUUID().toString().take(8)
        val inviteCode = name.take(4).uppercase() + (100..999).random()

        val squad = Squad(
            id = squadId,
            name = name,
            description = description,
            ownerUid = uid,
            inviteCode = inviteCode,
            isPrivate = isPrivate,
            memberCount = 1,
            createdAt = System.currentTimeMillis()
        )
        database.squadDao().insertOrUpdateSquad(SquadEntity.fromDomain(squad))

        val ownerMember = SquadMember(
            uid = uid,
            squadId = squadId,
            displayName = _currentUserProfile.value.displayName,
            role = SquadRole.SQUAD_LEADER,
            joinedAt = System.currentTimeMillis(),
            shareMode = _currentUserProfile.value.consentScope
        )
        database.squadDao().insertOrUpdateMember(SquadMemberEntity.fromDomain(ownerMember))

        _currentUserProfile.value = _currentUserProfile.value.copy(activeSquadId = squadId)
        recordAudit("SQUAD_CREATED", "Squad created: $name (Code: $inviteCode)")
        return squad
    }

    suspend fun joinSquad(inviteCode: String): Result<Squad> {
        val uid = _currentUserProfile.value.uid
        val squad = database.squadDao().getSquadByInviteCode(inviteCode.trim().uppercase())?.toDomain()
            ?: if (inviteCode.trim().equals("FAJR2026", ignoreCase = true)) {
                Squad(
                    id = "squad_fajr_champions",
                    name = "তাহাজ্জুদ ও ফজর স্কোয়াড (Tahajjud Squad Alpha)",
                    description = "রাতের শেষ প্রহরে তাহাজ্জুদ ও জামাতে ফজরের অঙ্গীকারবদ্ধ দ্বীনি ভ্রাতৃত্ব কাফেলা।",
                    ownerUid = "admin_lead_01",
                    inviteCode = "FAJR2026",
                    isPrivate = true,
                    memberCount = 19
                )
            } else if (inviteCode.trim().equals("ZIKR786", ignoreCase = true)) {
                Squad(
                    id = "squad_dhikr_lovers",
                    name = "যিকির ও ইস্তিগফার সার্কেল (Zikr & Dua Circle)",
                    description = "দৈনিক ১০০০+ আযকার ও সুন্নাত সালাত সুরক্ষার পরিবার।",
                    ownerUid = "admin_lead_02",
                    inviteCode = "ZIKR786",
                    isPrivate = false,
                    memberCount = 35
                )
            } else {
                return Result.failure(IllegalArgumentException("আমন্ত্রণ কোডটি সঠিক নয় (Invalid Invite Code)"))
            }

        database.squadDao().insertOrUpdateSquad(SquadEntity.fromDomain(squad.copy(memberCount = squad.memberCount + 1)))

        val member = SquadMember(
            uid = uid,
            squadId = squad.id,
            displayName = _currentUserProfile.value.displayName,
            role = SquadRole.MEMBER,
            joinedAt = System.currentTimeMillis(),
            shareMode = _currentUserProfile.value.consentScope
        )
        database.squadDao().insertOrUpdateMember(SquadMemberEntity.fromDomain(member))

        _currentUserProfile.value = _currentUserProfile.value.copy(activeSquadId = squad.id)
        recordAudit("SQUAD_JOINED", "Joined squad: ${squad.name}")
        return Result.success(squad)
    }

    suspend fun leaveSquad(squadId: String) {
        val uid = _currentUserProfile.value.uid
        database.squadDao().removeMember(uid, squadId)
        if (_currentUserProfile.value.activeSquadId == squadId) {
            _currentUserProfile.value = _currentUserProfile.value.copy(activeSquadId = "")
        }
        recordAudit("SQUAD_LEFT", "User left squad: $squadId")
    }

    // Account Data Wipe
    suspend fun deleteAllUserData() {
        val uid = _currentUserProfile.value.uid
        database.prayerDao().clearUserPrayers(uid)
        database.tahajjudDao().clearUserTahajjud(uid)
        database.dhikrDao().clearUserDhikr(uid)
        database.amalDao().clearUserAmal(uid)
        recordAudit("DATA_WIPE", "All user worship records and extended Amal wiped per user request.")
    }

    companion object {
        fun getTodayDateString(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }

        fun getBengaliDateFormatted(date: Date = Date()): String {
            val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("bn", "BD"))
            return sdf.format(date)
        }
    }
}
