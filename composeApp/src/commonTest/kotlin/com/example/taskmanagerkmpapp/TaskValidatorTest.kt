package com.example.taskmanagerkmpapp

import com.example.taskmanagerkmpapp.domain.TaskValidator
import com.example.taskmanagerkmpapp.domain.ValidationResult
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertIs

class TaskValidatorTest {

    @Test
    fun `empty title returns error`() {
        val result = TaskValidator.validate("", "Description")
        assertIs<ValidationResult.Error>(result)
        assertTrue { result.message.contains("Title") }
    }

    @Test
    fun `short title returns error`() {
        val result = TaskValidator.validate("Hi", "Description")
        assertIs<ValidationResult.Error>(result)
        assertTrue { result.message.contains("at least") }
    }

    @Test
    fun `empty description returns error`() {
        val result = TaskValidator.validate("Valid Title", "")
        assertIs<ValidationResult.Error>(result)
        assertTrue { result.message.contains("Description") }
    }

    @Test
    fun `valid inputs return success`() {
        val result = TaskValidator.validate("Complete Project", "Finish the implementation plan")
        assertIs<ValidationResult.Success>(result)
    }

    @Test
    fun `whitespace only title returns error`() {
        val result = TaskValidator.validate("   ", "Description")
        assertIs<ValidationResult.Error>(result)
    }
}
