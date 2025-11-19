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

    private lateinit var upcomingAdapter: UpcomingTripsAdapter
    private lateinit var pastAdapter: PastTripsAdapter
    private val upcomingTripsList = ArrayList<Trip>()
    private val pastTripsList = ArrayList<Trip>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setSupportActionBar(binding.toolbar)
        setupRecyclerAdapters()
        setupTabListener()
        setupBottomNavListener()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sign_out -> {
                auth.signOut()
                Toast.makeText(this, "Signed Out", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LandingActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerAdapters() {

        // --- THIS IS THE CHANGE ---
        // We now pass in the click listener lambda
        upcomingAdapter = UpcomingTripsAdapter(upcomingTripsList) { trip ->
            // This is the code that runs when a trip is clicked
            val intent = Intent(this, com.example.blm.ui.details.TripDetailsActivity::class.java)

            // Pass the ID of the clicked trip to the new activity
            intent.putExtra(com.example.blm.ui.details.TripDetailsActivity.EXTRA_TRIP_ID, trip.id)

            startActivity(intent)
        }

        binding.upcomingTripsList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = upcomingAdapter
        }

        // We should do the same for the PastTripsAdapter
        pastAdapter = PastTripsAdapter(pastTripsList) { memory ->
            // This is the code that runs when a memory is clicked
            val intent = Intent(this, com.example.blm.ui.details.TripDetailsActivity::class.java)

            // Pass the ID of the clicked memory
            intent.putExtra(com.example.blm.ui.details.TripDetailsActivity.EXTRA_TRIP_ID, memory.id)

            startActivity(intent)
        }
        binding.pastTripsList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = pastAdapter
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

                upcomingTripsList.clear()
                pastTripsList.clear()

                for (doc in snapshots!!) {
                    val trip = doc.toObject(Trip::class.java)
                    // TODO: Add logic to sort into upcoming vs past
                    upcomingTripsList.add(trip)
                }

                upcomingAdapter.notifyDataSetChanged()
                pastAdapter.notifyDataSetChanged()
            }
    }

    private fun setupTabListener() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    binding.upcomingTripsList.visibility = View.VISIBLE
                    binding.pastTripsList.visibility = View.GONE
                } else {
                    binding.upcomingTripsList.visibility = View.GONE
                    binding.pastTripsList.visibility = View.VISIBLE
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupBottomNavListener() {
        binding.bottomNavView.selectedItemId = R.id.nav_trips
        binding.bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_trips -> true
                R.id.nav_groups -> {
                    Toast.makeText(this, "Groups Clicked (WIP)", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_settings -> {
                    Toast.makeText(this, "Settings Clicked (WIP)", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupFabListener() {
        binding.fab.setOnClickListener {
            val intent = Intent(this, CreateTripActivity::class.java)
            startActivity(intent)
        }
    }
}