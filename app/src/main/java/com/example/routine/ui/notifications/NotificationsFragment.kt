package com.example.routine.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.routine.adapters.NotificationsAdapter
import com.example.routine.databinding.FragmentNotificationsBinding
import com.example.routine.models.Notification
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment() {
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: NotificationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadNotifications()
    }

    private fun setupRecyclerView() {
        adapter = NotificationsAdapter()
        binding.recyclerView.adapter = adapter
    }

    private fun loadNotifications() {
        val userId = Firebase.auth.currentUser?.uid ?: return
        Firebase.database.reference.child("notifications").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val notifications = snapshot.children.mapNotNull { 
                        it.getValue(Notification::class.java) 
                    }.sortedByDescending { it.timestamp }
                    
                    viewLifecycleOwner.lifecycleScope.launch {
                        adapter.submitList(notifications)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 