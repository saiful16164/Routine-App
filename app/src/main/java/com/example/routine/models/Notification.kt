package com.example.routine.models

data class Notification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false,
    val type: String = TYPE_CLASS_ADDED,
    val classId: String = ""
) {
    companion object {
        const val TYPE_CLASS_ADDED = "class_added"
        const val TYPE_CLASS_UPDATED = "class_updated"
        const val TYPE_CLASS_DELETED = "class_deleted"
    }
} 