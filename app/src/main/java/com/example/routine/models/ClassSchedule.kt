package com.example.routine.models

data class ClassSchedule(
    val classId: String = "",
    val subject: String = "",
    val teacher: String = "",
    val room: String = "",
    val date: Long = 0,
    val startTime: Long = 0,
    val endTime: Long = 0,
    val isNoClass: Boolean = false
) 