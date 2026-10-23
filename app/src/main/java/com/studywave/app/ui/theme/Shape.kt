package com.studywave.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val StudyWaveShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

object StudyWaveRadius {
    val Card = 22.dp
    val Button = 18.dp
    val Chip = 14.dp
    val Sheet = 28.dp
    val Avatar = 48.dp
    val Input = 16.dp
}
