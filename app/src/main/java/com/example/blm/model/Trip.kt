package com.example.blm.model

data class Trip(
    val id: String,
    val title: String,
    val date: String,
    val isSolo: Boolean,
    val group: TripGroup? = null,
    val imageUrl: String? = null // For the Memory Vault
)