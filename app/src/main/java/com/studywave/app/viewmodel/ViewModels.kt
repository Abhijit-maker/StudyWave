package com.studywave.app.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.studywave.app.StudyWaveApp
import com.studywave.app.core.errors.AppError
import com.studywave.app.core.errors.toAppError
import com.studywave.app.core.security.UserRole
import com.studywave.app.data.models.AdminOverview
import com.studywave.app.data.models.AppConfig
import com.studywave.app.data.models.AuditLog
import com.studywave.app.data.models.Bookmark
import com.studywave.app.data.models.ChatMessage
import com.studywave.app.data.models.Conversation
import com.studywave.app.data.models.MockTest
import com.studywave.app.data.models.Notice
import com.studywave.app.data.models.PdfDocument
import com.studywave.app.data.models.Question
import com.studywave.app.data.models.ReadingProgress
import com.studywave.app.data.models.SessionState
import com.studywave.app.data.models.TestAttempt
import com.studywave.app.data.models.UserProfile
import com.studywave.app.data.remote.FirebaseAvailability
import com.studywave.app.data.repository.VoiceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SessionViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as StudyWaveApp
    val session: StateFlow<SessionState> = app.container.authRepository.session
    val config: StateFlow<AppConfig> = app.container.configRepository.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppConfig(setupRequired = !FirebaseAvailability.isConfigured))
}

data class AuthUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val info: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = (application as StudyWaveApp).container.authRepository
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun updateName(value: String) = _state.update { it.copy(name = value, error = null) }
    fun updateEmail(value: String) = _state.update { it.copy(email = value, error = null) }
    fun updatePassword(value: String) = _state.update { it.copy(password = value, error = null) }

    fun signIn() = submit {
        require(_state.value.email.isNotBlank() && _state.value.password.length >= 6) {
            "Enter a valid email and a password of at least 6 characters."
        }
        auth.signIn(_state.value.email, _state.value.password)
    }

    fun register() = submit {
        require(_state.value.name.isNotBlank()) { "Enter your name." }
        require(_state.value.email.isNotBlank() && _state.value.password.length >= 6) {
            "Enter a valid email and a password of at least 6 characters."
        }
        auth.register(_state.value.name, _state.value.email, _state.value.password)
    }

    fun reset() = submit {
        require(_state.value.email.isNotBlank()) { "Enter the email for your account." }
        auth.sendPasswordReset(_state.value.email)
        _state.update { it.copy(info = "Password reset email sent.") }
    }

    fun signOut() = viewModelScope.launch {
        runCatching { auth.signOut() }
    }

    private fun submit(block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, info = null) }
            runCatching { block() }
                .onFailure { error ->
                    val mapped = when (error) {
                        is AppError -> error
                        is IllegalArgumentException -> AppError.Validation(error.message ?: "Check the form and try again.")
                        else -> error.toAppError()
                    }
                    _state.update { it.copy(loading = false, error = mapped.message) }
                }
                .onSuccess { _state.update { it.copy(loading = false) } }
        }
    }
}

data class HomeUiState(
    val name: String = "there",
    val pdfs: List<PdfDocument> = emptyList(),
    val tests: List<MockTest> = emptyList(),
    val notices: List<Notice> = emptyList(),
    val progress: List<ReadingProgress> = emptyList(),
    val setupRequired: Boolean = !FirebaseAvailability.isConfigured,
    val loading: Boolean = true,
    val error: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as StudyWaveApp
    val state: StateFlow<HomeUiState> = combine(
        app.container.authRepository.session,
        app.container.pdfRepository.observePublished(),
        app.container.testRepository.observePublished(),
        app.container.noticeRepository.observePublished()
    ) { session, pdfs, tests, notices ->
        HomeUiState(
            name = session.profile?.displayName?.ifBlank { "there" } ?: "there",
            pdfs = pdfs.take(6),
            tests = tests.take(4),
            notices = notices.take(4),
            setupRequired = session.setupRequired,
            loading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}

data class LibraryUiState(
    val query: String = "",
    val subject: String = "All",
    val documents: List<PdfDocument> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
    val setupRequired: Boolean = !FirebaseAvailability.isConfigured
)

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as StudyWaveApp
    private val query = MutableStateFlow("")
    private val subject = MutableStateFlow("All")

    val state: StateFlow<LibraryUiState> = combine(
        app.container.pdfRepository.observePublished(),
        query,
        subject
    ) { docs, q, sub ->
        val filtered = docs.filter { doc ->
            val matchesQuery = q.isBlank() || doc.title.contains(q, true) || doc.subject.contains(q, true)
            val matchesSubject = sub.equals("All", true) || doc.subject.equals(sub, true)
            matchesQuery && matchesSubject
        }
        LibraryUiState(q, sub, filtered, loading = false, setupRequired = !FirebaseAvailability.isConfigured)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    fun onQuery(value: String) { query.value = value }
    fun onSubject(value: String) { subject.value = value }
}

data class PdfViewerUiState(
    val document: PdfDocument? = null,
    val page: Int = 1,
    val loading: Boolean = true,
    val error: String? = null,
    val bookmarked: Boolean = false,
    val remoteUri: Uri? = null
)

class PdfViewerViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as StudyWaveApp
    private val _state = MutableStateFlow(PdfViewerUiState())
    val state: StateFlow<PdfViewerUiState> = _state.asStateFlow()

    fun load(pdfId: String) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching {
                val doc = app.container.pdfRepository.get(pdfId)
                    ?: throw AppError.NotFound("This PDF is not available.")
                val uri = runCatching { app.container.pdfRepository.downloadUrl(doc.storagePath) }.getOrNull()
                _state.update {
                    it.copy(document = doc, loading = false, remoteUri = uri, page = 1)
                }
            }.onFailure { error ->
                val mapped = if (error is AppError) error else error.toAppError()
                _state.update { it.copy(loading = false, error = mapped.message) }
            }
        }
    }

    fun setPage(page: Int) {
        val total = _state.value.document?.pageCount?.coerceAtLeast(1) ?: 1
        _state.update { it.copy(page = page.coerceIn(1, total)) }
    }
}

