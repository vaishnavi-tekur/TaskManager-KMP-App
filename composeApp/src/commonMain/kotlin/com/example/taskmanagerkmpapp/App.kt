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
import com.example.taskmanagerkmpapp.ui.TaskListScreen
import com.example.taskmanagerkmpapp.ui.AddTaskScreen
import com.example.taskmanagerkmpapp.ui.WelcomeScreen

@Composable
fun App() {
    val repository = remember { TaskRepository() }
    val viewModel: TaskViewModel = viewModel { TaskViewModel(repository) }
    val uiState by viewModel.uiState.collectAsState()

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when (uiState.currentScreen) {
                is Screen.Welcome -> WelcomeScreen(
                    onGetStarted = { viewModel.getStarted() }
                )
                is Screen.TaskList -> TaskListScreen(
                    tasks = uiState.tasks,
                    currentDate = uiState.currentDate,
                    onAddTaskClick = { viewModel.navigateToAdd() },
                    onDeleteTask = { viewModel.deleteTask(it) },
                    onToggleTask = { viewModel.toggleTask(it) }
                )
                is Screen.AddTask -> AddTaskScreen(
                    errorMessage = uiState.errorMessage,
                    onSaveTask = { title, description, priority -> 
                        viewModel.saveTask(title, description, priority) 
                    },
                    onBack = { viewModel.navigateBack() }
                )
            }
        }
    }
}
