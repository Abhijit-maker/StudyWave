package com.studywave.app.data.remote

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import com.studywave.app.core.errors.AppError
import com.studywave.app.core.security.UserRole
import com.studywave.app.data.models.AdminOverview
import com.studywave.app.data.models.AppConfig
import com.studywave.app.data.models.AuditLog
import com.studywave.app.data.models.Bookmark
import com.studywave.app.data.models.ChatMessage
import com.studywave.app.data.models.Conversation
import com.studywave.app.data.models.MessageRole
import com.studywave.app.data.models.MessageStatus
import com.studywave.app.data.models.MockTest
import com.studywave.app.data.models.Notice
import com.studywave.app.data.models.NoticeCategory
import com.studywave.app.data.models.PdfDocument
import com.studywave.app.data.models.Question
import com.studywave.app.data.models.QuestionType
import com.studywave.app.data.models.ReadingProgress
import com.studywave.app.data.models.TestAttempt
import com.studywave.app.data.models.UserProfile
import com.studywave.app.data.repository.AdminRepository
import com.studywave.app.data.repository.ChatRepository
import com.studywave.app.data.repository.ConfigRepository
import com.studywave.app.data.repository.NoticeRepository
import com.studywave.app.data.repository.PdfRepository
import com.studywave.app.data.repository.ProgressRepository
import com.studywave.app.data.repository.TestRepository
import com.studywave.app.data.repository.UserRepository
import com.studywave.app.data.repository.VoiceRepository
import com.studywave.app.data.repository.VoiceSession
import com.studywave.app.data.repository.VoiceState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

private fun requireDb(db: FirebaseFirestore?): FirebaseFirestore {
    return db ?: throw AppError.Configuration("Firebase Firestore is not configured.")
}

private fun requireStorage(storage: FirebaseStorage?): FirebaseStorage {
    return storage ?: throw AppError.Configuration("Firebase Storage is not configured.")
}

class FirebaseUserRepository(private val db: FirebaseFirestore?) : UserRepository {
    override fun observeProfile(uid: String): Flow<UserProfile?> {
        val firestore = db ?: return flowOf(null)
        return callbackFlow {
            val registration = firestore.collection("users").document(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(null)
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.toUserProfile())
                }
            awaitClose { registration.remove() }
        }
    }

    override suspend fun saveProfile(profile: UserProfile) {
        requireDb(db).collection("users").document(profile.uid).set(profile.toMap()).await()
    }

    override suspend fun deleteAccount(uid: String) {
        throw AppError.Configuration("Account deletion must run through a trusted Cloud Function.")
    }
}

class FirebasePdfRepository(
    private val db: FirebaseFirestore?,
    private val storage: FirebaseStorage?
) : PdfRepository {
    override fun observePublished(): Flow<List<PdfDocument>> = observe("published")
    override fun observeAllForAdmin(): Flow<List<PdfDocument>> = observe(null)

    private fun observe(statusField: String?): Flow<List<PdfDocument>> {
        val firestore = db ?: return flowOf(emptyList())
        return callbackFlow {
            var query: Query = firestore.collection("pdfs").orderBy("uploadedAt", Query.Direction.DESCENDING)
            if (statusField != null) {
                query = firestore.collection("pdfs")
                    .whereEqualTo("published", true)
                    .orderBy("uploadedAt", Query.Direction.DESCENDING)
            }
            val registration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.mapNotNull { it.toPdf() }.orEmpty())
            }
            awaitClose { registration.remove() }
        }
    }

    override suspend fun get(id: String): PdfDocument? {
        val snapshot = requireDb(db).collection("pdfs").document(id).get().await()
        return snapshot.toPdf()
    }

    override suspend fun upload(uri: Uri, metadata: PdfDocument, onProgress: (Double) -> Unit): String {
        val store = requireStorage(storage)
        val firestore = requireDb(db)
        val id = metadata.id.ifBlank { firestore.collection("pdfs").document().id }
        val path = "pdfs/$id/source.pdf"
        val ref = store.reference.child(path)
        val task = ref.putFile(uri)
        task.addOnProgressListener { snapshot ->
            val total = snapshot.totalByteCount.takeIf { it > 0 } ?: 1L
            onProgress(snapshot.bytesTransferred.toDouble() / total.toDouble())
        }
        task.await()
        val now = System.currentTimeMillis()
        val payload = metadata.copy(
            id = id,
            storagePath = path,
            uploadedAt = now,
            updatedAt = now,
            published = false
        )
        firestore.collection("pdfs").document(id).set(payload.toMap()).await()
        return id
    }

    override suspend fun publish(id: String, published: Boolean) {
        requireDb(db).collection("pdfs").document(id)
            .update(mapOf("published" to published, "updatedAt" to System.currentTimeMillis()))
            .await()
    }

    override suspend fun delete(id: String) {
        val firestore = requireDb(db)
        val doc = firestore.collection("pdfs").document(id).get().await().toPdf()
        if (doc != null && doc.storagePath.isNotBlank()) {
            requireStorage(storage).reference.child(doc.storagePath).delete().await()
        }
        firestore.collection("pdfs").document(id).delete().await()
    }

    override suspend fun downloadUrl(storagePath: String): Uri {
        return requireStorage(storage).reference.child(storagePath).downloadUrl.await()
    }
}

