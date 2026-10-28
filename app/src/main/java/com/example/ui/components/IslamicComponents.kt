package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PrayerStatus
import com.example.model.PrayerType
import com.example.ui.theme.*

@Composable
fun IslamicTopHeader(
    title: String,
    subtitle: String,
    isBengali: Boolean,
    onLanguageToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MinimalWhiteSurface,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MinimalWhiteCardBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = EmeraldDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Brightness5,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextDarkSecondary,
                            fontSize = 12.sp
                        )
                    )
                }

                // Language Toggle Chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = EmeraldSoftBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onLanguageToggle() }
                        .testTag("language_toggle_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Language",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBengali) "বাংলা" else "EN",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IslamicCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MinimalWhiteSurface,
    borderColor: Color = MinimalWhiteCardBorder,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            content = content
        )
    }
}

@Composable
fun PrayerProgressRing(
    completedCount: Int,
    totalCount: Int = 5,
    size: Dp = 100.dp,
    strokeWidth: Dp = 9.dp,
    modifier: Modifier = Modifier
) {
    val progress = (completedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            // Background ring
            drawCircle(
                color = MinimalWhiteCardBorder,
                radius = (size.toPx() - strokeWidth.toPx()) / 2f,
                style = stroke
            )
            // Progress arc
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(GoldPrimary, EmeraldAccent, EmeraldPrimary)
                ),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = stroke
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$completedCount/$totalCount",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDark
                )
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextDarkSecondary,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
fun PrayerStatusChip(
    status: PrayerStatus,
    isSelected: Boolean,
    onClick: () -> Unit,
    isBengali: Boolean,
    modifier: Modifier = Modifier
) {
    val (chipBg, chipContentColor, borderCol) = when (status) {
        PrayerStatus.PERFORMED -> if (isSelected) Triple(StatusPerformed, Color.White, StatusPerformed) else Triple(StatusPerformedLight, StatusPerformed, StatusPerformed.copy(alpha = 0.3f))
        PrayerStatus.MISSED -> if (isSelected) Triple(StatusMissed, Color.White, StatusMissed) else Triple(StatusMissedLight, StatusMissed, StatusMissed.copy(alpha = 0.3f))
        PrayerStatus.QAZA -> if (isSelected) Triple(StatusQaza, Color.White, StatusQaza) else Triple(StatusQazaLight, StatusQaza, StatusQaza.copy(alpha = 0.3f))
        PrayerStatus.NOT_RECORDED -> if (isSelected) Triple(StatusUnrecorded, Color.White, StatusUnrecorded) else Triple(StatusUnrecordedLight, StatusUnrecorded, StatusUnrecorded.copy(alpha = 0.3f))
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = chipBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderCol),
        modifier = modifier
            .defaultMinSize(minHeight = 36.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .testTag("status_chip_${status.code}")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (isBengali) status.bengaliLabel else status.englishLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = chipContentColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ArabicGeometricPatternDecoration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val goldTint = Color(0xFFD4AF37).copy(alpha = 0.08f)

        // Draw geometric diamond grid
        val step = 32f
        var x = 0f
        while (x < width) {
            drawLine(
                color = goldTint,
                start = Offset(x, 0f),
                end = Offset(x + height, height),
                strokeWidth = 1f
            )
            drawLine(
                color = goldTint,
                start = Offset(x, height),
                end = Offset(x + height, 0f),
                strokeWidth = 1f
            )
            x += step
        }
    }
}
