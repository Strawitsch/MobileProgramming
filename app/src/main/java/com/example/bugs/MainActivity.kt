package com.example.bugs

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.bugs.ui.authors.AuthorsFragment
import com.example.bugs.ui.registration.RegistrationFragment
import com.example.bugs.ui.rules.RulesFragment
import com.example.bugs.ui.settings.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.bugs.model.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, RegistrationFragment())
                .commit()
        }

        bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_registration -> RegistrationFragment()
                R.id.nav_rules -> RulesFragment()
                R.id.nav_authors -> AuthorsFragment()
                R.id.nav_settings -> SettingsFragment()
                else -> return@setOnItemSelectedListener false
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commit()
            true
        }
    }
}