package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TahajjudRecord
import com.example.ui.components.ArabicGeometricPatternDecoration
import com.example.ui.components.IslamicCard
import com.example.ui.components.IslamicTopHeader
import com.example.ui.theme.*
import com.example.viewmodel.AmalViewModel

@Composable
fun TahajjudScreen(
    viewModel: AmalViewModel,
    onNavigateToMissionSettings: () -> Unit = {},
    onNavigateToTestMission: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isBengali = userProfile.language == "bn"
    val selectedDate by viewModel.selectedDate.collectAsState()
    val currentTahajjudRecord by viewModel.tahajjudForSelectedDate.collectAsState()
    val nextFajr by viewModel.nextFajrTime.collectAsState()
    val nextTahajjud by viewModel.nextTahajjudReminderTime.collectAsState()
    val hoursUntilTahajjud by viewModel.hoursUntilTahajjud.collectAsState()
    val allTahajjudList by viewModel.allTahajjud.collectAsState()
    val smartAlarmConfig by viewModel.smartAlarmConfig.collectAsState()

    var performed by remember(currentTahajjudRecord) {
        mutableStateOf(currentTahajjudRecord?.performed ?: false)
    }
    var rakahs by remember(currentTahajjudRecord) {
        mutableStateOf(if (currentTahajjudRecord?.rakahs ?: 0 > 0) currentTahajjudRecord?.rakahs ?: 4 else 4)
    }
    var startTime by remember(currentTahajjudRecord) {
        mutableStateOf(currentTahajjudRecord?.startTime ?: "04:10 AM")
    }
    var endTime by remember(currentTahajjudRecord) {
        mutableStateOf(currentTahajjudRecord?.endTime ?: "04:45 AM")
    }
    var notes by remember(currentTahajjudRecord) {
        mutableStateOf(currentTahajjudRecord?.notes ?: "")
    }

    val totalTahajjudSessions = allTahajjudList.count { it.performed }
    val totalRakahsPrayed = allTahajjudList.filter { it.performed }.sumOf { it.rakahs }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            IslamicTopHeader(
                title = if (isBengali) "তাহাজ্জুদ সালাত" else "Tahajjud Prayers",
                subtitle = if (isBengali) "রাতের শেষ প্রহরে আল্লাহর নৈকট্য লাভ" else "Night vigil worship & consistency tracker",
                isBengali = isBengali,
                onLanguageToggle = {
                    viewModel.setLanguage(if (isBengali) "en" else "bn")
                }
            )
        }

        // Live Alarm Status Card
        item {
            Spacer(modifier = Modifier.height(14.dp))
            IslamicCard(
                backgroundColor = MinimalWhiteSurface,
                borderColor = GoldPrimary.copy(alpha = 0.45f),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBengali) "স্বয়ংক্রিয় অ্যালার্ম ও সময়সূচি" else "Automated Alarm & Schedule",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextDarkPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Icon(Icons.Default.Alarm, contentDescription = null, tint = GoldDark)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (isBengali) "পরবর্তী ফজর:" else "Next Fajr:",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = nextFajr.ifEmpty { "04:45 AM" },
                                style = MaterialTheme.typography.titleLarge.copy(color = TextDarkPrimary, fontWeight = FontWeight.Bold)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isBengali) "তাহাজ্জুদ অ্যালার্ম:" else "Tahajjud Alarm:",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = nextTahajjud.ifEmpty { "04:15 AM" },
                                style = MaterialTheme.typography.titleLarge.copy(color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldSoftBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "অবশিষ্ট: $hoursUntilTahajjud (ফজরের ৩০ মিনিট পূর্বে)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = EmeraldDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Mission badge and settings entry
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = GoldSoft,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onNavigateToMissionSettings() }
                            .testTag("tahajjud_mission_badge_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isBengali) "সক্রিয় মিশন: ${smartAlarmConfig.missionType.titleBn}" else "Active Mission: ${smartAlarmConfig.missionType.titleEn}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = GoldDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = if (smartAlarmConfig.enabled) {
                                        "কঠিনতা: ${smartAlarmConfig.difficulty.labelBn} • কাজ: ${smartAlarmConfig.taskCount} টি"
                                    } else {
                                        "মিশন ডিসমিসাল নিষ্ক্রিয় (সাধারণ অ্যালার্ম)"
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextDarkSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = GoldDark, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onNavigateToMissionSettings,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                            modifier = Modifier.weight(1f).testTag("goto_mission_settings_btn")
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBengali) "মিশন সেটিংস" else "Mission Config", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onNavigateToTestMission,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldDark),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                            modifier = Modifier.weight(1f).testTag("test_mission_interactive_btn")
                        ) {
                            Icon(Icons.Default.PlayCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBengali) "মিশন টেস্ট" else "Test Mission", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Tahajjud Entry Form
        item {
            Spacer(modifier = Modifier.height(16.dp))
            IslamicCard(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = if (isBengali) "তারিখ: $selectedDate — তাহাজ্জুদের আমল রেকর্ড" else "Date: $selectedDate — Tahajjud Entry",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = EmeraldDark,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Performed Toggle Switch
                Surface(
                    color = if (performed) StatusPerformedLight else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (performed) StatusPerformed else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { performed = !performed }
                        .testTag("tahajjud_performed_toggle")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (performed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (performed) StatusPerformed else TextDarkSecondary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (performed) {
                                    if (isBengali) "আলহামদুলিল্লাহ, তাহাজ্জুদ আদায় করেছি" else "Alhamdulillah, Tahajjud Performed"
                                } else {
                                    if (isBengali) "তাহাজ্জুদ আদায় করা হয়নি" else "Tahajjud Not Performed"
                                },
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (performed) StatusPerformed else TextDarkPrimary
                                )
                            )
                        }

                        Switch(
                            checked = performed,
                            onCheckedChange = { performed = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = StatusPerformed,
                                checkedTrackColor = StatusPerformedLight
                            )
                        )
                    }
                }

                if (performed) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isBengali) "কত রাকাত আদায় করেছেন?" else "Number of Rak'ahs",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Rak'ah Selector Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(2, 4, 6, 8, 10, 12).forEach { r ->
                            val isSelected = rakahs == r
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) EmeraldPrimary else EmeraldSoftBg,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) GoldAccent else EmeraldPrimary.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { rakahs = r }
                                    .testTag("rakah_btn_$r")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$r",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            color = if (isSelected) Color.White else EmeraldDark,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Start & End Time Inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text(if (isBengali) "শুরুর সময়" else "Start Time") },
                            modifier = Modifier.weight(1f).testTag("tahajjud_start_time_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = { Text(if (isBengali) "শেষের সময়" else "End Time") },
                            modifier = Modifier.weight(1f).testTag("tahajjud_end_time_input"),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(if (isBengali) "ব্যক্তিগত অনুভূতি ও দু'আ" else "Reflections & Dua") },
                        modifier = Modifier.fillMaxWidth().testTag("tahajjud_notes_input"),
                        placeholder = { Text(if (isBengali) "যেমন: সূরা মুলক ও সূরা সাজদাহ পাঠ করেছি..." else "e.g., recited Surah Al-Mulk...") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.recordTahajjud(
                            performed = performed,
                            rakahs = if (performed) rakahs else 0,
                            startTime = if (performed) startTime else "",
                            endTime = if (performed) endTime else "",
                            notes = notes
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_tahajjud_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBengali) "তাহাজ্জুদ রেকর্ড সংরক্ষণ করুন" else "Save Tahajjud Record",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        // Tahajjud Lifetime Consistency Summary Card
        item {
            Spacer(modifier = Modifier.height(16.dp))
            IslamicCard(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = if (isBengali) "তাহাজ্জুদ ধারাবাহিকতা পরিসংখ্যান" else "Tahajjud Consistency Stats",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = EmeraldDark,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = EmeraldSoftBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$totalTahajjudSessions",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = if (isBengali) "মোট তাহাজ্জুদ রাত" else "Nights Performed",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    Surface(
                        color = GoldSoft,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$totalRakahsPrayed",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = EmeraldDeep,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = if (isBengali) "মোট রাকাত আদায়" else "Total Rak'ahs",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isBengali)
                        "হাদীস শরীফ: 'ফরয সালাতের পর সবচেয়ে উত্তম সালাত হলো রাতের সালাত (তাহাজ্জুদ)।' — সহীহ মুসলিম (১১৬৩)"
                    else
                        "Hadith: 'The best prayer after the obligatory prayers is the night prayer.' — Sahih Muslim 1163",
                    style = MaterialTheme.typography.bodySmall.copy(color = GoldMuted, fontSize = 11.sp)
                )
            }
        }
    }
}
