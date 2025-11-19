package com.example.blm.model

data class ChecklistDocument(
    var title: String = "",
    var items: List<ChecklistItem> = emptyList(),
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)