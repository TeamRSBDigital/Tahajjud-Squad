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
import androidx.compose.runtime.*
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
    val prayerRatio = (completedPrayersCount.toFloat() / 5f).coerceIn(0f, 1f)
    val dhikrRatio = (dhikrCount.toFloat() / dhikrTarget.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)

    // Generate 7-day horizontal calendar strip matching reference image 1
    val calendarDays = remember {
        val list = mutableListOf<DayCalendarItem>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -3) // 3 days before today

        val dayFormat = SimpleDateFormat("EEE", Locale.ENGLISH)
        val numFormat = SimpleDateFormat("dd", Locale.ENGLISH)

        for (i in 0 until 7) {
            val isToday = i == 3
            val dayName = dayFormat.format(cal.time)
            val dayNum = numFormat.format(cal.time)
            list.add(
                DayCalendarItem(
                    dayName = dayName,
                    dayNumber = dayNum,
                    isToday = isToday,
                    isSelected = isToday
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    var selectedDayNumber by remember { mutableStateOf(calendarDays.find { it.isToday }?.dayNumber ?: "14") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MinimalWhiteBackground),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // 1. Reference App Header with soft Lavender Aura backdrop
        item {
            ReferenceAppHeader(
                greeting = if (isBengali) "আসসালামু আলাইকুম 👋" else "Welcome Back 👋",
                title = if (isBengali) "আজকের আমল ও তাহাজ্জুদ" else "Stay On Track Today",
                isBengali = isBengali,
                onLanguageToggle = {
                    viewModel.setLanguage(if (isBengali) "en" else "bn")
                },
                onNotificationClick = { onNavigateToTahajjud() }
            )
        }

        // 2. Horizontal 7-Day Calendar Strip
        item {
            val updatedDays = calendarDays.map {
                it.copy(isSelected = it.dayNumber == selectedDayNumber)
            }
            HorizontalDayCalendarStrip(
                days = updatedDays,
                onSelectDay = { selectedDayNumber = it.dayNumber }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 3. Main Content Container
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {

                // "Today's Goal" Hero Card (Matching reference screenshot 1)
                IslamicCard(
                    backgroundColor = MinimalWhiteSurface,
                    borderColor = MinimalWhiteCardBorder,
                    modifier = Modifier
                        .clickable { onNavigateToTahajjud() }
                        .testTag("home_tahajjud_card")
                ) {
                    // Top Row: Goal Title & Countdown on Left, Circular Progress Arc on Right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBengali) "তাহাজ্জুদ ও দৈনিক লক্ষ্য" else "Today's Goal",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = TextDarkMuted,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = nextTahajjud.ifEmpty { "02:15 AM" },
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = TextDarkPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 26.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = EmeraldSoftBg,
                                    border = BorderStroke(1.dp, EmeraldBorder)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = EmeraldDark,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = hoursUntilTahajjud,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = EmeraldDark,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = GoldSoft,
                                    border = BorderStroke(1.dp, GoldBorder)
                                ) {
                                    Text(
                                        text = if (isBengali) "ফজর: $nextFajr" else "Fajr: $nextFajr",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = GoldDark,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        // Hero Circular Arc Gauge
                        CircularGoalArc(
                            progress = 0.75f,
                            size = 96.dp,
                            strokeWidth = 8.5.dp,
                            trackColor = Color(0xFFF1F0F5),
                            progressBrush = Brush.sweepGradient(
                                listOf(EmeraldDark, GoldAccent, EmeraldPrimary)
                            )
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldSoftBg)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NightsStay,
                                    contentDescription = "Tahajjud",
                                    tint = EmeraldDark,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Divider
                    HorizontalDivider(color = Color(0xFFF1F0F5), thickness = 1.dp)

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bottom Row: 3 Mini Metric Rings (Matching reference image 1)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Metric 1: Salah Progress (Orange arc)
                        MiniMetricRing(
                            valueText = "$completedPrayersCount/5",
                            labelText = if (isBengali) "সালাত সম্পন্ন" else "Salah Done",
                            progress = prayerRatio,
                            arcColor = MetricOrange,
                            trackColor = MetricOrangeSoft.copy(alpha = 0.35f)
                        )

                        // Metric 2: Tahajjud Streak (Purple arc)
                        MiniMetricRing(
                            valueText = "7d",
                            labelText = if (isBengali) "তাহাজ্জুদ স্ট্রিক" else "Streak",
                            progress = 0.85f,
                            arcColor = MetricPurple,
                            trackColor = MetricPurpleSoft.copy(alpha = 0.35f)
                        )

                        // Metric 3: Dhikr & Sunnah (Green arc)
                        MiniMetricRing(
                            valueText = "$dhikrCount",
                            labelText = if (isBengali) "তাসবীহ / যিকির" else "Dhikr Done",
                            progress = dhikrRatio,
                            arcColor = MetricGreen,
                            trackColor = MetricGreenSoft.copy(alpha = 0.35f)
                        )
                    }

                    // Smart Mission Badge indicator
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MinimalWhiteSurfaceVariant,
                        border = BorderStroke(1.dp, MinimalWhiteCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBengali) "স্মার্ট অ্যালার্ম মিশন: ${smartAlarmConfig.missionType.titleBn}" else "Smart Mission: ${smartAlarmConfig.missionType.titleEn}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextDarkSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 4: "Today's Prayers" (Matching "Today's Meals" list design)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBengali) "আজকের ৫ ওয়াক্ত নামায" else "Today's Prayers",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextDarkPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    )

                    Text(
                        text = "$completedPrayersCount/5 সম্পন্ন",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onNavigateToPrayer() }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Five Prayer Cards in clean list format
                PrayerType.values().forEach { prayer ->
                    val record = todayPrayers.find { it.prayerName == prayer.name }
                    val isDone = record?.status == PrayerStatus.PERFORMED
                    val isMissed = record?.status == PrayerStatus.MISSED
                    val isQaza = record?.status == PrayerStatus.QAZA

                    val prayerTime = when (prayer) {
                        PrayerType.FAJR -> "04:45 AM"
                        PrayerType.DHUHR -> "01:15 PM"
                        PrayerType.ASR -> "04:30 PM"
                        PrayerType.MAGHRIB -> "06:15 PM"
                        PrayerType.ISHA -> "07:45 PM"
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MinimalWhiteSurface,
                        border = BorderStroke(1.dp, MinimalWhiteCardBorder),
                        shadowElevation = 0.5.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                val nextStatus = when (record?.status ?: PrayerStatus.NOT_RECORDED) {
                                    PrayerStatus.NOT_RECORDED -> PrayerStatus.PERFORMED
                                    PrayerStatus.PERFORMED -> PrayerStatus.MISSED
                                    PrayerStatus.MISSED -> PrayerStatus.QAZA
                                    PrayerStatus.QAZA -> PrayerStatus.NOT_RECORDED
                                }
                                viewModel.setPrayerStatus(prayer, nextStatus)
                            }
                            .testTag("prayer_row_${prayer.name}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Left Icon Pill with warm soft tone
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isDone -> StatusPerformedLight
                                                isMissed -> StatusMissedLight
                                                isQaza -> StatusQazaLight
                                                else -> Color(0xFFF3F4F6)
                                            }
                                        )
                                ) {
                                    Icon(
                                        imageVector = when (prayer) {
                                            PrayerType.FAJR -> Icons.Default.WbTwilight
                                            PrayerType.DHUHR -> Icons.Default.WbSunny
                                            PrayerType.ASR -> Icons.Default.Brightness5
                                            PrayerType.MAGHRIB -> Icons.Default.NightsStay
                                            PrayerType.ISHA -> Icons.Default.Bedtime
                                        },
                                        contentDescription = null,
                                        tint = when {
                                            isDone -> StatusPerformed
                                            isMissed -> StatusMissed
                                            isQaza -> StatusQaza
                                            else -> Color(0xFF6B7280)
                                        },
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = if (isBengali) prayer.bengaliName else prayer.englishName,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = TextDarkPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    )
                                    Text(
                                        text = prayerTime,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextDarkMuted,
                                            fontSize = 11.5.sp
                                        )
                                    )
                                }
                            }

                            // Right Status Indicator Pill
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = when {
                                    isDone -> StatusPerformedLight
                                    isMissed -> StatusMissedLight
                                    isQaza -> StatusQazaLight
                                    else -> MinimalWhiteSurfaceVariant
                                },
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        isDone -> EmeraldBorder
                                        isMissed -> StatusMissed.copy(alpha = 0.4f)
                                        isQaza -> GoldBorder
                                        else -> Color(0xFFE2E8F0)
                                    }
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = when {
                                            isDone -> Icons.Default.Check
                                            isMissed -> Icons.Default.Close
                                            isQaza -> Icons.Default.Replay
                                            else -> Icons.Default.Add
                                        },
                                        contentDescription = null,
                                        tint = when {
                                            isDone -> StatusPerformed
                                            isMissed -> StatusMissed
                                            isQaza -> StatusQaza
                                            else -> TextDarkMuted
                                        },
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = when {
                                            isDone -> if (isBengali) "আদায়" else "Done"
                                            isMissed -> if (isBengali) "ছুটেছে" else "Missed"
                                            isQaza -> if (isBengali) "কাযা" else "Qaza"
                                            else -> if (isBengali) "রেকর্ড" else "Log"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = when {
                                                isDone -> StatusPerformed
                                                isMissed -> StatusMissed
                                                isQaza -> StatusQaza
                                                else -> TextDarkSecondary
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section 5: "Goal Progress" Weekly Trend Curve (Matching reference screenshot 2 & 3)
                IslamicCard(
                    backgroundColor = MinimalWhiteSurface,
                    borderColor = MinimalWhiteCardBorder,
                    modifier = Modifier
                        .clickable { onNavigateToRecords() }
                        .testTag("home_goal_progress_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isBengali) "আমল ধারাবাহিকতা ও অগ্রগতি" else "Goal Progress",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextDarkPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "84%",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary,
                                        fontSize = 20.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = LavenderSoftBg
                                ) {
                                    Text(
                                        text = if (isBengali) "চমৎকার (Good)" else "Good",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = LavenderAccent,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.5.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Dropdown pill
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MinimalWhiteSurfaceVariant,
                            border = BorderStroke(1.dp, MinimalWhiteCardBorder)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isBengali) "সাপ্তাহিক" else "Weekly",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextDarkSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextDarkSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Spline chart
                    val progressPoints = listOf(0.40f, 0.55f, 0.45f, 0.84f, 0.70f, 0.85f, 0.90f)
                    val dayLabels = listOf("Wed", "Thu", "Fri", "Sat", "Sun", "Mon", "Tue")
                    WeeklySplineChart(
                        points = progressPoints,
                        labels = dayLabels,
                        lineColor = LavenderAccent
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 6: Quick Dhikr & Digital Tasbih Card
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
                                    text = if (isBengali) "ডিজিটাল তাসবীহ" else "Digital Tasbih",
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

                // Section 7: Active Squad Card
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

                Spacer(modifier = Modifier.height(16.dp))

                // Section 8: Fast Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onNavigateToTahajjud() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("quick_tahajjud_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
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
                            .height(50.dp)
                            .testTag("quick_amal_btn"),
                        border = BorderStroke(1.5.dp, EmeraldPrimary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MinimalWhiteSurface,
                            contentColor = EmeraldPrimary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBengali) "আমল ট্র্যাকার" else "Amal Tracker",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
