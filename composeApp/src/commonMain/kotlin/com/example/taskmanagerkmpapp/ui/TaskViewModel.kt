package com.example.taskmanagerkmpapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanagerkmpapp.model.Priority
import com.example.taskmanagerkmpapp.model.Task
import com.example.taskmanagerkmpapp.repository.TaskRepository
import com.example.taskmanagerkmpapp.validation.TaskValidator
import com.example.taskmanagerkmpapp.validation.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class Screen {
    object TaskList : Screen()
    object AddTask : Screen()
}

data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val currentScreen: Screen = Screen.TaskList,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val tasks = repository.loadInitialTasks()
            _uiState.update { it.copy(tasks = tasks, isLoading = false) }
        }
    }

    fun navigateToAdd() {
        _uiState.update { it.copy(currentScreen = Screen.AddTask, errorMessage = null) }
    }

    fun navigateBack() {
        _uiState.update { it.copy(currentScreen = Screen.TaskList, errorMessage = null) }
    }

    fun saveTask(title: String, description: String, priority: Priority) {
        val validation = TaskValidator.validateTask(title, description)
        if (validation is ValidationResult.Error) {
            _uiState.update { it.copy(errorMessage = validation.message) }
            return
        }

        val newTask = Task(
            id = (_uiState.value.tasks.maxOfOrNull { it.id } ?: 0) + 1,
            title = title,
            description = description,
            isCompleted = false,
            priority = priority
        )

        _uiState.update { 
            it.copy(
                tasks = it.tasks + newTask,
                currentScreen = Screen.TaskList,
                errorMessage = null
            )
        }
    }

    fun deleteTask(task: Task) {
        _uiState.update { state ->
            state.copy(tasks = state.tasks.filter { it.id != task.id })
        }
    }

    fun toggleTask(task: Task) {
        _uiState.update { state ->
            state.copy(tasks = state.tasks.map {
                if (it.id == task.id) it.copy(isCompleted = !it.isCompleted) else it
            })
        }
    }
}