class FirebaseProgressRepository(private val db: FirebaseFirestore?) : ProgressRepository {
    override fun observe(uid: String): Flow<List<ReadingProgress>> {
        val firestore = db ?: return flowOf(emptyList())
        return callbackFlow {
            val registration = firestore.collection("readingProgress")
                .whereEqualTo("uid", uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.documents?.mapNotNull { it.toProgress() }.orEmpty())
                }
            awaitClose { registration.remove() }
        }
    }

    override suspend fun save(progress: ReadingProgress) {
        val id = "${progress.uid}_${progress.pdfId}"
        requireDb(db).collection("readingProgress").document(id).set(progress.toMap()).await()
    }

    override fun observeBookmarks(uid: String): Flow<List<Bookmark>> {
        val firestore = db ?: return flowOf(emptyList())
        return callbackFlow {
            val registration = firestore.collection("bookmarks")
                .whereEqualTo("uid", uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.documents?.mapNotNull { it.toBookmark() }.orEmpty())
                }
            awaitClose { registration.remove() }
        }
    }

    override suspend fun toggleBookmark(uid: String, targetType: String, targetId: String) {
        val firestore = requireDb(db)
        val id = "${uid}_${targetType}_$targetId"
        val existing = firestore.collection("bookmarks").document(id).get().await()
        if (existing.exists()) {
            firestore.collection("bookmarks").document(id).delete().await()
        } else {
            firestore.collection("bookmarks").document(id).set(
                mapOf(
                    "id" to id,
                    "uid" to uid,
                    "targetType" to targetType,
                    "targetId" to targetId,
                    "createdAt" to System.currentTimeMillis()
                )
            ).await()
        }
    }
}

