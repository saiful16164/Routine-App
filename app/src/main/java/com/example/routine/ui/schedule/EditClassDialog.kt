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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

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
    private var isNoClass = false

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

        setupNoClassSwitch()
        setupDateSelector()
        setupTimeSelectors()
        if (classSchedule != null) {
            setupExistingData()
        } else {
            // Set today's date as default
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            selectedDate = today.timeInMillis
            binding.dateEditText.setText(dateFormat.format(Date(selectedDate)))
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (classSchedule == null) R.string.add_class else R.string.edit_class)
            .setView(binding.root)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel, null)

        if (classSchedule != null) {
            dialog.setNeutralButton(R.string.delete) { _, _ ->
                onDelete?.invoke(classSchedule!!)
            }
        }

        val alertDialog = dialog.create()
        alertDialog.setOnShowListener {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (validateAndSave()) {
                    dismiss()
                }
            }
        }

        return alertDialog
    }

    private fun setupNoClassSwitch() {
        binding.noClassSwitch.setOnCheckedChangeListener { _, isChecked ->
            isNoClass = isChecked
            binding.classDetailsContainer.visibility = if (isChecked) View.GONE else View.VISIBLE
        }
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
                noClassSwitch.isChecked = schedule.isNoClass
                classDetailsContainer.visibility = if (schedule.isNoClass) View.GONE else View.VISIBLE
                
                if (!schedule.isNoClass) {
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
        if (isNoClass) {
            val schedule = ClassSchedule(
                classId = classSchedule?.classId ?: UUID.randomUUID().toString(),
                date = selectedDate,
                isNoClass = true
            )
            onSave?.invoke(schedule)
            return true
        }

        val subject = binding.subjectEditText.text.toString().trim()
        val teacher = binding.teacherEditText.text.toString().trim()
        val room = binding.roomEditText.text.toString().trim()

        if (subject.isEmpty() || teacher.isEmpty() || room.isEmpty() || 
            selectedDate == 0L || startTime == 0L || endTime == 0L) {
            Toast.makeText(context, R.string.all_fields_required, Toast.LENGTH_SHORT).show()
            return false
        }

        if (startTime >= endTime) {
            Toast.makeText(context, R.string.invalid_time, Toast.LENGTH_SHORT).show()
            return false
        }

        val schedule = ClassSchedule(
            classId = classSchedule?.classId ?: UUID.randomUUID().toString(),
            subject = subject,
            teacher = teacher,
            room = room,
            date = selectedDate,
            startTime = startTime,
            endTime = endTime,
            isNoClass = false
        )

        onSave?.invoke(schedule)
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 