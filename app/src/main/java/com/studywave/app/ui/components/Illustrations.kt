package com.studywave.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.studywave.app.ui.theme.StudyWaveColors

@Composable
fun StudyCompanionIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    hairColor: Color = Color(0xFF2B2B33),
    hoodieColor: Color = Color(0xFFE8EEF8)
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size / 4))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFEAF3FF), Color(0xFFF6F0FF))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val skin = Color(0xFFFFD9C2)
            val blush = Color(0xFFFFB3B3)

            drawCircle(Color(0xFF9ED0FF).copy(alpha = 0.35f), radius = w * 0.42f, center = Offset(cx, h * 0.55f))

            drawRoundRect(
                color = hoodieColor,
                topLeft = Offset(w * 0.22f, h * 0.58f),
                size = Size(w * 0.56f, h * 0.42f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.12f, w * 0.12f)
            )

            drawCircle(skin, radius = w * 0.22f, center = Offset(cx, h * 0.42f))

            drawArc(
                color = hairColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(cx - w * 0.24f, h * 0.16f),
                size = Size(w * 0.48f, h * 0.32f)
            )
            drawCircle(hairColor, radius = w * 0.07f, center = Offset(cx - w * 0.18f, h * 0.34f))
            drawCircle(hairColor, radius = w * 0.07f, center = Offset(cx + w * 0.18f, h * 0.34f))

            drawCircle(Color.White, radius = w * 0.045f, center = Offset(cx - w * 0.08f, h * 0.42f))
            drawCircle(Color.White, radius = w * 0.045f, center = Offset(cx + w * 0.08f, h * 0.42f))
            drawCircle(Color(0xFF2A3348), radius = w * 0.022f, center = Offset(cx - w * 0.075f, h * 0.42f))
            drawCircle(Color(0xFF2A3348), radius = w * 0.022f, center = Offset(cx + w * 0.085f, h * 0.42f))

            drawCircle(blush.copy(alpha = 0.55f), radius = w * 0.03f, center = Offset(cx - w * 0.15f, h * 0.48f))
            drawCircle(blush.copy(alpha = 0.55f), radius = w * 0.03f, center = Offset(cx + w * 0.15f, h * 0.48f))

            drawArc(
                color = Color(0xFFE08A8A),
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(cx - w * 0.05f, h * 0.48f),
                size = Size(w * 0.1f, h * 0.06f),
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )

            drawCircle(Color(0xFFB8C4D8), radius = w * 0.05f, center = Offset(cx - w * 0.2f, h * 0.38f))
            drawCircle(Color(0xFFB8C4D8), radius = w * 0.05f, center = Offset(cx + w * 0.2f, h * 0.38f))
            drawLine(
                color = Color(0xFFB8C4D8),
                start = Offset(cx - w * 0.16f, h * 0.36f),
                end = Offset(cx + w * 0.16f, h * 0.36f),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun GirlTutorIllustration(modifier: Modifier = Modifier, size: Dp = 140.dp) {
    StudyCompanionIllustration(
        modifier = modifier,
        size = size,
        hairColor = Color(0xFF3A2A24),
        hoodieColor = Color(0xFFF3E8FF)
    )
}

@Composable
fun BookMarkLogo(modifier: Modifier = Modifier, size: Dp = 72.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(StudyWaveColors.PrimaryPurple, StudyWaveColors.SoftBlue)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(size * 0.55f)) {
            val w = this.size.width
            val h = this.size.height
            val path = Path().apply {
                moveTo(w * 0.15f, h * 0.1f)
                lineTo(w * 0.85f, h * 0.1f)
                lineTo(w * 0.85f, h * 0.9f)
                lineTo(w * 0.5f, h * 0.72f)
                lineTo(w * 0.15f, h * 0.9f)
                close()
            }
            drawPath(path, Color.White)
            drawLine(
                Color(0xFF4C8DFF),
                Offset(w * 0.5f, h * 0.22f),
                Offset(w * 0.5f, h * 0.62f),
                strokeWidth = 6f
            )
        }
    }
}

@Composable
fun WaveformBars(active: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val bars = 28
        val gap = size.width / bars
        for (i in 0 until bars) {
            val mid = kotlin.math.abs(i - bars / 2f) / (bars / 2f)
            val amp = if (active) (0.25f + (1f - mid) * 0.7f) else 0.18f
            val h = size.height * amp * (0.4f + (i % 4) * 0.18f)
            val color = when (i % 3) {
                0 -> StudyWaveColors.WaveformA
                1 -> StudyWaveColors.WaveformB
                else -> StudyWaveColors.WaveformC
            }
            drawRoundRect(
                color = color,
                topLeft = Offset(i * gap + gap * 0.25f, (size.height - h) / 2f),
                size = Size(gap * 0.45f, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
            )
        }
    }
}

@Composable
fun OnlineDot(online: Boolean) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(if (online) StudyWaveColors.Success else StudyWaveColors.SecondaryText)
    )
}
