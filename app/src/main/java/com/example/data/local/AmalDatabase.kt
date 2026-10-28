package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerDao {
    @Query("SELECT * FROM prayer_records WHERE userId = :userId AND localDate = :date")
    fun getPrayersByDate(userId: String, date: String): Flow<List<PrayerEntity>>

    @Query("SELECT * FROM prayer_records WHERE userId = :userId ORDER BY localDate DESC")
    fun getAllPrayers(userId: String): Flow<List<PrayerEntity>>

    @Query("SELECT * FROM prayer_records WHERE userId = :userId AND localDate = :date AND prayerName = :prayerName LIMIT 1")
    suspend fun getPrayer(userId: String, date: String, prayerName: String): PrayerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePrayer(entity: PrayerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePrayers(entities: List<PrayerEntity>)

    @Query("SELECT * FROM prayer_records WHERE syncState = 'PENDING'")
    suspend fun getPendingSyncPrayers(): List<PrayerEntity>

    @Query("UPDATE prayer_records SET syncState = :syncState WHERE id = :id")
    suspend fun updateSyncState(id: String, syncState: String)

    @Query("DELETE FROM prayer_records WHERE userId = :userId")
    suspend fun clearUserPrayers(userId: String)
}

@Dao
interface TahajjudDao {
    @Query("SELECT * FROM tahajjud_records WHERE userId = :userId AND localDate = :date LIMIT 1")
    fun getTahajjudByDate(userId: String, date: String): Flow<TahajjudEntity?>

    @Query("SELECT * FROM tahajjud_records WHERE userId = :userId ORDER BY localDate DESC")
    fun getAllTahajjud(userId: String): Flow<List<TahajjudEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTahajjud(entity: TahajjudEntity)

    @Query("SELECT * FROM tahajjud_records WHERE syncState = 'PENDING'")
    suspend fun getPendingSyncTahajjud(): List<TahajjudEntity>

    @Query("UPDATE tahajjud_records SET syncState = :syncState WHERE id = :id")
    suspend fun updateSyncState(id: String, syncState: String)

    @Query("DELETE FROM tahajjud_records WHERE userId = :userId")
    suspend fun clearUserTahajjud(userId: String)
}

@Dao
interface DhikrDao {
    @Query("SELECT * FROM dhikr_records WHERE userId = :userId AND localDate = :date")
    fun getDhikrByDate(userId: String, date: String): Flow<List<DhikrEntity>>

    @Query("SELECT * FROM dhikr_records WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllDhikr(userId: String): Flow<List<DhikrEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDhikr(entity: DhikrEntity)

    @Query("SELECT * FROM dhikr_records WHERE syncState = 'PENDING'")
    suspend fun getPendingSyncDhikr(): List<DhikrEntity>

    @Query("UPDATE dhikr_records SET syncState = :syncState WHERE id = :id")
    suspend fun updateSyncState(id: String, syncState: String)

    @Query("DELETE FROM dhikr_records WHERE userId = :userId")
    suspend fun clearUserDhikr(userId: String)
}

@Dao
interface AmalDao {
    @Query("SELECT * FROM amal_entries WHERE userId = :userId AND localDate = :date")
    fun getAmalByDate(userId: String, date: String): Flow<List<AmalEntity>>

    @Query("SELECT * FROM amal_entries WHERE userId = :userId ORDER BY localDate DESC")
    fun getAllAmal(userId: String): Flow<List<AmalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAmal(entity: AmalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAmalList(entities: List<AmalEntity>)

    @Query("SELECT * FROM amal_entries WHERE syncState = 'PENDING'")
    suspend fun getPendingSyncAmal(): List<AmalEntity>

    @Query("UPDATE amal_entries SET syncState = :syncState WHERE id = :id")
    suspend fun updateSyncState(id: String, syncState: String)

    @Query("DELETE FROM amal_entries WHERE id = :id")
    suspend fun deleteAmalById(id: String)

    @Query("DELETE FROM amal_entries WHERE userId = :userId")
    suspend fun clearUserAmal(userId: String)
}

@Dao
interface SquadDao {
    @Query("SELECT * FROM squads ORDER BY createdAt DESC")
    fun getAllSquads(): Flow<List<SquadEntity>>

    @Query("SELECT * FROM squads WHERE id = :squadId LIMIT 1")
    suspend fun getSquadById(squadId: String): SquadEntity?

    @Query("SELECT * FROM squads WHERE inviteCode = :code LIMIT 1")
    suspend fun getSquadByInviteCode(code: String): SquadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSquad(entity: SquadEntity)

    @Query("SELECT * FROM squad_members WHERE squadId = :squadId")
    fun getMembersForSquad(squadId: String): Flow<List<SquadMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMember(entity: SquadMemberEntity)

    @Query("DELETE FROM squad_members WHERE uid = :uid AND squadId = :squadId")
    suspend fun removeMember(uid: String, squadId: String)

    @Query("SELECT * FROM squad_members WHERE uid = :uid")
    fun getSquadsForUser(uid: String): Flow<List<SquadMemberEntity>>
}

@Dao
interface SyncJobDao {
    @Query("SELECT * FROM durable_sync_jobs WHERE status IN ('PENDING', 'RETRY_SCHEDULED') ORDER BY createdAt ASC")
    suspend fun getPendingJobs(): List<DurableSyncJobEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(entity: DurableSyncJobEntity)

    @Query("UPDATE durable_sync_jobs SET status = :status, attemptCount = :attemptCount, lastError = :error, updatedAt = :updatedAt WHERE jobId = :jobId")
    suspend fun updateJobStatus(jobId: String, status: String, attemptCount: Int, error: String, updatedAt: Long)

    @Query("SELECT * FROM durable_sync_jobs ORDER BY updatedAt DESC LIMIT 50")
    fun getRecentJobs(): Flow<List<DurableSyncJobEntity>>
}

@Database(
    entities = [
        PrayerEntity::class,
        TahajjudEntity::class,
        DhikrEntity::class,
        AmalEntity::class,
        SquadEntity::class,
        SquadMemberEntity::class,
        DurableSyncJobEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AmalDatabase : RoomDatabase() {
    abstract fun prayerDao(): PrayerDao
    abstract fun tahajjudDao(): TahajjudDao
    abstract fun dhikrDao(): DhikrDao
    abstract fun amalDao(): AmalDao
    abstract fun squadDao(): SquadDao
    abstract fun syncJobDao(): SyncJobDao
}
