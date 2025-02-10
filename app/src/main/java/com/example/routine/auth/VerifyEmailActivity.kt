package com.example.routine.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.routine.MainActivity
import com.example.routine.R
import com.example.routine.databinding.ActivityVerifyEmailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class VerifyEmailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerifyEmailBinding
    private lateinit var auth: FirebaseAuth
    private val handler = Handler(Looper.getMainLooper())
    private var isCheckingVerification = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        val email = intent.getStringExtra("email") ?: ""
        binding.descriptionText.text = getString(R.string.verification_sent_to, email)

        setupClickListeners()
        sendVerificationEmail()
        startVerificationCheck()
    }

    private fun setupClickListeners() {
        binding.verifyButton.setOnClickListener {
            checkVerificationStatus()
        }

        binding.resendButton.setOnClickListener {
            sendVerificationEmail()
        }
    }

    private fun sendVerificationEmail() {
        val user = auth.currentUser ?: return
        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, R.string.verification_link_sent, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.verification_failed, task.exception?.message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun startVerificationCheck() {
        isCheckingVerification = true
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (isCheckingVerification) {
                    checkVerificationStatus()
                    handler.postDelayed(this, 3000) // Check every 3 seconds
                }
            }
        }, 3000)
    }

    private fun checkVerificationStatus() {
        val user = auth.currentUser
        user?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (user.isEmailVerified) {
                    isCheckingVerification = false
                    updateUserVerificationStatus()
                }
            }
        }
    }

    private fun updateUserVerificationStatus() {
        val userId = auth.currentUser?.uid ?: return
        Firebase.database.reference.child("users").child(userId)
            .updateChildren(mapOf("isVerified" to true))
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, R.string.verification_success, Toast.LENGTH_SHORT).show()
                    startMainActivity()
                }
            }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        isCheckingVerification = false
    }
} 