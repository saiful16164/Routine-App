package com.example.routine.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.routine.MainActivity
import com.example.routine.R
import com.example.routine.databinding.ActivityRegisterBinding
import com.example.routine.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.registerButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val role = if (binding.crRadioButton.isChecked) User.ROLE_CR else User.ROLE_STUDENT

            if (validateInput(name, email, password)) {
                registerUser(name, email, password, role)
            }
        }
    }

    private fun validateInput(name: String, email: String, password: String): Boolean {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun registerUser(name: String, email: String, password: String, role: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = User(
                        uid = auth.currentUser?.uid ?: "",
                        name = name,
                        email = email,
                        role = role
                    )
                    saveUserToDatabase(user)
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.registration_failed, task.exception?.message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun saveUserToDatabase(user: User) {
        val database = Firebase.database.reference
        database.child("users").child(user.uid).setValue(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        R.string.registration_success,
                        Toast.LENGTH_SHORT
                    ).show()
                    startVerification(user.email)
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.registration_failed, task.exception?.message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun startVerification(email: String) {
        val intent = Intent(this, VerifyEmailActivity::class.java)
        intent.putExtra("email", email)
        startActivity(intent)
        finish()
    }
} 