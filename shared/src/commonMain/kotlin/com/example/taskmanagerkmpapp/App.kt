package com.example.taskmanagerkmpapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import taskmanagerkmpapp.shared.generated.resources.Res

import com.example.taskmanagerkmpapp.model.Task
import com.example.taskmanagerkmpapp.repository.TaskRepository

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
    MaterialTheme {
        var showTasks by remember { mutableStateOf(value = false) }
        val repository = remember { TaskRepository() }
        var taskList by remember { mutableStateOf(emptyList<Task>()) }

        // Read tasks.json when screen loads
        LaunchedEffect(Unit) {
            try {
                val jsonBytes = Res.readBytes("files/tasks.json")
                val jsonString = jsonBytes.decodeToString()
                taskList = repository.parseTasks(jsonString)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = { showTasks = !showTasks },
                modifier = Modifier.padding(16.dp),
            ) {
                Text(if (showTasks) "Hide Tasks" else "Show Tasks")
            }

            AnimatedVisibility(showTasks) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "My Task List",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn {
                        items(taskList) { task ->
                            Text(
                                text = "• ${task.title} [${if (task.isCompleted) "Done" else "Pending"}]",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(vertical = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}