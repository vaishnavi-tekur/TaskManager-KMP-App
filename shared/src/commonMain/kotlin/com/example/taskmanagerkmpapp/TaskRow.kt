package com.example.taskmanagerkmpapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
