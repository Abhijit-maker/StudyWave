package com.studywave.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studywave.app.ui.components.BookMarkLogo
import com.studywave.app.ui.components.SetupRequiredBanner
import com.studywave.app.ui.components.StudyCompanionIllustration
import com.studywave.app.ui.components.SwPrimaryButton
import com.studywave.app.ui.components.SwTextField
import com.studywave.app.ui.theme.StudyWaveColors
import com.studywave.app.viewmodel.AuthViewModel
import com.studywave.app.viewmodel.SessionViewModel

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(StudyWaveColors.LavenderSurface, StudyWaveColors.PrimaryBackground)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BookMarkLogo(size = 88.dp)
            Spacer(Modifier.height(16.dp))
            Text("StudyWave", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = StudyWaveColors.MainText)
            Text("Your AI-Powered Study Companion", color = StudyWaveColors.SecondaryText, fontSize = 14.sp)
        }
        androidx.compose.runtime.LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(900)
            onFinished()
        }
    }
}

@Composable
fun OnboardingScreen(onGetStarted: () -> Unit, onLogin: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(StudyWaveColors.LightBlueSurface, StudyWaveColors.PrimaryBackground)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        BookMarkLogo()
        Spacer(Modifier.height(12.dp))
        Text("StudyWave", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = StudyWaveColors.MainText)
        Text("Your AI-Powered Study Companion", color = StudyWaveColors.SecondaryText)
        Spacer(Modifier.height(24.dp))
        StudyCompanionIllustration(size = 220.dp)
        Spacer(Modifier.weight(1f))
        SwPrimaryButton("Get Started", onGetStarted)
        TextButton(onClick = onLogin) {
            Text("Already have an account? Login", color = StudyWaveColors.PrimaryPurple)
        }
    }
}

@Composable
fun SignInScreen(
    onRegister: () -> Unit,
    onReset: () -> Unit,
    sessionViewModel: SessionViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val auth by authViewModel.state.collectAsStateWithLifecycle()
    val session by sessionViewModel.session.collectAsStateWithLifecycle()
    AuthForm(
        title = "Welcome back",
        subtitle = "Sign in to continue learning",
        showName = false,
        auth = auth,
        setupRequired = session.setupRequired,
        onName = {},
        onEmail = authViewModel::updateEmail,
        onPassword = authViewModel::updatePassword,
        primary = "Sign In",
        onPrimary = authViewModel::signIn,
        secondary = "Create an account",
        onSecondary = onRegister,
        tertiary = "Forgot password?",
        onTertiary = onReset
    )
}

@Composable
fun RegisterScreen(
    onSignIn: () -> Unit,
    sessionViewModel: SessionViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val auth by authViewModel.state.collectAsStateWithLifecycle()
    val session by sessionViewModel.session.collectAsStateWithLifecycle()
    AuthForm(
        title = "Create account",
        subtitle = "Start your StudyWave journey",
        showName = true,
        auth = auth,
        setupRequired = session.setupRequired,
        onName = authViewModel::updateName,
        onEmail = authViewModel::updateEmail,
        onPassword = authViewModel::updatePassword,
        primary = "Create account",
        onPrimary = authViewModel::register,
        secondary = "Already have an account? Sign in",
        onSecondary = onSignIn
    )
}

@Composable
fun ResetPasswordScreen(
    onBack: () -> Unit,
    sessionViewModel: SessionViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val auth by authViewModel.state.collectAsStateWithLifecycle()
    val session by sessionViewModel.session.collectAsStateWithLifecycle()
    AuthForm(
        title = "Reset password",
        subtitle = "We will email you a reset link",
        showName = false,
        showPassword = false,
        auth = auth,
        setupRequired = session.setupRequired,
        onName = {},
        onEmail = authViewModel::updateEmail,
        onPassword = {},
        primary = "Send reset email",
        onPrimary = authViewModel::reset,
        secondary = "Back to sign in",
        onSecondary = onBack
    )
}

@Composable
private fun AuthForm(
    title: String,
    subtitle: String,
    showName: Boolean,
    showPassword: Boolean = true,
    auth: com.studywave.app.viewmodel.AuthUiState,
    setupRequired: Boolean,
    onName: (String) -> Unit,
    onEmail: (String) -> Unit,
    onPassword: (String) -> Unit,
    primary: String,
    onPrimary: () -> Unit,
    secondary: String,
    onSecondary: () -> Unit,
    tertiary: String? = null,
    onTertiary: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyWaveColors.PrimaryBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        if (setupRequired) {
            SetupRequiredBanner("Firebase is not configured yet. Add google-services.json to enable sign-in.")
            Spacer(Modifier.height(12.dp))
        }
        BookMarkLogo(size = 56.dp)
        Spacer(Modifier.height(16.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = StudyWaveColors.MainText)
        Text(subtitle, color = StudyWaveColors.SecondaryText)
        Spacer(Modifier.height(24.dp))
        if (showName) {
            SwTextField(auth.name, onName, "Full name")
            Spacer(Modifier.height(12.dp))
        }
        SwTextField(auth.email, onEmail, "Email")
        if (showPassword) {
            Spacer(Modifier.height(12.dp))
            SwTextField(
                value = auth.password,
                onValueChange = onPassword,
                label = "Password",
                visualTransformation = PasswordVisualTransformation()
            )
        }
        if (auth.error != null) {
            Spacer(Modifier.height(12.dp))
            Text(auth.error, color = StudyWaveColors.Error, fontSize = 13.sp)
        }
        if (auth.info != null) {
            Spacer(Modifier.height(12.dp))
            Text(auth.info, color = StudyWaveColors.Success, fontSize = 13.sp)
        }
        Spacer(Modifier.height(20.dp))
        SwPrimaryButton(primary, onPrimary, loading = auth.loading, enabled = !setupRequired)
        TextButton(onClick = onSecondary, modifier = Modifier.fillMaxWidth()) {
            Text(secondary, color = StudyWaveColors.PrimaryPurple, textAlign = TextAlign.Center)
        }
        if (tertiary != null && onTertiary != null) {
            TextButton(onClick = onTertiary, modifier = Modifier.fillMaxWidth()) {
                Text(tertiary, color = StudyWaveColors.SecondaryText)
            }
        }
    }
}
