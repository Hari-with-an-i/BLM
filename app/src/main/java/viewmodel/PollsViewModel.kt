package com.example.blm.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.blm.model.Poll
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PollsViewModel : ViewModel() {

    private val _polls = MutableLiveData<List<Poll>>(emptyList())
    val polls: LiveData<List<Poll>> get() = _polls

    private val gson = Gson()

    fun load(context: Context) {
        val prefs = context.getSharedPreferences("polls", Context.MODE_PRIVATE)
        val json = prefs.getString("poll_list", null)

        if (json != null) {
            val type = object : TypeToken<List<Poll>>() {}.type
            val list = gson.fromJson<List<Poll>>(json, type)
            _polls.value = list
        }
    }

    fun save(context: Context) {
        val prefs = context.getSharedPreferences("polls", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("poll_list", gson.toJson(_polls.value))
            .apply()
    }

    fun addPoll(context: Context, poll: Poll) {
        val list = _polls.value?.toMutableList() ?: mutableListOf()
        list.add(poll)
        _polls.value = list
        save(context)
    }

    fun deletePoll(context: Context, pollId: Long) {
        val list = _polls.value?.toMutableList() ?: mutableListOf()
        list.removeAll { it.id == pollId }
        _polls.value = list
        save(context)
    }

    fun vote(context: Context, pollId: Long, optionId: Long) {
        val list = _polls.value?.toMutableList() ?: mutableListOf()

        val poll = list.find { it.id == pollId }
        val option = poll?.options?.find { it.id == optionId }

        if (option != null) {
            option.votes += 1
            _polls.value = list
            save(context)
        }
    }
}
