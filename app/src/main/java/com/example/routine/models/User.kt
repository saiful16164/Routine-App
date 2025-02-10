package com.example.routine.models

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "student", // Default role is student
    val isVerified: Boolean = false,
    val verificationCode: String? = null,
    val codeExpiry: Long = 0
) {
    companion object {
        const val ROLE_STUDENT = "student"
        const val ROLE_CR = "cr"
    }
} 