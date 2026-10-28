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

// ============================================================================
// REFERENCE UI SYSTEM COMPONENTS (Matching reference screenshots)
// ============================================================================

/**
 * Reference UI Header with Lavender Aura gradient, greeting, and action pills.
 */
@Composable
fun ReferenceAppHeader(
    greeting: String,
    title: String,
    isBengali: Boolean,
    onLanguageToggle: () -> Unit,
    onNotificationClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        LavenderAuraStart,
                        LavenderAuraMid,
                        LavenderAuraEnd,
                        MinimalWhiteBackground
                    )
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextDarkSecondary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = TextDarkPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 21.sp
                    )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Language toggle pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MinimalWhiteSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MinimalWhiteCardBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onLanguageToggle() }
                        .testTag("header_language_pill")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (isBengali) "বাং" else "EN",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // Notification Bell Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MinimalWhiteSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MinimalWhiteCardBorder),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNotificationClick() }
                        .testTag("header_notification_btn")
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "Notifications",
                            tint = TextDarkPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        // Tiny notification dot
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 8.dp, end = 8.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary)
                        )
                    }
                }

                // Profile Avatar with subtle Islamic Green / Gold border
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(EmeraldSoftBg)
                        .border(1.5.dp, EmeraldPrimary, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = EmeraldDark,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/**
 * 7-Day Horizontal Calendar Strip matching reference screen 1.
 */
data class DayCalendarItem(
    val dayName: String,
    val dayNumber: String,
    val isToday: Boolean,
    val isSelected: Boolean
)

@Composable
fun HorizontalDayCalendarStrip(
    days: List<DayCalendarItem>,
    onSelectDay: (DayCalendarItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEach { item ->
            val isHighlighted = item.isSelected

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onSelectDay(item) }
                    .padding(vertical = 4.dp, horizontal = 2.dp)
                    .testTag("day_strip_${item.dayNumber}")
            ) {
                Text(
                    text = item.dayName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isHighlighted) EmeraldDark else TextDarkMuted,
                        fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (isHighlighted) EmeraldDark else MinimalWhiteSurface
                        )
                        .border(
                            width = 1.dp,
                            color = if (isHighlighted) EmeraldDark else MinimalWhiteCardBorder,
                            shape = CircleShape
                        )
                ) {
                    Text(
                        text = item.dayNumber,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isHighlighted) Color.White else TextDarkPrimary,
                            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    )

                    // Small indicator dot for active/today day
                    if (item.isToday) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 3.dp)
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(if (isHighlighted) GoldPrimary else EmeraldPrimary)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3/4 Circular Arc Gauge matching the reference "Today's Goal" hero ring.
 */
@Composable
fun CircularGoalArc(
    progress: Float, // 0.0f to 1.0f
    modifier: Modifier = Modifier,
    size: Dp = 104.dp,
    strokeWidth: Dp = 9.dp,
    trackColor: Color = Color(0xFFEFEFF4),
    progressBrush: Brush = Brush.sweepGradient(listOf(GoldPrimary, EmeraldAccent, EmeraldDark)),
    centerIcon: @Composable () -> Unit = {}
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val startAngle = 135f
    val sweepMaxAngle = 270f
    val currentSweepAngle = sweepMaxAngle * clampedProgress

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)

            // Background track arc
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = sweepMaxAngle,
                useCenter = false,
                style = stroke
            )

            // Progress arc
            if (currentSweepAngle > 0f) {
                drawArc(
                    brush = progressBrush,
                    startAngle = startAngle,
                    sweepAngle = currentSweepAngle,
                    useCenter = false,
                    style = stroke
                )
            }
        }

        centerIcon()
    }
}

/**
 * Mini Metric Ring with 3/4 arc, percentage or fraction, and label underneath.
 */
