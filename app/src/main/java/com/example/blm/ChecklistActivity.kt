package com.example.blm

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blm.databinding.ActivityChecklistBinding
import java.io.OutputStream
import com.example.blm.model.ChecklistItem
import com.example.blm.model.ChecklistDocument
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
class ChecklistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChecklistBinding
    private lateinit var adapter: ChecklistAdapter
    private val checklistItems = mutableListOf<ChecklistItem>(ChecklistItem())
    private lateinit var tripId: String

    // 1. Initialize Firestore and Auth instances
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChecklistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get Trip ID from intent
        tripId = intent.getStringExtra(TRIP_ID) ?: ""
        if (tripId.isBlank()) {
            throw IllegalStateException("Trip ID cannot be null or blank")
        }

        setupToolbar()
        loadChecklistFromFirestore()
        setupRecyclerView()

        if (checklistItems.isEmpty()) {
            adapter.addItem()
        }

        binding.btnAddItem.setOnClickListener {
            adapter.addItem()
        }

        binding.btnSaveImage.setOnClickListener {
            val bitmap = getBitmapFromView(binding.cardChecklist)
            saveImageToGallery(bitmap)
            Toast.makeText(this, "Image saved, data will auto-save on back press.", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 2. Attach the click listener to initiate Firestore save before closing
        binding.toolbar.setNavigationOnClickListener {
            saveChecklistToFirestore() // Initiate asynchronous save
            finish()                   // Close the activity immediately
        }
    }

    // 3. Firestore Saving Function (moved from previous context)
    private fun saveChecklistToFirestore() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Error: You must be logged in to save.", Toast.LENGTH_SHORT).show()
            return
        }

        val title = binding.etTitle.text.toString().trim()

        // Filter out items where the text is blank
        val actualItems = checklistItems.filter { it.text.isNotBlank() }

        if (title.isEmpty() || actualItems.isEmpty()) {
            // User feedback for not saving empty lists
            Toast.makeText(this, "List not saved. Please fill in Title and Items.", Toast.LENGTH_SHORT).show()
            return
        }

        val checklistDocument = ChecklistDocument(
            title = title,
            items = actualItems,
            userId = user.uid,
            tripId = tripId
        )

        // Save to Firestore under the user's specific subcollection
        db.collection("trips").document(tripId).collection("checklists")
            .add(checklistDocument)
            .addOnSuccessListener {
                // Only show a Toast for success, but don't hold up the finish() command
                Toast.makeText(this, "Checklist saved automatically!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving list: Check connection.", Toast.LENGTH_LONG).show()
                Log.e("Firestore", "Error writing document on back press", e)
            }
    }
//    private fun loadChecklistFromFirestore() {
//        val user = auth.currentUser
//        if (user == null) {
//            Toast.makeText(this, "Error: Cannot load. User not logged in.", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        db.collection("trips").document(tripId).collection("checklists")
//            // Order by timestamp to get the latest one first
//            .orderBy("timestamp", Query.Direction.DESCENDING)
//            .limit(1)
//            .get()
//            .addOnSuccessListener { querySnapshot ->
//                if (querySnapshot.isEmpty) {
//                    // No checklist found: Start fresh with one blank item
//                    Toast.makeText(this, "No saved list found. Starting new.", Toast.LENGTH_SHORT).show()
//                    adapter.addItem()
//                    return@addOnSuccessListener
//                }
//
//                // Get the document and convert it to our data class
//                val document = querySnapshot.documents[0]
//                val loadedChecklist = document.toObject<ChecklistDocument>()
//
//                if (loadedChecklist != null) {
//                    // 1. Update the Title
//                    binding.etTitle.setText(loadedChecklist.title)
//
//                    // 2. Update the Checklist Items
//                    checklistItems.clear()
//                    checklistItems.addAll(loadedChecklist.items)
//
//                    // Add one blank item at the end so the user can easily add more
//                    if (loadedChecklist.items.isNotEmpty()) {
//                        checklistItems.add(ChecklistItem())
//                    }
//
//                    // 3. Notify the Adapter to redraw the list with the new data
//                    adapter.notifyDataSetChanged()
//
//                    Toast.makeText(this, "Checklist loaded successfully!", Toast.LENGTH_SHORT).show()
//                }
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(this, "Error loading checklist.", Toast.LENGTH_LONG).show()
//                Log.e("Firestore", "Error loading document", e)
//            }
//    }

    private fun loadChecklistFromFirestore() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Error: Cannot load. User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("trips").document(tripId).collection("checklists")
            // Order by timestamp to get the latest one first
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    // No checklist found: Start fresh with one blank item
                    Toast.makeText(this, "No saved list found. Starting new.", Toast.LENGTH_SHORT).show()
                    adapter.addItem()
                    return@addOnSuccessListener
                }

                // Get the document and convert it to our data class
                val document = querySnapshot.documents[0]
                val loadedChecklist = document.toObject<ChecklistDocument>()

                if (loadedChecklist != null) {
                    // 1. Update the Title
                    binding.etTitle.setText(loadedChecklist.title)

                    // 2. Update the Checklist Items
                    checklistItems.clear()
                    checklistItems.addAll(loadedChecklist.items)

                    // Add one blank item at the end so the user can easily add more
                    if (loadedChecklist.items.isNotEmpty()) {
                        checklistItems.add(ChecklistItem())
                    }

                    // 3. Notify the Adapter to redraw the list with the new data
                    adapter.notifyDataSetChanged()

                    Toast.makeText(this, "Checklist loaded successfully!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading checklist.", Toast.LENGTH_LONG).show()
                Log.e("Firestore", "Error loading document", e)
            }
    }
    companion object {
        const val TRIP_ID = "trip_id"
    }

    private fun setupRecyclerView() {
        adapter = ChecklistAdapter(checklistItems)
        binding.rvChecklist.layoutManager = LinearLayoutManager(this)
        binding.rvChecklist.adapter = adapter
    }

    // Function to convert a View to Bitmap
    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return bitmap
    }

    // Function to save Bitmap to Gallery
    private fun saveImageToGallery(bitmap: Bitmap) {
        val filename = "Trip_Checklist_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        var imageUri: android.net.Uri? = null

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val contentResolver = applicationContext.contentResolver

        try {
            imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            imageUri?.let { uri ->
                fos = contentResolver.openOutputStream(uri)

                fos?.let { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && imageUri != null) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                contentResolver.update(imageUri, contentValues, null, null)
            }

            Toast.makeText(this, "Checklist saved to Gallery!", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("ImageSave", "Failed to save image", e)
        } finally {
            fos?.close()
        }
    }
}