package com.example.taskmanagerkmpapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskmanagerkmpapp.repository.TaskRepository
import com.example.taskmanagerkmpapp.ui.Screen
import com.example.taskmanagerkmpapp.ui.TaskViewModel
import com.example.taskmanagerkmpapp.ui.TaskListScreen
import com.example.taskmanagerkmpapp.ui.AddTaskScreen

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
                    onSaveTask = { title, desc, priority -> viewModel.saveTask(title, desc, priority) },
                    onBack = { viewModel.navigateBack() }
                )
            }
        }
    }
}

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    val darkBlue = Color(0xFF1A237E)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to",
            color = Color.Gray,
            fontSize = 18.sp
        )
        Text(
            text = "Task Manager",
            color = darkBlue,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = darkBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("GET STARTED", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