class FirebaseTestRepository(
    private val db: FirebaseFirestore?,
    private val functions: FirebaseFunctions?
) : TestRepository {
    override fun observePublished(): Flow<List<MockTest>> = observeTests(true)
    override fun observeAllForAdmin(): Flow<List<MockTest>> = observeTests(null)

    private fun observeTests(published: Boolean?): Flow<List<MockTest>> {
        val firestore = db ?: return flowOf(emptyList())
        return callbackFlow {
            var query: Query = firestore.collection("mockTests").orderBy("createdAt", Query.Direction.DESCENDING)
            if (published != null) {
                query = firestore.collection("mockTests")
                    .whereEqualTo("published", true)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
            }
            val registration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.mapNotNull { it.toMockTest() }.orEmpty())
            }
            awaitClose { registration.remove() }
        }
    }

    override suspend fun get(id: String): MockTest? {
        return requireDb(db).collection("mockTests").document(id).get().await().toMockTest()
    }

    override fun observeQuestions(testId: String): Flow<List<Question>> {
        val firestore = db ?: return flowOf(emptyList())
        return callbackFlow {
            val registration = firestore.collection("mockTests").document(testId)
                .collection("questions")
                .orderBy("order")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.documents?.mapNotNull { it.toQuestion(testId) }.orEmpty())
                }
            awaitClose { registration.remove() }
        }
    }

    override suspend fun saveTest(test: MockTest) {
        val firestore = requireDb(db)
        val id = test.id.ifBlank { firestore.collection("mockTests").document().id }
        firestore.collection("mockTests").document(id).set(test.copy(id = id).toMap()).await()
    }

    override suspend fun saveQuestion(question: Question) {
        val firestore = requireDb(db)
        val id = question.id.ifBlank {
            firestore.collection("mockTests").document(question.testId).collection("questions").document().id
        }
        firestore.collection("mockTests").document(question.testId)
            .collection("questions").document(id)
            .set(question.copy(id = id).toMap())
            .await()
    }

    override suspend fun publish(id: String, published: Boolean) {
        requireDb(db).collection("mockTests").document(id)
            .update(mapOf("published" to published)).await()
    }

    override fun observeAttempts(uid: String): Flow<List<TestAttempt>> {
        val firestore = db ?: return flowOf(emptyList())
        return callbackFlow {
            val registration = firestore.collection("testAttempts")
                .whereEqualTo("uid", uid)
                .orderBy("startedAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.documents?.mapNotNull { it.toAttempt() }.orEmpty())
                }
            awaitClose { registration.remove() }
        }
    }

    override suspend fun startAttempt(testId: String, uid: String): TestAttempt {
        val firestore = requireDb(db)
        val id = firestore.collection("testAttempts").document().id
        val attempt = TestAttempt(
            id = id,
            testId = testId,
            uid = uid,
            startedAt = System.currentTimeMillis()
        )
        firestore.collection("testAttempts").document(id).set(attempt.toMap()).await()
        return attempt
    }

    override suspend fun autosave(attempt: TestAttempt) {
        requireDb(db).collection("testAttempts").document(attempt.id).set(attempt.toMap()).await()
    }

    override suspend fun submit(attemptId: String): TestAttempt {
        val functionsClient = functions
            ?: throw AppError.Configuration("Cloud Functions are not deployed. Scoring is pending configuration.")
        functionsClient.getHttpsCallable("submitTest").call(mapOf("attemptId" to attemptId)).await()
        return requireDb(db).collection("testAttempts").document(attemptId).get().await().toAttempt()
            ?: throw AppError.Unknown("Submission completed but the result could not be loaded.")
    }
}

class FirebaseNoticeRepository(private val db: FirebaseFirestore?) : NoticeRepository {
    override fun observePublished(): Flow<List<Notice>> = observe(true)
    override fun observeAllForAdmin(): Flow<List<Notice>> = observe(null)

    private fun observe(published: Boolean?): Flow<List<Notice>> {
        val firestore = db ?: return flowOf(emptyList())
        return callbackFlow {
            var query: Query = firestore.collection("notices").orderBy("createdAt", Query.Direction.DESCENDING)
            if (published != null) {
                query = firestore.collection("notices")
                    .whereEqualTo("published", true)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
            }
            val registration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.mapNotNull { it.toNotice() }.orEmpty())
            }
            awaitClose { registration.remove() }
        }
    }

    override suspend fun save(notice: Notice) {
        val firestore = requireDb(db)
        val id = notice.id.ifBlank { firestore.collection("notices").document().id }
        firestore.collection("notices").document(id).set(notice.copy(id = id).toMap()).await()
    }

    override suspend fun archive(id: String) {
        requireDb(db).collection("notices").document(id).update("published", false).await()
    }
}

