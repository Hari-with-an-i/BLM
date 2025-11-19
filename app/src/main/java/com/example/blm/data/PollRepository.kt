package com.example.blm.data

import android.content.Context
import com.example.blm.model.Poll
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PollRepository {
    private const val PREFS = "blm_prefs"
    private const val KEY_POLLS = "key_polls"
    private val gson = Gson()

    fun loadPolls(context: Context): MutableList<Poll> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_POLLS, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Poll>>() {}.type
        return try {
            gson.fromJson(json, type) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun savePolls(context: Context, polls: List<Poll>) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = gson.toJson(polls)
        prefs.edit().putString(KEY_POLLS, json).apply()
    }
}
