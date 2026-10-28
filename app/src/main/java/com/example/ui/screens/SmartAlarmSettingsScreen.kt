package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.mission.ImageSignatureEngine
import com.example.mission.QrMissionEngine
import com.example.model.AlarmMissionType
import com.example.model.MissionDifficulty
import com.example.model.SmartAlarmConfig
import com.example.ui.components.IslamicCard
import com.example.ui.components.IslamicTopHeader
import com.example.ui.theme.*
import com.example.viewmodel.AmalViewModel

@Composable
fun SmartAlarmSettingsScreen(
    viewModel: AmalViewModel,
    onNavigateBack: () -> Unit,
    onTestMission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isBengali = userProfile.language == "bn"
    val config by viewModel.smartAlarmConfig.collectAsState()

    var showQrDialog by remember { mutableStateOf(false) }
    var showTargetPhotoDialog by remember { mutableStateOf(false) }
    var targetNameInput by remember { mutableStateOf(config.registeredTargetName.ifBlank { "জায়নামাজ" }) }
    var capturedTargetBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Camera launcher for registering photo target
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedTargetBitmap = bitmap
            showTargetPhotoDialog = true
        }
    }

    // QR Bitmap for current token
    val qrBitmap = remember(config.registeredQrToken) {
        if (config.registeredQrToken.isNotBlank()) {
            QrMissionEngine.generateQrBitmap(config.registeredQrToken, 480, 480)
        } else null
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MinimalWhiteBackground),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            IslamicTopHeader(
                title = if (isBengali) "স্মার্ট তাহাজ্জুদ অ্যালার্ম" else "Smart Tahajjud Alarm",
                subtitle = if (isBengali) "মিশন ও ঘুম ভাঙানোর উন্নত রিমাইন্ডার সিস্টেম" else "Configure wake-up missions & alarm triggers",
                isBengali = isBengali,
                onLanguageToggle = { viewModel.setLanguage(if (isBengali) "en" else "bn") }
            )
        }

        // Back and Test Action Header
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("alarm_settings_back_btn")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = EmeraldDark)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBengali) "তাহাজ্জুদ স্ক্রিনে ফিরুন" else "Back to Tahajjud",
                        color = EmeraldDark,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onTestMission,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("test_mission_now_btn")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBengali) "মিশন টেস্ট করুন" else "Test Mission",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Master Mission Toggle Card
        item {
            Spacer(modifier = Modifier.height(10.dp))
            IslamicCard(
                backgroundColor = MinimalWhiteSurface,
                borderColor = GoldPrimary.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBengali) "মিশনভিত্তিক অ্যালার্ম ডিসমিসাল" else "Mission-Based Alarm Dismissal",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextDarkPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBengali) {
                                "অ্যালার্ম বন্ধ করতে নির্দিষ্ট মিশন (গণিত, টাইপিং, কিউআর বা ছবি) সম্পন্ন করতে হবে।"
                            } else {
                                "Require completing a wake-up mission to dismiss the alarm."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary)
                        )
                    }

                    Switch(
                        checked = config.enabled,
                        onCheckedChange = { isEnabled ->
                            viewModel.updateSmartAlarmConfig(config.copy(enabled = isEnabled))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = EmeraldPrimary,
                            uncheckedThumbColor = TextDarkMuted,
                            uncheckedTrackColor = MinimalWhiteSurfaceVariant
                        ),
                        modifier = Modifier.testTag("toggle_smart_alarm_switch")
                    )
                }
            }
        }

        // Mission Type Selection
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isBengali) "ডিসমিসাল মিশন নির্বাচন করুন" else "Select Dismissal Mission",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDark
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(AlarmMissionType.values().size) { idx ->
            val mission = AlarmMissionType.values()[idx]
            val isSelected = config.missionType == mission

            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) EmeraldSoftBg else MinimalWhiteSurface,
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) EmeraldPrimary else MinimalWhiteCardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            viewModel.updateSmartAlarmConfig(config.copy(missionType = mission))
                        }
                        .testTag("mission_option_${mission.code}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                viewModel.updateSmartAlarmConfig(config.copy(missionType = mission))
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = EmeraldPrimary,
                                unselectedColor = TextDarkMuted
                            )
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isBengali) mission.titleBn else mission.titleEn,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) EmeraldDark else TextDarkPrimary
                                    )
                                )
                                if (mission == AlarmMissionType.QR_CODE && config.registeredQrToken.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = GoldSoft,
                                        border = BorderStroke(1.dp, GoldBorder)
                                    ) {
                                        Text(
                                            text = "কোড সক্রিয়",
                                            style = MaterialTheme.typography.labelSmall.copy(color = GoldDark, fontSize = 9.sp),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                } else if (mission == AlarmMissionType.PHOTO_TARGET && config.registeredTargetSignature.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = StatusPerformedLight,
                                        border = BorderStroke(1.dp, EmeraldBorder)
                                    ) {
                                        Text(
                                            text = "টার্গেট প্রস্তুত",
                                            style = MaterialTheme.typography.labelSmall.copy(color = StatusPerformed, fontSize = 9.sp),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isBengali) mission.descriptionBn else mission.descriptionEn,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextDarkSecondary,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Mission Customization: Difficulty & Task Count (Visible for Math/Typing)
        if (config.missionType == AlarmMissionType.BRAIN_MATH || config.missionType == AlarmMissionType.BRAIN_TYPING) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                IslamicCard(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = if (isBengali) "কঠিনতার স্তর (Difficulty Level)" else "Mission Difficulty",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextDarkPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MissionDifficulty.values().forEach { diff ->
                            val isSel = config.difficulty == diff
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) EmeraldPrimary else MinimalWhiteSurfaceVariant,
                                border = BorderStroke(1.dp, if (isSel) EmeraldPrimary else MinimalWhiteCardBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        viewModel.updateSmartAlarmConfig(config.copy(difficulty = diff))
                                    }
                                    .testTag("diff_option_${diff.code}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (isBengali) diff.labelBn else diff.labelEn,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) Color.White else TextDarkPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isBengali) "কাজের সংখ্যা (Number of Tasks): ${config.taskCount} টি" else "Number of Tasks: ${config.taskCount}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextDarkPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1, 2, 3, 5).forEach { count ->
                            val isSel = config.taskCount == count
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) GoldPrimary else MinimalWhiteSurfaceVariant,
                                border = BorderStroke(1.dp, if (isSel) GoldDark else MinimalWhiteCardBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.updateSmartAlarmConfig(config.copy(taskCount = count))
                                    }
                                    .testTag("task_count_$count")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "$count",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) Color.White else TextDarkPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // QR Code Setup Card (When QR Code Mission is Selected)
        if (config.missionType == AlarmMissionType.QR_CODE) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                IslamicCard(
                    borderColor = GoldPrimary.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isBengali) "নিবন্ধিত কিউআর কোড টোকেন" else "Registered QR Token",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextDarkPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = config.registeredQrToken.ifBlank { "টোকেন তৈরি হয়নি" },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = EmeraldPrimary
                                )
                            )
                        }

                        Icon(Icons.Default.QrCode2, contentDescription = null, tint = GoldDark, modifier = Modifier.size(28.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isBengali) {
                            "নির্দেশনা: কোডটি প্রিন্ট করে আপনার জায়নামাজে বা পড়ার টেবিলে রাখুন। তাহাজ্জুদের অ্যালার্ম বাজলে বিছানা ছেড়ে হেঁটে গিয়ে এটি স্ক্যান করতে হবে।"
                        } else {
                            "Instructions: Print and place this QR code at your prayer spot. You must walk over and scan it to dismiss the alarm."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary, fontSize = 11.5.sp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showQrDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("view_qr_dialog_btn")
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBengali) "কিউআর কোড দেখুন" else "View QR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.regenerateQrToken() },
                            border = BorderStroke(1.dp, EmeraldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("regenerate_qr_btn")
                        ) {
                            Icon(Icons.Default.Autorenew, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBengali) "নতুন কোড বানান" else "Regenerate", color = EmeraldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Camera Photo Target Setup Card (When Camera Photo Mission is Selected)
        if (config.missionType == AlarmMissionType.PHOTO_TARGET) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                IslamicCard(
                    borderColor = GoldPrimary.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = if (isBengali) "ক্যামেরা ছবি মিশন কনফিগারেশন" else "Camera Photo Target Setup",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextDarkPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isBengali) {
                            "জায়নামাজ, কুরআন স্ট্যান্ড বা পড়ার টেবিলের ছবি নিবন্ধন করুন। প্রাইভেসি সুরক্ষা: কোনো ফেস রিকগনিশন নেই এবং ছবি ডিভাইসের বাইরে পাঠানো হয় না।"
                        } else {
                            "Register an image of your prayer mat or desk. 100% on-device matching, no selfies or face detection."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary, fontSize = 11.5.sp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MinimalWhiteSurfaceVariant,
                        border = BorderStroke(1.dp, MinimalWhiteCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (config.registeredTargetSignature.isNotBlank()) {
                                        "টার্গেট: ${config.registeredTargetName}"
                                    } else {
                                        "কোনো টার্গেট ছবি নিবন্ধিত নেই"
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (config.registeredTargetSignature.isNotBlank()) EmeraldPrimary else StatusMissed
                                    )
                                )
                                Text(
                                    text = if (config.registeredTargetSignature.isNotBlank()) "অন-ডিভাইস কালার ও স্প্যাশিয়াল সিগনেচার সক্রিয়" else "অ্যালার্মের পূর্বে একটি ছবি যুক্ত করুন",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary, fontSize = 11.sp)
                                )
                            }

                            if (config.registeredTargetSignature.isNotBlank()) {
                                IconButton(onClick = { viewModel.clearPhotoTarget() }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = StatusMissed)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { takePhotoLauncher.launch() },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("capture_target_photo_btn")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBengali) "টার্গেট বস্তুর ছবি তুলুন" else "Capture Target Object",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Safety Fallback Disclosure & Policy Card
        item {
            Spacer(modifier = Modifier.height(16.dp))
            IslamicCard(
                backgroundColor = GoldSoft.copy(alpha = 0.5f),
                borderColor = GoldBorder,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = GoldDark, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isBengali) "জরুরী সেফটি ও ফজর সুরক্ষার নিশ্চয়তা" else "Emergency Fallback & Fajr Protection",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = GoldDark
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBengali) {
                                "ক্যামেরা পারমিশন না পাওয়া, অন্ধকার বা কিউআর কোড হারিয়ে গেলে আপনার ফজর যেন কোনোভাবেই বাধাগ্রস্ত না হয়, সেজন্য জরুরী আনলক (৮ বার কনফার্মেশন ট্যাপ) সর্বদা উন্মুক্ত থাকবে।"
                            } else {
                                "If camera fails or QR code is damaged, an emergency safety unlock (8 confirmation taps) ensures Fajr is never missed."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextDarkSecondary,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }
            }
        }
    }

    // QR Code Display Dialog
    if (showQrDialog && qrBitmap != null) {
        Dialog(onDismissRequest = { showQrDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MinimalWhiteSurface,
                border = BorderStroke(1.dp, MinimalWhiteCardBorder),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isBengali) "তাহাজ্জুদ স্কোয়াড কিউআর কোড" else "Tahajjud Squad QR Code",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "টোকেন: ${config.registeredQrToken}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextDarkSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, GoldBorder, RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (isBengali) {
                            "এই কোডটি জায়নামাজ বা পড়ার টেবিলে রেখে দিন। অ্যালার্মের সময় বিছানা থেকে উঠে স্ক্যান করতে হবে।"
                        } else {
                            "Place this code at your prayer area. Walk over and scan to dismiss alarm."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextDarkSecondary,
                            textAlign = TextAlign.Center,
                            fontSize = 11.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showQrDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isBengali) "বন্ধ করুন" else "Close", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Photo Target Confirmation Dialog
    if (showTargetPhotoDialog && capturedTargetBitmap != null) {
        Dialog(onDismissRequest = { showTargetPhotoDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MinimalWhiteSurface,
                border = BorderStroke(1.dp, MinimalWhiteCardBorder),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isBengali) "টার্গেট ছবি নিশ্চিত করুন" else "Confirm Target Photo",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, EmeraldBorder, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = capturedTargetBitmap!!.asImageBitmap(),
                            contentDescription = "Captured Target",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = targetNameInput,
                        onValueChange = { targetNameInput = it },
                        label = { Text(if (isBengali) "বস্তুর নাম" else "Object Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                showTargetPhotoDialog = false
                                capturedTargetBitmap = null
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isBengali) "বাতিল" else "Cancel")
                        }

                        Button(
                            onClick = {
                                val signature = ImageSignatureEngine.computeSignature(capturedTargetBitmap!!)
                                viewModel.registerPhotoTarget(signature, targetNameInput.ifBlank { "জায়নামাজ" })
                                showTargetPhotoDialog = false
                                capturedTargetBitmap = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isBengali) "নিবন্ধন করুন" else "Save Target", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