class FirebaseChatRepository(
    private val db: FirebaseFirestore?,
    private val functions: FirebaseFunctions?
) : ChatRepository {
    override fun observeConversations(uid: String): Flow<List<Conversation>> {
        val firestore = db ?: return flowOf(emptyList())
        return callbackFlow {
            val registration = firestore.collection("conversations")
                .whereEqualTo("uid", uid)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.documents?.mapNotNull { it.toConversation() }.orEmpty())
                }
            awaitClose { registration.remove() }
        }
    }

    override fun observeMessages(conversationId: String): Flow<List<ChatMessage>> {
        val firestore = db ?: return flowOf(emptyList())
        return callbackFlow {
            val registration = firestore.collection("conversations").document(conversationId)
                .collection("messages")
                .orderBy("createdAt")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.documents?.mapNotNull { it.toMessage(conversationId) }.orEmpty())
                }
            awaitClose { registration.remove() }
        }
    }

    override suspend fun createConversation(uid: String, title: String, pdfId: String?): Conversation {
        val firestore = requireDb(db)
        val id = firestore.collection("conversations").document().id
        val now = System.currentTimeMillis()
        val conversation = Conversation(id, uid, title, pdfId, now, now)
        firestore.collection("conversations").document(id).set(conversation.toMap()).await()
        return conversation
    }

    override suspend fun rename(id: String, title: String) {
        requireDb(db).collection("conversations").document(id)
            .update(mapOf("title" to title, "updatedAt" to System.currentTimeMillis())).await()
    }

    override suspend fun delete(id: String) {
        requireDb(db).collection("conversations").document(id).delete().await()
    }

    override suspend fun sendMessage(
        conversationId: String,
        text: String,
        imagePath: String?,
        pdfId: String?
    ): ChatMessage {
        val firestore = requireDb(db)
        val functionsClient = functions
            ?: throw AppError.Configuration("AI Cloud Function is not deployed. Chat is pending configuration.")
        val userMsgId = firestore.collection("conversations").document(conversationId)
            .collection("messages").document().id
        val userMessage = ChatMessage(
            id = userMsgId,
            conversationId = conversationId,
            role = MessageRole.USER,
            text = text,
            imagePath = imagePath,
            createdAt = System.currentTimeMillis(),
            status = MessageStatus.COMPLETE
        )
        firestore.collection("conversations").document(conversationId)
            .collection("messages").document(userMsgId).set(userMessage.toMap()).await()
        functionsClient.getHttpsCallable("askStudyAssistant").call(
            mapOf(
                "conversationId" to conversationId,
                "text" to text,
                "imagePath" to imagePath,
                "pdfId" to pdfId
            )
        ).await()
        return userMessage
    }
}

class FirebaseConfigRepository(private val db: FirebaseFirestore?) : ConfigRepository {
    override fun observe(): Flow<AppConfig> {
        val firestore = db ?: return flowOf(AppConfig(setupRequired = true))
        return callbackFlow {
            val registration = firestore.collection("appConfig").document("public")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        trySend(AppConfig(setupRequired = !FirebaseAvailability.isConfigured))
                        return@addSnapshotListener
                    }
                    trySend(
                        AppConfig(
                            aiEnabled = snapshot.getBoolean("aiEnabled") ?: false,
                            voiceEnabled = snapshot.getBoolean("voiceEnabled") ?: false,
                            maxPdfBytes = snapshot.getLong("maxPdfBytes") ?: 52_428_800L,
                            maintenance = snapshot.getBoolean("maintenance") ?: false,
                            setupRequired = false
                        )
                    )
                }
            awaitClose { registration.remove() }
        }
    }
}

class FirebaseAdminRepository(private val db: FirebaseFirestore?) : AdminRepository {
    override fun observeOverview(): Flow<AdminOverview> {
        val firestore = db ?: return flowOf(AdminOverview())
        return callbackFlow {
            val registration = firestore.collection("appConfig").document("adminStats")
                .addSnapshotListener { snapshot, _ ->
                    trySend(
                        AdminOverview(
                            totalUsers = (snapshot?.getLong("totalUsers") ?: 0L).toInt(),
                            totalPdfs = (snapshot?.getLong("totalPdfs") ?: 0L).toInt(),
                            totalTests = (snapshot?.getLong("totalTests") ?: 0L).toInt(),
                            storageBytes = snapshot?.getLong("storageBytes") ?: 0L
                        )
                    )
                }
            awaitClose { registration.remove() }
        }
    }

    override fun observeLogs(): Flow<List<AuditLog>> {
        val firestore = db ?: return flowOf(emptyList())
        return callbackFlow {
            val registration = firestore.collection("auditLogs")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.documents?.mapNotNull { it.toAudit() }.orEmpty())
                }
            awaitClose { registration.remove() }
        }
    }

    override fun observeUsers(): Flow<List<UserProfile>> {
        val firestore = db ?: return flowOf(emptyList())
        return callbackFlow {
            val registration = firestore.collection("users")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(100)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.documents?.mapNotNull { it.toUserProfile() }.orEmpty())
                }
            awaitClose { registration.remove() }
        }
    }

    override suspend fun setUserRole(uid: String, role: UserRole) {
        throw AppError.Configuration("Roles must be assigned with Firebase custom claims, not Firestore fields.")
    }
}

class PendingVoiceRepository : VoiceRepository {
    override val configurationReady: Boolean = false
    override fun createSession(): VoiceSession = object : VoiceSession {
        override val state: StateFlow<VoiceState> = MutableStateFlow(VoiceState.DISCONNECTED)
        override suspend fun connect(language: String) {
            throw AppError.Configuration(
                "Live Voice needs a backend-issued temporary credential. Deploy functions/src/index.ts first."
            )
        }
        override suspend fun startListening() = Unit
        override suspend fun stopListening() = Unit
        override suspend fun disconnect() = Unit
    }
}

private fun com.google.firebase.firestore.DocumentSnapshot.toUserProfile(): UserProfile? {
    if (!exists()) return null
    return UserProfile(
        uid = getString("uid") ?: id,
        displayName = getString("displayName").orEmpty(),
        email = getString("email").orEmpty(),
        photoUrl = getString("photoUrl"),
        className = getString("className").orEmpty(),
        board = getString("board").orEmpty(),
        preferredSubjects = (get("preferredSubjects") as? List<*>)?.mapNotNull { it as? String }.orEmpty(),
        language = getString("language") ?: "en",
        studyGoalMinutes = (getLong("studyGoalMinutes") ?: 120L).toInt(),
        createdAt = getLong("createdAt") ?: 0L,
        lastActiveAt = getLong("lastActiveAt") ?: 0L
    )
}

private fun com.google.firebase.firestore.DocumentSnapshot.toPdf(): PdfDocument? {
    if (!exists()) return null
    return PdfDocument(
        id = id,
        title = getString("title").orEmpty(),
        description = getString("description").orEmpty(),
        subject = getString("subject").orEmpty(),
        className = getString("className").orEmpty(),
        board = getString("board").orEmpty(),
        semester = getString("semester").orEmpty(),
        tags = (get("tags") as? List<*>)?.mapNotNull { it as? String }.orEmpty(),
        storagePath = getString("storagePath").orEmpty(),
        fileSizeBytes = getLong("fileSizeBytes") ?: 0L,
        pageCount = (getLong("pageCount") ?: 0L).toInt(),
        published = getBoolean("published") ?: false,
        uploadedBy = getString("uploadedBy").orEmpty(),
        uploadedAt = getLong("uploadedAt") ?: 0L,
        updatedAt = getLong("updatedAt") ?: 0L
    )
}

private fun com.google.firebase.firestore.DocumentSnapshot.toProgress(): ReadingProgress? {
    if (!exists()) return null
    return ReadingProgress(
        pdfId = getString("pdfId").orEmpty(),
        uid = getString("uid").orEmpty(),
        lastPage = (getLong("lastPage") ?: 1L).toInt(),
        totalPages = (getLong("totalPages") ?: 1L).toInt(),
        updatedAt = getLong("updatedAt") ?: 0L
    )
}

