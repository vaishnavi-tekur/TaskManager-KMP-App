package com.example.taskmanagerkmpapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform