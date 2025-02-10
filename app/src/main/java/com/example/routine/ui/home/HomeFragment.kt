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
        setupFabClick()
    }

    private fun setupViewPager() {
        val pagerAdapter = DaysPagerAdapter(requireActivity())
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.sunday)
                1 -> getString(R.string.monday)
                2 -> getString(R.string.tuesday)
                3 -> getString(R.string.wednesday)
                4 -> getString(R.string.thursday)
                5 -> getString(R.string.friday)
                else -> getString(R.string.saturday)
            }
        }.attach()
    }

    private fun checkUserRole() {
        val userId = Firebase.auth.currentUser?.uid ?: return
        Firebase.database.reference.child("users").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    binding.fabAdd.visibility = if (user?.role == User.ROLE_CR) View.VISIBLE else View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    private fun setupFabClick() {
        binding.fabAdd.setOnClickListener {
            showAddClassDialog()
        }
    }

    private fun showAddClassDialog() {
        val currentDay = days[binding.viewPager.currentItem]
        EditClassDialog.newInstance(
            onSave = { classSchedule ->
                Firebase.database.reference
                    .child("schedule")
                    .child(currentDay)
                    .child(classSchedule.classId)
                    .setValue(classSchedule)
            }
        ).show(childFragmentManager, "add_class")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 