package com.example.routine.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.routine.R
import com.example.routine.auth.LoginActivity
import com.example.routine.databinding.FragmentProfileBinding
import com.example.routine.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.StorageException
import com.example.routine.MainActivity

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var currentUser: User? = null
    private var isPhoneEdited = false
    private var valueEventListener: ValueEventListener? = null
    private var isLoggingOut = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        loadUserProfile()
    }

    private fun setupClickListeners() {
        binding.editPhoneButton.setOnClickListener {
            binding.phoneEditText.isEnabled = true
            binding.phoneEditText.requestFocus()
        }

        binding.phoneEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val newPhone = s?.toString()?.trim() ?: ""
                val currentPhone = currentUser?.phoneNumber ?: ""
                isPhoneEdited = newPhone != currentPhone
                updateSaveButtonVisibility()
            }
        })

        binding.saveButton.setOnClickListener {
            updateProfile()
        }

        binding.logoutButton.setOnClickListener {
            isLoggingOut = true
            (activity as? MainActivity)?.handleLogout()
        }
    }

    private fun loadUserProfile() {
        val userId = Firebase.auth.currentUser?.uid ?: return
        
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (isAdded && _binding != null) {
                    currentUser = snapshot.getValue(User::class.java)
                    currentUser?.let { user ->
                        updateUI(user)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Don't show error toast if we're logging out
                if (isAdded && context != null && !isLoggingOut) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.update_failed, error.message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        Firebase.database.reference
            .child("users")
            .child(userId)
            .addValueEventListener(valueEventListener!!)
    }

    private fun updateUI(user: User) {
        binding.apply {
            nameText.text = getString(R.string.profile_name, user.name)
            emailText.text = getString(R.string.profile_email, user.email)
            roleText.text = getString(R.string.profile_role, user.role)
            phoneEditText.setText(user.phoneNumber)
        }
    }

    private fun updateSaveButtonVisibility() {
        binding.saveButton.visibility = if (isPhoneEdited) View.VISIBLE else View.GONE
    }

    private fun updateProfile() {
        val userId = Firebase.auth.currentUser?.uid ?: return
        val phoneNumber = binding.phoneEditText.text.toString().trim()

        currentUser?.let { user ->
            val updatedUser = user.copy(phoneNumber = phoneNumber)
            Firebase.database.reference
                .child("users")
                .child(userId)
                .setValue(updatedUser)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            context,
                            R.string.profile_updated,
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.phoneEditText.isEnabled = false
                        isPhoneEdited = false
                        updateSaveButtonVisibility()
                    } else {
                        Toast.makeText(
                            context,
                            getString(R.string.update_failed, task.exception?.message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    override fun onDestroyView() {
        // Remove the listener when the view is destroyed
        valueEventListener?.let { listener ->
            val userId = Firebase.auth.currentUser?.uid
            if (userId != null) {
                Firebase.database.reference
                    .child("users")
                    .child(userId)
                    .removeEventListener(listener)
            }
        }
        _binding = null
        super.onDestroyView()
    }
} 