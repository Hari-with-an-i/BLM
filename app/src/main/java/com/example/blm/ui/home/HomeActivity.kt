package com.example.blm.ui.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.example.blm.R
import com.example.blm.databinding.ActivityHomeBinding
import com.example.blm.model.Trip
import com.example.blm.model.TripGroup

// TODO: Import your other activity classes when you create them
// import com.example.blm.ui.groups.GroupsActivity
// import com.example.blm.ui.settings.SettingsActivity
// import com.example.blm.ui.create.CreateTripActivity

class HomeActivity : AppCompatActivity() {

    // This property will let us access all views in activity_home.xml
    private lateinit var binding: ActivityHomeBinding

    // --- Dummy Data ---
    // We will replace this with live Firestore data later
    private val sampleGroups = listOf(
        TripGroup("g1", "Family"),
        TripGroup("g2", "College Crew")
    )
    private val sampleTrips = listOf(
        Trip(
            id = "1",
            title = "Paris 2026",
            date = "Sept 10-17, 2026",
            isSolo = false,
            group = sampleGroups[0]
        ),
        Trip(
            id = "2",
            title = "Solo Backpacking",
            date = "Oct 2025",
            isSolo = true,
            group = null
        ),
        Trip(
            id = "3",
            title = "NY with Friends",
            date = "Jan 12-15, 2027",
            isSolo = false,
            group = sampleGroups[1]
        )
    )
    private val sampleMemories = listOf(
        Trip("m1", "Japan 2024", "May 5-20, 2024", false, sampleGroups[1], "url_to_image"),
        Trip("m2", "Local Camping", "Aug 2024", true, null, "url_to_image")
    )
    // --- End of Dummy Data ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using View Binding
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root) // Set the content view to the root of the binding

        // Set our custom toolbar as the app's action bar
        setSupportActionBar(binding.toolbar)

        // Call our setup functions
        setupRecyclerAdapters()
        setupTabListener()
        setupBottomNavListener()
        setupFabListener()
    }

    private fun setupRecyclerAdapters() {
        // Setup Upcoming Trips List
        val upcomingAdapter = UpcomingTripsAdapter(sampleTrips)
        binding.upcomingTripsList.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = upcomingAdapter
        }

        // Setup Past Trips (Memory Vault) List
        val pastAdapter = PastTripsAdapter(sampleMemories)
        binding.pastTripsList.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = pastAdapter
        }
    }

    private fun setupTabListener() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    // Show "Upcoming" list, hide "Past"
                    binding.upcomingTripsList.visibility = View.VISIBLE
                    binding.pastTripsList.visibility = View.GONE
                } else {
                    // Show "Past" list, hide "Upcoming"
                    binding.upcomingTripsList.visibility = View.GONE
                    binding.pastTripsList.visibility = View.VISIBLE
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupBottomNavListener() {
        // Set "Trips" as selected by default
        binding.bottomNavView.selectedItemId = R.id.nav_trips

        binding.bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_trips -> {
                    // We are already here, do nothing
                    true
                }
                R.id.nav_groups -> {
                    // TODO: Launch GroupsActivity
                    // val intent = Intent(this, GroupsActivity::class.java)
                    // startActivity(intent)
                    Toast.makeText(this, "Groups Clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_settings -> {
                    // TODO: Launch SettingsActivity
                    // val intent = Intent(this, SettingsActivity::class.java)
                    // startActivity(intent)
                    Toast.makeText(this, "Settings Clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupFabListener() {
        binding.fab.setOnClickListener {
            // TODO: Launch CreateTripActivity
            // val intent = Intent(this, CreateTripActivity::class.java)
            // startActivity(intent)
            Toast.makeText(this, "Create Trip Clicked", Toast.LENGTH_SHORT).show()
        }
    }
}