package com.example.routine.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.routine.databinding.ItemClassBinding
import com.example.routine.models.ClassSchedule
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClassAdapter : ListAdapter<ClassSchedule, ClassAdapter.ClassViewHolder>(ClassDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
        val binding = ItemClassBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ClassViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ClassViewHolder(
        private val binding: ItemClassBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        fun bind(classSchedule: ClassSchedule) {
            binding.apply {
                subjectText.text = classSchedule.subject
                timeText.text = "${formatTime(classSchedule.startTime)} - ${formatTime(classSchedule.endTime)}"
                roomText.text = "Room: ${classSchedule.room}"
                teacherText.text = classSchedule.teacher
            }
        }

        private fun formatTime(timestamp: Long): String {
            return timeFormat.format(Date(timestamp))
        }
    }

    private class ClassDiffCallback : DiffUtil.ItemCallback<ClassSchedule>() {
        override fun areItemsTheSame(oldItem: ClassSchedule, newItem: ClassSchedule): Boolean {
            return oldItem.classId == newItem.classId
        }

        override fun areContentsTheSame(oldItem: ClassSchedule, newItem: ClassSchedule): Boolean {
            return oldItem == newItem
        }
    }
} 