private fun com.google.firebase.firestore.DocumentSnapshot.toBookmark(): Bookmark? {
    if (!exists()) return null
    return Bookmark(
        id = id,
        uid = getString("uid").orEmpty(),
        targetType = getString("targetType").orEmpty(),
        targetId = getString("targetId").orEmpty(),
        createdAt = getLong("createdAt") ?: 0L
    )
}

private fun com.google.firebase.firestore.DocumentSnapshot.toMockTest(): MockTest? {
    if (!exists()) return null
    return MockTest(
        id = id,
        title = getString("title").orEmpty(),
        description = getString("description").orEmpty(),
        subject = getString("subject").orEmpty(),
        className = getString("className").orEmpty(),
        board = getString("board").orEmpty(),
        topic = getString("topic").orEmpty(),
        instructions = getString("instructions").orEmpty(),
        durationMinutes = (getLong("durationMinutes") ?: 30L).toInt(),
        totalMarks = (getLong("totalMarks") ?: 0L).toInt(),
        questionCount = (getLong("questionCount") ?: 0L).toInt(),
        negativeMarking = getDouble("negativeMarking") ?: 0.0,
        published = getBoolean("published") ?: false,
        scheduledAt = getLong("scheduledAt"),
        createdBy = getString("createdBy").orEmpty(),
        createdAt = getLong("createdAt") ?: 0L,
        badge = getString("badge")
    )
}

private fun com.google.firebase.firestore.DocumentSnapshot.toQuestion(testId: String): Question? {
    if (!exists()) return null
    return Question(
        id = id,
        testId = testId,
        type = runCatching { QuestionType.valueOf(getString("type") ?: "MCQ") }.getOrDefault(QuestionType.MCQ),
        text = getString("text").orEmpty(),
        options = (get("options") as? List<*>)?.mapNotNull { it as? String }.orEmpty(),
        marks = (getLong("marks") ?: 1L).toInt(),
        order = (getLong("order") ?: 0L).toInt(),
        explanation = getString("explanation").orEmpty()
    )
}

private fun com.google.firebase.firestore.DocumentSnapshot.toAttempt(): TestAttempt? {
    if (!exists()) return null
    @Suppress("UNCHECKED_CAST")
    return TestAttempt(
        id = id,
        testId = getString("testId").orEmpty(),
        uid = getString("uid").orEmpty(),
        startedAt = getLong("startedAt") ?: 0L,
        submittedAt = getLong("submittedAt"),
        answers = (get("answers") as? Map<String, String>).orEmpty(),
        markedForReview = (get("markedForReview") as? List<*>)?.mapNotNull { it as? String }.orEmpty(),
        score = getDouble("score"),
        correct = getLong("correct")?.toInt(),
        incorrect = getLong("incorrect")?.toInt(),
        unanswered = getLong("unanswered")?.toInt()
    )
}

private fun com.google.firebase.firestore.DocumentSnapshot.toNotice(): Notice? {
    if (!exists()) return null
    return Notice(
        id = id,
        title = getString("title").orEmpty(),
        body = getString("body").orEmpty(),
        category = runCatching {
            NoticeCategory.valueOf(getString("category") ?: "GENERAL")
        }.getOrDefault(NoticeCategory.GENERAL),
        pinned = getBoolean("pinned") ?: false,
        published = getBoolean("published") ?: true,
        createdAt = getLong("createdAt") ?: 0L,
        expiresAt = getLong("expiresAt"),
        createdBy = getString("createdBy").orEmpty()
    )
}

private fun com.google.firebase.firestore.DocumentSnapshot.toConversation(): Conversation? {
    if (!exists()) return null
    return Conversation(
        id = id,
        uid = getString("uid").orEmpty(),
        title = getString("title") ?: "New chat",
        pdfId = getString("pdfId"),
        createdAt = getLong("createdAt") ?: 0L,
        updatedAt = getLong("updatedAt") ?: 0L
    )
}

