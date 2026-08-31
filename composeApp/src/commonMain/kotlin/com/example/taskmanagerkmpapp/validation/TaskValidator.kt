package com.example.taskmanagerkmpapp.validation

import com.example.taskmanagerkmpapp.model.Priority

object TaskValidator {
    fun validateTask(title: String, description: String): ValidationResult {
        if (title.isBlank()) {
            return ValidationResult.Error("Title cannot be empty")
        }
        if (title.length < 3) {
            return ValidationResult.Error("Title must be at least 3 characters")
        }
        if (description.isBlank()) {
            return ValidationResult.Error("Description cannot be empty")
        }
        return ValidationResult.Success
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
