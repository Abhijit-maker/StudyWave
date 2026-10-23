package com.studywave.app.ui.navigation

object Routes {
    const val Splash = "splash"
    const val Onboarding = "onboarding"
    const val SignIn = "sign_in"
    const val Register = "register"
    const val ResetPassword = "reset_password"
    const val Home = "home"
    const val Library = "library"
    const val PdfViewer = "pdf/{pdfId}"
    const val Tests = "tests"
    const val TestPlayer = "test/{testId}"
    const val TestResult = "result/{attemptId}"
    const val Ai = "ai"
    const val Voice = "voice"
    const val Notices = "notices"
    const val NoticeDetail = "notice/{noticeId}"
    const val Profile = "profile"
    const val Admin = "admin"
    const val AdminPdfs = "admin/pdfs"
    const val AdminTests = "admin/tests"
    const val AdminQuestions = "admin/questions/{testId}"
    const val AdminNotices = "admin/notices"
    const val AdminUsers = "admin/users"
    const val AdminAi = "admin/ai"
    const val AdminLogs = "admin/logs"
    const val AdminSettings = "admin/settings"

    fun pdf(id: String) = "pdf/$id"
    fun test(id: String) = "test/$id"
    fun result(id: String) = "result/$id"
    fun notice(id: String) = "notice/$id"
    fun questions(id: String) = "admin/questions/$id"
}

enum class BottomTab(val route: String, val label: String) {
    Home(Routes.Home, "Home"),
    Library(Routes.Library, "Library"),
    Tests(Routes.Tests, "Tests"),
    Ai(Routes.Ai, "AI"),
    Profile(Routes.Profile, "Profile")
}
