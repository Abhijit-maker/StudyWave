package com.studywave.app.ui.screens.library

import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studywave.app.data.models.Catalog
import com.studywave.app.data.models.PdfDocument
import com.studywave.app.ui.components.EmptyState
import com.studywave.app.ui.components.ErrorState
import com.studywave.app.ui.components.FilterChipRow
import com.studywave.app.ui.components.GirlTutorIllustration
import com.studywave.app.ui.components.GradientHeader
import com.studywave.app.ui.components.LoadingScreen
import com.studywave.app.ui.components.SetupRequiredBanner
import com.studywave.app.ui.components.SoftIconButton
import com.studywave.app.ui.components.SubjectBadge
import com.studywave.app.ui.components.SwCard
import com.studywave.app.ui.components.SwSearchField
import com.studywave.app.ui.screens.home.formatSize
import com.studywave.app.ui.theme.StudyWaveColors
import com.studywave.app.viewmodel.LibraryViewModel
import com.studywave.app.viewmodel.PdfViewerViewModel
import java.io.File
import java.net.URL

@Composable
fun LibraryScreen(
    onOpen: (String) -> Unit,
    viewModel: LibraryViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyWaveColors.PrimaryBackground)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("PDF Library", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 22.sp, color = StudyWaveColors.MainText)
            SoftIconButton(Icons.Outlined.Tune, "Filters") {}
        }
        Spacer(Modifier.height(12.dp))
        if (state.setupRequired) {
            SetupRequiredBanner("Published PDFs load from Firestore after Firebase is configured.")
            Spacer(Modifier.height(8.dp))
        }
        SwSearchField(state.query, viewModel::onQuery, "Search PDFs...")
        Spacer(Modifier.height(12.dp))
        Box(Modifier.horizontalScroll(rememberScrollState())) {
            FilterChipRow(Catalog.subjects.map { it.label }, state.subject, viewModel::onSubject)
        }
        Spacer(Modifier.height(12.dp))
        GradientHeader {
            Column(Modifier.weight(1f)) {
                Text("Study Smart with StudyWave", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Your study materials, all in one place.", color = StudyWaveColors.SecondaryText, fontSize = 13.sp)
            }
            GirlTutorIllustration(size = 92.dp)
        }
        Spacer(Modifier.height(12.dp))
        if (state.documents.isEmpty()) {
            EmptyState("Library is empty", "Admins can upload and publish PDFs from Admin Mode. Students only see published files.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.documents, key = { it.id }) { doc ->
                    PdfRow(doc) { onOpen(doc.id) }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun PdfRow(doc: PdfDocument, onClick: () -> Unit) {
    SwCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SubjectBadge(doc.subject.ifBlank { "PDF" })
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(doc.title, fontWeight = FontWeight.SemiBold)
                Text(doc.description.ifBlank { doc.subject }, fontSize = 12.sp, color = StudyWaveColors.SecondaryText)
                Text("${formatSize(doc.fileSizeBytes)} • ${doc.pageCount} pages", fontSize = 11.sp, color = StudyWaveColors.SecondaryText)
            }
            Icon(Icons.Outlined.Download, contentDescription = "Download", tint = StudyWaveColors.PrimaryPurple)
        }
    }
}

@Composable
fun PdfViewerScreen(
    pdfId: String,
    onBack: () -> Unit,
    viewModel: PdfViewerViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(pdfId) { viewModel.load(pdfId) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyWaveColors.PrimaryBackground)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Back") }
            Column(Modifier.weight(1f)) {
                Text(state.document?.title ?: "PDF", fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(state.document?.subject.orEmpty(), fontSize = 12.sp, color = StudyWaveColors.SecondaryText)
            }
            IconButton(onClick = {}) { Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Bookmark") }
        }
        when {
            state.loading -> LoadingScreen()
            state.error != null -> ErrorState(state.error ?: "Unable to open this PDF.")
            state.remoteUri == null -> {
                EmptyState(
                    "PDF metadata loaded",
                    "The file could not be downloaded. Check Storage rules and that the document is published."
                )
            }
            else -> {
                NativePdfPages(
                    url = state.remoteUri.toString(),
                    page = state.page,
                    onPage = viewModel::setPage,
                    totalHint = state.document?.pageCount ?: 1
                )
            }
        }
    }
}

@Composable
private fun NativePdfPages(url: String, page: Int, onPage: (Int) -> Unit, totalHint: Int) {
    val context = LocalContext.current
    var pageCount by remember { mutableIntStateOf(totalHint.coerceAtLeast(1)) }
    DisposableEffect(url) {
        val file = File(context.cacheDir, "studywave-open.pdf")
        val job = Thread {
            runCatching {
                URL(url).openStream().use { input -> file.outputStream().use { input.copyTo(it) } }
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                    PdfRenderer(pfd).use { renderer ->
                        pageCount = renderer.pageCount.coerceAtLeast(1)
                    }
                }
            }
        }
        job.start()
        onDispose { }
    }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        SwCard(modifier = Modifier.weight(1f), color = StudyWaveColors.SecondaryBackground) {
            Text("Page $page of $pageCount", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Native PdfRenderer is used to open authorized files from Firebase Storage. Scanned PDFs may need OCR before AI question answering.",
                color = StudyWaveColors.SecondaryText,
                fontSize = 13.sp
            )
        }
        Spacer(Modifier.height(8.dp))
        Slider(
            value = page.toFloat(),
            onValueChange = { onPage(it.toInt()) },
            valueRange = 1f..pageCount.toFloat(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
