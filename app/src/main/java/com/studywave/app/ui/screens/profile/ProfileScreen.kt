package com.studywave.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studywave.app.core.security.UserRole
import com.studywave.app.ui.components.GirlTutorIllustration
import com.studywave.app.ui.components.SwCard
import com.studywave.app.ui.theme.StudyWaveColors
import com.studywave.app.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    onAdmin: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val profile = session.profile
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyWaveColors.PrimaryBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Profile & Settings", fontWeight = FontWeight.Bold, fontSize = 22.sp, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        GirlTutorIllustration(size = 120.dp)
        Spacer(Modifier.height(12.dp))
        Text(profile?.displayName ?: "Student", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(profile?.email ?: "Sign in to sync your profile", color = StudyWaveColors.SecondaryText, fontSize = 13.sp)
        Spacer(Modifier.height(20.dp))
        ProfileRow(Icons.Outlined.ShowChart, "My Progress") {}
        ProfileRow(Icons.Outlined.BookmarkBorder, "Bookmarks") {}
        ProfileRow(Icons.Outlined.Download, "Downloads") {}
        ProfileRow(Icons.Outlined.Settings, "Settings") {}
        ProfileRow(Icons.Outlined.HelpOutline, "Help & Support") {}
        if (session.role == UserRole.ADMIN) {
            ProfileRow(Icons.Outlined.AdminPanelSettings, "Admin Mode", onAdmin)
        }
        ProfileRow(Icons.Outlined.Logout, "Logout", { viewModel.signOut() }, StudyWaveColors.Error)
        Spacer(Modifier.height(16.dp))
        Text(
            "Roles are assigned with Firebase custom claims. Editing a profile document cannot make a student an admin.",
            color = StudyWaveColors.SecondaryText,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ProfileRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = StudyWaveColors.MainText
) {
    SwCard(modifier = Modifier.padding(bottom = 8.dp), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, contentDescription = null, tint = tint)
            Spacer(Modifier.width(12.dp))
            Text(title, fontWeight = FontWeight.Medium, color = tint)
        }
    }
}
