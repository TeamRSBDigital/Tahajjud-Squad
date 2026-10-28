package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.mission.QrMissionEngine
import com.example.ui.theme.*

/**
 * Reusable Secure QR Code Scanner Component using ZXing for Tahajjud Alarm Missions.
 *
 * Implements:
 * 1. ZXing-driven on-device QR frame analysis with 100% local memory processing.
 * 2. Mathematical Checksum & Local Allowlist validation (rejects invalid/counterfeit codes).
 * 3. Reference Viewfinder overlay with gold corner brackets and animated sweep laser.
 * 4. Runtime Camera permission handling with privacy rationale.
 * 5. Fajr Protection emergency bypass (8 taps) and manual token entry for accessibility.
 */
@Composable
fun QrScannerComponent(
    registeredToken: String,
    localAllowlist: Set<String> = emptySet(),
    onScanSuccess: (token: String) -> Unit,
    onScanFailure: (errorMessage: String) -> Unit,
    onEmergencyBypass: () -> Unit,
    isBengali: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var isScanningActive by remember { mutableStateOf(false) }
    var scanResultMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessState by remember { mutableStateOf(false) }
    var isChecksumVerifiedState by remember { mutableStateOf(false) }
    var showManualTokenEntry by remember { mutableStateOf(false) }
    var manualTokenInput by remember { mutableStateOf("") }
    var emergencyTapCount by remember { mutableStateOf(0) }

    // Camera Capture Launcher (Uses ZXing on captured local bitmap)
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { capturedBitmap: Bitmap? ->
        isScanningActive = false
        if (capturedBitmap != null) {
            val validation = QrMissionEngine.decodeAndValidateBitmap(
                bitmap = capturedBitmap,
                registeredToken = registeredToken,
                localAllowlist = localAllowlist,
                requireChecksum = true
            )

            when (validation) {
                is QrMissionEngine.QrValidationResult.ValidMatch -> {
                    isSuccessState = true
                    isChecksumVerifiedState = validation.checksumVerified
                    scanResultMessage = if (isBengali) {
                        "কিউআর কোড ও চেকসাম সফলভাবে যাচাই হয়েছে! মিশন সম্পন্ন।"
                    } else {
                        "QR Code & Checksum Verified On-Device! Mission Passed."
                    }
                    onScanSuccess(validation.token)
                }
                is QrMissionEngine.QrValidationResult.ChecksumFailure -> {
                    isSuccessState = false
                    isChecksumVerifiedState = false
                    val msg = if (isBengali) validation.messageBn else validation.messageEn
                    scanResultMessage = msg
                    onScanFailure(msg)
                }
                is QrMissionEngine.QrValidationResult.AllowlistRejected -> {
                    isSuccessState = false
                    isChecksumVerifiedState = false
                    val msg = if (isBengali) validation.messageBn else validation.messageEn
                    scanResultMessage = msg
                    onScanFailure(msg)
                }
                is QrMissionEngine.QrValidationResult.InvalidCode -> {
                    isSuccessState = false
                    isChecksumVerifiedState = false
                    val msg = if (isBengali) validation.messageBn else validation.messageEn
                    scanResultMessage = msg
                    onScanFailure(msg)
                }
                is QrMissionEngine.QrValidationResult.NotConfigured -> {
                    // Safe fallback if token was never configured
                    isSuccessState = true
                    scanResultMessage = if (isBengali) "মিশন সফল হয়েছে।" else "Mission Passed."
                    onScanSuccess(registeredToken)
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

    // Animated vertical laser scanning sweep
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_sweep")
    val sweepProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sweep_progress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("qr_scanner_component"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Privacy & Checksum Security Indicator Pill
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = GoldSoft,
            border = BorderStroke(1.dp, GoldBorder),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = GoldDark, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (isBengali) "ZXing অন-ডিভাইস স্ক্যানার • লোকাল চেকসাম ও এলাউলিস্ট সক্রিয়" else "ZXing On-Device • Checksum & Allowlist Protected",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = GoldDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp
                    )
                )
            }
        }

        // Camera Viewport Box with Reference Brackets
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF141D18),
            border = BorderStroke(
                width = if (isSuccessState) 2.dp else 1.dp,
                color = when {
                    isSuccessState -> EmeraldAccent
                    scanResultMessage != null && !isSuccessState -> StatusMissed
                    else -> EmeraldDark
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(20.dp))
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // Viewfinder Corner Brackets
                ReferenceCameraViewfinder(
                    bracketColor = if (isSuccessState) EmeraldAccent else GoldAccent,
                    frameSize = 150.dp,
                    bracketLength = 26.dp,
                    strokeWidth = 3.5.dp
                )

                // Animated vertical laser sweep line
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(2.dp)
                        .offset(y = (-65).dp + (130.dp * sweepProgress))
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

                // Center Icon and status
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (isSuccessState) {
                        Surface(
                            shape = CircleShape,
                            color = EmeraldAccent.copy(alpha = 0.2f),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = EmeraldAccent,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isBengali) "চেকসাম ও এলাউলিস্ট যাচাই সফল!" else "Checksum & Allowlist Verified!",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )
                    } else {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = "Scanner",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isBengali) "জায়নামাজের কিউআর কোড ফ্রেমের মধ্যে রাখুন" else "Align prayer mat QR in frame",
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

        Spacer(modifier = Modifier.height(10.dp))

        // Result / Feedback Message
        AnimatedVisibility(visible = scanResultMessage != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSuccessState) EmeraldSoftBg else StatusMissedLight,
                border = BorderStroke(1.dp, if (isSuccessState) EmeraldBorder else StatusMissed.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isSuccessState) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = if (isSuccessState) EmeraldDark else StatusMissed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = scanResultMessage ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isSuccessState) EmeraldDark else StatusMissed,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        // Night Lighting / Guidance Advice Checklist
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MinimalWhiteSurfaceVariant,
            border = BorderStroke(1.dp, MinimalWhiteCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LightMode, contentDescription = null, tint = GoldDark, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBengali) "রাতের অন্ধকার কাটাতে ঘরের আলো অন রাখুন" else "Turn on room light for clear camera vision",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary, fontSize = 11.5.sp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CropFree, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBengali) "কোডটি ব্র্যাকেট ফ্রেমের মধ্যে স্থির রাখুন" else "Hold code flat and steady in frame",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary, fontSize = 11.5.sp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBengali) "১০০% অফলাইন: কোনো ছবি বা ডেটা সার্ভারে যায় না" else "100% offline: Frames are evaluated locally and discarded",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary, fontSize = 11.5.sp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main Action: Launch Scanner Button
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
                .height(50.dp)
                .testTag("launch_qr_scanner_btn")
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isBengali) "ক্যামেরা দিয়ে কিউআর স্ক্যান করুন" else "Scan QR Code with Camera",
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Accessible Fallback & Emergency Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Manual code toggle
            TextButton(
                onClick = { showManualTokenEntry = !showManualTokenEntry },
                modifier = Modifier.testTag("toggle_manual_qr_btn")
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
                        if (showManualTokenEntry) "ম্যানুয়াল অপশন লুকান" else "ম্যানুয়ালি কোড লিখুন"
                    } else {
                        if (showManualTokenEntry) "Hide Manual" else "Enter Code Manually"
                    },
                    color = TextDarkSecondary,
                    fontSize = 12.sp
                )
            }

            // Emergency safety unlock (Fajr Protection policy)
            TextButton(
                onClick = {
                    emergencyTapCount++
                    if (emergencyTapCount >= 8) {
                        onEmergencyBypass()
                    } else {
                        val remaining = 8 - emergencyTapCount
                        scanResultMessage = if (isBengali) {
                            "জরুরী সেফটি আনলক: আর $remaining বার ট্যাপ করুন"
                        } else {
                            "Emergency Unlock: Tap $remaining more times"
                        }
                    }
                },
                modifier = Modifier.testTag("emergency_qr_bypass_btn")
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

        // Manual Token Entry Box
        AnimatedVisibility(visible = showManualTokenEntry) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = manualTokenInput,
                    onValueChange = { manualTokenInput = it.uppercase() },
                    label = { Text(if (isBengali) "নিবন্ধিত টোকেন (TS-QR-...)" else "Token Code (TS-QR-...)") },
                    placeholder = { Text("TS-QR-XXXXXXXXXXXX") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manual_token_input_field")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val validation = QrMissionEngine.validateScannedCode(
                            scannedCode = manualTokenInput,
                            registeredToken = registeredToken,
                            localAllowlist = localAllowlist,
                            requireChecksum = true
                        )
                        when (validation) {
                            is QrMissionEngine.QrValidationResult.ValidMatch -> {
                                isSuccessState = true
                                isChecksumVerifiedState = validation.checksumVerified
                                scanResultMessage = if (isBengali) "টোকেন ও চেকসাম সফলভাবে যাচাই হয়েছে!" else "Token & Checksum Verified!"
                                onScanSuccess(validation.token)
                            }
                            is QrMissionEngine.QrValidationResult.InvalidCode -> {
                                isSuccessState = false
                                isChecksumVerifiedState = false
                                scanResultMessage = if (isBengali) validation.messageBn else validation.messageEn
                                onScanFailure(scanResultMessage ?: "")
                            }
                            is QrMissionEngine.QrValidationResult.NotConfigured -> {
                                isSuccessState = true
                                onScanSuccess(registeredToken)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("verify_manual_token_btn")
                ) {
                    Text(if (isBengali) "টোকেন যাচাই করুন" else "Verify Token", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
