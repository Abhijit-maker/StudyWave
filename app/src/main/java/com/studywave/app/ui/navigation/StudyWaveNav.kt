package com.studywave.app.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.studywave.app.ui.screens.admin.AdminAiScreen
import com.studywave.app.ui.screens.admin.AdminDashboard
import com.studywave.app.ui.screens.admin.AdminLogsScreen
import com.studywave.app.ui.screens.admin.AdminNoticesScreen
import com.studywave.app.ui.screens.admin.AdminPdfScreen
import com.studywave.app.ui.screens.admin.AdminSettingsScreen
import com.studywave.app.ui.screens.admin.AdminTestsScreen
import com.studywave.app.ui.screens.admin.AdminUsersScreen
import com.studywave.app.ui.screens.ai.AiScreen
import com.studywave.app.ui.screens.auth.OnboardingScreen
import com.studywave.app.ui.screens.auth.RegisterScreen
import com.studywave.app.ui.screens.auth.ResetPasswordScreen
import com.studywave.app.ui.screens.auth.SignInScreen
import com.studywave.app.ui.screens.auth.SplashScreen
import com.studywave.app.ui.screens.home.HomeScreen
import com.studywave.app.ui.screens.library.LibraryScreen
import com.studywave.app.ui.screens.library.PdfViewerScreen
import com.studywave.app.ui.screens.notices.NoticeDetailScreen
import com.studywave.app.ui.screens.notices.NoticesScreen
import com.studywave.app.ui.screens.profile.ProfileScreen
import com.studywave.app.ui.screens.tests.TestPlayerScreen
import com.studywave.app.ui.screens.tests.TestResultScreen
import com.studywave.app.ui.screens.tests.TestsScreen
import com.studywave.app.ui.screens.voice.VoiceScreen
import com.studywave.app.ui.theme.StudyWaveColors
import com.studywave.app.viewmodel.SessionViewModel

