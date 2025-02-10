package com.example.routine.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.routine.adapters.ClassAdapter
import com.example.routine.databinding.FragmentDailyScheduleBinding
import com.example.routine.models.ClassSchedule
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class DailyScheduleFragment : Fragment() {
    private var _binding: FragmentDailyScheduleBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ClassAdapter
    private var dayId: String = ""

    companion object {
        private const val ARG_DAY_ID = "day_id"

        fun newInstance(dayId: String): DailyScheduleFragment {
            return DailyScheduleFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DAY_ID, dayId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dayId = arguments?.getString(ARG_DAY_ID) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDailyScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadSchedule()
    }

    private fun setupRecyclerView() {
        adapter = ClassAdapter()
        binding.recyclerView.adapter = adapter
    }

    private fun loadSchedule() {
        val database = Firebase.database.reference
        database.child("schedule").child(dayId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val classes = snapshot.children.mapNotNull { 
                        it.getValue(ClassSchedule::class.java) 
                    }.sortedBy { it.startTime }
                    
                    viewLifecycleOwner.lifecycleScope.launch {
                        adapter.submitList(classes)
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