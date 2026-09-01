package com.example.taskmanagerkmpapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskmanagerkmpapp.model.Priority
import com.example.taskmanagerkmpapp.model.Task

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
        Text(text = "Welcome to", color = Color.Gray, fontSize = 18.sp)
        Text(text = "Task Manager", color = darkBlue, fontSize = 36.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onGetStarted,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = darkBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("GET STARTED", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TaskListScreen(
    tasks: List<Task>,
    currentDate: String,
    onAddTaskClick: () -> Unit,
    onDeleteTask: (Task) -> Unit,
    onToggleTask: (Task) -> Unit
) {
    val darkBlue = Color(0xFF1A237E)
    val pink = Color(0xFFF06292)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTaskClick,
                containerColor = pink,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxWidth().background(darkBlue).padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("My Tasks", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        Text(currentDate, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
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
                modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tasks) { task ->
                    TaskItem(task, { onDeleteTask(task) }, { onToggleTask(task) })
                }
            }
        }
    }
}

@Composable
fun TaskItem(task: Task, onDelete: () -> Unit, onToggle: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(task.isCompleted, { onToggle() })
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(task.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text(task.description, color = Color.Gray, fontSize = 12.sp)
                Text(
                    task.priority.name,
                    color = when (task.priority) {
                        Priority.High -> Color.Red
                        Priority.Medium -> Color(0xFFFFA000)
                        Priority.Low -> Color(0xFF4CAF50)
                    },
                    fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp)
                )
            }
            IconButton(onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFFFCDD2))
            }
        }
    }
}

@Composable
fun AddTaskScreen(errorMessage: String?, onSaveTask: (String, String, Priority) -> Unit, onBack: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.High) }
    var expanded by remember { mutableStateOf(false) }
    val darkBlue = Color(0xFF1A237E)

    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Add New Task", color = darkBlue, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 32.dp))
        if (errorMessage != null) Text(errorMessage, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        TextField(
            value = title, onValueChange = { title = it }, label = { Text("Task Title") }, modifier = Modifier.fillMaxWidth(),
            isError = errorMessage != null && title.isBlank(),
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = darkBlue, unfocusedIndicatorColor = Color.Gray)
        )
        Spacer(Modifier.height(16.dp))
        TextField(
            value = description, onValueChange = { description = it }, label = { Text("Task Description") }, modifier = Modifier.fillMaxWidth(),
            isError = errorMessage != null && description.isBlank(),
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = darkBlue, unfocusedIndicatorColor = Color.Gray)
        )
        Spacer(Modifier.height(24.dp))
        Box(Modifier.fillMaxWidth()) {
            Column {
                Text("Select Priority", color = Color.Gray, fontSize = 14.sp)
                TextButton({ expanded = true }, Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(priority.name, color = Color.Black)
                        Text("▼", color = Color.Gray)
                    }
                }
                HorizontalDivider(color = Color.Gray, thickness = 1.dp)
            }
            DropdownMenu(expanded, { expanded = false }) {
                Priority.entries.forEach { p -> DropdownMenuItem(text = { Text(p.name) }, onClick = { priority = p; expanded = false }) }
            }
        }
        Spacer(Modifier.height(48.dp))
        Button({ onSaveTask(title, description, priority) }, Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = darkBlue), shape = RoundedCornerShape(4.dp)) {
            Text("SAVE TASK", color = Color.White, fontWeight = FontWeight.Bold)
        }
        TextButton(onBack) { Text("Cancel", color = Color.Gray) }
    }
}
