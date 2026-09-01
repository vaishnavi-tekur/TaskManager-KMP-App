package com.example.taskmanagerkmpapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
