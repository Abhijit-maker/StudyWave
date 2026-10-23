package com.studywave.app.core.errors

sealed class AppError(override val message: String) : Exception(message) {
    data class Network(override val message: String = "Check your internet connection and try again.") : AppError(message)
    data class Auth(override val message: String) : AppError(message)
    data class Permission(override val message: String = "You do not have permission to do this.") : AppError(message)
    data class NotFound(override val message: String = "This item is no longer available.") : AppError(message)
    data class Validation(override val message: String) : AppError(message)
    data class Upload(override val message: String) : AppError(message)
    data class Configuration(override val message: String) : AppError(message)
    data class Ai(override val message: String) : AppError(message)
    data class Unknown(override val message: String = "Something went wrong. Please try again.") : AppError(message)
}

fun Throwable.toAppError(): AppError {
    val text = message.orEmpty()
    return when {
        text.contains("network", true) || text.contains("Unable to resolve", true) ->
            AppError.Network()
        text.contains("permission", true) || text.contains("PERMISSION_DENIED", true) ->
            AppError.Permission()
        text.contains("UNAUTHENTICATED", true) || text.contains("password", true) ->
            AppError.Auth(text.ifBlank { "Sign in failed. Check your details." })
        else -> AppError.Unknown(text.ifBlank { "Something went wrong. Please try again." })
    }
}
