package com.example.blm.model

import java.io.Serializable

data class Poll(
    val id: Long = System.nanoTime(),
    val title: String = "",
    val options: MutableList<PollOption> = mutableListOf()
) : Serializable

data class PollOption(
    val id: Long = System.nanoTime(),
    val text: String = "",
    var votes: Int = 0
) : Serializable
