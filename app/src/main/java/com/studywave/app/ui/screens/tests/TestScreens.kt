package com.studywave.app.ui.screens.tests

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.studywave.app.data.models.MockTest
import com.studywave.app.ui.components.EmptyState
import com.studywave.app.ui.components.ErrorState
import com.studywave.app.ui.components.FilterChipRow
import com.studywave.app.ui.components.LoadingScreen
import com.studywave.app.ui.components.SetupRequiredBanner
import com.studywave.app.ui.components.StatusPill
import com.studywave.app.ui.components.SubjectBadge
import com.studywave.app.ui.components.SwCard
import com.studywave.app.ui.components.SwPrimaryButton
import com.studywave.app.ui.theme.StudyWaveColors
import com.studywave.app.viewmodel.TestPlayerViewModel
import com.studywave.app.viewmodel.TestsViewModel

@Composable
fun TestsScreen(
    onStart: (String) -> Unit,
    viewModel: TestsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyWaveColors.PrimaryBackground)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Text("Mock Test", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = StudyWaveColors.MainText)
        Spacer(Modifier.height(12.dp))
        if (state.setupRequired) {
            SetupRequiredBanner("Tests load from Firestore. Scoring uses Cloud Functions so answer keys stay off the client.")
            Spacer(Modifier.height(8.dp))
        }
        FilterChipRow(
            items = listOf("Available", "My Tests"),
            selected = if (state.tab == 0) "Available" else "My Tests",
            onSelected = { viewModel.selectTab(if (it == "Available") 0 else 1) }
        )
        Spacer(Modifier.height(12.dp))
        if (state.tab == 0) {
            if (state.available.isEmpty()) {
                EmptyState("No published tests", "Administrators publish tests from Admin Mode. Students never see draft answer keys.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(state.available) { _, test ->
                        TestCard(test) { onStart(test.id) }
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        } else {
            EmptyState("No attempts yet", "Start a published test to see history and results here.")
        }
    }
}

@Composable
private fun TestCard(test: MockTest, onStart: () -> Unit) {
    SwCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SubjectBadge(test.subject.ifBlank { "Test" })
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(test.title, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    test.badge?.let { StatusPill(it, StudyWaveColors.Error) }
                }
                Text("${test.questionCount} Questions • ${test.durationMinutes} Minutes", fontSize = 12.sp, color = StudyWaveColors.SecondaryText)
            }
        }
        Spacer(Modifier.height(12.dp))
        SwPrimaryButton("Start Test", onStart)
    }
}

@Composable
fun TestPlayerScreen(
    testId: String,
    onBack: () -> Unit,
    onSubmitted: (String) -> Unit,
    viewModel: TestPlayerViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(testId) { viewModel.load(testId) }
    when {
        state.loading -> LoadingScreen()
        state.error != null && state.test == null -> ErrorState(state.error ?: "Unable to start test.", onBack)
        else -> {
            val question = state.questions.getOrNull(state.index)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(StudyWaveColors.PrimaryBackground)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Back") }
                    Column(Modifier.weight(1f)) {
                        Text(state.test?.title ?: "Mock Test", fontWeight = FontWeight.SemiBold)
                        Text("${state.remainingSeconds / 60} min remaining", fontSize = 12.sp, color = StudyWaveColors.SecondaryText)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    state.questions.forEachIndexed { index, item ->
                        val answered = state.attempt?.answers?.containsKey(item.id) == true
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        index == state.index -> StudyWaveColors.PrimaryPurple
                                        answered -> StudyWaveColors.Success
                                        else -> StudyWaveColors.ChipInactive
                                    }
                                )
                                .clickable { viewModel.goTo(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${index + 1}",
                                color = if (index == state.index || answered) Color.White else StudyWaveColors.SecondaryText,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                if (question == null) {
                    EmptyState("Questions unavailable", "Question text is loaded from Firestore. Answer keys stay in a protected collection.")
                } else {
                    SwCard(modifier = Modifier.weight(1f)) {
                        Text("Question ${state.index + 1}", color = StudyWaveColors.PrimaryPurple, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text(question.text, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                        Spacer(Modifier.height(16.dp))
                        question.options.forEach { option ->
                            val selected = state.attempt?.answers?.get(question.id) == option
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(
                                        1.dp,
                                        if (selected) StudyWaveColors.PrimaryPurple else StudyWaveColors.Divider,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .background(if (selected) StudyWaveColors.LavenderSurface else Color.White)
                                    .clickable { viewModel.selectAnswer(question.id, option) }
                                    .padding(14.dp)
                            ) {
                                Text(option, color = StudyWaveColors.MainText)
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(StudyWaveColors.SoftPeach)
                                .clickable { viewModel.toggleReview(question.id) }
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Flag, contentDescription = null, tint = StudyWaveColors.Warning)
                                Spacer(Modifier.width(6.dp))
                                Text("Mark for review", fontSize = 13.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.weight(1f)) {
                            SwPrimaryButton("Previous", { viewModel.goTo(state.index - 1) }, enabled = state.index > 0)
                        }
                        Box(Modifier.weight(1f)) {
                            if (state.index == state.questions.lastIndex) {
                                SwPrimaryButton("Submit", { viewModel.submit(onSubmitted) }, loading = state.submitting)
                            } else {
                                SwPrimaryButton("Next", { viewModel.goTo(state.index + 1) })
                            }
                        }
                    }
                    if (state.error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(state.error ?: "", color = StudyWaveColors.Error, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun TestResultScreen(attemptId: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyWaveColors.PrimaryBackground)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Back") }
        Text("Result", fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Spacer(Modifier.height(12.dp))
        SwCard {
            Text("Attempt $attemptId", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Final scores are calculated by Cloud Functions so clients never receive answer keys before submission. If scoring is pending configuration, this screen stays empty instead of inventing a score.",
                color = StudyWaveColors.SecondaryText,
                fontSize = 14.sp
            )
        }
        Spacer(Modifier.height(16.dp))
        SwPrimaryButton("Back to tests", onBack)
    }
}
