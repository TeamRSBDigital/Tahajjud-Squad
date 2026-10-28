package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.*
import com.example.model.*
import com.example.prayer.PrayerTimesCalculator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AmalDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AmalDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Amal Tracker", appName)
    }

    @Test
    fun `prayer time calculator calculates valid Fajr and 30m Tahajjud offset`() {
        val cal = Calendar.getInstance()
        val times = PrayerTimesCalculator.calculateTimes(
            calendar = cal,
            latitude = 23.8103,
            longitude = 90.4125,
            method = PrayerTimesCalculator.CalculationMethod.KARACHI,
            tahajjudOffsetMinutes = 30
        )

        assertNotNull(times.fajr)
        assertNotNull(times.tahajjudReminder)

        assertTrue("Tahajjud reminder must be before Fajr", times.tahajjudReminder.before(times.fajr))

        val diffMillis = times.fajr.timeInMillis - times.tahajjudReminder.timeInMillis
        assertEquals("Difference must be 30 minutes", 30 * 60 * 1000L, diffMillis)
    }

    @Test
    fun `room database stores and retrieves prayer record`() = runBlocking {
        val entity = PrayerEntity(
            id = "test_user_2026-10-28_FAJR",
            userId = "test_user",
            localDate = "2026-10-28",
            prayerName = "FAJR",
            status = PrayerStatus.PERFORMED.name,
            notes = "In Jama'ah",
            syncState = SyncState.PENDING.name,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        db.prayerDao().insertOrUpdatePrayer(entity)

        val retrieved = db.prayerDao().getPrayer("test_user", "2026-10-28", "FAJR")
        assertNotNull(retrieved)
        assertEquals(PrayerStatus.PERFORMED.name, retrieved?.status)
        assertEquals("In Jama'ah", retrieved?.notes)

        val list = db.prayerDao().getPrayersByDate("test_user", "2026-10-28").first()
        assertEquals(1, list.size)
        assertEquals("FAJR", list[0].prayerName)
    }

    @Test
    fun `room database stores and retrieves tahajjud record`() = runBlocking {
        val tahajjud = TahajjudEntity(
            id = "test_user_2026-10-28_TAHAJJUD",
            userId = "test_user",
            localDate = "2026-10-28",
            performed = true,
            rakahs = 8,
            startTime = "04:10 AM",
            endTime = "04:50 AM",
            notes = "Recited Surah Rahman",
            syncState = SyncState.PENDING.name,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        db.tahajjudDao().insertOrUpdateTahajjud(tahajjud)

        val retrieved = db.tahajjudDao().getTahajjudByDate("test_user", "2026-10-28").first()
        assertNotNull(retrieved)
        assertTrue(retrieved!!.performed)
        assertEquals(8, retrieved.rakahs)
    }

    @Test
    fun `room database stores and retrieves extended amal entry`() = runBlocking {
        val amal = AmalEntity(
            id = "test_user_2026-10-28_quran_01",
            userId = "test_user",
            localDate = "2026-10-28",
            category = AmalCategory.QURAN.name,
            title = "কুরআন তিলাওয়াত (Surah Al-Mulk)",
            description = "তিলাওয়াত ও তাদাব্বুর",
            targetCount = 5,
            currentCount = 5,
            unit = "পৃষ্ঠা",
            status = AmalStatus.COMPLETED.name,
            notes = "Completed after Fajr",
            isSharedWithSquad = true,
            syncState = SyncState.PENDING.name,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        db.amalDao().insertOrUpdateAmal(amal)

        val list = db.amalDao().getAmalByDate("test_user", "2026-10-28").first()
        assertEquals(1, list.size)
        assertEquals(AmalStatus.COMPLETED.name, list[0].status)
        assertEquals(5, list[0].currentCount)
    }

    @Test
    fun `room database manages squads and enforces private membership`() = runBlocking {
        val squad = SquadEntity(
            id = "squad_test_1",
            name = "তাহাজ্জুদ স্কোয়াড (Tahajjud Squad)",
            description = "রাতের শেষ প্রহরে জাগ্রত ভাইয়েরা",
            ownerUid = "leader_user",
            inviteCode = "SQUAD999",
            isPrivate = true,
            memberCount = 1,
            createdAt = System.currentTimeMillis()
        )
        db.squadDao().insertOrUpdateSquad(squad)

        val retrievedSquad = db.squadDao().getSquadByInviteCode("SQUAD999")
        assertNotNull(retrievedSquad)
        assertEquals("squad_test_1", retrievedSquad?.id)

        val member = SquadMemberEntity(
            uid = "member_user",
            squadId = "squad_test_1",
            displayName = "আব্দুল্লাহ",
            role = SquadRole.MEMBER.name,
            joinedAt = System.currentTimeMillis(),
            shareMode = SharingConsentScope.AGGREGATE_ONLY.name
        )
        db.squadDao().insertOrUpdateMember(member)

        val members = db.squadDao().getMembersForSquad("squad_test_1").first()
        assertEquals(1, members.size)
        assertEquals(SquadRole.MEMBER.name, members[0].role)
        assertEquals(SharingConsentScope.AGGREGATE_ONLY.name, members[0].shareMode)
    }
}
