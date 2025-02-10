package com.example.routine.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.routine.adapters.NotificationsAdapter
import com.example.routine.databinding.FragmentNotificationsBinding
import com.example.routine.models.Notification
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
    private var valueEventListener: ValueEventListener? = null
    private val notificationsRef = Firebase.database.reference.child("notifications")

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
        println("Loading notifications...")  // Keep this for debugging in logcat if needed

        Firebase.database.reference
            .child("notifications")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    println("Notification data changed. Count: ${snapshot.childrenCount}")
                    
                    val notifications = snapshot.children.mapNotNull { 
                        it.getValue(Notification::class.java) 
                    }.sortedByDescending { it.timestamp }
                    
                    println("Parsed notifications: ${notifications.size}")
                    
                    if (isAdded && _binding != null) {
                        viewLifecycleOwner.lifecycleScope.launch {
                            adapter.submitList(notifications)
                            binding.emptyText.visibility = 
                                if (notifications.isEmpty()) View.VISIBLE else View.GONE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error loading notifications: ${error.message}")
                    if (isAdded) {
                        context?.let { ctx ->
                            Toast.makeText(
                                ctx,
                                "Error loading notifications: ${error.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            })
    }

    override fun onDestroyView() {
        // Remove the listener when the view is destroyed
        valueEventListener?.let {
            notificationsRef.removeEventListener(it)
        }
        _binding = null
        super.onDestroyView()
    }
} 