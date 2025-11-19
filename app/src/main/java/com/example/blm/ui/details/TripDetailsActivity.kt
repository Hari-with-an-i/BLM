package com.example.blm.ui.details

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.blm.databinding.ActivityTripDetailsBinding
import com.example.blm.model.Trip
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent
import com.example.blm.ChecklistActivity

class TripDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTripDetailsBinding
    private lateinit var db: FirebaseFirestore
    private var tripId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTripDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // 1. Get the Trip ID passed from the Home Screen
        tripId = intent.getStringExtra(EXTRA_TRIP_ID)

        if (tripId == null) {
            Toast.makeText(this, "Error: No Trip ID loaded", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 2. Setup the Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // 3. Load the Trip Name (so we know which trip we are in)
        loadTripInfo()

        // 4. Setup the 3 Hub Buttons
        setupHubButtons()
    }

    private fun loadTripInfo() {
        db.collection("trips").document(tripId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val trip = document.toObject(Trip::class.java)
                    // Update the big header and the toolbar
                    binding.tvTripTitleHeader.text = trip?.title
                    supportActionBar?.title = trip?.title
                }
            }
            .addOnFailureListener {
                binding.tvTripTitleHeader.text = "Error loading trip"
            }
    }

    private fun setupHubButtons() {
        // Button 1: Checklist
        binding.btnChecklist.setOnClickListener {
            // TODO for Teammate 1: Launch ChecklistActivity here
            val intent = Intent(this, ChecklistActivity::class.java)
            startActivity(intent)
        }

        // Button 2: Polls
        binding.btnPolls.setOnClickListener {
            // TODO for Teammate 2: Launch PollsActivity here
            Toast.makeText(this, "Polls feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Button 3: Gallery
        binding.btnGallery.setOnClickListener {
            binding.btnGallery.setOnClickListener {
                // Launch the GalleryActivity
                val intent = android.content.Intent(this, com.example.blm.ui.gallery.GalleryActivity::class.java)
                // CRITICAL: Pass the tripId so the gallery knows WHICH trip to load
                intent.putExtra("EXTRA_TRIP_ID", tripId)
                startActivity(intent)
            }
        }


    }

    companion object {
        const val EXTRA_TRIP_ID = "EXTRA_TRIP_ID"
    }
}