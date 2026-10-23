package com.studywave.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SettingsSuggest
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studywave.app.ui.theme.StudyWaveColors
import com.studywave.app.ui.theme.StudyWaveRadius

@Composable
fun SwCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    color: Color = StudyWaveColors.CardWhite,
    radius: Dp = StudyWaveRadius.Card,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(radius)
    Column(
        modifier = modifier
            .shadow(8.dp, shape, ambientColor = StudyWaveColors.Shadow, spotColor = StudyWaveColors.Shadow)
            .clip(shape)
            .background(color)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun SwPrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, label = "btn")
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        interactionSource = interaction,
        modifier = modifier
            .scale(scale)
            .height(52.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(StudyWaveRadius.Button),
        colors = ButtonDefaults.buttonColors(
            containerColor = StudyWaveColors.PrimaryPurple,
            contentColor = Color.White,
            disabledContainerColor = StudyWaveColors.PrimaryPurple.copy(alpha = 0.45f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}

@Composable
fun SwSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = StudyWaveColors.SecondaryText) },
        leadingIcon = {
            Icon(Icons.Outlined.Search, contentDescription = null, tint = StudyWaveColors.SecondaryText)
        },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = StudyWaveColors.PrimaryPurple,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = StudyWaveColors.SecondaryBackground,
            unfocusedContainerColor = StudyWaveColors.SecondaryBackground,
            cursorColor = StudyWaveColors.PrimaryPurple
        )
    )
}

@Composable
fun SwTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        isError = isError,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(StudyWaveRadius.Input),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = StudyWaveColors.PrimaryPurple,
            unfocusedBorderColor = StudyWaveColors.Divider,
            cursorColor = StudyWaveColors.PrimaryPurple
        )
    )
}

@Composable
fun FilterChipRow(
    items: List<String>,
    selected: String,
    modifier: Modifier = Modifier,
    onSelected: (String) -> Unit
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            val active = item.equals(selected, true)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (active) StudyWaveColors.PrimaryPurple else StudyWaveColors.ChipInactive)
                    .clickable { onSelected(item) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    item,
                    color = if (active) Color.White else StudyWaveColors.SecondaryText,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = StudyWaveColors.MainText)
        if (action != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(action, color = StudyWaveColors.PrimaryPurple, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    icon: ImageVector = Icons.Outlined.Inbox,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(StudyWaveColors.LavenderSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = StudyWaveColors.PrimaryPurple, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = StudyWaveColors.MainText)
        Spacer(Modifier.height(6.dp))
        Text(message, color = StudyWaveColors.SecondaryText, textAlign = TextAlign.Center, fontSize = 14.sp)
        if (action != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            SwPrimaryButton(text = action, modifier = Modifier.width(200.dp), onClick = onAction)
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: (() -> Unit)? = null) {
    EmptyState(
        title = "Could not load",
        message = message,
        icon = Icons.Outlined.ErrorOutline,
        action = if (onRetry != null) "Try again" else null,
        onAction = onRetry
    )
}

@Composable
fun OfflineBanner(visible: Boolean) {
    if (!visible) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudyWaveColors.SoftPeach)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.CloudOff, contentDescription = null, tint = StudyWaveColors.Warning)
        Spacer(Modifier.width(8.dp))
        Text("You are offline. Showing cached data when available.", color = StudyWaveColors.MainText, fontSize = 13.sp)
    }
}

@Composable
fun SetupRequiredBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudyWaveColors.LavenderSurface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.SettingsSuggest, contentDescription = null, tint = StudyWaveColors.PrimaryPurple)
        Spacer(Modifier.width(8.dp))
        Text(message, color = StudyWaveColors.MainText, fontSize = 13.sp)
    }
}

@Composable
fun LoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = StudyWaveColors.PrimaryPurple)
    }
}

@Composable
fun GradientHeader(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(StudyWaveColors.LavenderSurface, StudyWaveColors.LightBlueSurface)
                )
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
fun SubjectBadge(subject: String) {
    val color = when (subject.lowercase()) {
        "physics" -> Color(0xFFE53935)
        "chemistry" -> Color(0xFFFB8C00)
        "math", "mathematics" -> Color(0xFF1E88E5)
        "biology" -> Color(0xFF43A047)
        "english" -> Color(0xFF8E24AA)
        else -> StudyWaveColors.PrimaryPurple
    }
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            subject.take(1).uppercase(),
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun StatusPill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SoftIconButton(
    icon: ImageVector,
    contentDescription: String?,
    tint: Color = StudyWaveColors.MainText,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(StudyWaveColors.SecondaryBackground)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun ProgressRing(progress: Float, label: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(88.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.fillMaxSize(),
            color = StudyWaveColors.Divider,
            strokeWidth = 8.dp,
            trackColor = Color.Transparent
        )
        CircularProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxSize(),
            color = StudyWaveColors.PrimaryPurple,
            strokeWidth = 8.dp,
            trackColor = Color.Transparent
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${(progress * 100).toInt()}%", fontWeight = FontWeight.Bold, color = StudyWaveColors.MainText)
            Text(label, fontSize = 10.sp, color = StudyWaveColors.SecondaryText)
        }
    }
}

@Composable
fun QuickActionTile(
    title: String,
    icon: ImageVector,
    tint: Color,
    background: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = tint)
        }
        Spacer(Modifier.height(8.dp))
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = StudyWaveColors.MainText)
    }
}
