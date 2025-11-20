package com.example.blm.ui.polls

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.example.blm.R
import com.example.blm.model.Poll
import com.example.blm.model.PollOption

class PollCreateActivity : AppCompatActivity() {

    private lateinit var llOptions: LinearLayout
    private lateinit var btnAddOption: Button
    private lateinit var etTitle: EditText
    private lateinit var btnCreate: Button

    private val MAX_OPTIONS = 6

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_poll)

        etTitle = findViewById(R.id.etTitle)
        llOptions = findViewById(R.id.llOptions)
        btnAddOption = findViewById(R.id.btnAddOption)
        btnCreate = findViewById(R.id.btnCreate)

        btnAddOption.setOnClickListener {
            val current = llOptions.childCount
            if (current >= MAX_OPTIONS) {
                Toast.makeText(this, "Max $MAX_OPTIONS options allowed", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            addOptionField() // dynamically add one EditText
        }

        btnCreate.setOnClickListener {
            val title = etTitle.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(this, "Enter poll title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Gather option texts from all EditTexts inside llOptions
            val optionTexts = mutableListOf<String>()
            for (i in 0 until llOptions.childCount) {
                val v = llOptions.getChildAt(i)
                if (v is EditText) {
                    val t = v.text.toString().trim()
                    if (t.isNotEmpty()) optionTexts.add(t)
                }
            }

            if (optionTexts.size < 2) {
                Toast.makeText(this, "Enter at least 2 options", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val options = optionTexts.map { text ->
                PollOption(text = text) // PollOption uses nanoTime id by default
            }.toMutableList()

            val poll = Poll(title = title, options = options)

            val intent = Intent().apply {
                putExtra(PollsActivity.EXTRA_NEW_POLL, poll as java.io.Serializable)
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    // helper to add an EditText into llOptions
    private fun addOptionField() {
        val et = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).also {
                it.topMargin = 16   // 16px (OK for now; works fine)
            }
            hint = "Option ${llOptions.childCount + 1}"
        }
        llOptions.addView(et)
    }
}
