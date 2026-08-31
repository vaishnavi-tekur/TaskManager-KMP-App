package com.example.taskmanagerkmpapp

import com.example.taskmanagerkmpapp.model.Priority
import com.example.taskmanagerkmpapp.repository.TaskRepository
import com.example.taskmanagerkmpapp.ui.Screen
import com.example.taskmanagerkmpapp.ui.TaskViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModelTest {

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
    fun `initial screen is Welcome`() {
        assertEquals(Screen.Welcome, viewModel.uiState.value.currentScreen)
    }

    @Test
    fun `getStarted navigates to TaskList`() {
        viewModel.getStarted()
        assertEquals(Screen.TaskList, viewModel.uiState.value.currentScreen)
    }

    @Test
    fun `navigateToAdd updates screen and clears errors`() {
        viewModel.navigateToAdd()
        assertEquals(Screen.AddTask, viewModel.uiState.value.currentScreen)
    }

    @Test
    fun `saveTask with invalid input updates errorMessage`() {
        viewModel.saveTask("", "", Priority.High)
        assertEquals("Title cannot be empty", viewModel.uiState.value.errorMessage)
    }
}
