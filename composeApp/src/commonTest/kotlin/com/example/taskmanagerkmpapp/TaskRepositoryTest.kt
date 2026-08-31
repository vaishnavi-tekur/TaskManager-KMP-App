package com.example.taskmanagerkmpapp

import com.example.taskmanagerkmpapp.model.Priority
import com.example.taskmanagerkmpapp.repository.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TaskRepositoryTest {

    @Test
    fun `addTask adds a new task to the flow`() = runTest {
        val repository = TaskRepository()
        repository.addTask("Test Task", "Test Desc", Priority.High)
        
        val tasks = repository.tasks.value
        assertEquals(1, tasks.size)
        assertEquals("Test Task", tasks[0].title)
    }

    @Test
    fun `deleteTask removes task from the flow`() = runTest {
        val repository = TaskRepository()
        repository.addTask("Test Task", "Test Desc", Priority.High)
        val taskId = repository.tasks.value[0].id
        
        repository.deleteTask(taskId)
        assertEquals(0, repository.tasks.value.size)
    }

    @Test
    fun `toggleTaskCompletion changes completion state`() = runTest {
        val repository = TaskRepository()
        repository.addTask("Test Task", "Test Desc", Priority.High)
        val taskId = repository.tasks.value[0].id
        
        repository.toggleTaskCompletion(taskId)
        assertTrue(repository.tasks.value[0].isCompleted)
        
        repository.toggleTaskCompletion(taskId)
        assertTrue(!repository.tasks.value[0].isCompleted)
    }
}
