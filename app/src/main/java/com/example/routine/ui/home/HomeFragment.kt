package com.example.routine.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.routine.R
import com.example.routine.adapters.DaysPagerAdapter
import com.example.routine.databinding.FragmentHomeBinding
import com.example.routine.models.User
import com.example.routine.ui.schedule.EditClassDialog
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val days = listOf("sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday")
    private var valueEventListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        checkUserRole()
    }

    private fun setupViewPager() {
        val adapter = DaysPagerAdapter(this, days)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = getString(
                when (days[position]) {
                    "sunday" -> R.string.sunday
                    "monday" -> R.string.monday
                    "tuesday" -> R.string.tuesday
                    "wednesday" -> R.string.wednesday
                    "thursday" -> R.string.thursday
                    "friday" -> R.string.friday
                    else -> R.string.saturday
                }
            )
        }.attach()
    }

    private fun checkUserRole() {
        val userId = Firebase.auth.currentUser?.uid ?: return
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (isAdded && _binding != null) {  // Check if fragment is still attached
                    val user = snapshot.getValue(User::class.java)
                    binding.fabAdd.visibility = if (user?.role == User.ROLE_CR) View.VISIBLE else View.GONE

                    binding.fabAdd.setOnClickListener {
                        val position = binding.viewPager.currentItem
                        showAddClassDialog(days[position])
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        }

        Firebase.database.reference
            .child("users")
            .child(userId)
            .addValueEventListener(valueEventListener!!)
    }

    private fun showAddClassDialog(dayId: String) {
        EditClassDialog.newInstance(
            onSave = { classSchedule ->
                Firebase.database.reference
                    .child("schedule")
                    .child(dayId)
                    .child(classSchedule.classId)
                    .setValue(classSchedule)
            }
        ).show(childFragmentManager, "add_class")
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