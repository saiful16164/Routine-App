package com.example.routine.ui.schedule

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.routine.R
import com.example.routine.databinding.DialogEditClassBinding
import com.example.routine.models.ClassSchedule
import com.example.routine.models.Notification
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class EditClassDialog : DialogFragment() {
    private var _binding: DialogEditClassBinding? = null
    private val binding get() = _binding!!
    private var selectedDate: Long = 0
    private var startTime: Long = 0
    private var endTime: Long = 0
    private var classSchedule: ClassSchedule? = null
    private var onSave: ((ClassSchedule) -> Unit)? = null
    private var onDelete: ((ClassSchedule) -> Unit)? = null
    private val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    companion object {
        fun newInstance(
            classSchedule: ClassSchedule? = null,
            onSave: (ClassSchedule) -> Unit,
            onDelete: ((ClassSchedule) -> Unit)? = null
        ): EditClassDialog {
            return EditClassDialog().apply {
                this.classSchedule = classSchedule
                this.onSave = onSave
                this.onDelete = onDelete
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogEditClassBinding.inflate(LayoutInflater.from(context))

        setupDateSelector()
        setupTimeSelectors()
        if (classSchedule != null) {
            setupExistingData()
        } else {
            // Set today's date as default
            val today = Calendar.getInstance()
            selectedDate = today.timeInMillis
            binding.dateEditText.setText(dateFormat.format(Date(selectedDate)))
        }

        return AlertDialog.Builder(requireContext())
            .setTitle(if (classSchedule != null) R.string.edit_class else R.string.add_class)
            .setView(binding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                if (validateAndSave()) {
                    dismiss()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                classSchedule?.let {
                    setNeutralButton(R.string.delete) { _, _ ->
                        onDelete?.invoke(it)
                        dismiss()
                    }
                }
            }
            .create()
    }

    private fun setupDateSelector() {
        binding.dateEditText.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        if (selectedDate > 0) {
            calendar.timeInMillis = selectedDate
        }

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = calendar.timeInMillis
                binding.dateEditText.setText(dateFormat.format(Date(selectedDate)))
                
                // Reset times when date changes
                startTime = 0
                endTime = 0
                binding.startTimeEditText.text = null
                binding.endTimeEditText.text = null
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setupTimeSelectors() {
        binding.startTimeEditText.setOnClickListener { showTimePicker(true) }
        binding.endTimeEditText.setOnClickListener { showTimePicker(false) }
    }

    private fun setupExistingData() {
        classSchedule?.let { schedule ->
            binding.apply {
                subjectEditText.setText(schedule.subject)
                teacherEditText.setText(schedule.teacher)
                roomEditText.setText(schedule.room)
                selectedDate = schedule.date
                startTime = schedule.startTime
                endTime = schedule.endTime
                dateEditText.setText(dateFormat.format(Date(selectedDate)))
                startTimeEditText.setText(timeFormat.format(Date(startTime)))
                endTimeEditText.setText(timeFormat.format(Date(endTime)))
            }
        }
    }

    private fun showTimePicker(isStartTime: Boolean) {
        if (selectedDate == 0L) {
            Toast.makeText(context, R.string.select_date_first, Toast.LENGTH_SHORT).show()
            return
        }

        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                val time = Calendar.getInstance().apply {
                    timeInMillis = selectedDate
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                if (isStartTime) {
                    startTime = time
                    binding.startTimeEditText.setText(timeFormat.format(Date(time)))
                } else {
                    endTime = time
                    binding.endTimeEditText.setText(timeFormat.format(Date(time)))
                }
            },
            currentHour,
            currentMinute,
            false
        ).show()
    }

    private fun validateAndSave(): Boolean {
        val subject = binding.subjectEditText.text.toString().trim()
        val teacher = binding.teacherEditText.text.toString().trim()
        val room = binding.roomEditText.text.toString().trim()

        if (subject.isEmpty() || teacher.isEmpty() || room.isEmpty()) {
            Toast.makeText(context, R.string.all_fields_required, Toast.LENGTH_SHORT).show()
            return false
        }

        val schedule = ClassSchedule(
            classId = classSchedule?.classId ?: UUID.randomUUID().toString(),
            subject = subject,
            teacher = teacher,
            room = room,
            date = selectedDate,
            startTime = startTime,
            endTime = endTime
        )

        onSave?.invoke(schedule)

        if (classSchedule == null) {
            sendNotification(schedule, isUpdate = false)
        } else {
            sendNotification(schedule, isUpdate = true)
        }

        return true
    }

    private fun sendNotification(classSchedule: ClassSchedule, isUpdate: Boolean) {
        val notification = Notification(
            id = UUID.randomUUID().toString(),
            title = if (isUpdate) "Class Updated" else "New Class Added",
            message = buildNotificationMessage(classSchedule, isUpdate),
            timestamp = System.currentTimeMillis(),
            classId = classSchedule.classId,
            type = if (isUpdate) Notification.TYPE_CLASS_UPDATED else Notification.TYPE_CLASS_ADDED
        )

        println("Sending notification: $notification")

        Firebase.database.reference
            .child("notifications")
            .child(notification.id)
            .setValue(notification)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    println("Notification sent successfully")
                } else {
                    println("Failed to send notification: ${task.exception?.message}")
                }
            }
    }

    private fun buildNotificationMessage(classSchedule: ClassSchedule, isUpdate: Boolean): String {
        val dateFormat = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        
        return if (isUpdate) {
            "Class ${classSchedule.subject} has been updated for ${dateFormat.format(Date(classSchedule.date))} " +
            "at ${timeFormat.format(Date(classSchedule.startTime))}"
        } else {
            "New class added for ${classSchedule.subject} on ${dateFormat.format(Date(classSchedule.date))} " +
            "at ${timeFormat.format(Date(classSchedule.startTime))}"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 