data class TestsUiState(
    val available: List<MockTest> = emptyList(),
    val attempts: List<TestAttempt> = emptyList(),
    val tab: Int = 0,
    val loading: Boolean = true,
    val setupRequired: Boolean = !FirebaseAvailability.isConfigured
)

class TestsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as StudyWaveApp
    private val tab = MutableStateFlow(0)
    val state: StateFlow<TestsUiState> = combine(
        app.container.testRepository.observePublished(),
        app.container.authRepository.session,
        tab
    ) { tests, session, selected ->
        TestsUiState(available = tests, tab = selected, loading = false, setupRequired = session.setupRequired)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TestsUiState())

    fun selectTab(index: Int) { tab.value = index }
}

data class TestPlayerUiState(
    val test: MockTest? = null,
    val questions: List<Question> = emptyList(),
    val attempt: TestAttempt? = null,
    val index: Int = 0,
    val remainingSeconds: Long = 0,
    val loading: Boolean = true,
    val submitting: Boolean = false,
    val error: String? = null
)

class TestPlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as StudyWaveApp
    private val _state = MutableStateFlow(TestPlayerUiState())
    val state: StateFlow<TestPlayerUiState> = _state.asStateFlow()

    fun load(testId: String) {
        viewModelScope.launch {
            val uid = app.container.authRepository.session.value.uid
                ?: run {
                    _state.update { it.copy(loading = false, error = "Sign in to start a test.") }
                    return@launch
                }
            runCatching {
                val test = app.container.testRepository.get(testId)
                    ?: throw AppError.NotFound("This test is not available.")
                val attempt = app.container.testRepository.startAttempt(testId, uid)
                _state.update {
                    it.copy(test = test, attempt = attempt, remainingSeconds = test.durationMinutes * 60L, loading = false)
                }
            }.onFailure { error ->
                val mapped = if (error is AppError) error else error.toAppError()
                _state.update { it.copy(loading = false, error = mapped.message) }
            }
        }
        viewModelScope.launch {
            app.container.testRepository.observeQuestions(testId).collect { questions ->
                _state.update { it.copy(questions = questions) }
            }
        }
    }

    fun selectAnswer(questionId: String, answer: String) {
        val attempt = _state.value.attempt ?: return
        val updated = attempt.copy(answers = attempt.answers + (questionId to answer))
        _state.update { it.copy(attempt = updated) }
        viewModelScope.launch { runCatching { app.container.testRepository.autosave(updated) } }
    }

    fun toggleReview(questionId: String) {
        val attempt = _state.value.attempt ?: return
        val marked = if (questionId in attempt.markedForReview) {
            attempt.markedForReview - questionId
        } else {
            attempt.markedForReview + questionId
        }
        _state.update { it.copy(attempt = attempt.copy(markedForReview = marked)) }
    }

    fun goTo(index: Int) {
        val max = (_state.value.questions.size - 1).coerceAtLeast(0)
        _state.update { it.copy(index = index.coerceIn(0, max)) }
    }

    fun submit(onDone: (String) -> Unit) {
        val attempt = _state.value.attempt ?: return
        viewModelScope.launch {
            _state.update { it.copy(submitting = true, error = null) }
            runCatching { app.container.testRepository.submit(attempt.id) }
                .onSuccess { onDone(it.id.ifBlank { attempt.id }) }
                .onFailure { error ->
                    val mapped = if (error is AppError) error else error.toAppError()
                    _state.update { it.copy(submitting = false, error = mapped.message) }
                }
        }
    }
}

data class AiUiState(
    val conversations: List<Conversation> = emptyList(),
    val messages: List<ChatMessage> = emptyList(),
    val currentId: String? = null,
    val draft: String = "",
    val sending: Boolean = false,
    val error: String? = null,
    val setupRequired: Boolean = !FirebaseAvailability.isConfigured
)

class AiViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as StudyWaveApp
    private val currentId = MutableStateFlow<String?>(null)
    private val draft = MutableStateFlow("")
    private val sending = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)

    val state: StateFlow<AiUiState> = combine(
        combine(
            app.container.authRepository.session,
            app.container.chatRepository.observeConversations(
                app.container.authRepository.session.value.uid.orEmpty()
            ),
            currentId
        ) { session, conversations, id -> Triple(session, conversations, id) },
        combine(draft, sending, error) { d, s, e -> Triple(d, s, e) }
    ) { left, right ->
        AiUiState(
            conversations = left.second,
            currentId = left.third,
            draft = right.first,
            sending = right.second,
            error = right.third,
            setupRequired = left.first.setupRequired
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AiUiState())

    init {
        viewModelScope.launch {
            currentId.collect { id ->
                if (id == null) return@collect
                app.container.chatRepository.observeMessages(id).collect { messages ->
                    // messages are observed inside screens via repository when currentId is set
                }
            }
        }
    }

    fun onDraft(value: String) { draft.value = value }

    fun send(text: String = draft.value, pdfId: String? = null) {
        val uid = app.container.authRepository.session.value.uid ?: run {
            error.value = "Sign in to use AI Study Room."
            return
        }
        if (text.isBlank()) return
        viewModelScope.launch {
            sending.value = true
            error.value = null
            runCatching {
                val conversationId = currentId.value ?: app.container.chatRepository
                    .createConversation(uid, text.take(32), pdfId).id.also { currentId.value = it }
                app.container.chatRepository.sendMessage(conversationId, text, null, pdfId)
                draft.value = ""
            }.onFailure {
                val mapped = if (it is AppError) it else it.toAppError()
                error.value = mapped.message
            }
            sending.value = false
        }
    }

    fun newChat() { currentId.value = null }
}

data class VoiceUiState(
    val state: VoiceState = VoiceState.DISCONNECTED,
    val language: String = "en",
    val error: String? = "Live Voice needs a backend-issued temporary credential. Deploy Cloud Functions first.",
    val ready: Boolean = false
)

class VoiceViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as StudyWaveApp
    private val _state = MutableStateFlow(
        VoiceUiState(ready = app.container.voiceRepository.configurationReady)
    )
    val state: StateFlow<VoiceUiState> = _state.asStateFlow()

    fun setLanguage(code: String) = _state.update { it.copy(language = code) }

    fun connect() {
        viewModelScope.launch {
            _state.update { it.copy(state = VoiceState.CONNECTING, error = null) }
            runCatching { app.container.voiceRepository.createSession().connect(_state.value.language) }
                .onFailure {
                    val mapped = if (it is AppError) it else it.toAppError()
                    _state.update { current -> current.copy(state = VoiceState.ERROR, error = mapped.message) }
                }
        }
    }
}

class NoticesViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as StudyWaveApp
    val notices: StateFlow<List<Notice>> = app.container.noticeRepository.observePublished()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as StudyWaveApp
    val session = app.container.authRepository.session
    val bookmarks: StateFlow<List<Bookmark>> = kotlinx.coroutines.flow.flowOf(emptyList<Bookmark>())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun signOut() = viewModelScope.launch { runCatching { app.container.authRepository.signOut() } }
}

data class AdminUiState(
    val overview: AdminOverview = AdminOverview(),
    val pdfs: List<PdfDocument> = emptyList(),
    val tests: List<MockTest> = emptyList(),
    val notices: List<Notice> = emptyList(),
    val users: List<UserProfile> = emptyList(),
    val logs: List<AuditLog> = emptyList(),
    val error: String? = null,
    val uploading: Boolean = false,
    val uploadProgress: Double = 0.0
)

class AdminViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as StudyWaveApp
    val isAdmin: StateFlow<Boolean> = app.container.authRepository.session
        .combine(MutableStateFlow(Unit)) { session, _ -> session.role == UserRole.ADMIN }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val state: StateFlow<AdminUiState> = combine(
        app.container.adminRepository.observeOverview(),
        app.container.pdfRepository.observeAllForAdmin(),
        app.container.testRepository.observeAllForAdmin(),
        app.container.noticeRepository.observeAllForAdmin(),
        app.container.adminRepository.observeLogs()
    ) { overview, pdfs, tests, notices, logs ->
        AdminUiState(overview = overview, pdfs = pdfs, tests = tests, notices = notices, logs = logs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdminUiState())

    fun uploadPdf(uri: Uri, title: String, subject: String, className: String) {
        val uid = app.container.authRepository.session.value.uid ?: return
        viewModelScope.launch {
            runCatching {
                app.container.pdfRepository.upload(
                    uri,
                    PdfDocument(
                        title = title,
                        subject = subject,
                        className = className,
                        uploadedBy = uid
                    )
                ) { }
            }
        }
    }

    fun publishPdf(id: String, published: Boolean) {
        viewModelScope.launch { runCatching { app.container.pdfRepository.publish(id, published) } }
    }

    fun saveNotice(title: String, body: String) {
        val uid = app.container.authRepository.session.value.uid ?: return
        viewModelScope.launch {
            runCatching {
                app.container.noticeRepository.save(
                    Notice(title = title, body = body, createdBy = uid, createdAt = System.currentTimeMillis())
                )
            }
        }
    }
}
