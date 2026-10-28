package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mission.BrainMissionEngine
import com.example.mission.ImageSignatureEngine
import com.example.mission.QrMissionEngine
import com.example.model.AlarmMissionType
import com.example.model.MathProblem
import com.example.model.TypingProblem
import com.example.ui.components.IslamicCard
import com.example.ui.theme.*
import com.example.viewmodel.AmalViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AlarmMissionScreen(
    viewModel: AmalViewModel,
    isTestMode: Boolean = false,
    onMissionCompleted: () -> Unit,
    onNavigateToTahajjudEntry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isBengali = userProfile.language == "bn"
    val config by viewModel.smartAlarmConfig.collectAsState()
    val nextFajr by viewModel.nextFajrTime.collectAsState()
    val hoursUntilTahajjud by viewModel.hoursUntilTahajjud.collectAsState()

    var isMissionDismissed by remember { mutableStateOf(false) }
    var emergencyTapCount by remember { mutableStateOf(0) }
    var taskProgressIndex by remember { mutableStateOf(0) }
    var statusFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var isFeedbackError by remember { mutableStateOf(false) }

    // Dynamic current clock
    val currentTimeString = remember {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        sdf.format(Date())
    }

    // Mathematical Mission State
    val mathProblems = remember(config.difficulty, config.taskCount) {
        BrainMissionEngine.generateMathProblems(config.difficulty, config.taskCount)
    }
    var mathInputBuffer by remember { mutableStateOf("") }
    val currentMathProblem: MathProblem? = mathProblems.getOrNull(taskProgressIndex)

    // Typing Mission State
    val typingProblems = remember(config.taskCount) {
        (1..config.taskCount.coerceIn(1, 3)).map { BrainMissionEngine.generateTypingProblem(isBengali) }
    }
    var typingInputText by remember { mutableStateOf("") }
    val currentTypingProblem: TypingProblem? = typingProblems.getOrNull(taskProgressIndex)

    // QR & Camera Scan States
    var manualQrInput by remember { mutableStateOf("") }
    var showManualQrField by remember { mutableStateOf(false) }

    // Camera launcher for QR scanning / target matching
    val cameraCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            when (config.missionType) {
                AlarmMissionType.QR_CODE -> {
                    val decoded = QrMissionEngine.decodeQrBitmap(bitmap)
                    val result = QrMissionEngine.validateScannedCode(decoded, config.registeredQrToken)
                    when (result) {
                        is QrMissionEngine.QrValidationResult.ValidMatch -> {
                            isMissionDismissed = true
                            viewModel.stopAlarmRinging()
                            statusFeedbackMessage = if (isBengali) "কিউআর কোড মিলেছে! মিশন সম্পন্ন হয়েছে।" else "QR code verified! Mission completed."
                            isFeedbackError = false
                        }
                        is QrMissionEngine.QrValidationResult.InvalidCode -> {
                            statusFeedbackMessage = if (isBengali) result.messageBn else result.messageEn
                            isFeedbackError = true
                        }
                        is QrMissionEngine.QrValidationResult.NotConfigured -> {
                            // If no token was saved, accept any scan or direct dismiss as fallback
                            isMissionDismissed = true
                            viewModel.stopAlarmRinging()
                            statusFeedbackMessage = if (isBengali) "মিশন সম্পন্ন হয়েছে।" else "Mission completed."
                            isFeedbackError = false
                        }
                    }
                }
                AlarmMissionType.PHOTO_TARGET -> {
                    val matchResult = ImageSignatureEngine.compareImageToSignature(
                        capturedBitmap = bitmap,
                        registeredSignature = config.registeredTargetSignature,
                        isBengali = isBengali
                    )
                    statusFeedbackMessage = matchResult.feedbackMessage
                    if (matchResult.isMatch) {
                        isMissionDismissed = true
                        viewModel.stopAlarmRinging()
                        isFeedbackError = false
                    } else {
                        isFeedbackError = true
                    }
                }
                else -> {}
            }
        } else {
            statusFeedbackMessage = if (isBengali) "ক্যামেরা থেকে কোনো ছবি পাওয়া যায়নি।" else "No photo received from camera."
            isFeedbackError = true
        }
    }

    // Bell Pulsing Animation for wakefulness
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MinimalWhiteBackground),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Wake-Up Header Card
        item {
            Spacer(modifier = Modifier.height(24.dp))
            IslamicCard(
                backgroundColor = if (isMissionDismissed) StatusPerformedLight else GoldSoft,
                borderColor = if (isMissionDismissed) EmeraldBorder else GoldBorder,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(72.dp)
                            .scale(if (!isMissionDismissed) pulseScale else 1f)
                            .clip(CircleShape)
                            .background(if (isMissionDismissed) StatusPerformed else GoldPrimary)
                    ) {
                        Icon(
                            imageVector = if (isMissionDismissed) Icons.Default.CheckCircle else Icons.Default.AlarmOn,
                            contentDescription = "Alarm State",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentTimeString,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextDarkPrimary,
                            fontSize = 36.sp
                        )
                    )

                    Text(
                        text = if (isMissionDismissed) {
                            if (isBengali) "আলহামদুলিল্লাহ! অ্যালার্ম সফলভাবে বন্ধ হয়েছে" else "Alhamdulillah! Alarm Dismissed"
                        } else {
                            if (isBengali) "তাহাজ্জুদের সময় — ঘুম থেকে জাগ্রত হোন!" else "Tahajjud Time — Wake Up for Night Vigil!"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isMissionDismissed) EmeraldDark else GoldDark
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "ফজর: $nextFajr ($hoursUntilTahajjud বাকি)",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary)
                    )

                    if (isTestMode && !isMissionDismissed) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EmeraldSoftBg,
                            border = BorderStroke(1.dp, EmeraldBorder)
                        ) {
                            Text(
                                text = "সিমুলেশন ও টেস্ট মোড সক্রিয়",
                                style = MaterialTheme.typography.labelSmall.copy(color = EmeraldDark, fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Status Feedback Banner (if any)
        if (statusFeedbackMessage != null) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isFeedbackError) StatusMissedLight else StatusPerformedLight,
                    border = BorderStroke(1.dp, if (isFeedbackError) StatusMissed else StatusPerformed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isFeedbackError) Icons.Default.Warning else Icons.Default.Check,
                            contentDescription = null,
                            tint = if (isFeedbackError) StatusMissed else StatusPerformed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = statusFeedbackMessage!!,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isFeedbackError) StatusMissed else StatusPerformed,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }

        // When Dismissed: Post-Wakeup Action Card
        if (isMissionDismissed) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                IslamicCard(
                    borderColor = EmeraldPrimary,
                    modifier = Modifier.fillMaxWidth().testTag("post_alarm_success_card")
                ) {
                    Text(
                        text = if (isBengali) "পরবর্তী করণীয়:" else "Next Steps:",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isBengali) {
                            "স্মার্ট অ্যালার্ম বন্ধ হয়েছে। এবার অজু করে তাহাজ্জুদের নামায আদায়ে নিমগ্ন হোন। সালাত শেষে আপনার আদায় রেকর্ড করুন।"
                        } else {
                            "The alarm is now dismissed. Perform wudu and devote yourself to Tahajjud. Record your prayer once completed."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onNavigateToTahajjudEntry,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("log_tahajjud_prayer_btn")
                    ) {
                        Icon(Icons.Default.NightsStay, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBengali) "তাহাজ্জুদ আদায় রেকর্ড করুন" else "Log Tahajjud Prayer", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onMissionCompleted,
                        border = BorderStroke(1.dp, EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("return_home_btn")
                    ) {
                        Text(if (isBengali) "হোম স্ক্রিনে ফিরে যান" else "Return to Dashboard", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // ACTIVE MISSION CONTENT
            when (config.missionType) {
                // 1. STANDARD DISMISS
                AlarmMissionType.NONE -> {
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                isMissionDismissed = true
                                viewModel.stopAlarmRinging()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("standard_dismiss_btn")
                        ) {
                            Icon(Icons.Default.AlarmOff, contentDescription = null, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBengali) "অ্যালার্ম বন্ধ করুন" else "Dismiss Alarm",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 2. BRAIN MATH MISSION
                AlarmMissionType.BRAIN_MATH -> {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        IslamicCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = if (isBengali) "গণিত সমাধান মিশন" else "Mental Math Mission",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "কাজ: ${taskProgressIndex + 1}/${mathProblems.size} — সঠিক উত্তর প্রদান করুন",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Problem Box
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MinimalWhiteSurfaceVariant,
                                border = BorderStroke(1.5.dp, GoldBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = currentMathProblem?.questionText ?: "০ + ০ = ?",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextDarkPrimary,
                                            fontSize = 32.sp
                                        ),
                                        modifier = Modifier.testTag("math_problem_text")
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (mathInputBuffer.isNotBlank()) mathInputBuffer else "উত্তর লিখুন...",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (mathInputBuffer.isNotBlank()) EmeraldPrimary else TextDarkMuted,
                                            fontSize = 26.sp
                                        ),
                                        modifier = Modifier.testTag("math_input_display")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Numeric Keypad (3x4 Grid)
                            val keypadKeys = listOf(
                                listOf("1", "2", "3"),
                                listOf("4", "5", "6"),
                                listOf("7", "8", "9"),
                                listOf("C", "0", "OK")
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                keypadKeys.forEach { rowKeys ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowKeys.forEach { key ->
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = when (key) {
                                                    "OK" -> EmeraldPrimary
                                                    "C" -> StatusMissedLight
                                                    else -> MinimalWhiteSurface
                                                },
                                                border = BorderStroke(
                                                    1.dp,
                                                    when (key) {
                                                        "OK" -> EmeraldDark
                                                        "C" -> StatusMissed
                                                        else -> MinimalWhiteCardBorder
                                                    }
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(52.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        when (key) {
                                                            "C" -> mathInputBuffer = ""
                                                            "OK" -> {
                                                                if (currentMathProblem != null) {
                                                                    val isCorrect = BrainMissionEngine.validateMathAnswer(mathInputBuffer, currentMathProblem)
                                                                    if (isCorrect) {
                                                                        statusFeedbackMessage = if (isBengali) "সঠিক উত্তর!" else "Correct answer!"
                                                                        isFeedbackError = false
                                                                        mathInputBuffer = ""
                                                                        if (taskProgressIndex + 1 < mathProblems.size) {
                                                                            taskProgressIndex += 1
                                                                        } else {
                                                                            isMissionDismissed = true
                                                                            viewModel.stopAlarmRinging()
                                                                        }
                                                                    } else {
                                                                        statusFeedbackMessage = if (isBengali) "ভুল উত্তর! আবার চেষ্টা করুন।" else "Incorrect answer. Try again!"
                                                                        isFeedbackError = true
                                                                        mathInputBuffer = ""
                                                                    }
                                                                }
                                                            }
                                                            else -> {
                                                                if (mathInputBuffer.length < 6) {
                                                                    mathInputBuffer += key
                                                                }
                                                            }
                                                        }
                                                    }
                                                    .testTag("keypad_btn_$key")
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = key,
                                                        style = MaterialTheme.typography.titleMedium.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = when (key) {
                                                                "OK" -> Color.White
                                                                "C" -> StatusMissed
                                                                else -> TextDarkPrimary
                                                            },
                                                            fontSize = 20.sp
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. BRAIN TYPING MISSION
                AlarmMissionType.BRAIN_TYPING -> {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        IslamicCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = if (isBengali) "বাক্য টাইপিং মিশন" else "Phrase Typing Mission",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "কাজ: ${taskProgressIndex + 1}/${typingProblems.size} — প্রদর্শিত বাক্যটি হুবহু লিখুন",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = EmeraldSoftBg,
                                border = BorderStroke(1.dp, EmeraldBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = currentTypingProblem?.targetPhrase ?: "",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = EmeraldDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        textAlign = TextAlign.Center
                                    ),
                                    modifier = Modifier.padding(16.dp).fillMaxWidth().testTag("typing_prompt_phrase")
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = typingInputText,
                                onValueChange = { typingInputText = it },
                                placeholder = { Text(if (isBengali) "এখানে লিখুন..." else "Type here...") },
                                modifier = Modifier.fillMaxWidth().testTag("typing_input_field"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    if (currentTypingProblem != null) {
                                        val isMatch = BrainMissionEngine.validateTypingInput(typingInputText, currentTypingProblem.targetPhrase)
                                        if (isMatch) {
                                            statusFeedbackMessage = if (isBengali) "সঠিকভাবে টাইপ করেছেন!" else "Matched successfully!"
                                            isFeedbackError = false
                                            typingInputText = ""
                                            if (taskProgressIndex + 1 < typingProblems.size) {
                                                taskProgressIndex += 1
                                            } else {
                                                isMissionDismissed = true
                                                viewModel.stopAlarmRinging()
                                            }
                                        } else {
                                            statusFeedbackMessage = if (isBengali) "বানান বা শব্দ মেলেনি। সতর্কভাবে আবার টাইপ করুন।" else "Text does not match. Please check spelling."
                                            isFeedbackError = true
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("typing_submit_btn")
                            ) {
                                Text(if (isBengali) "সাবমিট করুন" else "Submit Answer", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 4. QR CODE MISSION
                AlarmMissionType.QR_CODE -> {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        IslamicCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = if (isBengali) "কিউআর কোড স্ক্যানার মিশন" else "QR Code Scanner Mission",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isBengali) {
                                    "আপনার জায়নামাজ বা পড়ার টেবিলে রাখা কিউআর কোডটি স্ক্যান করে অ্যালার্ম বন্ধ করুন।"
                                } else {
                                    "Scan the QR code registered and placed at your prayer location."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { cameraCaptureLauncher.launch() },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("open_qr_camera_btn")
                            ) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBengali) "ক্যামেরা চালু করে স্ক্যান করুন" else "Launch Camera & Scan QR",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Direct token matching option for accessibility/fallback
                            TextButton(
                                onClick = { showManualQrField = !showManualQrField },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(
                                    text = if (showManualQrField) "ম্যানুয়াল অপশন লুকান" else "ক্যামেরা সমস্যা হলে টোকেন লিখুন",
                                    color = TextDarkSecondary,
                                    fontSize = 12.sp
                                )
                            }

                            if (showManualQrField) {
                                OutlinedTextField(
                                    value = manualQrInput,
                                    onValueChange = { manualQrInput = it },
                                    label = { Text("টোকেন কোড (উদা: TS-QR-...)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        val result = QrMissionEngine.validateScannedCode(manualQrInput, config.registeredQrToken)
                                        when (result) {
                                            is QrMissionEngine.QrValidationResult.ValidMatch -> {
                                                isMissionDismissed = true
                                                viewModel.stopAlarmRinging()
                                                statusFeedbackMessage = "টোকেন সফলভাবে যাচাই হয়েছে!"
                                                isFeedbackError = false
                                            }
                                            is QrMissionEngine.QrValidationResult.InvalidCode -> {
                                                statusFeedbackMessage = result.messageBn
                                                isFeedbackError = true
                                            }
                                            is QrMissionEngine.QrValidationResult.NotConfigured -> {
                                                isMissionDismissed = true
                                                viewModel.stopAlarmRinging()
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("টোকেন যাচাই করুন")
                                }
                            }
                        }
                    }
                }

                // 5. CAMERA PHOTO MISSION
                AlarmMissionType.PHOTO_TARGET -> {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        IslamicCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = if (isBengali) "ছবি মেলানো মিশন" else "Photo Target Mission",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "টার্গেট বস্তু: ${config.registeredTargetName.ifBlank { "জায়নামাজ" }}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                            )
                            Text(
                                text = if (isBengali) {
                                    "আপনার নিবন্ধিত বস্তুর স্পষ্ট ছবি তুলুন। পর্যাপ্ত আলোতে সরাসরি ক্যামেরার সামনে ধরুন।"
                                } else {
                                    "Take a clear photo of your registered prayer mat or object."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { cameraCaptureLauncher.launch() },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("open_photo_match_btn")
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBengali) "ছবি তুলে যাচাই করুন" else "Capture & Verify Target",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            // EMERGENCY SAFETY FALLBACK SECTION
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MinimalWhiteSurfaceVariant,
                    border = BorderStroke(1.dp, MinimalWhiteCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isBengali) "জরুরী সেফটি ব্যাকআপ (Safety Fallback)" else "Emergency Safety Fallback",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextDarkSecondary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBengali) {
                                "ক্যামেরা ত্রুটি বা অনিবার্য কারণে মিশন সম্পূর্ণ করা না গেলে নিচের বাটনে ৮ বার ট্যাপ করে জরুরী অ্যালার্ম বন্ধ করুন।"
                            } else {
                                "If camera or mission fails, tap 8 times below to safely unlock without blocking Fajr."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextDarkMuted,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val tapsRemaining = (8 - emergencyTapCount).coerceAtLeast(0)
                        OutlinedButton(
                            onClick = {
                                emergencyTapCount += 1
                                if (emergencyTapCount >= 8) {
                                    isMissionDismissed = true
                                    viewModel.stopAlarmRinging()
                                    statusFeedbackMessage = if (isBengali) "জরুরী সেফটি আনলক কার্যকর হয়েছে।" else "Emergency safety unlock activated."
                                    isFeedbackError = false
                                }
                            },
                            border = BorderStroke(1.dp, GoldBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldDark),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("emergency_unlock_btn")
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (tapsRemaining > 0) {
                                    if (isBengali) "জরুরী আনলক ($tapsRemaining বার ট্যাপ বাকি)" else "Emergency Unlock ($tapsRemaining taps left)"
                                } else {
                                    if (isBengali) "আনলক সম্পন্ন!" else "Unlocked!"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
