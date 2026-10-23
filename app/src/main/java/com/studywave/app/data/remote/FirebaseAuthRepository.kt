package com.studywave.app.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.studywave.app.core.errors.AppError
import com.studywave.app.core.security.UserRole
import com.studywave.app.data.models.SessionState
import com.studywave.app.data.models.UserProfile
import com.studywave.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth?,
    private val firestore: FirebaseFirestore?
) : AuthRepository {

    private val _session = MutableStateFlow(
        SessionState(
            firebaseReady = FirebaseAvailability.isConfigured && auth != null,
            setupRequired = !FirebaseAvailability.isConfigured
        )
    )
    override val session: StateFlow<SessionState> = _session.asStateFlow()

    init {
        auth?.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _session.value = _session.value.copy(
                isAuthenticated = user != null,
                uid = user?.uid,
                firebaseReady = true,
                setupRequired = false,
                profile = user?.let {
                    UserProfile(
                        uid = it.uid,
                        displayName = it.displayName.orEmpty().ifBlank { "Student" },
                        email = it.email.orEmpty()
                    )
                }
            )
            if (user != null) {
                user.getIdToken(true).addOnSuccessListener { result ->
                    val role = UserRole.fromClaim(result.claims["role"] as? String)
                    _session.value = _session.value.copy(role = role)
                }
            }
        }
    }

    override suspend fun signIn(email: String, password: String) {
        val client = requireAuth()
        client.signInWithEmailAndPassword(email.trim(), password).await()
        refreshClaims()
    }

    override suspend fun register(name: String, email: String, password: String) {
        val client = requireAuth()
        val result = client.createUserWithEmailAndPassword(email.trim(), password).await()
        val user = result.user ?: throw AppError.Auth("Account was created but session is missing.")
        user.updateProfile(
            UserProfileChangeRequest.Builder().setDisplayName(name.trim()).build()
        ).await()
        firestore?.collection("users")?.document(user.uid)?.set(
            mapOf(
                "uid" to user.uid,
                "displayName" to name.trim(),
                "email" to email.trim(),
                "createdAt" to System.currentTimeMillis(),
                "lastActiveAt" to System.currentTimeMillis()
            )
        )?.await()
    }

    override suspend fun sendPasswordReset(email: String) {
        requireAuth().sendPasswordResetEmail(email.trim()).await()
    }

    override suspend fun signOut() {
        requireAuth().signOut()
        _session.value = _session.value.copy(
            isAuthenticated = false,
            uid = null,
            role = UserRole.STUDENT,
            profile = null
        )
    }

    override suspend fun refreshClaims() {
        val user = requireAuth().currentUser ?: return
        val token = user.getIdToken(true).await()
        val role = UserRole.fromClaim(token.claims["role"] as? String)
        _session.value = _session.value.copy(role = role)
    }

    private fun requireAuth(): FirebaseAuth {
        return auth ?: throw AppError.Configuration(
            "Firebase Authentication is not configured. Add google-services.json first."
        )
    }
}
