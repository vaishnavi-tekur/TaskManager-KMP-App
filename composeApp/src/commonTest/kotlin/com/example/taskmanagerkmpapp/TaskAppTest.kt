package com.example.taskmanagerkmpapp

import com.example.taskmanagerkmpapp.model.Priority
import com.example.taskmanagerkmpapp.repository.TaskRepository
import com.example.taskmanagerkmpapp.ui.Screen
import com.example.taskmanagerkmpapp.ui.TaskViewModel
import com.example.taskmanagerkmpapp.domain.TaskValidator
import com.example.taskmanagerkmpapp.domain.ValidationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class TaskAppTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: TaskRepository
    private lateinit var viewModel: TaskViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = TaskRepository()
        viewModel = TaskViewModel(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `validation fails for empty title`() {
        val result = TaskValidator.validate("", "Description")
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun `validation succeeds for valid data`() {
        val result = TaskValidator.validate("Valid Title", "Valid Description")
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `viewModel starts on Welcome screen`() {
        assertEquals(Screen.Welcome, viewModel.uiState.value.currentScreen)
    }

    @Test
    fun `repository adds task and flow updates`() = runTest {
        repository.addTask("New Task", "Description", Priority.High)
        val tasks = repository.tasks.value
        assertEquals(1, tasks.size)
        assertEquals("New Task", tasks[0].title)
    }

    @Test
    fun `viewModel navigates correctly`() {
        viewModel.getStarted()
        assertEquals(Screen.TaskList, viewModel.uiState.value.currentScreen)
        viewModel.navigateToAdd()
        assertEquals(Screen.AddTask, viewModel.uiState.value.currentScreen)
    }
}
