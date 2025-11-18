package com.example.blm.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// This is the new, correct version.
// It has "createdBy", "members", "createdAt", and default null values.
data class Trip(
    var id: String? = null,
    var title: String? = null,
    var date: String? = null,
    var createdBy: String? = null,
    var members: List<String>? = null,
    var isSolo: Boolean = true,
    var imageUrl: String? = null,
    @ServerTimestamp
    var createdAt: Date? = null
)