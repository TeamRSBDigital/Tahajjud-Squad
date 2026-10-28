package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.model.PrayerRecord
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
fun PrayerScreen(
    viewModel: AmalViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isBengali = userProfile.language == "bn"
    val selectedDate by viewModel.selectedDate.collectAsState()
    val prayers by viewModel.prayersForSelectedDate.collectAsState()

    var activeNoteDialogPrayer by remember { mutableStateOf<PrayerType?>(null) }
    var currentNoteText by remember { mutableStateOf("") }

    val performedCount = prayers.count { it.status == PrayerStatus.PERFORMED }
    val missedCount = prayers.count { it.status == PrayerStatus.MISSED }
    val qazaCount = prayers.count { it.status == PrayerStatus.QAZA }
    val unrecordedCount = prayers.count { it.status == PrayerStatus.NOT_RECORDED }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Header
        item {
            IslamicTopHeader(
                title = if (isBengali) "পাঁচ ওয়াক্ত সালাত" else "Five Daily Prayers",
                subtitle = if (isBengali) "দৈনন্দিন সালাতের হিসাব ও পর্যালোচনা" else "Log and manage daily prayer status",
                isBengali = isBengali,
                onLanguageToggle = {
                    viewModel.setLanguage(if (isBengali) "en" else "bn")
                }
            )
        }

        // Date Selector Bar
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.navigateDateByDays(-1) },
                        modifier = Modifier.testTag("prayer_prev_date_btn")
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day", tint = EmeraldDark)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formatDisplayDate(selectedDate, isBengali),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark
                            )
                        )
                        Text(
                            text = selectedDate,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.navigateDateByDays(1) },
                        modifier = Modifier.testTag("prayer_next_date_btn")
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Day", tint = EmeraldDark)
                    }
                }
            }
        }

        // Daily Summary Stats
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryStatCard(
                    title = if (isBengali) "আদায়" else "Done",
                    count = performedCount,
                    color = StatusPerformed,
                    bgColor = StatusPerformedLight,
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    title = if (isBengali) "কাযা" else "Qaza",
                    count = qazaCount,
                    color = StatusQaza,
                    bgColor = StatusQazaLight,
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    title = if (isBengali) "ছুটে গেছে" else "Missed",
                    count = missedCount,
                    color = StatusMissed,
                    bgColor = StatusMissedLight,
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    title = if (isBengali) "বাকি" else "Pending",
                    count = unrecordedCount,
                    color = StatusUnrecorded,
                    bgColor = StatusUnrecordedLight,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // List of 5 Prayers
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isBengali) "ওয়াক্তভিত্তিক স্ট্যাটাস আপডেট" else "Prayer Status Checklist",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDark
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(PrayerType.values().toList()) { prayerType ->
            val record = prayers.find { it.prayerName == prayerType.name }
            val currentStatus = record?.status ?: PrayerStatus.NOT_RECORDED
            val hasNotes = !record?.notes.isNullOrBlank()

            IslamicCard(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("prayer_card_${prayerType.name}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isBengali) prayerType.bengaliName else prayerType.englishName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = prayerType.arabicName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        if (hasNotes) {
                            Text(
                                text = "নোট: ${record?.notes}",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary),
                                maxLines = 1
                            )
                        }
                    }

                    // Note icon button
                    IconButton(
                        onClick = {
                            activeNoteDialogPrayer = prayerType
                            currentNoteText = record?.notes ?: ""
                        }
                    ) {
                        Icon(
                            imageVector = if (hasNotes) Icons.Default.EditNote else Icons.Default.NoteAdd,
                            contentDescription = "Notes",
                            tint = if (hasNotes) GoldPrimary else EmeraldDark.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Four Action Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PrayerStatus.values().forEach { status ->
                        PrayerStatusChip(
                            status = status,
                            isSelected = currentStatus == status,
                            onClick = {
                                viewModel.setPrayerStatus(prayerType, status, record?.notes ?: "")
                            },
                            isBengali = isBengali,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    // Notes Dialog
    if (activeNoteDialogPrayer != null) {
        val prayer = activeNoteDialogPrayer!!
        AlertDialog(
            onDismissRequest = { activeNoteDialogPrayer = null },
            title = {
                Text(
                    text = "${if (isBengali) prayer.bengaliName else prayer.englishName} - " +
                            if (isBengali) "নোট ও দু'আ" else "Personal Notes",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isBengali) "এই নামায সম্পর্কে কোনো বিশেষ স্মরণীয় তথ্য বা দু'আ লিখে রাখুন:"
                        else "Add reflection, congregation info, or notes for this prayer:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = currentNoteText,
                        onValueChange = { currentNoteText = it },
                        modifier = Modifier.fillMaxWidth().testTag("prayer_note_input"),
                        placeholder = { Text(if (isBengali) "যেমন: জামাতে আদায় করেছি..." else "e.g., Prayed in Jama'ah...") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val record = prayers.find { it.prayerName == prayer.name }
                        val status = record?.status ?: PrayerStatus.NOT_RECORDED
                        viewModel.setPrayerStatus(prayer, status, currentNoteText)
                        activeNoteDialogPrayer = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text(if (isBengali) "সংরক্ষণ" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeNoteDialogPrayer = null }) {
                    Text(if (isBengali) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun SummaryStatCard(
    title: String,
    count: Int,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = color
                )
            )
        }
    }
}

fun formatDisplayDate(dateString: String, isBengali: Boolean): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(dateString) ?: Date()
        val outSdf = if (isBengali) {
            SimpleDateFormat("dd MMMM, EEEE", Locale("bn", "BD"))
        } else {
            SimpleDateFormat("EEE, MMM dd", Locale.ENGLISH)
        }
        outSdf.format(date)
    } catch (e: Exception) {
        dateString
    }
}