private fun com.google.firebase.firestore.DocumentSnapshot.toMessage(conversationId: String): ChatMessage? {
    if (!exists()) return null
    return ChatMessage(
        id = id,
        conversationId = conversationId,
        role = runCatching { MessageRole.valueOf(getString("role") ?: "USER") }.getOrDefault(MessageRole.USER),
        text = getString("text").orEmpty(),
        imagePath = getString("imagePath"),
        createdAt = getLong("createdAt") ?: 0L,
        status = runCatching {
            MessageStatus.valueOf(getString("status") ?: "COMPLETE")
        }.getOrDefault(MessageStatus.COMPLETE)
    )
}

private fun com.google.firebase.firestore.DocumentSnapshot.toAudit(): AuditLog? {
    if (!exists()) return null
    return AuditLog(
        id = id,
        actorUid = getString("actorUid").orEmpty(),
        action = getString("action").orEmpty(),
        target = getString("target").orEmpty(),
        createdAt = getLong("createdAt") ?: 0L
    )
}

private fun UserProfile.toMap() = mapOf(
    "uid" to uid,
    "displayName" to displayName,
    "email" to email,
    "photoUrl" to photoUrl,
    "className" to className,
    "board" to board,
    "preferredSubjects" to preferredSubjects,
    "language" to language,
    "studyGoalMinutes" to studyGoalMinutes,
    "createdAt" to createdAt,
    "lastActiveAt" to lastActiveAt
)

private fun PdfDocument.toMap() = mapOf(
    "title" to title,
    "description" to description,
    "subject" to subject,
    "className" to className,
    "board" to board,
    "semester" to semester,
    "tags" to tags,
    "storagePath" to storagePath,
    "fileSizeBytes" to fileSizeBytes,
    "pageCount" to pageCount,
    "published" to published,
    "uploadedBy" to uploadedBy,
    "uploadedAt" to uploadedAt,
    "updatedAt" to updatedAt
)

private fun ReadingProgress.toMap() = mapOf(
    "pdfId" to pdfId,
    "uid" to uid,
    "lastPage" to lastPage,
    "totalPages" to totalPages,
    "updatedAt" to updatedAt
)

private fun MockTest.toMap() = mapOf(
    "title" to title,
    "description" to description,
    "subject" to subject,
    "className" to className,
    "board" to board,
    "topic" to topic,
    "instructions" to instructions,
    "durationMinutes" to durationMinutes,
    "totalMarks" to totalMarks,
    "questionCount" to questionCount,
    "negativeMarking" to negativeMarking,
    "published" to published,
    "scheduledAt" to scheduledAt,
    "createdBy" to createdBy,
    "createdAt" to createdAt,
    "badge" to badge
)

private fun Question.toMap() = mapOf(
    "type" to type.name,
    "text" to text,
    "options" to options,
    "marks" to marks,
    "order" to order,
    "explanation" to explanation
)

private fun TestAttempt.toMap() = mapOf(
    "testId" to testId,
    "uid" to uid,
    "startedAt" to startedAt,
    "submittedAt" to submittedAt,
    "answers" to answers,
    "markedForReview" to markedForReview,
    "score" to score,
    "correct" to correct,
    "incorrect" to incorrect,
    "unanswered" to unanswered,
    "status" to status.name
)

private fun Notice.toMap() = mapOf(
    "title" to title,
    "body" to body,
    "category" to category.name,
    "pinned" to pinned,
    "published" to published,
    "createdAt" to createdAt,
    "expiresAt" to expiresAt,
    "createdBy" to createdBy
)

private fun Conversation.toMap() = mapOf(
    "uid" to uid,
    "title" to title,
    "pdfId" to pdfId,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt
)

private fun ChatMessage.toMap() = mapOf(
    "role" to role.name,
    "text" to text,
    "imagePath" to imagePath,
    "createdAt" to createdAt,
    "status" to status.name
)
