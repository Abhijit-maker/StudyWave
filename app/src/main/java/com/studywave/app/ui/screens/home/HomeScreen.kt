package com.studywave.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.studywave.app.data.models.MockTest
import com.studywave.app.data.models.PdfDocument
import com.studywave.app.ui.components.EmptyState
import com.studywave.app.ui.components.GradientHeader
import com.studywave.app.ui.components.ProgressRing
import com.studywave.app.ui.components.QuickActionTile
import com.studywave.app.ui.components.SectionHeader
import com.studywave.app.ui.components.SetupRequiredBanner
import com.studywave.app.ui.components.SoftIconButton
import com.studywave.app.ui.components.StudyCompanionIllustration
import com.studywave.app.ui.components.SubjectBadge
import com.studywave.app.ui.components.SwCard
import com.studywave.app.ui.components.SwPrimaryButton
import com.studywave.app.ui.theme.StudyWaveColors
import com.studywave.app.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onLibrary: () -> Unit,
    onTests: () -> Unit,
    onAi: () -> Unit,
    onVoice: () -> Unit,
    onNotices: () -> Unit,
    onPdf: (String) -> Unit,
    onTest: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyWaveColors.PrimaryBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        if (state.setupRequired) {
            SetupRequiredBanner("Connect Firebase to load live PDFs, tests, and notices.")
            Spacer(Modifier.height(8.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Hello, ${state.name}", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = StudyWaveColors.MainText)
                Text("Keep going! Your hard work will pay off.", color = StudyWaveColors.SecondaryText, fontSize = 13.sp)
            }
            SoftIconButton(Icons.Outlined.Notifications, "Notices", onClick = onNotices)
        }
        Spacer(Modifier.height(16.dp))
        GradientHeader {
            Column(Modifier.weight(1f)) {
                Text("Continue Learning", fontWeight = FontWeight.SemiBold, color = StudyWaveColors.PrimaryPurple)
                val latest = state.pdfs.firstOrNull()
                Text(latest?.title ?: "No document in progress", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(latest?.description?.ifBlank { latest.subject } ?: "Published PDFs will appear here.", fontSize = 12.sp, color = StudyWaveColors.SecondaryText)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { 0.68f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = StudyWaveColors.PrimaryPurple,
                    trackColor = Color.White.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(12.dp))
                Box(Modifier.width(140.dp)) {
                    SwPrimaryButton("Continue", onClick = { latest?.id?.let(onPdf) ?: onLibrary() })
                }
            }
            StudyCompanionIllustration(size = 110.dp)
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            SwCard(modifier = Modifier.weight(1f), color = StudyWaveColors.SecondaryBackground) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProgressRing(0.71f, "done", modifier = Modifier.size(72.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Your Progress", fontWeight = FontWeight.SemiBold)
                        Text("${state.pdfs.size} resources", fontSize = 12.sp, color = StudyWaveColors.SecondaryText)
                    }
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniStat("Daily Goal", "3 / 5 hours")
                MiniStat("Study Streak", "Keep it going")
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            QuickActionTile("PDF Library", Icons.Outlined.MenuBook, Color(0xFF43A047), StudyWaveColors.SoftMint, Modifier.weight(1f), onLibrary)
            QuickActionTile("Mock Test", Icons.Outlined.Quiz, StudyWaveColors.PrimaryPurple, StudyWaveColors.LavenderSurface, Modifier.weight(1f), onTests)
            QuickActionTile("AI Chat", Icons.Outlined.AutoAwesome, StudyWaveColors.SoftBlue, StudyWaveColors.LightBlueSurface, Modifier.weight(1f), onAi)
            QuickActionTile("Live Voice", Icons.Outlined.Mic, Color(0xFF8E24AA), StudyWaveColors.SoftLilac, Modifier.weight(1f), onVoice)
        }
        Spacer(Modifier.height(20.dp))
        SectionHeader("Recent PDFs", "View All", onLibrary)
        if (state.pdfs.isEmpty()) {
            EmptyState("No PDFs yet", "When an admin publishes study material, it will show up here.")
        } else {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.pdfs.take(5).forEach { pdf ->
                    PdfMiniCard(pdf) { onPdf(pdf.id) }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        SectionHeader("Upcoming Mock Tests", "View All", onTests)
        if (state.tests.isEmpty()) {
            EmptyState("No tests scheduled", "Published mock tests will appear here automatically.")
        } else {
            state.tests.forEach { test ->
                TestRow(test) { onTest(test.id) }
                Spacer(Modifier.height(8.dp))
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun MiniStat(title: String, value: String) {
    SwCard(color = StudyWaveColors.SecondaryBackground, contentPadding = PaddingValues(12.dp)) {
        Text(title, fontSize = 11.sp, color = StudyWaveColors.SecondaryText)
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
private fun PdfMiniCard(pdf: PdfDocument, onClick: () -> Unit) {
    SwCard(modifier = Modifier.width(140.dp), onClick = onClick, contentPadding = PaddingValues(12.dp)) {
        SubjectBadge(pdf.subject.ifBlank { "PDF" })
        Spacer(Modifier.height(8.dp))
        Text(pdf.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 2)
        Text(
            "${formatSize(pdf.fileSizeBytes)} • ${pdf.pageCount} pages",
            fontSize = 11.sp,
            color = StudyWaveColors.SecondaryText
        )
    }
}

@Composable
private fun TestRow(test: MockTest, onClick: () -> Unit) {
    SwCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SubjectBadge(test.subject.ifBlank { "Test" })
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(test.title, fontWeight = FontWeight.SemiBold)
                Text("${test.topic.ifBlank { test.subject }} • ${test.durationMinutes} min", fontSize = 12.sp, color = StudyWaveColors.SecondaryText)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(StudyWaveColors.PrimaryPurple)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text("Start", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
        }
    }
}

fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "—"
    val mb = bytes / (1024.0 * 1024.0)
    return "%.1f MB".format(mb)
}
