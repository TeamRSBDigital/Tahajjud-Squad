package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AmalStatus
import com.example.model.PrayerStatus
import com.example.model.PrayerType
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.AmalViewModel

@Composable
fun HomeScreen(
    viewModel: AmalViewModel,
    onNavigateToPrayer: () -> Unit,
    onNavigateToTahajjud: () -> Unit,
    onNavigateToDhikr: () -> Unit,
    onNavigateToAmal: () -> Unit,
    onNavigateToSquad: () -> Unit,
    onNavigateToRecords: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isBengali = userProfile.language == "bn"
    val todayPrayers by viewModel.prayersForSelectedDate.collectAsState()
    val todayAmal by viewModel.amalForSelectedDate.collectAsState()
    val squads by viewModel.allSquads.collectAsState()
    val activeSquad = squads.find { it.id == userProfile.activeSquadId }

    val nextFajr by viewModel.nextFajrTime.collectAsState()
    val nextTahajjud by viewModel.nextTahajjudReminderTime.collectAsState()
    val hoursUntilTahajjud by viewModel.hoursUntilTahajjud.collectAsState()
    val activeDhikr by viewModel.activeDhikrPreset.collectAsState()
    val dhikrCount by viewModel.currentDhikrCount.collectAsState()
    val dhikrTarget by viewModel.dhikrTarget.collectAsState()
    val smartAlarmConfig by viewModel.smartAlarmConfig.collectAsState()

    val completedPrayersCount = todayPrayers.count { it.status == PrayerStatus.PERFORMED }
    val completedAmalCount = todayAmal.count { it.status == AmalStatus.COMPLETED }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Hero Header Card
        item {
            IslamicTopHeader(
                title = if (isBengali) "আসসালামু আলাইকুম" else "Assalamu Alaikum",
                subtitle = if (isBengali) "আজকের আমল, সালাত ও তাহাজ্জুদ পরিচালনা করুন" else "Track daily worship, prayer & Tahajjud squad",
                isBengali = isBengali,
                onLanguageToggle = {
                    viewModel.setLanguage(if (isBengali) "en" else "bn")
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                // Tahajjud Countdown Card (Clean White Surface with Vibrant Emerald & Gold Accents)
                IslamicCard(
                    backgroundColor = MinimalWhiteSurface,
                    borderColor = GoldPrimary.copy(alpha = 0.45f),
                    modifier = Modifier.clickable { onNavigateToTahajjud() }.testTag("home_tahajjud_card")
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(GoldSoft)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NightsStay,
                                        contentDescription = "Tahajjud",
                                        tint = GoldDark,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isBengali) "তাহাজ্জুদ রিমাইন্ডার" else "Tahajjud Reminder",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = TextDarkPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GoldSoft,
                                border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorder)
                            ) {
                                Text(
                                    text = if (isBengali) "ফজরের ৩০ মিনিট আগে" else "30m before Fajr",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = GoldDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isBengali) "পরবর্তী ফজর সময়" else "Next Fajr Time",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextDarkSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = nextFajr.ifEmpty { "04:45 AM" },
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = TextDarkPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp
                                    )
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isBengali) "তাহাজ্জুদ অ্যালার্ম" else "Tahajjud Alarm",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextDarkSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = nextTahajjud.ifEmpty { "04:15 AM" },
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = EmeraldPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Time remaining pill
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = EmeraldSoftBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Timer",
                                    tint = EmeraldDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = hoursUntilTahajjud,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = EmeraldDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        // Active Smart Mission label
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = GoldDark, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBengali) "স্মার্ট অ্যালার্ম মিশন: ${smartAlarmConfig.missionType.titleBn}" else "Smart Mission: ${smartAlarmConfig.missionType.titleEn}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = GoldDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Active Squad Banner Card
                IslamicCard(
                    backgroundColor = MinimalWhiteSurface,
                    borderColor = MinimalWhiteCardBorder,
                    modifier = Modifier.clickable { onNavigateToSquad() }.testTag("home_squad_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GoldSoft)
                            ) {
                                Icon(Icons.Default.Group, contentDescription = null, tint = GoldDark, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = activeSquad?.name ?: (if (isBengali) "তাহাজ্জুদ স্কোয়াডে যুক্ত হন" else "Join Tahajjud Squad"),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextDarkPrimary),
                                    maxLines = 1
                                )
                                Text(
                                    text = if (activeSquad != null) "শেয়ারিং মোড: ${userProfile.consentScope.labelBn}" else "একত্রে তাহাজ্জুদ ও আমলের বরকত নিন",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary, fontSize = 11.sp)
                                )
                            }
                        }

                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = EmeraldPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Five Daily Prayers Checklist Card (Crisp White Card with Colorful Badges)
                IslamicCard(
                    backgroundColor = MinimalWhiteSurface,
                    borderColor = MinimalWhiteCardBorder,
                    modifier = Modifier.clickable { onNavigateToPrayer() }.testTag("home_prayers_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isBengali) "আজকের ৫ ওয়াক্ত নামায" else "Today's 5 Daily Prayers",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = TextDarkPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isBengali) "ওয়াক্ত অনুযায়ী আদায় রেকর্ড করুন" else "Record your prayer performance",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary)
                            )
                        }

                        PrayerProgressRing(
                            completedCount = completedPrayersCount,
                            size = 54.dp,
                            strokeWidth = 5.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5 Prayer Icons & status indicators row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PrayerType.values().forEach { prayer ->
                            val record = todayPrayers.find { it.prayerName == prayer.name }
                            val isDone = record?.status == PrayerStatus.PERFORMED
                            val isMissed = record?.status == PrayerStatus.MISSED
                            val isQaza = record?.status == PrayerStatus.QAZA

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 2.dp)
                                    .clickable {
                                        val nextStatus = when (record?.status ?: PrayerStatus.NOT_RECORDED) {
                                            PrayerStatus.NOT_RECORDED -> PrayerStatus.PERFORMED
                                            PrayerStatus.PERFORMED -> PrayerStatus.MISSED
                                            PrayerStatus.MISSED -> PrayerStatus.QAZA
                                            PrayerStatus.QAZA -> PrayerStatus.NOT_RECORDED
                                        }
                                        viewModel.setPrayerStatus(prayer, nextStatus)
                                    }
                            ) {
                                val pillBg = when {
                                    isDone -> StatusPerformed
                                    isMissed -> StatusMissed
                                    isQaza -> StatusQaza
                                    else -> StatusUnrecordedLight
                                }
                                val pillBorder = when {
                                    isDone -> StatusPerformed
                                    isMissed -> StatusMissed
                                    isQaza -> StatusQaza
                                    else -> Color(0xFFCBD5E1)
                                }
                                val pillIconColor = when {
                                    isDone || isMissed || isQaza -> Color.White
                                    else -> StatusUnrecorded
                                }

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(pillBg)
                                        .border(
                                            width = 1.dp,
                                            color = pillBorder,
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = when {
                                            isDone -> Icons.Default.Check
                                            isMissed -> Icons.Default.Close
                                            isQaza -> Icons.Default.Replay
                                            else -> Icons.Default.AccessTime
                                        },
                                        contentDescription = prayer.englishName,
                                        tint = pillIconColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isBengali) prayer.bengaliName else prayer.englishName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = TextDarkPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Extended Amal Card (Quran, Adhkar, Sadaqah)
                IslamicCard(
                    backgroundColor = MinimalWhiteSurface,
                    borderColor = MinimalWhiteCardBorder,
                    modifier = Modifier.clickable { onNavigateToAmal() }.testTag("home_amal_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldSoftBg)
                                ) {
                                    Icon(
                                        Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBengali) "দৈনন্দিন আমল ট্র্যাকার" else "Daily Amal Tracker",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = TextDarkPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "কুরআন, সকাল-সন্ধ্যার দু'আ, সাদাকাহ ও দ্বীনি ইলম",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary, fontSize = 11.sp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = StatusPerformedLight,
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldBorder)
                        ) {
                            Text(
                                text = "$completedAmalCount/${todayAmal.size} সম্পন্ন",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = StatusPerformed,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Dhikr Tasbih Card (Clean White Surface with Vibrant Tap Button)
                IslamicCard(
                    backgroundColor = MinimalWhiteSurface,
                    borderColor = MinimalWhiteCardBorder,
                    modifier = Modifier.clickable { onNavigateToDhikr() }.testTag("home_dhikr_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(GoldSoft)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = "Tasbih",
                                        tint = GoldDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBengali) "ডিজিটাল তাসবীহ ও যিকির" else "Digital Tasbih & Dhikr",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = TextDarkPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = activeDhikr.arabicText,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isBengali) activeDhikr.bengaliTransliteration else activeDhikr.englishMeaning,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary)
                            )
                        }

                        // Tap Button for Dhikr
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(EmeraldSoftBg)
                                .border(
                                    width = 1.5.dp,
                                    color = EmeraldPrimary,
                                    shape = CircleShape
                                )
                                .clickable { viewModel.incrementDhikr() }
                                .testTag("home_dhikr_tap_button")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$dhikrCount",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = EmeraldDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "/$dhikrTarget",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = EmeraldPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Fast Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onNavigateToTahajjud() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("quick_tahajjud_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.NightsStay,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBengali) "তাহাজ্জুদ" else "Tahajjud",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { onNavigateToAmal() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("quick_amal_btn"),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldPrimary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MinimalWhiteSurface,
                            contentColor = EmeraldPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBengali) "আমল" else "Amal",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
