package com.studywave.app.di

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import com.studywave.app.data.remote.FirebaseAdminRepository
import com.studywave.app.data.remote.FirebaseAuthRepository
import com.studywave.app.data.remote.FirebaseAvailability
import com.studywave.app.data.remote.FirebaseChatRepository
import com.studywave.app.data.remote.FirebaseConfigRepository
import com.studywave.app.data.remote.FirebaseNoticeRepository
import com.studywave.app.data.remote.FirebasePdfRepository
import com.studywave.app.data.remote.FirebaseProgressRepository
import com.studywave.app.data.remote.FirebaseTestRepository
import com.studywave.app.data.remote.FirebaseUserRepository
import com.studywave.app.data.remote.PendingVoiceRepository
import com.studywave.app.data.repository.AdminRepository
import com.studywave.app.data.repository.AuthRepository
import com.studywave.app.data.repository.ChatRepository
import com.studywave.app.data.repository.ConfigRepository
import com.studywave.app.data.repository.NoticeRepository
import com.studywave.app.data.repository.PdfRepository
import com.studywave.app.data.repository.ProgressRepository
import com.studywave.app.data.repository.TestRepository
import com.studywave.app.data.repository.UserRepository
import com.studywave.app.data.repository.VoiceRepository

class AppContainer(context: Context) {
    private val firebaseReady = runCatching {
        if (FirebaseApp.getApps(context).isEmpty() && FirebaseAvailability.isConfigured) {
            FirebaseApp.initializeApp(context)
        }
        FirebaseAvailability.isConfigured && FirebaseApp.getApps(context).isNotEmpty()
    }.getOrDefault(false)

    private val auth = if (firebaseReady) runCatching { FirebaseAuth.getInstance() }.getOrNull() else null
    private val firestore = if (firebaseReady) runCatching { FirebaseFirestore.getInstance() }.getOrNull() else null
    private val storage = if (firebaseReady) runCatching { FirebaseStorage.getInstance() }.getOrNull() else null
    private val functions = if (firebaseReady) runCatching { FirebaseFunctions.getInstance() }.getOrNull() else null

    val authRepository: AuthRepository = FirebaseAuthRepository(auth, firestore)
    val userRepository: UserRepository = FirebaseUserRepository(firestore)
    val pdfRepository: PdfRepository = FirebasePdfRepository(firestore, storage)
    val progressRepository: ProgressRepository = FirebaseProgressRepository(firestore)
    val testRepository: TestRepository = FirebaseTestRepository(firestore, functions)
    val noticeRepository: NoticeRepository = FirebaseNoticeRepository(firestore)
    val chatRepository: ChatRepository = FirebaseChatRepository(firestore, functions)
    val configRepository: ConfigRepository = FirebaseConfigRepository(firestore)
    val adminRepository: AdminRepository = FirebaseAdminRepository(firestore)
    val voiceRepository: VoiceRepository = PendingVoiceRepository()
}
