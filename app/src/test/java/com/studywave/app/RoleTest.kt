package com.studywave.app

import com.studywave.app.core.security.UserRole
import org.junit.Assert.assertEquals
import org.junit.Test

class RoleTest {
    @Test
    fun adminClaimMapsToAdmin() {
        assertEquals(UserRole.ADMIN, UserRole.fromClaim("admin"))
    }

    @Test
    fun missingClaimMapsToStudent() {
        assertEquals(UserRole.STUDENT, UserRole.fromClaim(null))
        assertEquals(UserRole.STUDENT, UserRole.fromClaim("student"))
    }
}
