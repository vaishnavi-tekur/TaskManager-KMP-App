package com.example.taskmanagerkmpapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanagerkmpapp.model.Priority
import com.example.taskmanagerkmpapp.model.Task
import com.example.taskmanagerkmpapp.repository.TaskRepository
import com.example.taskmanagerkmpapp.domain.TaskValidator
import com.example.taskmanagerkmpapp.domain.ValidationResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen {
    object Welcome : Screen()
    object TaskList : Screen()
    object AddTask : Screen()
}

data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val currentScreen: Screen = Screen.Welcome,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentDate: String = "31.08.2026" // Centralized date management
)

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    init {
        observeTasks()
        loadInitialData()
    }

    private fun observeTasks() {
        viewModelScope.launch {
            repository.tasks.collect { tasks ->
                _uiState.update { it.copy(tasks = tasks) }
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.initialize()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun navigateToAdd() = _uiState.update { it.copy(currentScreen = Screen.AddTask, errorMessage = null) }
    fun navigateBack() = _uiState.update { it.copy(currentScreen = Screen.TaskList, errorMessage = null) }
    fun getStarted() = _uiState.update { it.copy(currentScreen = Screen.TaskList) }

    fun saveTask(title: String, description: String, priority: Priority) {
        val validation = TaskValidator.validate(title, description)
        if (validation is ValidationResult.Error) {
            _uiState.update { it.copy(errorMessage = validation.message) }
            return
        }
        
        repository.addTask(title.trim(), description.trim(), priority)
        _uiState.update { it.copy(currentScreen = Screen.TaskList, errorMessage = null) }
    }

    fun deleteTask(task: Task) {
        repository.deleteTask(task.id)
    }

    fun toggleTask(task: Task) {
        repository.toggleTaskCompletion(task.id)
    }
}
