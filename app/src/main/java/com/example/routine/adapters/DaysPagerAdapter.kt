package com.example.routine.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.routine.ui.schedule.DailyScheduleFragment

class DaysPagerAdapter(
    fragment: Fragment,
    private val days: List<String>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = days.size

    override fun createFragment(position: Int): Fragment {
        return DailyScheduleFragment.newInstance(days[position])
    }
} 