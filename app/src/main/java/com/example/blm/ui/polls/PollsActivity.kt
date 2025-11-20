package com.example.blm.ui.polls

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blm.R
import com.example.blm.model.Poll
import com.example.blm.viewmodel.PollsViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PollsActivity : AppCompatActivity() {

    private val vm: PollsViewModel by viewModels()
    private lateinit var adapter: PollAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_polls)

        // Load saved polls
        vm.load(this)

        // Setup RecyclerView
        val rv = findViewById<RecyclerView>(R.id.rvPolls)

        adapter = PollAdapter(
            onVote = { poll, option ->
                vm.vote(this, poll.id, option.id)
            },
            onDelete = { poll ->
                vm.deletePoll(this, poll.id)
            }
        )

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        // FAB → create poll
        val fab = findViewById<FloatingActionButton>(R.id.fabCreatePoll)
        fab.setOnClickListener {
            val i = Intent(this, PollCreateActivity::class.java)
            startActivityForResult(i, REQUEST_CREATE_POLL)
        }

        // Observe list updates
        vm.polls.observe(this) { list ->
            adapter.submitList(list.toList()) // fresh copy for diff updates
        }
    }

    // Receive newly created poll
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CREATE_POLL &&
            resultCode == Activity.RESULT_OK &&
            data != null
        ) {
            val poll = data.getSerializableExtra(EXTRA_NEW_POLL) as? Poll
            if (poll != null) {
                vm.addPoll(this, poll)
            }
        }
    }

    companion object {
        const val REQUEST_CREATE_POLL = 1001
        const val EXTRA_NEW_POLL = "extra_new_poll"
    }
}
