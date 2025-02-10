package com.example.routine.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.routine.adapters.ClassScheduleAdapter
import com.example.routine.databinding.FragmentDailyScheduleBinding
import com.example.routine.models.ClassSchedule
import com.example.routine.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class DailyScheduleFragment : Fragment() {
    private var _binding: FragmentDailyScheduleBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ClassScheduleAdapter
    private var dayId: String = ""
    private var isCR = false

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
        checkUserRole()
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

    private fun checkUserRole() {
        val userId = Firebase.auth.currentUser?.uid ?: return
        Firebase.database.reference.child("users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    isCR = user?.role == User.ROLE_CR
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    private fun setupRecyclerView() {
        adapter = ClassScheduleAdapter().apply {
            setOnItemClickListener { classSchedule ->
                if (isCR) {
                    showEditDialog(classSchedule)
                }
            }
        }
        binding.recyclerView.adapter = adapter
    }

    private fun loadSchedule() {
        Firebase.database.reference
            .child("schedule")
            .child(dayId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val classes = snapshot.children.mapNotNull { 
                        it.getValue(ClassSchedule::class.java) 
                    }.sortedBy { it.startTime }
                    
                    viewLifecycleOwner.lifecycleScope.launch {
                        adapter.submitList(classes)
                        binding.noClassText.visibility = if (classes.isEmpty()) View.VISIBLE else View.GONE
                        binding.recyclerView.visibility = if (classes.isEmpty()) View.GONE else View.VISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    private fun showEditDialog(classSchedule: ClassSchedule) {
        EditClassDialog.newInstance(
            classSchedule = classSchedule,
            onSave = { updatedClass ->
                saveClass(updatedClass)
            },
            onDelete = { classToDelete ->
                deleteClass(classToDelete)
            }
        ).show(childFragmentManager, "edit_class")
    }

    private fun saveClass(classSchedule: ClassSchedule) {
        Firebase.database.reference
            .child("schedule")
            .child(dayId)
            .child(classSchedule.classId)
            .setValue(classSchedule)
    }

    private fun deleteClass(classSchedule: ClassSchedule) {
        Firebase.database.reference
            .child("schedule")
            .child(dayId)
            .child(classSchedule.classId)
            .removeValue()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 