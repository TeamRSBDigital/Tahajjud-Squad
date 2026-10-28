package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AmalCategory
import com.example.model.AmalEntry
import com.example.model.AmalStatus
import com.example.ui.components.IslamicCard
import com.example.ui.components.IslamicTopHeader
import com.example.ui.theme.*
import com.example.viewmodel.AmalViewModel

@Composable
fun AmalTrackerScreen(
    viewModel: AmalViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isBengali = userProfile.language == "bn"
    val selectedDate by viewModel.selectedDate.collectAsState()
    val amalEntries by viewModel.amalForSelectedDate.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf(AmalCategory.CUSTOM) }
    var newTarget by remember { mutableStateOf("1") }
    var newUnit by remember { mutableStateOf("বার") }

    val completedCount = amalEntries.count { it.status == AmalStatus.COMPLETED }
    val totalCount = amalEntries.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            IslamicTopHeader(
                title = if (isBengali) "আমল ট্র্যাকার" else "Amal Tracker",
                subtitle = if (isBengali) "দৈনন্দিন সুন্নাত, কুরআন তিলাওয়াত ও নেক আমল" else "Quran, Adhkar, Sadaqah & daily good deeds",
                isBengali = isBengali,
                onLanguageToggle = { viewModel.setLanguage(if (isBengali) "en" else "bn") }
            )
        }

        // Date Bar
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
                    IconButton(onClick = { viewModel.navigateDateByDays(-1) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day", tint = EmeraldDark)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formatDisplayDate(selectedDate, isBengali),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldDark)
                        )
                        Text(text = selectedDate, style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary))
                    }

                    IconButton(onClick = { viewModel.navigateDateByDays(1) }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Day", tint = EmeraldDark)
                    }
                }
            }
        }

        // Summary Bar & Add Button
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isBengali) "আজকের সম্পন্ন আমল: $completedCount/$totalCount" else "Completed today: $completedCount/$totalCount",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldDark)
                    )
                    Text(
                        text = if (isBengali) "দ্বীনি অভ্যাসে অবিচল থাকুন" else "Steadfastness in daily worship",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary)
                    )
                }

                FilledTonalButton(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                    modifier = Modifier.testTag("add_custom_amal_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBengali) "নতুন আমল" else "Add Habit")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(amalEntries) { entry ->
            AmalEntryCard(
                entry = entry,
                isBengali = isBengali,
                onStatusChange = { newStatus ->
                    val newCount = if (newStatus == AmalStatus.COMPLETED) entry.targetCount else entry.currentCount
                    viewModel.updateAmalStatus(entry, newStatus, newCount)
                },
                onCountIncrement = {
                    val nextCount = entry.currentCount + 1
                    val newStatus = if (nextCount >= entry.targetCount) AmalStatus.COMPLETED else AmalStatus.PARTIAL
                    viewModel.updateAmalStatus(entry, newStatus, nextCount)
                },
                onDelete = { viewModel.deleteAmal(entry.id) }
            )
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(if (isBengali) "নতুন আমল লক্ষ্য যোগ করুন" else "Add Custom Worship Habit") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text(if (isBengali) "আমলের নাম" else "Habit Title") },
                        placeholder = { Text(if (isBengali) "যেমন: সূরা মুলক পাঠ" else "e.g., Surah Al-Mulk") },
                        modifier = Modifier.fillMaxWidth().testTag("new_amal_title_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newTarget,
                            onValueChange = { newTarget = it.filter { ch -> ch.isDigit() } },
                            label = { Text(if (isBengali) "লক্ষ্য" else "Target") },
                            modifier = Modifier.weight(1f).testTag("new_amal_target_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newUnit,
                            onValueChange = { newUnit = it },
                            label = { Text(if (isBengali) "একক" else "Unit") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            viewModel.addNewCustomAmal(
                                title = newTitle,
                                category = newCategory,
                                targetCount = newTarget.toIntOrNull() ?: 1,
                                unit = newUnit.ifBlank { "বার" }
                            )
                            newTitle = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text(if (isBengali) "যোগ করুন" else "Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(if (isBengali) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun AmalEntryCard(
    entry: AmalEntry,
    isBengali: Boolean,
    onStatusChange: (AmalStatus) -> Unit,
    onCountIncrement: () -> Unit,
    onDelete: () -> Unit
) {
    val isDone = entry.status == AmalStatus.COMPLETED
    val isPartial = entry.status == AmalStatus.PARTIAL
    val isMissed = entry.status == AmalStatus.MISSED

    IslamicCard(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("amal_card_${entry.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldDark)
                    )
                }
                if (entry.description.isNotBlank()) {
                    Text(
                        text = entry.description,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary),
                        maxLines = 2
                    )
                }
                Text(
                    text = "লক্ষ্য: ${entry.targetCount} ${entry.unit} • বর্তমান: ${entry.currentCount}",
                    style = MaterialTheme.typography.labelSmall.copy(color = EmeraldPrimary, fontWeight = FontWeight.SemiBold)
                )
            }

            // Quick increment button
            IconButton(
                onClick = onCountIncrement,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(EmeraldSoftBg)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increment", tint = EmeraldDark)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Status row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(AmalStatus.NOT_RECORDED, AmalStatus.COMPLETED, AmalStatus.PARTIAL, AmalStatus.MISSED).forEach { status ->
                val isSel = entry.status == status
                val (chipBg, chipFg) = when (status) {
                    AmalStatus.COMPLETED -> if (isSel) Pair(StatusPerformed, Color.White) else Pair(StatusPerformedLight, StatusPerformed)
                    AmalStatus.PARTIAL -> if (isSel) Pair(StatusQaza, Color.White) else Pair(StatusQazaLight, StatusQaza)
                    AmalStatus.MISSED -> if (isSel) Pair(StatusMissed, Color.White) else Pair(StatusMissedLight, StatusMissed)
                    AmalStatus.NOT_RECORDED -> if (isSel) Pair(StatusUnrecorded, Color.White) else Pair(StatusUnrecordedLight, StatusUnrecorded)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = chipBg,
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onStatusChange(status) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isBengali) status.labelBn else status.labelEn,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = chipFg,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}
