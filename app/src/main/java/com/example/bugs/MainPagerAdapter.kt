package com.example.bugs

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.bugs.ui.authors.AuthorsFragment
import com.example.bugs.ui.game.GameFragment
import com.example.bugs.ui.registration.RegistrationFragment
import com.example.bugs.ui.rules.RulesFragment
import com.example.bugs.ui.settings.SettingsFragment

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            POS_REGISTRATION -> RegistrationFragment()
            POS_GAME -> GameFragment()
            POS_RULES -> RulesFragment()
            POS_AUTHORS -> AuthorsFragment()
            POS_SETTINGS -> SettingsFragment()
            else -> RegistrationFragment()
        }
    }

    companion object {
        const val POS_REGISTRATION = 0
        const val POS_GAME = 2
        const val POS_RULES = 1
        const val POS_AUTHORS = 3
        const val POS_SETTINGS = 4
    }
}