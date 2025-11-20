package com.example.blm.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.blm.model.Poll
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class PollsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _polls = MutableLiveData<List<Poll>>()
    val polls: LiveData<List<Poll>> = _polls

    private var snapshotListener: ListenerRegistration? = null

    // 1. Load polls for a specific Trip ID (String)
    fun load(tripId: String) {
        snapshotListener?.remove()

        // Path: trips/{tripId}/polls
        val pollsRef = db.collection("trips").document(tripId).collection("polls")

        snapshotListener = pollsRef.addSnapshotListener { snapshots, e ->
            if (e != null) return@addSnapshotListener

            val list = mutableListOf<Poll>()
            if (snapshots != null) {
                for (doc in snapshots) {
                    val poll = doc.toObject(Poll::class.java)
                    list.add(poll)
                }
            }
            _polls.value = list
        }
    }

    // 2. Add a poll (Accepts tripId String)
    fun addPoll(tripId: String, poll: Poll) {
        db.collection("trips").document(tripId).collection("polls")
            .document(poll.id.toString())
            .set(poll)
    }

    // 3. Vote (Accepts tripId String)
    fun vote(tripId: String, pollId: Long, optionId: Long) {
        val pollRef = db.collection("trips").document(tripId)
            .collection("polls").document(pollId.toString())

        db.runTransaction { transaction ->
            val snapshot = transaction.get(pollRef)
            val poll = snapshot.toObject(Poll::class.java) ?: return@runTransaction

            val option = poll.options.find { it.id == optionId }
            if (option != null) {
                option.votes += 1
                transaction.set(pollRef, poll)
            }
        }
    }

    // 4. Delete (Accepts tripId String)
    fun deletePoll(tripId: String, pollId: Long) {
        db.collection("trips").document(tripId).collection("polls")
            .document(pollId.toString())
            .delete()
    }

    override fun onCleared() {
        super.onCleared()
        snapshotListener?.remove()
    }
}