@Composable
fun MiniMetricRing(
    valueText: String,
    labelText: String,
    progress: Float,
    arcColor: Color,
    trackColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(46.dp)
        ) {
            Canvas(modifier = Modifier.size(46.dp)) {
                val stroke = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
                // Track arc
                drawArc(
                    color = trackColor,
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = stroke
                )
                // Progress arc
                drawArc(
                    color = arcColor,
                    startAngle = 135f,
                    sweepAngle = 270f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    style = stroke
                )
            }

            Text(
                text = valueText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextDarkPrimary,
                    fontSize = 11.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = labelText,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextDarkSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            ),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Curved Spline Progress Line Chart with soft gradient fill under the curve.
 * Matching Reference Image 2 & 3.
 */
@Composable
fun WeeklySplineChart(
    points: List<Float>, // 7 values normalized between 0.0f and 1.0f
    labels: List<String>, // 7 day labels
    modifier: Modifier = Modifier,
    lineColor: Color = LavenderAccent,
    fillBrush: Brush = Brush.verticalGradient(
        colors = listOf(
            LavenderAccent.copy(alpha = 0.35f),
            LavenderAccent.copy(alpha = 0.05f),
            Color.Transparent
        )
    )
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            if (points.isEmpty()) return@Canvas

            val width = size.width
            val height = size.height
            val spacing = width / (points.size - 1).coerceAtLeast(1)
            val paddingBottom = 12.dp.toPx()
            val paddingTop = 12.dp.toPx()
            val usableHeight = height - paddingTop - paddingBottom

            val coordinates = points.mapIndexed { index, ratio ->
                val x = index * spacing
                val y = paddingTop + (1f - ratio.coerceIn(0f, 1f)) * usableHeight
                Offset(x, y)
            }

            // Draw smooth cubic bezier path
            val strokePath = androidx.compose.ui.graphics.Path()
            val fillPath = androidx.compose.ui.graphics.Path()

            strokePath.moveTo(coordinates[0].x, coordinates[0].y)
            fillPath.moveTo(coordinates[0].x, height)
            fillPath.lineTo(coordinates[0].x, coordinates[0].y)

            for (i in 0 until coordinates.size - 1) {
                val p0 = coordinates[i]
                val p1 = coordinates[i + 1]
                val controlX1 = p0.x + (p1.x - p0.x) / 2f
                val controlY1 = p0.y
                val controlX2 = p0.x + (p1.x - p0.x) / 2f
                val controlY2 = p1.y

                strokePath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
            }

            fillPath.lineTo(coordinates.last().x, height)
            fillPath.close()

            // Draw filled area under curve
            drawPath(path = fillPath, brush = fillBrush)

            // Draw curve line
            drawPath(
                path = strokePath,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw circular points on nodes
            coordinates.forEach { pt ->
                drawCircle(color = Color.White, radius = 4.5.dp.toPx(), center = pt)
                drawCircle(color = lineColor, radius = 3.dp.toPx(), center = pt)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // X-Axis day labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextDarkMuted,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Camera / QR Viewfinder Overlay with 4 Corner Brackets (┌ ┐, └ ┘)
 * Matching Reference Image 3 & 4.
 */
@Composable
fun ReferenceCameraViewfinder(
    modifier: Modifier = Modifier,
    bracketColor: Color = Color.White,
    bracketLength: Dp = 32.dp,
    strokeWidth: Dp = 4.dp,
    frameSize: Dp = 220.dp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(frameSize)
    ) {
        Canvas(modifier = Modifier.size(frameSize)) {
            val len = bracketLength.toPx()
            val stroke = strokeWidth.toPx()
            val w = size.width
            val h = size.height

            // Top-Left bracket: ┌
            drawLine(color = bracketColor, start = Offset(0f, 0f), end = Offset(len, 0f), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(color = bracketColor, start = Offset(0f, 0f), end = Offset(0f, len), strokeWidth = stroke, cap = StrokeCap.Round)

            // Top-Right bracket: ┐
            drawLine(color = bracketColor, start = Offset(w, 0f), end = Offset(w - len, 0f), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(color = bracketColor, start = Offset(w, 0f), end = Offset(w, len), strokeWidth = stroke, cap = StrokeCap.Round)

            // Bottom-Left bracket: └
            drawLine(color = bracketColor, start = Offset(0f, h), end = Offset(len, h), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(color = bracketColor, start = Offset(0f, h), end = Offset(0f, h - len), strokeWidth = stroke, cap = StrokeCap.Round)

            // Bottom-Right bracket: ┘
            drawLine(color = bracketColor, start = Offset(w, h), end = Offset(w - len, h), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(color = bracketColor, start = Offset(w, h), end = Offset(w, h - len), strokeWidth = stroke, cap = StrokeCap.Round)
        }
    }
}

