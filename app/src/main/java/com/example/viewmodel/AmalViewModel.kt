package com.example.viewmodel

import android.app.Application
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm.AlarmRingingManager
import com.example.alarm.TahajjudAlarmScheduler
import com.example.data.repository.AmalRepository
import com.example.model.*
import com.example.prayer.PrayerTimesCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AmalViewModel(application: Application) : AndroidViewModel(application) {

    val repository = AmalRepository(application)

    // Current selected date (YYYY-MM-DD)
    private val _selectedDate = MutableStateFlow(AmalRepository.getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Observable Prayers for selected date
    val prayersForSelectedDate: StateFlow<List<PrayerRecord>> = _selectedDate
        .flatMapLatest { date -> repository.observePrayersForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Observable Tahajjud for selected date
    val tahajjudForSelectedDate: StateFlow<TahajjudRecord?> = _selectedDate
        .flatMapLatest { date -> repository.observeTahajjudForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Observable Dhikr for selected date
    val dhikrForSelectedDate: StateFlow<List<DhikrRecord>> = _selectedDate
        .flatMapLatest { date -> repository.observeDhikrForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Observable Extended Amal Entries for selected date
    val amalForSelectedDate: StateFlow<List<AmalEntry>> = _selectedDate
        .flatMapLatest { date -> repository.observeAmalForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All prayers for analytics
    val allPrayers: StateFlow<List<PrayerRecord>> = repository.observeAllPrayers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All tahajjud for analytics
    val allTahajjud: StateFlow<List<TahajjudRecord>> = repository.observeAllTahajjud()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All dhikr for analytics
    val allDhikr: StateFlow<List<DhikrRecord>> = repository.observeAllDhikr()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All extended amal for analytics
    val allAmal: StateFlow<List<AmalEntry>> = repository.observeAllAmal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All squads
    val allSquads: StateFlow<List<Squad>> = repository.observeAllSquads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current user profile
    val userProfile: StateFlow<UserProfile> = repository.currentUserProfile

    // Sync Manager status
    val isSyncing: StateFlow<Boolean> = repository.syncManager.isSyncing
    val lastSyncTimestamp: StateFlow<Long> = repository.syncManager.lastSyncTimestamp
    val syncLogs = repository.syncManager.syncLogs
    val auditLogs = repository.auditLogs
    val appsScriptUrl = repository.appsScriptUrl
    val syncSecretToken = repository.syncSecretToken

    // Dynamic Prayer times & Next Tahajjud calculation
    private val _nextFajrTime = MutableStateFlow("")
    val nextFajrTime: StateFlow<String> = _nextFajrTime.asStateFlow()

    private val _nextTahajjudReminderTime = MutableStateFlow("")
    val nextTahajjudReminderTime: StateFlow<String> = _nextTahajjudReminderTime.asStateFlow()

    private val _hoursUntilTahajjud = MutableStateFlow("")
    val hoursUntilTahajjud: StateFlow<String> = _hoursUntilTahajjud.asStateFlow()

    // Active Tasbih state
    private val _activeDhikrPreset = MutableStateFlow(AuthenticDhikrCatalog.presets.first())
    val activeDhikrPreset: StateFlow<DhikrPreset> = _activeDhikrPreset.asStateFlow()

    private val _currentDhikrCount = MutableStateFlow(0)
    val currentDhikrCount: StateFlow<Int> = _currentDhikrCount.asStateFlow()

    private val _dhikrTarget = MutableStateFlow(33)
    val dhikrTarget: StateFlow<Int> = _dhikrTarget.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(true)
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    // Notification toast / banner message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        refreshPrayerSchedule()
    }

    fun refreshPrayerSchedule() {
        val profile = userProfile.value
        val (fajrCal, tahajjudCal) = TahajjudAlarmScheduler.scheduleNextAlarm(
            getApplication(),
            latitude = profile.latitude,
            longitude = profile.longitude,
            offsetMinutes = profile.tahajjudAlarmOffsetMinutes
        )

        _nextFajrTime.value = PrayerTimesCalculator.formatTime(fajrCal)
        _nextTahajjudReminderTime.value = PrayerTimesCalculator.formatTime(tahajjudCal)

        val diffMillis = tahajjudCal.timeInMillis - System.currentTimeMillis()
        if (diffMillis > 0) {
            val hours = diffMillis / (1000 * 60 * 60)
            val minutes = (diffMillis % (1000 * 60 * 60)) / (1000 * 60)
            _hoursUntilTahajjud.value = if (profile.language == "bn") {
                "$hours ঘণ্টা $minutes মিনিট বাকি"
            } else {
                "${hours}h ${minutes}m remaining"
            }
        } else {
            _hoursUntilTahajjud.value = if (profile.language == "bn") "সময় নিকটবর্তী" else "Approaching soon"
        }
    }

    fun selectDate(dateString: String) {
        _selectedDate.value = dateString
    }

    fun navigateDateByDays(days: Int) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val current = sdf.parse(_selectedDate.value) ?: Date()
            val cal = Calendar.getInstance().apply {
                time = current
                add(Calendar.DAY_OF_MONTH, days)
            }
            _selectedDate.value = sdf.format(cal.time)
        } catch (e: Exception) {
            _selectedDate.value = AmalRepository.getTodayDateString()
        }
    }

    fun setPrayerStatus(prayerType: PrayerType, status: PrayerStatus, notes: String = "") {
        viewModelScope.launch {
            repository.setPrayerStatus(_selectedDate.value, prayerType, status, notes)
            triggerAutoSyncIfEnabled()
        }
    }

    fun recordTahajjud(performed: Boolean, rakahs: Int, startTime: String, endTime: String, notes: String) {
        viewModelScope.launch {
            repository.saveTahajjud(_selectedDate.value, performed, rakahs, startTime, endTime, notes)
            triggerAutoSyncIfEnabled()
            _userMessage.value = if (performed) "তাহাজ্জুদের আমল সফলভাবে সংরক্ষিত হয়েছে!" else "তাহাজ্জুদের রেকর্ড আপডেট হয়েছে।"
        }
    }

    // Extended Amal methods
    fun updateAmalStatus(entry: AmalEntry, newStatus: AmalStatus, count: Int = entry.currentCount) {
        viewModelScope.launch {
            val updated = entry.copy(
                status = newStatus,
                currentCount = count,
                localDate = _selectedDate.value,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveAmalEntry(updated)
            triggerAutoSyncIfEnabled()
        }
    }

    fun addNewCustomAmal(title: String, category: AmalCategory, targetCount: Int, unit: String) {
        viewModelScope.launch {
            val newEntry = AmalEntry(
                localDate = _selectedDate.value,
                category = category,
                title = title,
                targetCount = targetCount,
                unit = unit,
                status = AmalStatus.NOT_RECORDED
            )
            repository.saveAmalEntry(newEntry)
            triggerAutoSyncIfEnabled()
            _userMessage.value = "নতুন আমল লক্ষ্য যুক্ত করা হয়েছে।"
        }
    }

    fun deleteAmal(id: String) {
        viewModelScope.launch {
            repository.deleteAmalEntry(id)
            _userMessage.value = "আমল লক্ষ্য মুছে ফেলা হয়েছে।"
        }
    }

    // Squad methods
    fun createSquad(name: String, description: String, isPrivate: Boolean) {
        viewModelScope.launch {
            val squad = repository.createSquad(name, description, isPrivate)
            _userMessage.value = "স্কোয়াড '${squad.name}' তৈরি হয়েছে! কোড: ${squad.inviteCode}"
        }
    }

    fun joinSquad(inviteCode: String) {
        viewModelScope.launch {
            val res = repository.joinSquad(inviteCode)
            res.onSuccess { squad ->
                _userMessage.value = "আলহামদুলিল্লাহ! '${squad.name}' স্কোয়াডে যুক্ত হয়েছেন।"
            }.onFailure { err ->
                _userMessage.value = "ব্যর্থ: ${err.message}"
            }
        }
    }

    fun leaveSquad(squadId: String) {
        viewModelScope.launch {
            repository.leaveSquad(squadId)
            _userMessage.value = "স্কোয়াড ত্যাগ সম্পন্ন হয়েছে।"
        }
    }

    fun setSharingConsentScope(scope: SharingConsentScope) {
        repository.updateSharingConsentScope(scope)
        _userMessage.value = "শেয়ারিং প্রাইভেসি মোড আপডেট হয়েছে: ${scope.labelBn}"
    }

    fun selectDhikrPreset(preset: DhikrPreset) {
        _activeDhikrPreset.value = preset
        _dhikrTarget.value = preset.defaultTarget
        _currentDhikrCount.value = 0
    }

    fun incrementDhikr() {
        val next = _currentDhikrCount.value + 1
        _currentDhikrCount.value = next

        if (_vibrationEnabled.value) {
            vibrate(if (next % _dhikrTarget.value == 0) 100 else 30)
        }

        if (next % _dhikrTarget.value == 0) {
            val preset = _activeDhikrPreset.value
            viewModelScope.launch {
                repository.recordDhikrSession(
                    date = AmalRepository.getTodayDateString(),
                    dhikrId = preset.id,
                    dhikrTitle = preset.bengaliTransliteration,
                    count = _dhikrTarget.value,
                    targetCount = _dhikrTarget.value
                )
                triggerAutoSyncIfEnabled()
            }
        }
    }

    fun resetDhikr() {
        _currentDhikrCount.value = 0
    }

    fun toggleVibration() {
        _vibrationEnabled.value = !_vibrationEnabled.value
    }

    fun updateDhikrTarget(newTarget: Int) {
        if (newTarget > 0) {
            _dhikrTarget.value = newTarget
        }
    }

    fun triggerGoogleSheetsSync() {
        viewModelScope.launch {
            val result = repository.syncManager.syncPendingRecords(
                scriptUrl = repository.appsScriptUrl.value,
                token = repository.syncSecretToken.value,
                userProfile = repository.currentUserProfile.value
            )
            result.onSuccess { count ->
                _userMessage.value = "$count টি রেকর্ড গুগল শিটে সিঙ্ক সম্পন্ন হয়েছে।"
            }.onFailure { err ->
                _userMessage.value = "সিঙ্ক ব্যর্থ: ${err.message}"
            }
        }
    }

    private fun triggerAutoSyncIfEnabled() {
        if (userProfile.value.consentGiven && repository.appsScriptUrl.value.isNotBlank()) {
            viewModelScope.launch {
                repository.syncManager.syncPendingRecords(
                    scriptUrl = repository.appsScriptUrl.value,
                    token = repository.syncSecretToken.value,
                    userProfile = repository.currentUserProfile.value
                )
            }
        }
    }

    // Smart Alarm configuration & Ringing State
    val smartAlarmConfig: StateFlow<SmartAlarmConfig> = repository.smartAlarmConfig

    private val _isAlarmRinging = MutableStateFlow(false)
    val isAlarmRinging: StateFlow<Boolean> = _isAlarmRinging.asStateFlow()

    fun setAlarmRinging(ringing: Boolean) {
        _isAlarmRinging.value = ringing
        if (!ringing) {
            AlarmRingingManager.stopRinging()
        }
    }

    fun updateSmartAlarmConfig(config: SmartAlarmConfig) {
        repository.updateSmartAlarmConfig(config)
        _userMessage.value = "স্মার্ট অ্যালার্ম সেটিংস সংরক্ষিত হয়েছে।"
    }

    fun regenerateQrToken(): String {
        val token = repository.regenerateQrToken()
        _userMessage.value = "নতুন কিউআর কোড টোকেন তৈরি হয়েছে!"
        return token
    }

    fun registerPhotoTarget(signature: String, targetName: String) {
        repository.registerPhotoTarget(signature, targetName)
        _userMessage.value = "টার্গেট ছবি '$targetName' সফলভাবে নিবন্ধিত হয়েছে!"
    }

    fun clearPhotoTarget() {
        repository.clearPhotoTarget()
        _userMessage.value = "নিবন্ধিত টার্গেট ছবি মুছে ফেলা হয়েছে।"
    }

    fun stopAlarmRinging() {
        _isAlarmRinging.value = false
        AlarmRingingManager.stopRinging()
    }

    fun testTahajjudAlarm() {
        TahajjudAlarmScheduler.sendTestNotification(getApplication())
        _userMessage.value = "টেস্ট নোটিফিকেশন পাঠানো হয়েছে। স্ট্যাটাস বার চেক করুন।"
    }

    fun setLanguage(lang: String) {
        repository.updateLanguage(lang)
        refreshPrayerSchedule()
    }

    fun setConsent(given: Boolean) {
        repository.updateConsent(given)
    }

    fun setSuperAdmin(isAdmin: Boolean) {
        repository.updateSuperAdminStatus(isAdmin)
    }

    fun updateAppsScriptConfig(url: String, token: String) {
        repository.updateAppsScriptSettings(url, token)
        _userMessage.value = "গুগল শিট স্ক্রিপ্ট কনফিগারেশন আপডেট হয়েছে।"
    }

    fun wipeAllData() {
        viewModelScope.launch {
            repository.deleteAllUserData()
            _userMessage.value = "আপনার সমস্ত রেকর্ড সম্পূর্ণভাবে মুছে ফেলা হয়েছে।"
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    private fun vibrate(durationMillis: Long) {
        try {
            val vibrator = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    vibrator.vibrate(durationMillis)
                }
            }
        } catch (_: Exception) {}
    }
}
