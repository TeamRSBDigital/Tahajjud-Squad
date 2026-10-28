package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AuthenticDhikrCatalog
import com.example.model.DhikrPreset
import com.example.ui.components.ArabicGeometricPatternDecoration
import com.example.ui.components.IslamicCard
import com.example.ui.components.IslamicTopHeader
import com.example.ui.theme.*
import com.example.viewmodel.AmalViewModel

@Composable
fun DhikrScreen(
    viewModel: AmalViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isBengali = userProfile.language == "bn"
    val activePreset by viewModel.activeDhikrPreset.collectAsState()
    val count by viewModel.currentDhikrCount.collectAsState()
    val target by viewModel.dhikrTarget.collectAsState()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()
    val todayDhikrRecords by viewModel.dhikrForSelectedDate.collectAsState()

    var showResetDialog by remember { mutableStateOf(false) }
    var showCustomTargetDialog by remember { mutableStateOf(false) }
    var customTargetInput by remember { mutableStateOf("") }

    val todayTotalDhikr = todayDhikrRecords.sumOf { it.count }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 96.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            IslamicTopHeader(
                title = if (isBengali) "ডিজিটাল তাসবীহ ও যিকির" else "Digital Tasbih & Dhikr",
                subtitle = if (isBengali) "হৃদয়ের প্রশান্তি ও মহান আল্লাহর স্মরণ" else "Authentic daily remembrance & digital counter",
                isBengali = isBengali,
                onLanguageToggle = {
                    viewModel.setLanguage(if (isBengali) "en" else "bn")
                }
            )
        }

        // Dhikr Presets Horizontal Carousel
        item {
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AuthenticDhikrCatalog.presets) { preset ->
                    val isSelected = activePreset.id == preset.id
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) GoldAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { viewModel.selectDhikrPreset(preset) }
                            .testTag("dhikr_chip_${preset.id}")
                    ) {
                        Text(
                            text = if (isBengali) preset.bengaliTransliteration else preset.englishMeaning,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (isSelected) Color.White else EmeraldDark,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Active Dhikr Display Card (Minimal White Surface with Radiant Emerald & Gold)
        item {
            Spacer(modifier = Modifier.height(14.dp))
            IslamicCard(
                backgroundColor = MinimalWhiteSurface,
                borderColor = GoldPrimary.copy(alpha = 0.45f),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(
                        text = activePreset.arabicText,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isBengali) activePreset.bengaliTransliteration else activePreset.englishMeaning,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextDarkPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isBengali) activePreset.bengaliMeaning else activePreset.reference,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextDarkSecondary,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }

        // Giant Digital Counter Button
        item {
            Spacer(modifier = Modifier.height(20.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(EmeraldAccent, EmeraldPrimary, EmeraldDark)
                        )
                    )
                    .clickable { viewModel.incrementDhikr() }
                    .testTag("digital_tasbih_giant_btn")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = Color.White,
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = if (isBengali) "লক্ষ্য: $target বার" else "Target: $target",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = GoldAccent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isBengali) "ট্যাপ করুন" else "TAP",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    )
                }
            }
        }

        // Controls Row (Reset, Target, Vibrate)
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                IconButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(EmeraldSoftBg)
                        .testTag("dhikr_reset_btn")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = EmeraldDark)
                }

                // Target Selector Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(33, 100).forEach { t ->
                        val isSel = target == t
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) EmeraldPrimary else EmeraldSoftBg,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.updateDhikrTarget(t) }
                        ) {
                            Text(
                                text = "$t",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = if (isSel) Color.White else EmeraldDark,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // Custom Target button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (target != 33 && target != 100) EmeraldPrimary else EmeraldSoftBg,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showCustomTargetDialog = true }
                    ) {
                        Text(
                            text = if (target != 33 && target != 100) "$target" else "+",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = if (target != 33 && target != 100) Color.White else EmeraldDark,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                // Vibration Toggle Button
                IconButton(
                    onClick = { viewModel.toggleVibration() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (vibrationEnabled) GoldSoft else EmeraldSoftBg)
                        .testTag("dhikr_vibrate_btn")
                ) {
                    Icon(
                        imageVector = if (vibrationEnabled) Icons.Default.Vibration else Icons.Default.VolumeOff,
                        contentDescription = "Vibrate",
                        tint = if (vibrationEnabled) GoldMuted else EmeraldDark
                    )
                }
            }
        }

        // Today's Dhikr Session Summary
        item {
            Spacer(modifier = Modifier.height(18.dp))
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
                            text = if (isBengali) "আজকের মোট যিকির" else "Today's Total Dhikr",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = EmeraldDark,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = if (isBengali) "সংরক্ষিত সেশন সংখ্যা: ${todayDhikrRecords.size}" else "Sessions completed: ${todayDhikrRecords.size}",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary)
                        )
                    }
                    Text(
                        text = "$todayTotalDhikr",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                if (todayDhikrRecords.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))

                    todayDhikrRecords.take(4).forEach { record ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(record.dhikrTitle, style = MaterialTheme.typography.bodySmall)
                            Text("+${record.count} বার", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = EmeraldAccent))
                        }
                    }
                }
            }
        }
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(if (isBengali) "তাসবীহ রিসেট নিশ্চিতকরণ" else "Reset Counter?") },
            text = { Text(if (isBengali) "আপনি কি বর্তমান গণনা শূন্যে (০) রিসেট করতে চান?" else "Do you want to reset current count to 0?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetDhikr()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text(if (isBengali) "হ্যাঁ, রিসেট করুন" else "Yes, Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(if (isBengali) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Custom Target Dialog
    if (showCustomTargetDialog) {
        AlertDialog(
            onDismissRequest = { showCustomTargetDialog = false },
            title = { Text(if (isBengali) "কাস্টম লক্ষ্য নির্ধারণ" else "Set Custom Target") },
            text = {
                OutlinedTextField(
                    value = customTargetInput,
                    onValueChange = { customTargetInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text(if (isBengali) "লক্ষ্য সংখ্যা (যেমন: ৫০০)" else "Target Count (e.g. 500)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("custom_dhikr_target_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = customTargetInput.toIntOrNull()
                        if (num != null && num > 0) {
                            viewModel.updateDhikrTarget(num)
                        }
                        showCustomTargetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text(if (isBengali) "সংরক্ষণ" else "Set")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomTargetDialog = false }) {
                    Text(if (isBengali) "বাতিল" else "Cancel")
                }
            }
        )
    }
}
