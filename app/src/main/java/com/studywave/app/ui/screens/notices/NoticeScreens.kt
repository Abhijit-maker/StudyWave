package com.studywave.app.ui.screens.notices

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studywave.app.data.models.Notice
import com.studywave.app.data.models.NoticeCategory
import com.studywave.app.ui.components.EmptyState
import com.studywave.app.ui.components.FilterChipRow
import com.studywave.app.ui.components.StatusPill
import com.studywave.app.ui.components.SubjectBadge
import com.studywave.app.ui.components.SwCard
import com.studywave.app.ui.theme.StudyWaveColors
import com.studywave.app.viewmodel.NoticesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NoticesScreen(
    onOpen: (String) -> Unit,
    onBack: (() -> Unit)? = null,
    viewModel: NoticesViewModel = viewModel()
) {
    val notices by viewModel.notices.collectAsStateWithLifecycle()
    var filter by remember { mutableStateOf("All") }
    val visible = notices.filter {
        filter == "All" || it.category.name.equals(filter, true) ||
            (filter == "Important" && it.category == NoticeCategory.IMPORTANT)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyWaveColors.PrimaryBackground)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Back") }
            }
            Text("Notice Board", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = StudyWaveColors.MainText)
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            FilterChipRow(listOf("All", "Important", "PDF", "Test"), filter) { filter = it }
        }
        Spacer(Modifier.height(12.dp))
        if (visible.isEmpty()) {
            EmptyState("No notices", "When administrators publish a notice in Firestore, it appears here even if push notifications are disabled.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(visible, key = { it.id }) { notice ->
                    NoticeRow(notice) { onOpen(notice.id) }
                }
            }
        }
    }
}

@Composable
fun NoticeDetailScreen(noticeId: String, onBack: () -> Unit, viewModel: NoticesViewModel = viewModel()) {
    val notices by viewModel.notices.collectAsStateWithLifecycle()
    val notice = notices.firstOrNull { it.id == noticeId }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyWaveColors.PrimaryBackground)
            .padding(20.dp)
    ) {
        IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Back") }
        if (notice == null) {
            EmptyState("Notice unavailable", "This notice may have expired or is no longer published.")
        } else {
            Text(notice.title, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Spacer(Modifier.height(8.dp))
            Text(formatTime(notice.createdAt), color = StudyWaveColors.SecondaryText, fontSize = 12.sp)
            Spacer(Modifier.height(16.dp))
            SwCard { Text(notice.body, fontSize = 15.sp, color = StudyWaveColors.MainText) }
        }
    }
}

@Composable
private fun NoticeRow(notice: Notice, onClick: () -> Unit) {
    SwCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SubjectBadge(notice.category.name)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(notice.title, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    if (notice.pinned) StatusPill("New", StudyWaveColors.SoftBlue)
                }
                Text(notice.body, maxLines = 1, fontSize = 12.sp, color = StudyWaveColors.SecondaryText)
                Text(formatTime(notice.createdAt), fontSize = 11.sp, color = StudyWaveColors.SecondaryText)
            }
        }
    }
}

private fun formatTime(epoch: Long): String {
    if (epoch <= 0) return ""
    return SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(epoch))
}
