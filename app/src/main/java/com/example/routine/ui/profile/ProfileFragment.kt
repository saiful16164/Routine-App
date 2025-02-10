package com.example.routine.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var currentUser: User? = null
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                binding.profileImage.setImageURI(uri)
                uploadImage(uri)
            }
        }
    }

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
        binding.editImageButton.setOnClickListener {
            openImagePicker()
        }

        binding.saveButton.setOnClickListener {
            updateProfile()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        imagePickerLauncher.launch(intent)
    }

    private fun loadUserProfile() {
        val userId = Firebase.auth.currentUser?.uid ?: return
        Firebase.database.reference
            .child("users")
            .child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    currentUser = snapshot.getValue(User::class.java)
                    currentUser?.let { user ->
                        updateUI(user)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        context,
                        getString(R.string.update_failed, error.message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun updateUI(user: User) {
        binding.apply {
            nameText.text = getString(R.string.profile_name, user.name)
            emailText.text = getString(R.string.profile_email, user.email)
            roleText.text = getString(R.string.profile_role, user.role)
            phoneEditText.setText(user.phoneNumber)

            if (user.profileImageUrl.isNotEmpty()) {
                Glide.with(this@ProfileFragment)
                    .load(user.profileImageUrl)
                    .placeholder(R.drawable.default_profile)
                    .into(profileImage)
            }
        }
    }

    private fun uploadImage(uri: Uri) {
        val userId = Firebase.auth.currentUser?.uid ?: return
        val storageRef = Firebase.storage.reference
            .child("profile_images")
            .child("$userId.jpg")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    updateProfileImageUrl(downloadUri.toString())
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    getString(R.string.update_failed, e.message),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun updateProfileImageUrl(imageUrl: String) {
        val userId = Firebase.auth.currentUser?.uid ?: return
        Firebase.database.reference
            .child("users")
            .child(userId)
            .child("profileImageUrl")
            .setValue(imageUrl)
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

    private fun setupLogoutButton() {
        binding.logoutButton.setOnClickListener {
            Firebase.auth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 