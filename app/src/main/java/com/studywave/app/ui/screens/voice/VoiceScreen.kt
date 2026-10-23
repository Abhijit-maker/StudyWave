package com.studywave.app.ui.screens.voice

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CallEnd
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studywave.app.data.repository.VoiceState
import com.studywave.app.ui.components.GirlTutorIllustration
import com.studywave.app.ui.components.WaveformBars
import com.studywave.app.ui.theme.StudyWaveColors
import com.studywave.app.viewmodel.VoiceViewModel

@Composable
fun VoiceScreen(
    onBack: () -> Unit,
    viewModel: VoiceViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyWaveColors.PrimaryBackground)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Back") }
            Text("Live Voice Mode", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(24.dp))
        GirlTutorIllustration(size = 180.dp)
        Spacer(Modifier.height(20.dp))
        Text(
            when (state.state) {
                VoiceState.LISTENING -> "Listening..."
                VoiceState.PROCESSING -> "Thinking..."
                VoiceState.SPEAKING -> "Speaking..."
                VoiceState.CONNECTING -> "Connecting..."
                VoiceState.ERROR -> "Not connected"
                VoiceState.DISCONNECTED -> "Tap the mic to start"
            },
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp
        )
        Text(
            state.error ?: "Tap the mic and start speaking",
            color = if (state.error != null) StudyWaveColors.Error else StudyWaveColors.SecondaryText,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        WaveformBars(
            active = state.state == VoiceState.LISTENING || state.state == VoiceState.SPEAKING,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 24.dp)
        )
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(28.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(StudyWaveColors.PrimaryPurple)
                    .clickable { viewModel.connect() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Mic, contentDescription = "Microphone", tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(StudyWaveColors.Error)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.CallEnd, contentDescription = "End", tint = Color.White)
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LangChip("Bangla", state.language == "bn") { viewModel.setLanguage("bn") }
            LangChip("English", state.language == "en") { viewModel.setLanguage("en") }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Needs RECORD_AUDIO permission and a backend-issued Live API session. This screen is finished; the adapter reports missing configuration instead of faking a conversation.",
            color = StudyWaveColors.SecondaryText,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun LangChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) StudyWaveColors.LavenderSurface else StudyWaveColors.ChipInactive)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(label, color = if (selected) StudyWaveColors.PrimaryPurple else StudyWaveColors.SecondaryText, fontWeight = FontWeight.Medium)
    }
}
