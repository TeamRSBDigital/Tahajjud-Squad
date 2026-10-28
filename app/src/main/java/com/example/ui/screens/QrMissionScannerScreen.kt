package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.mission.QrMissionEngine
import com.example.ui.components.IslamicCard
import com.example.ui.components.ReferenceCameraViewfinder
import com.example.ui.theme.*
import com.example.viewmodel.AmalViewModel

/**
 * QrMissionScannerScreen
 *
 * Dedicated Emerald-Green themed QR Mission Scanner Screen.
 *
 * Key Capabilities:
 * 1. Uses the existing [com.google.zxing] library to decode camera frames locally in RAM.
 * 2. Cryptographic Hash Validation: Validates scanned codes by computing the SHA-256 hash
 *    of the scanned payload and matching against [storedMissionHash], eliminating raw string comparison.
 * 3. Persistent Back Navigation with top-bar icon and [BackHandler].
 * 4. Emerald-Green themed camera viewport overlay with gold corner brackets and animated sweep laser.
 * 5. Fajr Protection emergency bypass (8 taps) and accessible manual fallback.
 */
@Composable
fun QrMissionScannerScreen(
    storedMissionHash: String,
    onValidationResult: (isValid: Boolean, scannedToken: String, computedHash: String) -> Unit,
    onNavigateBack: () -> Unit,
    onMissionSuccess: (scannedToken: String) -> Unit = {},
    isBengali: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Hardware back handler
    BackHandler { onNavigateBack() }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var isScanningActive by remember { mutableStateOf(false) }
    var scanResultMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessState by remember { mutableStateOf(false) }
    var lastScannedToken by remember { mutableStateOf<String?>(null) }
    var lastComputedHash by remember { mutableStateOf<String?>(null) }
    var showManualTokenEntry by remember { mutableStateOf(false) }
    var manualTokenInput by remember { mutableStateOf("") }
    var emergencyTapCount by remember { mutableStateOf(0) }

    // Camera Capture Launcher: Decodes frame with ZXing and verifies cryptographic hash
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { capturedBitmap: Bitmap? ->
        isScanningActive = false
        if (capturedBitmap != null) {
            val decodedToken = QrMissionEngine.decodeQrBitmap(capturedBitmap)

            if (decodedToken.isNullOrBlank()) {
                isSuccessState = false
                val msg = if (isBengali) {
                    "কোনো কিউআর কোড শনাক্ত করা যায়নি। অনুগ্রহ করে ক্যামেরার সামনে কোডটি সঠিকভাবে ধরুন।"
                } else {
                    "No QR code detected. Please align the code clearly in the scanner view."
                }
                scanResultMessage = msg
                onValidationResult(false, "", "")
                return@rememberLauncherForActivityResult
            }

            val cleanToken = decodedToken.trim().uppercase()
            lastScannedToken = cleanToken

            // 1. Format & Mathematical Checksum Verification
            if (!QrMissionEngine.isValidTokenFormat(cleanToken)) {
                isSuccessState = false
                val msg = if (isBengali) "অবৈধ ফরম্যাট! এটি তাহাজ্জুদ স্কোয়াড মিশন কোড নয়।" else "Invalid format! Not a recognized mission code."
                scanResultMessage = msg
                onValidationResult(false, cleanToken, "")
                return@rememberLauncherForActivityResult
            }

            // 2. Cryptographic SHA-256 Hash Calculation
            val computedHash = QrMissionEngine.hashMissionToken(cleanToken)
            lastComputedHash = computedHash

            // 3. Constant-time secure hash comparison against stored mission hash
            val isValid = QrMissionEngine.verifyScannedTokenHash(cleanToken, storedMissionHash)

            // 4. Trigger validation callback
            onValidationResult(isValid, cleanToken, computedHash)

            if (isValid) {
                isSuccessState = true
                scanResultMessage = if (isBengali) {
                    "ক্রিপ্টোগ্রাফিক হ্যাশ যাচাই সফল! তাহাজ্জুদ মিশন সম্পন্ন।"
                } else {
                    "Cryptographic Hash Verified! Tahajjud Mission Passed."
                }
                onMissionSuccess(cleanToken)
            } else {
                isSuccessState = false
                scanResultMessage = if (isBengali) {
                    "অননুমোদিত কিউআর কোড! কোডের ক্রিপ্টোগ্রাফিক হ্যাশ আপনার নিবন্ধিত মিশনের সাথে মেলেনি।"
                } else {
                    "Unauthorized QR Code! Cryptographic hash does not match your registered mission hash."
                }
            }
        } else {
            scanResultMessage = if (isBengali) "ছবি গ্রহণ করা যায়নি। আবার চেষ্টা করুন।" else "Scan cancelled or failed. Please retry."
            isSuccessState = false
        }
    }

    // Permission Request Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) {
            isScanningActive = true
            takePictureLauncher.launch()
        } else {
            scanResultMessage = if (isBengali) {
                "ক্যামেরা পারমিশন দেওয়া হয়নি। ম্যানুয়াল কোড বা জরুরী আনলক ব্যবহার করুন।"
            } else {
                "Camera permission denied. Use manual code or emergency unlock."
            }
            isSuccessState = false
        }
    }

    // Animated vertical laser sweep line
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser_sweep")
    val sweepProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sweep_progress"
    )

    Scaffold(
        topBar = {
            Surface(
                color = MinimalWhiteSurface,
                border = BorderStroke(1.dp, MinimalWhiteCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Persistent Back Button
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("qr_scanner_persistent_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = EmeraldDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBengali) "কিউআর মিশন স্ক্যানার" else "QR Mission Scanner",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark,
                                fontSize = 17.sp
                            )
                        )
                        Text(
                            text = if (isBengali) "অন-ডিভাইস ZXing ও ক্রিপ্টোগ্রাফিক হ্যাশ যাচাই" else "ZXing On-Device Cryptographic Hash Verification",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextDarkSecondary,
                                fontSize = 10.5.sp
                            )
                        )
                    }

                    // Security Shield Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = EmeraldSoftBg,
                        border = BorderStroke(1.dp, EmeraldBorder),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.EnhancedEncryption, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SHA-256",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }
        },
        containerColor = MinimalWhiteBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Camera Feed Viewport Overlay (Emerald-Green themed container)
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF102117), // Deep Emerald/Charcoal Camera Backing
                    border = BorderStroke(
                        width = if (isSuccessState) 2.5.dp else 1.5.dp,
                        color = when {
                            isSuccessState -> EmeraldAccent
                            scanResultMessage != null && !isSuccessState -> StatusMissed
                            else -> EmeraldPrimary
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .testTag("camera_feed_overlay")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Emerald Green Corner Brackets
                        ReferenceCameraViewfinder(
                            bracketColor = if (isSuccessState) EmeraldAccent else GoldAccent,
                            frameSize = 180.dp,
                            bracketLength = 32.dp,
                            strokeWidth = 4.dp
                        )

                        // Animated Scanning Sweep Line
                        Box(
                            modifier = Modifier
                                .width(180.dp)
                                .height(2.5.dp)
                                .offset(y = (-80).dp + (160.dp * sweepProgress))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            if (isSuccessState) EmeraldAccent else GoldAccent,
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // Viewport Center Content
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            if (isSuccessState) {
                                Surface(
                                    shape = CircleShape,
                                    color = EmeraldAccent.copy(alpha = 0.2f),
                                    modifier = Modifier.size(60.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Success",
                                            tint = EmeraldAccent,
                                            modifier = Modifier.size(42.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (isBengali) "হ্যাশ যাচাই সম্পন্ন!" else "Hash Match Confirmed!",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isBengali) "জায়নামাজে রাখা কোডটি ফ্রেমের মাঝে রাখুন" else "Center your prayer spot QR in the brackets",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Scan Result & Hash Validation Card
            item {
                Spacer(modifier = Modifier.height(14.dp))
                AnimatedVisibility(visible = scanResultMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSuccessState) EmeraldSoftBg else StatusMissedLight,
                        border = BorderStroke(1.dp, if (isSuccessState) EmeraldBorder else StatusMissed.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isSuccessState) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = if (isSuccessState) EmeraldDark else StatusMissed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = scanResultMessage ?: "",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (isSuccessState) EmeraldDark else StatusMissed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                )
                            }

                            if (lastComputedHash != null && isSuccessState) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "SHA-256: ${lastComputedHash?.take(16)}...${lastComputedHash?.takeLast(8)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = EmeraldDark.copy(alpha = 0.8f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Secure Hash Verification Explainer Card
            item {
                Spacer(modifier = Modifier.height(12.dp))
                IslamicCard(
                    backgroundColor = MinimalWhiteSurface,
                    borderColor = GoldBorder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = GoldDark, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isBengali) "ক্রিপ্টোগ্রাফিক হ্যাশ যাচাই নীতি" else "Cryptographic Hash Policy",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GoldDark,
                                    fontSize = 13.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (isBengali) {
                                    "অ্যাপটি কোনো অপরিবর্তিত র-স্ট্রিং (Raw String) মিলিয়ে দেখে না। স্ক্যান করা কোডটি সল্টযুক্ত SHA-256 হ্যাশ অ্যালগরিদমে রূপান্তরিত করে কনস্ট্যান্ট-টাইম পদ্ধতিতে সংরক্ষিত হ্যাশের সাথে যাচাই করা হয়।"
                                } else {
                                    "Validation uses a salted SHA-256 cryptographic digest with constant-time comparison against your stored mission hash, preventing raw string leakage."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary, fontSize = 11.5.sp)
                            )
                        }
                    }
                }
            }

            // Guidance & Lighting Checklist
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MinimalWhiteSurfaceVariant,
                    border = BorderStroke(1.dp, MinimalWhiteCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LightMode, contentDescription = null, tint = GoldDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBengali) "রাতের অন্ধকার দূর করতে ঘরের আলো অন রাখুন" else "Turn on room lights for clear visibility",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary, fontSize = 11.5.sp)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CropFree, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBengali) "কোডটি ব্র্যাকেট ফ্রেমের মধ্যে সমান্তরাল রাখুন" else "Keep the QR code parallel and inside corner brackets",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary, fontSize = 11.5.sp)
                            )
                        }
                    }
                }
            }

            // Primary Launch Scanner Action Button
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (hasCameraPermission) {
                            isScanningActive = true
                            takePictureLauncher.launch()
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("launch_zxing_camera_btn")
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBengali) "ক্যামেরা ওপেন করে কিউআর স্ক্যান করুন" else "Open Camera & Scan QR Code",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            // Fallback & Emergency Actions
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showManualTokenEntry = !showManualTokenEntry },
                        modifier = Modifier.testTag("toggle_manual_hash_entry_btn")
                    ) {
                        Icon(
                            if (showManualTokenEntry) Icons.Default.KeyboardArrowUp else Icons.Default.Keyboard,
                            contentDescription = null,
                            tint = TextDarkSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBengali) {
                                if (showManualTokenEntry) "ম্যানুয়াল অপশন লুকান" else "ম্যানুয়ালি টোকেন লিখুন"
                            } else {
                                if (showManualTokenEntry) "Hide Manual" else "Enter Token Manually"
                            },
                            color = TextDarkSecondary,
                            fontSize = 12.sp
                        )
                    }

                    // Emergency 8-Tap Unlock (Fajr Protection)
                    TextButton(
                        onClick = {
                            emergencyTapCount++
                            if (emergencyTapCount >= 8) {
                                onValidationResult(true, "EMERGENCY_BYPASS", "")
                                onMissionSuccess("EMERGENCY_BYPASS")
                            } else {
                                val remaining = 8 - emergencyTapCount
                                scanResultMessage = if (isBengali) {
                                    "জরুরী সেফটি আনলক: আর $remaining বার ট্যাপ করুন"
                                } else {
                                    "Emergency Unlock: Tap $remaining more times"
                                }
                            }
                        },
                        modifier = Modifier.testTag("emergency_hash_bypass_btn")
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = null, tint = StatusMissed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (emergencyTapCount > 0) {
                                "${8 - emergencyTapCount} ট্যাপ বাকি"
                            } else {
                                if (isBengali) "জরুরী আনলক" else "Emergency Unlock"
                            },
                            color = StatusMissed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Accessible Manual Code Field (Hashes manual input and verifies against stored hash)
            if (showManualTokenEntry) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = manualTokenInput,
                        onValueChange = { manualTokenInput = it.uppercase() },
                        label = { Text(if (isBengali) "নিবন্ধিত টোকেন (TS-QR-...)" else "Registered Token (TS-QR-...)") },
                        placeholder = { Text("TS-QR-XXXXXXXXXXXX") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_token_hash_field")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val cleanInput = manualTokenInput.trim().uppercase()
                            if (QrMissionEngine.isValidTokenFormat(cleanInput)) {
                                val computedHash = QrMissionEngine.hashMissionToken(cleanInput)
                                val isValid = QrMissionEngine.verifyScannedTokenHash(cleanInput, storedMissionHash)
                                onValidationResult(isValid, cleanInput, computedHash)

                                if (isValid) {
                                    isSuccessState = true
                                    scanResultMessage = if (isBengali) "হ্যাশ যাচাই সফল হয়েছে!" else "Hash Verified Successfully!"
                                    onMissionSuccess(cleanInput)
                                } else {
                                    isSuccessState = false
                                    scanResultMessage = if (isBengali) "টোকেনের ক্রিপ্টোগ্রাফিক হ্যাশ মেলেনি।" else "Token hash mismatch."
                                }
                            } else {
                                isSuccessState = false
                                scanResultMessage = if (isBengali) "অবৈধ টোকেন ফরম্যাট।" else "Invalid token format."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("verify_manual_token_hash_btn")
                    ) {
                        Text(if (isBengali) "টোকেন হ্যাশ যাচাই করুন" else "Verify Token Hash", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Convenience overload for integrating with [AmalViewModel].
 */
@Composable
fun QrMissionScannerScreen(
    viewModel: AmalViewModel,
    onNavigateBack: () -> Unit,
    onMissionCompleted: () -> Unit,
    onValidationResult: ((isValid: Boolean, scannedToken: String, computedHash: String) -> Unit)? = null,
    isBengali: Boolean = true,
    modifier: Modifier = Modifier
) {
    val config by viewModel.smartAlarmConfig.collectAsState()
    val storedMissionHash = remember(config.registeredQrToken) {
        if (config.registeredQrToken.isNotBlank()) {
            QrMissionEngine.hashMissionToken(config.registeredQrToken)
        } else ""
    }

    QrMissionScannerScreen(
        storedMissionHash = storedMissionHash,
        onValidationResult = { isValid, scannedToken, computedHash ->
            onValidationResult?.invoke(isValid, scannedToken, computedHash)
            if (isValid) {
                viewModel.stopAlarmRinging()
            }
        },
        onNavigateBack = onNavigateBack,
        onMissionSuccess = {
            viewModel.stopAlarmRinging()
            onMissionCompleted()
        },
        isBengali = isBengali,
        modifier = modifier
    )
}
