package com.example.routine.models

data class Notification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = 0,
    val read: Boolean = false
) 