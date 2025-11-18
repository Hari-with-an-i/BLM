package com.example.blm.ui.create

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.blm.databinding.ActivityCreateTripBinding
import com.example.blm.model.Trip
// --- NEW IMPORTS (NON-KTX) ---
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateTripActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateTripBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore // This is the non-KTX class

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTripBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- NEW INITIALIZATION (NON-KTX) ---
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnCreateTrip.setOnClickListener {
            saveTripToFirestore()
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun saveTripToFirestore() {
        val title = binding.etTripTitle.text.toString()
        val date = binding.etTripDate.text.toString()
        val isSolo = binding.radioSolo.isChecked
        val currentUser = auth.currentUser

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
            return
        }
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val newTrip = Trip(
            title = title,
            date = date,
            isSolo = isSolo,
            createdBy = currentUser.uid,
            members = listOf(currentUser.uid)
        )

        db.collection("trips")
            .add(newTrip)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Trip Created!", Toast.LENGTH_SHORT).show()
                val newTripId = documentReference.id
                db.collection("trips").document(newTripId).update("id", newTripId)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error creating trip: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}