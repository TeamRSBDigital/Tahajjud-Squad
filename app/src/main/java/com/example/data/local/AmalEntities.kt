package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.*

@Entity(tableName = "prayer_records")
data class PrayerEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val localDate: String,
    val prayerName: String,
    val status: String,
    val notes: String,
    val syncState: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): PrayerRecord = PrayerRecord(
        id = id,
        userId = userId,
        localDate = localDate,
        prayerName = prayerName,
        status = runCatching { PrayerStatus.valueOf(status) }.getOrDefault(PrayerStatus.NOT_RECORDED),
        notes = notes,
        syncState = runCatching { SyncState.valueOf(syncState) }.getOrDefault(SyncState.PENDING),
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(domain: PrayerRecord): PrayerEntity = PrayerEntity(
            id = domain.id,
            userId = domain.userId,
            localDate = domain.localDate,
            prayerName = domain.prayerName,
            status = domain.status.name,
            notes = domain.notes,
            syncState = domain.syncState.name,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
}

@Entity(tableName = "tahajjud_records")
data class TahajjudEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val localDate: String,
    val performed: Boolean,
    val rakahs: Int,
    val startTime: String,
    val endTime: String,
    val notes: String,
    val syncState: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): TahajjudRecord = TahajjudRecord(
        id = id,
        userId = userId,
        localDate = localDate,
        performed = performed,
        rakahs = rakahs,
        startTime = startTime,
        endTime = endTime,
        notes = notes,
        syncState = runCatching { SyncState.valueOf(syncState) }.getOrDefault(SyncState.PENDING),
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(domain: TahajjudRecord): TahajjudEntity = TahajjudEntity(
            id = domain.id,
            userId = domain.userId,
            localDate = domain.localDate,
            performed = domain.performed,
            rakahs = domain.rakahs,
            startTime = domain.startTime,
            endTime = domain.endTime,
            notes = domain.notes,
            syncState = domain.syncState.name,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
}

@Entity(tableName = "dhikr_records")
data class DhikrEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val localDate: String,
    val dhikrId: String,
    val dhikrTitle: String,
    val count: Int,
    val targetCount: Int,
    val syncState: String,
    val createdAt: Long
) {
    fun toDomain(): DhikrRecord = DhikrRecord(
        id = id,
        userId = userId,
        localDate = localDate,
        dhikrId = dhikrId,
        dhikrTitle = dhikrTitle,
        count = count,
        targetCount = targetCount,
        syncState = runCatching { SyncState.valueOf(syncState) }.getOrDefault(SyncState.PENDING),
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(domain: DhikrRecord): DhikrEntity = DhikrEntity(
            id = domain.id,
            userId = domain.userId,
            localDate = domain.localDate,
            dhikrId = domain.dhikrId,
            dhikrTitle = domain.dhikrTitle,
            count = domain.count,
            targetCount = domain.targetCount,
            syncState = domain.syncState.name,
            createdAt = domain.createdAt
        )
    }
}

@Entity(tableName = "amal_entries")
data class AmalEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val localDate: String,
    val category: String,
    val title: String,
    val description: String,
    val targetCount: Int,
    val currentCount: Int,
    val unit: String,
    val status: String,
    val notes: String,
    val isSharedWithSquad: Boolean,
    val syncState: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): AmalEntry = AmalEntry(
        id = id,
        userId = userId,
        localDate = localDate,
        category = runCatching { AmalCategory.valueOf(category) }.getOrDefault(AmalCategory.CUSTOM),
        title = title,
        description = description,
        targetCount = targetCount,
        currentCount = currentCount,
        unit = unit,
        status = runCatching { AmalStatus.valueOf(status) }.getOrDefault(AmalStatus.NOT_RECORDED),
        notes = notes,
        isSharedWithSquad = isSharedWithSquad,
        syncState = runCatching { SyncState.valueOf(syncState) }.getOrDefault(SyncState.PENDING),
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(domain: AmalEntry): AmalEntity = AmalEntity(
            id = domain.id,
            userId = domain.userId,
            localDate = domain.localDate,
            category = domain.category.name,
            title = domain.title,
            description = domain.description,
            targetCount = domain.targetCount,
            currentCount = domain.currentCount,
            unit = domain.unit,
            status = domain.status.name,
            notes = domain.notes,
            isSharedWithSquad = domain.isSharedWithSquad,
            syncState = domain.syncState.name,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
}

@Entity(tableName = "squads")
data class SquadEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val ownerUid: String,
    val inviteCode: String,
    val isPrivate: Boolean,
    val memberCount: Int,
    val createdAt: Long
) {
    fun toDomain(): Squad = Squad(
        id = id,
        name = name,
        description = description,
        ownerUid = ownerUid,
        inviteCode = inviteCode,
        isPrivate = isPrivate,
        memberCount = memberCount,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(domain: Squad): SquadEntity = SquadEntity(
            id = domain.id,
            name = domain.name,
            description = domain.description,
            ownerUid = domain.ownerUid,
            inviteCode = domain.inviteCode,
            isPrivate = domain.isPrivate,
            memberCount = domain.memberCount,
            createdAt = domain.createdAt
        )
    }
}

@Entity(tableName = "squad_members", primaryKeys = ["uid", "squadId"])
data class SquadMemberEntity(
    val uid: String,
    val squadId: String,
    val displayName: String,
    val role: String,
    val joinedAt: Long,
    val shareMode: String
) {
    fun toDomain(): SquadMember = SquadMember(
        uid = uid,
        squadId = squadId,
        displayName = displayName,
        role = runCatching { SquadRole.valueOf(role) }.getOrDefault(SquadRole.MEMBER),
        joinedAt = joinedAt,
        shareMode = runCatching { SharingConsentScope.valueOf(shareMode) }.getOrDefault(SharingConsentScope.AGGREGATE_ONLY)
    )

    companion object {
        fun fromDomain(domain: SquadMember): SquadMemberEntity = SquadMemberEntity(
            uid = domain.uid,
            squadId = domain.squadId,
            displayName = domain.displayName,
            role = domain.role.name,
            joinedAt = domain.joinedAt,
            shareMode = domain.shareMode.name
        )
    }
}

@Entity(tableName = "durable_sync_jobs")
data class DurableSyncJobEntity(
    @PrimaryKey val jobId: String,
    val sourceType: String,
    val sourceId: String,
    val eventVersion: Int,
    val status: String,
    val attemptCount: Int,
    val nextRetryAt: Long,
    val lastError: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): DurableSyncJob = DurableSyncJob(
        jobId = jobId,
        sourceType = sourceType,
        sourceId = sourceId,
        eventVersion = eventVersion,
        status = runCatching { JobStatus.valueOf(status) }.getOrDefault(JobStatus.PENDING),
        attemptCount = attemptCount,
        nextRetryAt = nextRetryAt,
        lastError = lastError,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(domain: DurableSyncJob): DurableSyncJobEntity = DurableSyncJobEntity(
            jobId = domain.jobId,
            sourceType = domain.sourceType,
            sourceId = domain.sourceId,
            eventVersion = domain.eventVersion,
            status = domain.status.name,
            attemptCount = domain.attemptCount,
            nextRetryAt = domain.nextRetryAt,
            lastError = domain.lastError,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
}
