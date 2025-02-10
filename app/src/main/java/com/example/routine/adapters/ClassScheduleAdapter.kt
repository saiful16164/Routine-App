package com.example.routine.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.routine.databinding.ItemClassScheduleBinding
import com.example.routine.models.ClassSchedule
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClassScheduleAdapter : ListAdapter<ClassSchedule, ClassScheduleAdapter.ViewHolder>(ClassDiffCallback()) {
    private var onItemClickListener: ((ClassSchedule) -> Unit)? = null

    fun setOnItemClickListener(listener: (ClassSchedule) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemClassScheduleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onItemClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemClassScheduleBinding,
        private val onItemClickListener: ((ClassSchedule) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
        private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        fun bind(classSchedule: ClassSchedule) {
            binding.apply {
                dateText.text = dateFormat.format(Date(classSchedule.date))
                timeText.text = "${formatTime(classSchedule.startTime)} - ${formatTime(classSchedule.endTime)}"
                subjectText.text = classSchedule.subject
                roomText.text = classSchedule.room
                teacherText.text = classSchedule.teacher

                root.setOnClickListener {
                    onItemClickListener?.invoke(classSchedule)
                }
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