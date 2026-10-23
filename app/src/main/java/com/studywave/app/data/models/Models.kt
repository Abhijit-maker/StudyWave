package com.studywave.app.data.models

import com.studywave.app.core.security.UserRole

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val className: String = "",
    val board: String = "",
    val preferredSubjects: List<String> = emptyList(),
    val language: String = "en",
    val studyGoalMinutes: Int = 120,
    val createdAt: Long = 0L,
    val lastActiveAt: Long = 0L
)

data class SessionState(
    val isAuthenticated: Boolean = false,
    val uid: String? = null,
    val role: UserRole = UserRole.STUDENT,
    val profile: UserProfile? = null,
    val firebaseReady: Boolean = false,
    val setupRequired: Boolean = false
)

data class PdfDocument(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val subject: String = "",
    val className: String = "",
    val board: String = "",
    val semester: String = "",
    val tags: List<String> = emptyList(),
    val storagePath: String = "",
    val fileSizeBytes: Long = 0L,
    val pageCount: Int = 0,
    val published: Boolean = false,
    val uploadedBy: String = "",
    val uploadedAt: Long = 0L,
    val updatedAt: Long = 0L
)

data class ReadingProgress(
    val pdfId: String = "",
    val uid: String = "",
    val lastPage: Int = 1,
    val totalPages: Int = 1,
    val updatedAt: Long = 0L
)

data class Bookmark(
    val id: String = "",
    val uid: String = "",
    val targetType: String = "",
    val targetId: String = "",
    val createdAt: Long = 0L
)

data class MockTest(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val subject: String = "",
    val className: String = "",
    val board: String = "",
    val topic: String = "",
    val instructions: String = "",
    val durationMinutes: Int = 30,
    val totalMarks: Int = 0,
    val questionCount: Int = 0,
    val negativeMarking: Double = 0.0,
    val published: Boolean = false,
    val scheduledAt: Long? = null,
    val createdBy: String = "",
    val createdAt: Long = 0L,
    val badge: String? = null
)

data class Question(
    val id: String = "",
    val testId: String = "",
    val type: QuestionType = QuestionType.MCQ,
    val text: String = "",
    val options: List<String> = emptyList(),
    val marks: Int = 1,
    val order: Int = 0,
    val explanation: String = ""
)

enum class QuestionType { MCQ, MULTI_SELECT, WRITTEN }

data class TestAttempt(
    val id: String = "",
    val testId: String = "",
    val uid: String = "",
    val startedAt: Long = 0L,
    val submittedAt: Long? = null,
    val answers: Map<String, String> = emptyMap(),
    val markedForReview: List<String> = emptyList(),
    val score: Double? = null,
    val correct: Int? = null,
    val incorrect: Int? = null,
    val unanswered: Int? = null,
    val status: AttemptStatus = AttemptStatus.IN_PROGRESS
)

enum class AttemptStatus { IN_PROGRESS, SUBMITTED, GRADED }

data class Notice(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val category: NoticeCategory = NoticeCategory.GENERAL,
    val pinned: Boolean = false,
    val published: Boolean = true,
    val createdAt: Long = 0L,
    val expiresAt: Long? = null,
    val createdBy: String = ""
)

enum class NoticeCategory { ALL, IMPORTANT, PDF, TEST, EXAM, SYSTEM, GENERAL }

data class Conversation(
    val id: String = "",
    val uid: String = "",
    val title: String = "New chat",
    val pdfId: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

data class ChatMessage(
    val id: String = "",
    val conversationId: String = "",
    val role: MessageRole = MessageRole.USER,
    val text: String = "",
    val imagePath: String? = null,
    val createdAt: Long = 0L,
    val status: MessageStatus = MessageStatus.COMPLETE
)

enum class MessageRole { USER, ASSISTANT, SYSTEM }
enum class MessageStatus { SENDING, COMPLETE, FAILED }

data class AppConfig(
    val aiEnabled: Boolean = false,
    val voiceEnabled: Boolean = false,
    val maxPdfBytes: Long = 52_428_800L,
    val maintenance: Boolean = false,
    val setupRequired: Boolean = true
)

data class AdminOverview(
    val totalUsers: Int = 0,
    val totalPdfs: Int = 0,
    val totalTests: Int = 0,
    val storageBytes: Long = 0L
)

data class AuditLog(
    val id: String = "",
    val actorUid: String = "",
    val action: String = "",
    val target: String = "",
    val createdAt: Long = 0L
)

data class SubjectFilter(
    val id: String,
    val label: String
)

object Catalog {
    val subjects = listOf(
        SubjectFilter("all", "All"),
        SubjectFilter("physics", "Physics"),
        SubjectFilter("chemistry", "Chemistry"),
        SubjectFilter("math", "Math"),
        SubjectFilter("biology", "Biology"),
        SubjectFilter("english", "English")
    )
    val classes = listOf("Class 9", "Class 10", "Class 11", "Class 12")
    val boards = listOf("SSC", "HSC", "CBSE", "NCTB")
}
