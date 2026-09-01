package com.example.taskmanagerkmpapp

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

class SharedCommonTest {

    @Test
    fun `register and reset password by email works`() = runBlocking {
        val repository = MockAuthRepository

        repository.register("Test User", "testuser", "test@example.com", "oldPassword")
        val loginBeforeReset = repository.login("testuser", "oldPassword")
        assertTrue(loginBeforeReset.success)

        val resetResult = repository.resetPasswordByEmail("test@example.com", "newPassword")
        assertTrue(resetResult.success)

        val loginAfterReset = repository.login("testuser", "newPassword")
        assertTrue(loginAfterReset.success)
    }
}