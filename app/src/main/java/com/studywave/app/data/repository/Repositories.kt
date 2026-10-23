package com.studywave.app.data.repository

import android.net.Uri
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val session: StateFlow<SessionState>
    suspend fun signIn(email: String, password: String)
    suspend fun register(name: String, email: String, password: String)
    suspend fun sendPasswordReset(email: String)
    suspend fun signOut()
    suspend fun refreshClaims()
}

interface UserRepository {
    fun observeProfile(uid: String): Flow<UserProfile?>
    suspend fun saveProfile(profile: UserProfile)
    suspend fun deleteAccount(uid: String)
}

interface PdfRepository {
    fun observePublished(): Flow<List<PdfDocument>>
    fun observeAllForAdmin(): Flow<List<PdfDocument>>
    suspend fun get(id: String): PdfDocument?
    suspend fun upload(
        uri: Uri,
        metadata: PdfDocument,
        onProgress: (Double) -> Unit
    ): String
    suspend fun publish(id: String, published: Boolean)
    suspend fun delete(id: String)
    suspend fun downloadUrl(storagePath: String): Uri
}

interface ProgressRepository {
    fun observe(uid: String): Flow<List<ReadingProgress>>
    suspend fun save(progress: ReadingProgress)
    fun observeBookmarks(uid: String): Flow<List<Bookmark>>
    suspend fun toggleBookmark(uid: String, targetType: String, targetId: String)
}

interface TestRepository {
    fun observePublished(): Flow<List<MockTest>>
    fun observeAllForAdmin(): Flow<List<MockTest>>
    suspend fun get(id: String): MockTest?
    fun observeQuestions(testId: String): Flow<List<Question>>
    suspend fun saveTest(test: MockTest)
    suspend fun saveQuestion(question: Question)
    suspend fun publish(id: String, published: Boolean)
    fun observeAttempts(uid: String): Flow<List<TestAttempt>>
    suspend fun startAttempt(testId: String, uid: String): TestAttempt
    suspend fun autosave(attempt: TestAttempt)
    suspend fun submit(attemptId: String): TestAttempt
}

interface NoticeRepository {
    fun observePublished(): Flow<List<Notice>>
    fun observeAllForAdmin(): Flow<List<Notice>>
    suspend fun save(notice: Notice)
    suspend fun archive(id: String)
}

interface ChatRepository {
    fun observeConversations(uid: String): Flow<List<Conversation>>
    fun observeMessages(conversationId: String): Flow<List<ChatMessage>>
    suspend fun createConversation(uid: String, title: String, pdfId: String?): Conversation
    suspend fun rename(id: String, title: String)
    suspend fun delete(id: String)
    suspend fun sendMessage(
        conversationId: String,
        text: String,
        imagePath: String?,
        pdfId: String?
    ): ChatMessage
}

interface ConfigRepository {
    fun observe(): Flow<AppConfig>
}

interface AdminRepository {
    fun observeOverview(): Flow<AdminOverview>
    fun observeLogs(): Flow<List<AuditLog>>
    fun observeUsers(): Flow<List<UserProfile>>
    suspend fun setUserRole(uid: String, role: UserRole)
}

interface VoiceSession {
    val state: StateFlow<VoiceState>
    suspend fun connect(language: String)
    suspend fun startListening()
    suspend fun stopListening()
    suspend fun disconnect()
}

enum class VoiceState {
    DISCONNECTED, CONNECTING, LISTENING, PROCESSING, SPEAKING, ERROR
}

interface VoiceRepository {
    fun createSession(): VoiceSession
    val configurationReady: Boolean
}
