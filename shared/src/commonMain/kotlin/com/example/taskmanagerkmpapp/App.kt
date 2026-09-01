package com.example.taskmanagerkmpapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    var currentScreen by remember { mutableStateOf("taskList") }
    var tasks by remember {
        mutableStateOf(
            listOf(
                Task(1, "Complete Project Proposal", "Finalize the draft for the TaskManager project and send it to the team.", "High"),
                Task(2, "Buy Groceries", "Milk, Eggs, Bread, and Fruits.", "Medium"),
                Task(3, "Workout", "Morning cardio and strength training.", "Low"),
                Task(4, "Call Mom", "Catch up with family in the evening.", "Medium")
            )
        )
    }

    MaterialTheme {
        when (currentScreen) {
            "taskList" -> TaskListScreen(
                tasks = tasks,
                onAddTaskClick = { currentScreen = "addTask" },
                onToggleTask = { taskId ->
                    tasks = tasks.map { task ->
                        if (task.id == taskId) task.copy(isCompleted = !task.isCompleted) else task
                    }
                },
                onDeleteTask = { taskId ->
                    tasks = tasks.filter { it.id != taskId }
                }
            )
            "addTask" -> AddTaskScreen(
                onSave = { title, description, priority ->
                    if (title.isNotBlank() && description.isNotBlank()) {
                        tasks = tasks + Task(
                            id = (tasks.maxOfOrNull { it.id } ?: 0) + 1,
                            title = title,
                            description = description,
                            priority = priority
                        )
                        currentScreen = "taskList"
                    }
                },
                onBack = { currentScreen = "taskList" }
            )
            else -> TaskListScreen(
                tasks = tasks,
                onAddTaskClick = { currentScreen = "addTask" },
                onToggleTask = { taskId ->
                    tasks = tasks.map { task ->
                        if (task.id == taskId) task.copy(isCompleted = !task.isCompleted) else task
                    }
                },
                onDeleteTask = { taskId ->
                    tasks = tasks.filter { it.id != taskId }
                }
            )
        }
    }
}

@Composable
fun TaskListScreen(
    tasks: List<Task>,
    onAddTaskClick: () -> Unit,
    onToggleTask: (Int) -> Unit,
    onDeleteTask: (Int) -> Unit,
) {
    val darkBlue = Color(0xFF1A237E)
    val pink = Color(0xFFF06292)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTaskClick,
                containerColor = pink,
                contentColor = Color.White,
                shape = RoundedCornerShape(50),
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(darkBlue)
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("My Tasks", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        Text("01.09.2026", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.size(70.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("${tasks.size}", color = darkBlue, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Text("Tasks", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tasks) { task ->
                    TaskRow(
                        task = task,
                        onToggleTask = onToggleTask,
                        onDeleteTask = onDeleteTask
                    )
                }
            }
        }
    }
}

@Composable
fun AddTaskScreen(onSave: (String, String, String) -> Unit, onBack: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("High") }
    var showError by remember { mutableStateOf(false) }
    val darkBlue = Color(0xFF1A237E)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Add New Task", color = darkBlue, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 32.dp))

        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                if (showError && title.isNotBlank()) showError = false
            },
            label = { Text("Task Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = showError
        )

        if (showError) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("✓", color = Color(0xFF15803D), fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Text("Please add a clear title and description", color = Color(0xFF166534))
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Task Description") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false
        )

        Spacer(Modifier.height(24.dp))

        Box(Modifier.fillMaxWidth()) {
            var expanded by remember { mutableStateOf(false) }
            Column {
                Text("Select Priority", color = Color.Gray, fontSize = 14.sp)
                TextButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(priority, color = Color.Black)
                        Text("▼", color = Color.Gray)
                    }
                }
                HorizontalDivider(color = Color.Gray, thickness = 1.dp)
            }
            DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                listOf("High", "Medium", "Low").forEach { item ->
                    DropdownMenuItem(text = { Text(item) }, onClick = { priority = item; expanded = false })
                }
            }
        }

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = {
                if (title.isBlank() || description.isBlank()) {
                    showError = true
                } else {
                    showError = false
                    onSave(title, description, priority)
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = darkBlue),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("SAVE TASK", color = Color.White, fontWeight = FontWeight.Bold)
        }

        TextButton(onClick = onBack) { Text("Cancel", color = Color.Gray) }
    }
}

@Composable
fun TaskRow(
    task: Task,
    onToggleTask: (Int) -> Unit,
    onDeleteTask: (Int) -> Unit,
) {
    val priorityColor = when (task.priority.lowercase()) {
        "high" -> Color.Red
        "medium" -> Color(0xFFFFA000)
        else -> Color(0xFF4CAF50)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleTask(task.id) }
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(task.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text(task.description, color = Color.Gray, fontSize = 12.sp)
                Text(
                    task.priority,
                    color = priorityColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            IconButton(onClick = { onDeleteTask(task.id) }) {
                Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFFFCDD2))
            }
        }
    }
}