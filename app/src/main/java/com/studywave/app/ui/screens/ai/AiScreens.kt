package com.studywave.app.ui.screens.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.studywave.app.data.models.ChatMessage
import com.studywave.app.data.models.MessageRole
import com.studywave.app.ui.components.GirlTutorIllustration
import com.studywave.app.ui.components.OnlineDot
import com.studywave.app.ui.components.SetupRequiredBanner
import com.studywave.app.ui.components.SoftIconButton
import com.studywave.app.ui.theme.StudyWaveColors
import com.studywave.app.viewmodel.AiViewModel

@Composable
fun AiScreen(
    onVoice: () -> Unit,
    viewModel: AiViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyWaveColors.PrimaryBackground)
            .imePadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("AI Study Room", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 22.sp, color = StudyWaveColors.MainText)
            SoftIconButton(Icons.Outlined.Tune, "New chat") { viewModel.newChat() }
        }
        if (state.setupRequired) {
            Spacer(Modifier.height(8.dp))
            SetupRequiredBanner("AI replies go through a Cloud Function. The app never stores a Gemini API key.")
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(StudyWaveColors.LavenderSurface)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Hi, I'm StudyWave AI", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Your personal study tutor. Always here for you!", color = StudyWaveColors.SecondaryText, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OnlineDot(online = !state.setupRequired)
                    Spacer(Modifier.width(6.dp))
                    Text(if (state.setupRequired) "Pending configuration" else "Online", fontSize = 12.sp, color = StudyWaveColors.SecondaryText)
                }
            }
            GirlTutorIllustration(size = 88.dp)
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (state.messages.isEmpty()) {
                item {
                    AssistantBubble("Ask anything about your study. Upload a PDF from the library, then send a question grounded in that document.")
                }
            }
            items(state.messages, key = { it.id }) { message ->
                if (message.role == MessageRole.USER) UserBubble(message) else AssistantBubble(message.text)
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickChip("Explain this topic") { viewModel.send("Explain this topic in simple words.") }
            QuickChip("Solve a question") { viewModel.send("Help me solve this question step by step.") }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickChip("Make a short note") { viewModel.send("Make short notes for this chapter.") }
            QuickChip("Give practice questions") { viewModel.send("Generate practice questions with answers.") }
        }
        if (state.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(state.error ?: "", color = StudyWaveColors.Error, fontSize = 12.sp)
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.draft,
                onValueChange = viewModel::onDraft,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask anything...") },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StudyWaveColors.PrimaryPurple,
                    unfocusedBorderColor = StudyWaveColors.Divider
                )
            )
            Spacer(Modifier.width(8.dp))
            CircleAction(Icons.Outlined.GraphicEq, onVoice)
            Spacer(Modifier.width(4.dp))
            CircleAction(Icons.Outlined.Mic, onVoice)
            Spacer(Modifier.width(4.dp))
            CircleAction(Icons.Outlined.Send, { viewModel.send() }, filled = true)
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun QuickChip(text: String, onClick: () -> Unit) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = StudyWaveColors.SecondaryBackground
    ) {
        Text(text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 12.sp)
    }
}

@Composable
private fun CircleAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    filled: Boolean = false
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (filled) StudyWaveColors.PrimaryPurple else StudyWaveColors.SecondaryBackground)
    ) {
        Icon(icon, contentDescription = null, tint = if (filled) Color.White else StudyWaveColors.PrimaryPurple)
    }
}

@Composable
private fun UserBubble(message: ChatMessage) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(StudyWaveColors.PrimaryPurple)
                .padding(12.dp)
        ) {
            Text(message.text, color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
private fun AssistantBubble(text: String) {
    Row(Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(StudyWaveColors.SecondaryBackground)
                .padding(12.dp)
        ) {
            Text(text, color = StudyWaveColors.MainText, fontSize = 14.sp)
        }
    }
}