@Composable
fun StudyWaveNav(sessionViewModel: SessionViewModel = viewModel()) {
    val navController = rememberNavController()
    val session by sessionViewModel.session.collectAsStateWithLifecycle()
    var sawSplash by rememberSaveable { mutableStateOf(false) }
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
    val showBottomBar = current in setOf(Routes.Home, Routes.Library, Routes.Tests, Routes.Ai, Routes.Profile)

    LaunchedEffect(session.isAuthenticated, sawSplash) {
        if (!sawSplash) return@LaunchedEffect
        val target = if (session.isAuthenticated) Routes.Home else Routes.Onboarding
        val onAuthGraph = current in setOf(Routes.Onboarding, Routes.SignIn, Routes.Register, Routes.ResetPassword, Routes.Splash)
        if (session.isAuthenticated && onAuthGraph) {
            navController.navigate(Routes.Home) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
            }
        } else if (!session.isAuthenticated && current in setOf(Routes.Home, Routes.Library, Routes.Tests, Routes.Ai, Routes.Profile, Routes.Admin)) {
            navController.navigate(target) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
            }
        }
    }

    Scaffold(
        containerColor = StudyWaveColors.PrimaryBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AnimatedVisibility(visible = showBottomBar, enter = fadeIn(), exit = fadeOut()) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                ) {
                    BottomTab.entries.forEach { tab ->
                        val selected = current == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(Routes.Home) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = when (tab) {
                                        BottomTab.Home -> if (selected) Icons.Rounded.Home else Icons.Outlined.Home
                                        BottomTab.Library -> if (selected) Icons.Rounded.MenuBook else Icons.Outlined.MenuBook
                                        BottomTab.Tests -> if (selected) Icons.Rounded.Quiz else Icons.Outlined.Quiz
                                        BottomTab.Ai -> if (selected) Icons.Rounded.AutoAwesome else Icons.Outlined.AutoAwesome
                                        BottomTab.Profile -> if (selected) Icons.Rounded.Person else Icons.Outlined.PersonOutline
                                    },
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = StudyWaveColors.PrimaryPurple,
                                selectedTextColor = StudyWaveColors.PrimaryPurple,
                                unselectedIconColor = StudyWaveColors.NavInactive,
                                unselectedTextColor = StudyWaveColors.NavInactive,
                                indicatorColor = StudyWaveColors.LavenderSurface
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(StudyWaveColors.PrimaryBackground)
                .statusBarsPadding()
                .padding(padding)
        ) {
            NavHost(navController = navController, startDestination = Routes.Splash) {
                composable(Routes.Splash) {
                    SplashScreen {
                        sawSplash = true
                        navController.navigate(if (session.isAuthenticated) Routes.Home else Routes.Onboarding) {
                            popUpTo(Routes.Splash) { inclusive = true }
                        }
                    }
                }
                composable(Routes.Onboarding) {
                    OnboardingScreen(
                        onGetStarted = { navController.navigate(Routes.Register) },
                        onLogin = { navController.navigate(Routes.SignIn) }
                    )
                }
                composable(Routes.SignIn) {
                    SignInScreen(
                        onRegister = { navController.navigate(Routes.Register) },
                        onReset = { navController.navigate(Routes.ResetPassword) }
                    )
                }
                composable(Routes.Register) {
                    RegisterScreen(onSignIn = { navController.navigate(Routes.SignIn) })
                }
                composable(Routes.ResetPassword) {
                    ResetPasswordScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.Home) {
                    HomeScreen(
                        onLibrary = { navController.navigate(Routes.Library) },
                        onTests = { navController.navigate(Routes.Tests) },
                        onAi = { navController.navigate(Routes.Ai) },
                        onVoice = { navController.navigate(Routes.Voice) },
                        onNotices = { navController.navigate(Routes.Notices) },
                        onPdf = { navController.navigate(Routes.pdf(it)) },
                        onTest = { navController.navigate(Routes.test(it)) }
                    )
                }
                composable(Routes.Library) {
                    LibraryScreen(onOpen = { navController.navigate(Routes.pdf(it)) })
                }
                composable(
                    Routes.PdfViewer,
                    arguments = listOf(navArgument("pdfId") { type = NavType.StringType })
                ) { entry ->
                    PdfViewerScreen(
                        pdfId = entry.arguments?.getString("pdfId").orEmpty(),
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.Tests) {
                    TestsScreen(onStart = { navController.navigate(Routes.test(it)) })
                }
                composable(
                    Routes.TestPlayer,
                    arguments = listOf(navArgument("testId") { type = NavType.StringType })
                ) { entry ->
                    TestPlayerScreen(
                        testId = entry.arguments?.getString("testId").orEmpty(),
                        onBack = { navController.popBackStack() },
                        onSubmitted = { navController.navigate(Routes.result(it)) }
                    )
                }
                composable(
                    Routes.TestResult,
                    arguments = listOf(navArgument("attemptId") { type = NavType.StringType })
                ) { entry ->
                    TestResultScreen(
                        attemptId = entry.arguments?.getString("attemptId").orEmpty(),
                        onBack = { navController.popBackStack(Routes.Tests, false) }
                    )
                }
                composable(Routes.Ai) {
                    AiScreen(onVoice = { navController.navigate(Routes.Voice) })
                }
                composable(Routes.Voice) {
                    VoiceScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.Notices) {
                    NoticesScreen(
                        onOpen = { navController.navigate(Routes.notice(it)) },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(
                    Routes.NoticeDetail,
                    arguments = listOf(navArgument("noticeId") { type = NavType.StringType })
                ) { entry ->
                    NoticeDetailScreen(
                        noticeId = entry.arguments?.getString("noticeId").orEmpty(),
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.Profile) {
                    ProfileScreen(onAdmin = { navController.navigate(Routes.Admin) })
                }
                composable(Routes.Admin) {
                    AdminDashboard(
                        onPdfs = { navController.navigate(Routes.AdminPdfs) },
                        onTests = { navController.navigate(Routes.AdminTests) },
                        onNotices = { navController.navigate(Routes.AdminNotices) },
                        onUsers = { navController.navigate(Routes.AdminUsers) },
                        onAi = { navController.navigate(Routes.AdminAi) },
                        onLogs = { navController.navigate(Routes.AdminLogs) },
                        onSettings = { navController.navigate(Routes.AdminSettings) },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.AdminPdfs) { AdminPdfScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.AdminTests) { AdminTestsScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.AdminNotices) { AdminNoticesScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.AdminUsers) { AdminUsersScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.AdminAi) { AdminAiScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.AdminLogs) { AdminLogsScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.AdminSettings) { AdminSettingsScreen(onBack = { navController.popBackStack() }) }
            }
        }
    }
}
