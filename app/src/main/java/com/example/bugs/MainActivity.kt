package com.example.bugs

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.viewPager)
        bottomNav = findViewById(R.id.bottom_navigation)

        viewPager.adapter = MainPagerAdapter(this)
        viewPager.setPageTransformer { page, position ->
            page.alpha = 1f - 0.2f * abs(position)
        }

        val startPos = if (isUserRegistered()) {
            MainPagerAdapter.POS_GAME
        } else {
            MainPagerAdapter.POS_REGISTRATION
        }
        viewPager.setCurrentItem(startPos, false)
        bottomNav.selectedItemId = positionToMenuId(startPos)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == MainPagerAdapter.POS_GAME && !isUserRegistered()) {
                    Toast.makeText(
                        this@MainActivity,
                        "Сначала зарегистрируйтесь на вкладке «Игрок»",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewPager.setCurrentItem(MainPagerAdapter.POS_REGISTRATION, true)
                    return
                }
                bottomNav.selectedItemId = positionToMenuId(position)
            }
        })

        bottomNav.setOnItemSelectedListener { item ->
            val position = menuIdToPosition(item.itemId)
            if (position == MainPagerAdapter.POS_GAME && !isUserRegistered()) {
                Toast.makeText(this, "Сначала зарегистрируйтесь", Toast.LENGTH_SHORT).show()
                bottomNav.selectedItemId = positionToMenuId(viewPager.currentItem)
                return@setOnItemSelectedListener false
            }
            viewPager.setCurrentItem(position, true)
            true
        }
    }

    private fun isUserRegistered(): Boolean {
        val prefs = getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_registered", false)
    }

    private fun positionToMenuId(position: Int): Int = when (position) {
        MainPagerAdapter.POS_REGISTRATION -> R.id.nav_registration
        MainPagerAdapter.POS_GAME -> R.id.nav_game
        MainPagerAdapter.POS_RULES -> R.id.nav_rules
        MainPagerAdapter.POS_AUTHORS -> R.id.nav_authors
        MainPagerAdapter.POS_SETTINGS -> R.id.nav_settings
        else -> R.id.nav_registration
    }

    private fun menuIdToPosition(menuId: Int): Int = when (menuId) {
        R.id.nav_registration -> MainPagerAdapter.POS_REGISTRATION
        R.id.nav_game -> MainPagerAdapter.POS_GAME
        R.id.nav_rules -> MainPagerAdapter.POS_RULES
        R.id.nav_authors -> MainPagerAdapter.POS_AUTHORS
        R.id.nav_settings -> MainPagerAdapter.POS_SETTINGS
        else -> MainPagerAdapter.POS_REGISTRATION
    }
}