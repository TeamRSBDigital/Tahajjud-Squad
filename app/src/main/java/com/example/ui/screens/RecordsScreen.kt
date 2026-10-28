package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PrayerStatus
import com.example.ui.components.IslamicCard
import com.example.ui.components.IslamicTopHeader
import com.example.ui.components.PrayerProgressRing
import com.example.ui.theme.*
import com.example.viewmodel.AmalViewModel

enum class RecordTimeFilter(val labelBn: String, val labelEn: String) {
    LAST_7_DAYS("গত ৭ দিন", "Last 7 Days"),
    LAST_30_DAYS("গত ৩০ দিন", "Last 30 Days"),
    ALL_TIME("সর্বমোট", "All Time")
}

@Composable
fun RecordsScreen(
    viewModel: AmalViewModel,
    onNavigateToSuperAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isBengali = userProfile.language == "bn"
    val allPrayers by viewModel.allPrayers.collectAsState()
    val allTahajjud by viewModel.allTahajjud.collectAsState()
    val allDhikr by viewModel.allDhikr.collectAsState()

    var selectedFilter by remember { mutableStateOf(RecordTimeFilter.LAST_7_DAYS) }

    val totalPrayersRecorded = allPrayers.size
    val totalPerformed = allPrayers.count { it.status == PrayerStatus.PERFORMED }
    val totalMissed = allPrayers.count { it.status == PrayerStatus.MISSED }
    val totalQaza = allPrayers.count { it.status == PrayerStatus.QAZA }

    val totalTahajjudNights = allTahajjud.count { it.performed }
    val totalTahajjudRakahs = allTahajjud.filter { it.performed }.sumOf { it.rakahs }
    val totalDhikrCount = allDhikr.sumOf { it.count }

    val consistencyRate = if (totalPrayersRecorded > 0) {
        ((totalPerformed.toFloat() / totalPrayersRecorded.toFloat()) * 100).toInt()
    } else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            IslamicTopHeader(
                title = if (isBengali) "আমার আমল রেকর্ড" else "My Worship Records",
                subtitle = if (isBengali) "ধারাবাহিকতা ও সামগ্রিক পর্যালোচনা" else "Consistency insights & worship history",
                isBengali = isBengali,
                onLanguageToggle = {
                    viewModel.setLanguage(if (isBengali) "en" else "bn")
                }
            )
        }

        // Time Range Filter Selector
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RecordTimeFilter.values().forEach { filter ->
                    val isSel = selectedFilter == filter
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(if (isBengali) filter.labelBn else filter.labelEn)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f).testTag("record_filter_${filter.name}")
                    )
                }
            }
        }

        // Overall Consistency Hero Card
        item {
            Spacer(modifier = Modifier.height(14.dp))
            IslamicCard(
                backgroundColor = MinimalWhiteSurface,
                borderColor = GoldPrimary.copy(alpha = 0.45f),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBengali) "ফরয সালাত ধারাবাহিকতা" else "Prayer Consistency",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextDarkPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBengali) "নিয়মিত সালাত আদায়ের হার: $consistencyRate%" else "Punctuality rate: $consistencyRate%",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary)
                        )
                    }

                    PrayerProgressRing(
                        completedCount = totalPerformed,
                        totalCount = if (totalPrayersRecorded > 0) totalPrayersRecorded else 5,
                        size = 76.dp,
                        strokeWidth = 7.dp
                    )
                }
            }
        }

        // 4 Key Metrics Grid
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Done
                Surface(
                    color = StatusPerformedLight,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$totalPerformed", style = MaterialTheme.typography.titleLarge.copy(color = StatusPerformed, fontWeight = FontWeight.Bold))
                        Text(if (isBengali) "আদায়কৃত সালাত" else "Performed", style = MaterialTheme.typography.labelSmall.copy(color = StatusPerformed))
                    }
                }
                // Missed
                Surface(
                    color = StatusMissedLight,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$totalMissed", style = MaterialTheme.typography.titleLarge.copy(color = StatusMissed, fontWeight = FontWeight.Bold))
                        Text(if (isBengali) "ছুটে যাওয়া সালাত" else "Missed", style = MaterialTheme.typography.labelSmall.copy(color = StatusMissed))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Qaza
                Surface(
                    color = StatusQazaLight,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$totalQaza", style = MaterialTheme.typography.titleLarge.copy(color = StatusQaza, fontWeight = FontWeight.Bold))
                        Text(if (isBengali) "কাযা আদায়কৃত" else "Qaza Done", style = MaterialTheme.typography.labelSmall.copy(color = StatusQaza))
                    }
                }
                // Tahajjud
                Surface(
                    color = GoldSoft,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$totalTahajjudNights", style = MaterialTheme.typography.titleLarge.copy(color = EmeraldDeep, fontWeight = FontWeight.Bold))
                        Text(if (isBengali) "তাহাজ্জুদ রাত" else "Tahajjud Nights", style = MaterialTheme.typography.labelSmall.copy(color = EmeraldDeep))
                    }
                }
            }
        }

        // Dhikr Total Card
        item {
            Spacer(modifier = Modifier.height(16.dp))
            IslamicCard(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBengali) "সর্বমোট যিকির গণনা" else "Cumulative Dhikr Count",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = EmeraldDark,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = if (isBengali) "ডিজিটাল তাসবীহ সেশন রেকর্ড" else "Logged tasbih sessions",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary)
                        )
                    }
                    Text(
                        text = "$totalDhikrCount",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        // Super Admin Entry Affordance Card
        item {
            Spacer(modifier = Modifier.height(20.dp))
            IslamicCard(
                backgroundColor = IvorySurfaceVariant,
                borderColor = GoldPrimary.copy(alpha = 0.4f),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = GoldPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBengali) "সুপার অ্যাডমিন ড্যাশবোর্ড" else "Super Admin Dashboard",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBengali) "সম্মতিপ্রাপ্ত ব্যবহারকারীদের রিপোর্ট ও গুগল শিট সিঙ্ক" else "Consented user monitoring & Google Sheets sync",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary)
                        )
                    }

                    Button(
                        onClick = onNavigateToSuperAdmin,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                        modifier = Modifier.testTag("open_super_admin_btn")
                    ) {
                        Text(if (isBengali) "প্রবেশ" else "Open")
                    }
                }
            }
        }
    }
}
