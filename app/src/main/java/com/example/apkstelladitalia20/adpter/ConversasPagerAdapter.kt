package com.example.apkstelladitalia20.adpter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.apkstelladitalia20.fragment.ConversasFinalizadasFragment
import com.example.apkstelladitalia20.fragment.ConversasRecentesFragment

class ConversasPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return if (position == 0)
            ConversasRecentesFragment()
        else
            ConversasFinalizadasFragment()
    }
}
