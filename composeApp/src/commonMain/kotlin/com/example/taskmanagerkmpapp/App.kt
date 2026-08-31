package com.example.taskmanagerkmpapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskmanagerkmpapp.repository.TaskRepository
import com.example.taskmanagerkmpapp.ui.Screen
import com.example.taskmanagerkmpapp.ui.TaskViewModel
import com.example.taskmanagerkmpapp.ui.screens.AddTaskScreen
import com.example.taskmanagerkmpapp.ui.screens.TaskListScreen

@Composable
fun App() {
    val repository = remember { TaskRepository() }
    val viewModel: TaskViewModel = viewModel { TaskViewModel(repository) }
    val uiState by viewModel.uiState.collectAsState()

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (val screen = uiState.currentScreen) {
                is Screen.TaskList -> TaskListScreen(
                    tasks = uiState.tasks,
                    onAddTaskClick = { viewModel.navigateToAdd() },
                    onDeleteTask = { viewModel.deleteTask(it) },
                    onToggleTask = { viewModel.toggleTask(it) }
                )
                is Screen.AddTask -> AddTaskScreen(
                    errorMessage = uiState.errorMessage,
                    onSaveTask = { title, desc, priority ->
                        viewModel.saveTask(title, desc, priority)
                    },
                    onBack = { viewModel.navigateBack() }
                )
            }
        }
    }
}
