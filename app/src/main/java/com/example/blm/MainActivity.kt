package com.example.blm

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blm.databinding.ActivityMainBinding
import com.example.blm.model.Trip
import com.example.blm.ui.create.CreateTripActivity
import com.example.blm.ui.home.PastTripsAdapter
import com.example.blm.ui.home.UpcomingTripsAdapter
import com.google.android.material.tabs.TabLayout
// --- NEW IMPORTS (NON-KTX) ---
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var tripsAdapter: UpcomingTripsAdapter
    private val tripsList = ArrayList<Trip>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setSupportActionBar(binding.toolbar)
        setupRecyclerAdapters()
        setupFabListener()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            loadTripsFromFirestore()
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                auth.signOut()
                Toast.makeText(this, "Signed Out", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerAdapters() {
        tripsAdapter = UpcomingTripsAdapter(tripsList) { trip ->
            val intent = Intent(this, com.example.blm.ui.details.TripDetailsActivity::class.java)
            intent.putExtra(com.example.blm.ui.details.TripDetailsActivity.EXTRA_TRIP_ID, trip.id)
            startActivity(intent)
        }

        binding.tripsList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = tripsAdapter
        }
    }

    private fun loadTripsFromFirestore() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Log.e("MainActivity", "User not logged in, cannot load trips.")
            return
        }

        db.collection("trips")
            .whereArrayContains("members", currentUserId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("MainActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }

                tripsList.clear()

                for (doc in snapshots!!) {
                    val trip = doc.toObject(Trip::class.java)
                    tripsList.add(trip)
                }

                tripsAdapter.notifyDataSetChanged()
            }
    }

    private fun setupFabListener() {
        binding.fab.setOnClickListener {
            val intent = Intent(this, CreateTripActivity::class.java)
            startActivity(intent)
        }
    }
}