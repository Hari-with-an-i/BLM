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
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FieldValue
import com.example.blm.R

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
            val intent = Intent(this, ChecklistActivity::class.java)
            intent.putExtra(ChecklistActivity.TRIP_ID, tripId)
            startActivity(intent)
        }

        // Button 2: Polls
        binding.btnPolls.setOnClickListener {
            // Open the Polls screen
            val intent = Intent(this, com.example.blm.ui.polls.PollsActivity::class.java)
            if (tripId != null) {
                intent.putExtra(com.example.blm.ui.polls.PollsActivity.TRIP_ID, tripId)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Error: Trip ID is null", Toast.LENGTH_SHORT).show()
            }
        }

        // Button 3: Gallery
        binding.btnGallery.setOnClickListener {
            // Launch the GalleryActivity
            val intent = android.content.Intent(this, com.example.blm.ui.gallery.GalleryActivity::class.java)
            // CRITICAL: Pass the tripId so the gallery knows WHICH trip to load
            intent.putExtra("EXTRA_TRIP_ID", tripId)
            startActivity(intent)
        }

        // Button 4: Share
        binding.btnShare.setOnClickListener {
            showShareDialog()
        }
    }

    private fun showShareDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_share_trip, null)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)

        builder.setView(dialogView)
            .setPositiveButton("Share") { _, _ ->
                val email = etEmail.text.toString().trim()
                if (email.isNotEmpty()) {
                    addCollaborator(email)
                } else {
                    Toast.makeText(this, "Please enter an email address", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }

    private fun addCollaborator(email: String) {
        // 1. Find the user by email
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    Toast.makeText(this, "User with this email not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // 2. Get the user's ID
                val user = querySnapshot.documents[0]
                val userId = user.id

                // 3. Add the user to the trip's members list
                tripId?.let {
                    db.collection("trips").document(it)
                        .update("members", FieldValue.arrayUnion(userId))
                        .addOnSuccessListener {
                            Toast.makeText(this, "User added to the trip", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to add user: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to find user: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    companion object {
        const val EXTRA_TRIP_ID = "EXTRA_TRIP_ID"
    }
}