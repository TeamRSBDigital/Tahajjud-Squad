package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mission.QrMissionEngine
import com.example.ui.theme.*

/**
 * QR Code Display & Export Card.
 *
 * Renders the user's registered Tahajjud mission QR code with options
 * to print, share, toggle between high-contrast and branded styles,
 * and copy the token to clipboard.
 */
@Composable
fun QrCodeDisplayCard(
    token: String,
    onRegenerateToken: () -> Unit,
    isBengali: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isHighContrastMode by remember { mutableStateOf(true) }

    val qrBitmap: Bitmap? = remember(token, isHighContrastMode) {
        if (token.isNotBlank()) {
            if (isHighContrastMode) {
                QrMissionEngine.generateHighContrastPrintableQr(token, 600)
            } else {
                QrMissionEngine.generateBrandedQrBitmap(token, 600)
            }
        } else null
    }

    IslamicCard(
        backgroundColor = MinimalWhiteSurface,
        borderColor = GoldPrimary.copy(alpha = 0.5f),
        modifier = modifier
            .fillMaxWidth()
            .testTag("qr_code_display_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isBengali) "নিবন্ধিত কিউআর কোড" else "Registered QR Mission Code",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDarkPrimary
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = token.ifBlank { if (isBengali) "কোনো কোড নেই" else "No token generated" },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        fontSize = 13.sp
                    )
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = GoldSoft,
                border = BorderStroke(1.dp, GoldBorder),
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = GoldDark, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBengali) "অফলাইন নিরাপদ" else "Offline Safe",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GoldDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // QR Code Preview Box
        Box(
            modifier = Modifier
                .size(230.dp)
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, MinimalWhiteCardBorder, RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Tahajjud Squad QR Code",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                CircularProgressIndicator(
                    color = EmeraldPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // High contrast vs branded style toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = isHighContrastMode,
                onClick = { isHighContrastMode = true },
                label = { Text(if (isBengali) "প্রিন্টের উপযোগী (কালো/সাদা)" else "Print High Contrast", fontSize = 11.sp) },
                leadingIcon = {
                    if (isHighContrastMode) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                },
                modifier = Modifier.padding(end = 6.dp)
            )
            FilterChip(
                selected = !isHighContrastMode,
                onClick = { isHighContrastMode = false },
                label = { Text(if (isBengali) "পান্না সবুজ থিম" else "Emerald Theme", fontSize = 11.sp) },
                leadingIcon = {
                    if (!isHighContrastMode) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Buttons: Print/Share & Copy
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (qrBitmap != null) {
                        val uri = QrMissionEngine.exportQrBitmapToCache(context, qrBitmap, token)
                        if (uri != null) {
                            val shareIntent = QrMissionEngine.createShareQrIntent(context, uri, token, isBengali)
                            context.startActivity(android.content.Intent.createChooser(shareIntent, if (isBengali) "কিউআর কোড প্রিন্ট বা শেয়ার করুন" else "Print or Share QR Code"))
                        } else {
                            Toast.makeText(context, if (isBengali) "কোড এক্সপোর্ট করা সম্ভব হয়নি" else "Could not export QR", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("print_share_qr_btn")
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isBengali) "প্রিন্ট / শেয়ার" else "Print / Share",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            OutlinedButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    val clip = ClipData.newPlainText("Tahajjud QR Token", token)
                    clipboard?.setPrimaryClip(clip)
                    Toast.makeText(
                        context,
                        if (isBengali) "টোকেন কপি করা হয়েছে" else "Token copied to clipboard",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                border = BorderStroke(1.dp, EmeraldPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("copy_qr_token_btn")
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isBengali) "টোকেন কপি" else "Copy Token",
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onRegenerateToken,
            border = BorderStroke(1.dp, MinimalWhiteCardBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .testTag("regenerate_qr_token_btn")
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = TextDarkSecondary, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isBengali) "নতুন নিরাপদ টোকেন তৈরি করুন" else "Generate New Secure Token",
                color = TextDarkSecondary,
                fontSize = 11.5.sp
            )
        }
    }
}
