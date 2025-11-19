package com.example.blm.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class GalleryItem(
    var id: String? = null,
    var imageUrl: String? = null, // This will store the Cloudinary URL
    var uploadedBy: String? = null,
    @ServerTimestamp
    var uploadedAt: Date? = null
)