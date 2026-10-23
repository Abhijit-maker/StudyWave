package com.studywave.app.core.security

enum class UserRole {
    STUDENT,
    ADMIN;

    companion object {
        fun fromClaim(value: String?): UserRole {
            return if (value.equals("admin", ignoreCase = true)) ADMIN else STUDENT
        }
    }
}
