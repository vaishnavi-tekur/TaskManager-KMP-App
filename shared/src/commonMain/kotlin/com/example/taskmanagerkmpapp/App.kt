package com.example.taskmanagerkmpapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val priority: String,
    val isCompleted: Boolean = false
)

@Composable
@Preview
fun App() {
    val tasks = remember {
        listOf(
            Task(1, "Design login flow", "Create the onboarding screens", "High"),
            Task(2, "Review API contract", "Check the task payload format", "Medium"),
            Task(3, "Write unit tests", "Add coverage for validation rules", "Low")
        )
    }

    var currentScreen by remember { mutableStateOf("welcome") }

    MaterialTheme {
        if (currentScreen == "welcome") {
            WelcomeScreen(onGetStarted = { currentScreen = "taskList" })
        } else {
            TaskListScreen(
                tasks = tasks,
                onBack = { currentScreen = "welcome" }
            )
        }
    }
}

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Welcome to", color = Color.Gray, fontSize = 18.sp)
        Text(
            text = "Task Manager",
            color = Color(0xFF1A237E),
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(onClick = onGetStarted) {
            Text("GET STARTED")
        }
    }
}

@Composable
fun TaskListScreen(tasks: List<Task>, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Tasks",
                color = Color(0xFF1A237E),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${tasks.size} tasks",
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tasks) { task ->
                TaskRow(task = task)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Welcome")
        }
    }
}

@Composable
fun TaskRow(task: Task) {
    val priorityColor = when (task.priority.lowercase()) {
        "high" -> Color(0xFFE53935)
        "medium" -> Color(0xFFFFA000)
        else -> Color(0xFF43A047)
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = null
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(task.title, fontWeight = FontWeight.Bold)
                Text(task.description, color = Color.Gray)
                Text(
                    text = task.priority,
                    color = priorityColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}