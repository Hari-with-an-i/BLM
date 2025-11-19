package com.example.blm.ui.gallery

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.blm.databinding.ActivityGalleryBinding
import com.example.blm.model.GalleryItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class GalleryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGalleryBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: GalleryAdapter
    private val galleryList = ArrayList<GalleryItem>()
    private var tripId: String? = null

    // --- CONFIGURATION: REPLACE THESE! ---
    private val CLOUD_NAME = "duadkesfl"
    private val UPLOAD_PRESET = "PackIt!"

    // This launcher handles the "Pick Image" intent
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            uploadToCloudinary(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // 1. Get the Trip ID passed from TripDetailsActivity
        tripId = intent.getStringExtra("EXTRA_TRIP_ID")
        if (tripId == null) {
            Toast.makeText(this, "Error: No Trip ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initCloudinary()
        setupToolbar()
        setupRecyclerView()

        // 2. Start listening for images from Firestore
        loadImagesFromFirestore()

        // 3. Handle Upload Button
        binding.fabUpload.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun initCloudinary() {
        // Initialize Cloudinary only once to avoid crashes
        try {
            MediaManager.get()
        } catch (e: Exception) {
            val config = HashMap<String, String>()
            config["cloud_name"] = CLOUD_NAME
            MediaManager.init(this, config)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Trip Gallery"
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = GalleryAdapter(galleryList)
        // Grid with 3 columns
        binding.galleryRecyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.galleryRecyclerView.adapter = adapter
    }

    // --- UPLOAD LOGIC ---
    private fun uploadToCloudinary(uri: Uri) {
        binding.progressBar.visibility = android.view.View.VISIBLE
        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show()

        // Use the MediaManager we initialized to upload
        MediaManager.get().upload(uri)
            .unsigned(UPLOAD_PRESET) // Using the unsigned preset you created
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    // Success! Cloudinary returns the URL.
                    val secureUrl = resultData["secure_url"] as String
                    // Now save that URL to our database
                    saveToFirestore(secureUrl)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    runOnUiThread {
                        binding.progressBar.visibility = android.view.View.GONE
                        Toast.makeText(this@GalleryActivity, "Upload Error: ${error.description}", Toast.LENGTH_LONG).show()
                    }
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }

    // --- DATABASE LOGIC ---
    private fun saveToFirestore(url: String) {
        val newItem = GalleryItem(
            imageUrl = url,
            uploadedBy = auth.currentUser?.uid
        )

        // Add to sub-collection: trips/{tripId}/gallery
        db.collection("trips").document(tripId!!)
            .collection("gallery")
            .add(newItem)
            .addOnSuccessListener {
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this, "Database Error", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun loadImagesFromFirestore() {
        // Listen to the same sub-collection
        db.collection("trips").document(tripId!!)
            .collection("gallery")
            .orderBy("uploadedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener

                galleryList.clear()
                for (doc in snapshots!!) {
                    val item = doc.toObject(GalleryItem::class.java)
                    galleryList.add(item)
                }
                adapter.notifyDataSetChanged()
            }
    }
}