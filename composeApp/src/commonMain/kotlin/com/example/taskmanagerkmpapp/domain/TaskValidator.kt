package com.example.taskmanagerkmpapp.domain

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

object TaskValidator {
    private const val MIN_TITLE_LENGTH = 3

    fun validate(title: String, description: String): ValidationResult {
        val trimmedTitle = title.trim()
        val trimmedDescription = description.trim()

        return when {
            trimmedTitle.isEmpty() -> ValidationResult.Error("Title cannot be empty")
            trimmedTitle.length < MIN_TITLE_LENGTH -> ValidationResult.Error("Title must be at least $MIN_TITLE_LENGTH characters")
            trimmedDescription.isEmpty() -> ValidationResult.Error("Description cannot be empty")
            else -> ValidationResult.Success
        }
    }
}
