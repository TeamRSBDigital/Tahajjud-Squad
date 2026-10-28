package com.example.sync

import android.content.Context
import android.util.Log
import com.example.data.local.AmalDatabase
import com.example.data.local.DurableSyncJobEntity
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class GoogleSheetsSyncManager(
    private val context: Context,
    private val database: AmalDatabase
) {
    private val _syncLogs = MutableStateFlow<List<SyncLogItem>>(emptyList())
    val syncLogs: StateFlow<List<SyncLogItem>> = _syncLogs.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(0L)
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    suspend fun syncPendingRecords(
        scriptUrl: String,
        token: String,
        userProfile: UserProfile
    ): Result<Int> = withContext(Dispatchers.IO) {
        if (!userProfile.consentGiven) {
            val errorMsg = "সম্মতি প্রত্যাহার করা হয়েছে। সিঙ্ক বাতিল করা হলো। (Sync skipped: User consent withdrawn)"
            addLog("SYNC_CANCELLED", 0, "SKIPPED", errorMsg)
            return@withContext Result.failure(IllegalStateException(errorMsg))
        }

        if (scriptUrl.isBlank()) {
            val errorMsg = "গুগল শিট স্ক্রিপ্ট URL কনফিগার করা হয়নি (Google Sheets Web App URL not configured)"
            addLog("CONFIG_ERROR", 0, "FAILED", errorMsg)
            return@withContext Result.failure(IllegalArgumentException(errorMsg))
        }

        _isSyncing.value = true

        val jobId = "job_" + UUID.randomUUID().toString().take(8)
        val now = System.currentTimeMillis()

        // Create durable job record
        val jobEntity = DurableSyncJobEntity(
            jobId = jobId,
            sourceType = "BATCH_SYNC",
            sourceId = userProfile.uid,
            eventVersion = 1,
            status = JobStatus.PROCESSING.name,
            attemptCount = 1,
            nextRetryAt = 0L,
            lastError = "",
            createdAt = now,
            updatedAt = now
        )
        database.syncJobDao().insertJob(jobEntity)

        try {
            val pendingPrayers = database.prayerDao().getPendingSyncPrayers()
            val pendingTahajjud = database.tahajjudDao().getPendingSyncTahajjud()
            val pendingDhikr = database.dhikrDao().getPendingSyncDhikr()
            val pendingAmal = database.amalDao().getPendingSyncAmal()

            val totalRecords = pendingPrayers.size + pendingTahajjud.size + pendingDhikr.size + pendingAmal.size

            if (totalRecords == 0) {
                _isSyncing.value = false
                database.syncJobDao().updateJobStatus(jobId, JobStatus.SUCCEEDED.name, 1, "", System.currentTimeMillis())
                addLog("BATCH_SYNC", 0, "SUCCESS", "কোনো নতুন রেকর্ড নেই। সমস্ত আমল ডেটা সিঙ্ক করা আছে।")
                return@withContext Result.success(0)
            }

            val includeDetailedNotes = userProfile.consentScope == SharingConsentScope.DETAILED

            // Build JSON payload
            val rootObj = JSONObject().apply {
                put("action", "SYNC_BATCH")
                put("token", token)
                val dataObj = JSONObject().apply {
                    // User metadata & consent scope
                    put("user", JSONObject().apply {
                        put("uid", userProfile.uid)
                        put("displayName", userProfile.displayName)
                        put("email", userProfile.email)
                        put("timezone", userProfile.timezone)
                        put("consentGiven", userProfile.consentGiven)
                        put("consentScope", userProfile.consentScope.name)
                        put("activeSquadId", userProfile.activeSquadId)
                    })

                    // Prayers
                    val prayerArray = JSONArray()
                    pendingPrayers.forEach { p ->
                        prayerArray.put(JSONObject().apply {
                            put("id", p.id)
                            put("userId", p.userId)
                            put("localDate", p.localDate)
                            put("prayerName", p.prayerName)
                            put("status", p.status)
                            put("notes", if (includeDetailedNotes) p.notes else "")
                            put("createdAt", p.createdAt)
                            put("updatedAt", p.updatedAt)
                        })
                    }
                    put("prayers", prayerArray)

                    // Tahajjud
                    val tahajjudArray = JSONArray()
                    pendingTahajjud.forEach { t ->
                        tahajjudArray.put(JSONObject().apply {
                            put("id", t.id)
                            put("userId", t.userId)
                            put("localDate", t.localDate)
                            put("performed", t.performed)
                            put("rakahs", t.rakahs)
                            put("startTime", t.startTime)
                            put("endTime", t.endTime)
                            put("notes", if (includeDetailedNotes) t.notes else "")
                            put("createdAt", t.createdAt)
                            put("updatedAt", t.updatedAt)
                        })
                    }
                    put("tahajjud", tahajjudArray)

                    // Dhikr
                    val dhikrArray = JSONArray()
                    pendingDhikr.forEach { d ->
                        dhikrArray.put(JSONObject().apply {
                            put("id", d.id)
                            put("userId", d.userId)
                            put("localDate", d.localDate)
                            put("dhikrTitle", d.dhikrTitle)
                            put("count", d.count)
                            put("targetCount", d.targetCount)
                            put("createdAt", d.createdAt)
                        })
                    }
                    put("dhikr", dhikrArray)

                    // Extended Amal Entries
                    val amalArray = JSONArray()
                    pendingAmal.forEach { a ->
                        amalArray.put(JSONObject().apply {
                            put("id", a.id)
                            put("userId", a.userId)
                            put("localDate", a.localDate)
                            put("category", a.category)
                            put("title", a.title)
                            put("currentCount", a.currentCount)
                            put("targetCount", a.targetCount)
                            put("unit", a.unit)
                            put("status", a.status)
                            put("notes", if (includeDetailedNotes) a.notes else "")
                            put("isSharedWithSquad", a.isSharedWithSquad)
                            put("createdAt", a.createdAt)
                            put("updatedAt", a.updatedAt)
                        })
                    }
                    put("amal", amalArray)
                }
                put("data", dataObj)
            }

            val url = URL(scriptUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15000
                readTimeout = 15000
                doOutput = true
                instanceFollowRedirects = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
            }

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(rootObj.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                // Success: mark local records as SYNCED
                pendingPrayers.forEach { database.prayerDao().updateSyncState(it.id, SyncState.SYNCED.name) }
                pendingTahajjud.forEach { database.tahajjudDao().updateSyncState(it.id, SyncState.SYNCED.name) }
                pendingDhikr.forEach { database.dhikrDao().updateSyncState(it.id, SyncState.SYNCED.name) }
                pendingAmal.forEach { database.amalDao().updateSyncState(it.id, SyncState.SYNCED.name) }

                database.syncJobDao().updateJobStatus(jobId, JobStatus.SUCCEEDED.name, 1, "", System.currentTimeMillis())
                _lastSyncTimestamp.value = System.currentTimeMillis()
                addLog("BATCH_SYNC", totalRecords, "SUCCESS", "$totalRecords টি রেকর্ড (সালাত, তাহাজ্জুদ, আযকার ও আমল) গুগল শিটে সফলভাবে সংরক্ষিত হয়েছে।")
                Result.success(totalRecords)
            } else {
                val errorMsg = "সার্ভার রেসপন্স কোড: $responseCode"
                database.syncJobDao().updateJobStatus(jobId, JobStatus.RETRY_SCHEDULED.name, 1, errorMsg, System.currentTimeMillis())
                addLog("BATCH_SYNC", totalRecords, "FAILED", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("AmalSync", "Error syncing with Google Sheets", e)
            val errorMsg = e.localizedMessage ?: "নেটওয়ার্ক সংযোগ বিঘ্নিত"
            database.syncJobDao().updateJobStatus(jobId, JobStatus.RETRY_SCHEDULED.name, 1, errorMsg, System.currentTimeMillis())
            addLog("BATCH_SYNC", 0, "FAILED", errorMsg)
            Result.failure(e)
        } finally {
            _isSyncing.value = false
        }
    }

    private fun addLog(type: String, count: Int, status: String, message: String) {
        val newItem = SyncLogItem(
            id = System.currentTimeMillis().toString(),
            timestamp = System.currentTimeMillis(),
            recordType = type,
            recordCount = count,
            status = status,
            message = message
        )
        _syncLogs.value = listOf(newItem) + _syncLogs.value.take(25)
    }
}
