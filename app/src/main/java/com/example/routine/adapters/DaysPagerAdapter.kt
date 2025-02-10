package com.example.routine.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.routine.ui.schedule.DailyScheduleFragment

class DaysPagerAdapter(
    activity: FragmentActivity
) : FragmentStateAdapter(activity) {

    private val days = listOf("sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday")

    override fun getItemCount(): Int = days.size

    override fun createFragment(position: Int): Fragment {
        return DailyScheduleFragment.newInstance(days[position])
    }
} 