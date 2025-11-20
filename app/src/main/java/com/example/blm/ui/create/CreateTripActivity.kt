package com.example.blm.ui.create

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.blm.R
import com.example.blm.databinding.ActivityCreateTripBinding
import com.example.blm.model.Trip
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.app.DatePickerDialog
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

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

        binding.radioGroupTripType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radioGroup) {
                binding.llCollaborators.visibility = View.VISIBLE
            } else {
                binding.llCollaborators.visibility = View.GONE
            }
        }

        binding.btnAddCollaborator.setOnClickListener {
            addCollaboratorField()
        }

        binding.etTripDate.setOnClickListener {
            showDatePickerDialog()
        }

        binding.btnCreateTrip.setOnClickListener {
            saveTripToFirestore()
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                binding.etTripDate.setText(dateFormat.format(selectedDate.time))
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun addCollaboratorField() {
        val textInputLayout = TextInputLayout(this)
        textInputLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        textInputLayout.hint = "Collaborator's Email"
        textInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE)

        val etEmail = TextInputEditText(this)
        etEmail.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        textInputLayout.addView(etEmail)
        binding.llCollaborators.addView(textInputLayout, binding.llCollaborators.childCount - 1)
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

        val memberIds = mutableListOf(currentUser.uid)
        val collaboratorEmails = mutableListOf<String>()

        if (!isSolo) {
            for (i in 0 until binding.llCollaborators.childCount) {
                val view = binding.llCollaborators.getChildAt(i)
                if (view is TextInputLayout) {
                    val editText = view.editText
                    if (editText != null && editText.text.toString().isNotEmpty()) {
                        collaboratorEmails.add(editText.text.toString())
                    }
                }
            }
        }

        if (collaboratorEmails.isNotEmpty()) {
            db.collection("users")
                .whereIn("email", collaboratorEmails)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        memberIds.add(document.id)
                    }
                    createTrip(title, date, isSolo, currentUser.uid, memberIds)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error finding collaborators: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            createTrip(title, date, isSolo, currentUser.uid, memberIds)
        }
    }

    private fun createTrip(title: String, date: String, isSolo: Boolean, createdBy: String, members: List<String>) {
        val tripsRef = db.collection("trips").document()
        val newTripId = tripsRef.id

        val newTrip = Trip(
            id = newTripId,
            title = title,
            date = date,
            isSolo = isSolo,
            createdBy = createdBy,
            members = members
        )

        tripsRef.set(newTrip)
            .addOnSuccessListener {
                Toast.makeText(this, "Trip Created!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error creating trip: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}