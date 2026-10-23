package com.studywave.app.ui.screens.admin

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studywave.app.ui.components.EmptyState
import com.studywave.app.ui.components.ErrorState
import com.studywave.app.ui.components.SwCard
import com.studywave.app.ui.components.SwPrimaryButton
import com.studywave.app.ui.components.SwTextField
import com.studywave.app.ui.theme.StudyWaveColors
import com.studywave.app.viewmodel.AdminViewModel

@Composable
fun AdminDashboard(
    onPdfs: () -> Unit,
    onTests: () -> Unit,
    onNotices: () -> Unit,
    onUsers: () -> Unit,
    onAi: () -> Unit,
    onLogs: () -> Unit,
    onSettings: () -> Unit,
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    if (!isAdmin) {
        Column(Modifier.fillMaxSize().background(StudyWaveColors.PrimaryBackground).padding(20.dp)) {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Back") }
            ErrorState("Admin Mode is hidden unless your ID token contains a trusted admin custom claim.")
        }
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyWaveColors.SecondaryBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Back") }
            Text("StudyWave Admin", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Users", state.overview.totalUsers.toString(), Modifier.weight(1f))
            StatCard("PDFs", state.overview.totalPdfs.toString(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Tests", state.overview.totalTests.toString(), Modifier.weight(1f))
            StatCard("Storage", "${state.overview.storageBytes / (1024 * 1024)} MB", Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        AdminLink(Icons.Outlined.Dashboard, "Overview", onPdfs)
        AdminLink(Icons.Outlined.Description, "PDF Manager", onPdfs)
        AdminLink(Icons.Outlined.Quiz, "Test Manager", onTests)
        AdminLink(Icons.Outlined.Campaign, "Notice Manager", onNotices)
        AdminLink(Icons.Outlined.AutoAwesome, "AI Configuration", onAi)
        AdminLink(Icons.Outlined.People, "User Management", onUsers)
        AdminLink(Icons.Outlined.Report, "Logs & Reports", onLogs)
        AdminLink(Icons.Outlined.Settings, "Settings", onSettings)
    }
}

@Composable
fun AdminPdfScreen(onBack: () -> Unit, viewModel: AdminViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Physics") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data
        if (uri != null && title.isNotBlank()) {
            viewModel.uploadPdf(uri, title, subject, "Class 12")
        }
    }
    Column(Modifier.fillMaxSize().background(StudyWaveColors.PrimaryBackground).padding(20.dp)) {
        AdminHeader("PDF Manager", onBack)
        SwTextField(title, { title = it }, "Title")
        Spacer(Modifier.height(8.dp))
        SwTextField(subject, { subject = it }, "Subject")
        Spacer(Modifier.height(8.dp))
        SwPrimaryButton("Pick PDF and upload") {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
            }
            picker.launch(intent)
        }
        Spacer(Modifier.height(12.dp))
        if (state.pdfs.isEmpty()) {
            EmptyState("No PDFs", "Uploaded files stay unpublished until you toggle publish. Failed uploads never appear as live documents.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.pdfs, key = { it.id }) { pdf ->
                    SwCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(pdf.title, fontWeight = FontWeight.SemiBold)
                                Text(if (pdf.published) "Published" else "Draft", color = StudyWaveColors.SecondaryText, fontSize = 12.sp)
                            }
                            Switch(checked = pdf.published, onCheckedChange = { viewModel.publishPdf(pdf.id, it) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminTestsScreen(onBack: () -> Unit, viewModel: AdminViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().background(StudyWaveColors.PrimaryBackground).padding(20.dp)) {
        AdminHeader("Test Manager", onBack)
        if (state.tests.isEmpty()) {
            EmptyState("No tests", "Create tests in Firestore or extend this screen. Answer keys belong in mockTests/{id}/answerKey, not the student-readable document.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.tests, key = { it.id }) { test ->
                    SwCard {
                        Text(test.title, fontWeight = FontWeight.SemiBold)
                        Text("${test.questionCount} questions • ${if (test.published) "Published" else "Draft"}", fontSize = 12.sp, color = StudyWaveColors.SecondaryText)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminNoticesScreen(onBack: () -> Unit, viewModel: AdminViewModel = viewModel()) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().background(StudyWaveColors.PrimaryBackground).padding(20.dp).verticalScroll(rememberScrollState())) {
        AdminHeader("Notice Manager", onBack)
        SwTextField(title, { title = it }, "Title")
        Spacer(Modifier.height(8.dp))
        SwTextField(body, { body = it }, "Body")
        Spacer(Modifier.height(8.dp))
        SwPrimaryButton("Publish notice") {
            if (title.isNotBlank()) viewModel.saveNotice(title, body)
        }
        Spacer(Modifier.height(12.dp))
        state.notices.forEach { notice ->
            SwCard(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(notice.title, fontWeight = FontWeight.SemiBold)
                Text(notice.body, fontSize = 13.sp, color = StudyWaveColors.SecondaryText)
            }
        }
    }
}

@Composable
fun AdminUsersScreen(onBack: () -> Unit, viewModel: AdminViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().background(StudyWaveColors.PrimaryBackground).padding(20.dp)) {
        AdminHeader("User Management", onBack)
        EmptyState(
            "Custom claims only",
            "Promote admins with the setAdminRole Cloud Function. ${state.users.size} profile documents are visible, but none of them can change their own role."
        )
    }
}

@Composable
fun AdminAiScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(StudyWaveColors.PrimaryBackground).padding(20.dp)) {
        AdminHeader("AI Configuration", onBack)
        SwCard {
            Text("Model and limits live in appConfig/public.", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(
                "GEMINI_API_KEY must be stored in Google Cloud Secret Manager or Cloud Functions environment config. Never paste it into the Android app, Firestore, or GitHub.",
                color = StudyWaveColors.SecondaryText,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun AdminLogsScreen(onBack: () -> Unit, viewModel: AdminViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().background(StudyWaveColors.PrimaryBackground).padding(20.dp)) {
        AdminHeader("Logs & Reports", onBack)
        if (state.logs.isEmpty()) {
            EmptyState("No audit logs", "Trusted Cloud Functions write auditLogs after privileged actions.")
        } else {
            LazyColumn {
                items(state.logs, key = { it.id }) { log ->
                    SwCard(modifier = Modifier.padding(bottom = 8.dp)) {
                        Text(log.action, fontWeight = FontWeight.SemiBold)
                        Text("${log.actorUid} • ${log.target}", fontSize = 12.sp, color = StudyWaveColors.SecondaryText)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSettingsScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(StudyWaveColors.PrimaryBackground).padding(20.dp)) {
        AdminHeader("Admin Settings", onBack)
        SwCard {
            Text("Default max PDF size is 50 MB.", fontWeight = FontWeight.SemiBold)
            Text("Change appConfig/public.maxPdfBytes after checking your Storage quota. Unlimited uploads are not supported.", color = StudyWaveColors.SecondaryText, fontSize = 13.sp)
        }
    }
}

@Composable
private fun AdminHeader(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Back") }
        Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    SwCard(modifier = modifier, color = StudyWaveColors.CardWhite) {
        Text(label, color = StudyWaveColors.SecondaryText, fontSize = 12.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 22.sp)
    }
}

@Composable
private fun AdminLink(icon: ImageVector, title: String, onClick: () -> Unit) {
    SwCard(modifier = Modifier.padding(bottom = 8.dp), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = StudyWaveColors.PrimaryPurple)
            Spacer(Modifier.width(12.dp))
            Text(title, fontWeight = FontWeight.Medium)
        }
    }
}
