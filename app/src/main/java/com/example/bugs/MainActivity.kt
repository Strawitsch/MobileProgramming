package com.example.bugs

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TAB_INDEX = "extra_tab_index"
    }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var viewPager: ViewPager2

    private var navigationLocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        viewPager = findViewById(R.id.viewPager)

        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewPager.adapter = MainPagerAdapter(this)
        viewPager.setPageTransformer { page, position ->
            page.alpha = 1f - 0.2f * abs(position)
        }

        val tabTitles = arrayOf(
            getString(R.string.nav_users),
            getString(R.string.nav_game),
            getString(R.string.nav_records),
            getString(R.string.nav_rules),
            getString(R.string.nav_authors),
            getString(R.string.nav_settings)
        )

        val requestedPos = intent.getIntExtra(EXTRA_TAB_INDEX, if (isUserRegistered()) MainPagerAdapter.POS_GAME else MainPagerAdapter.POS_REGISTRATION)

        viewPager.setCurrentItem(requestedPos, false)
        toolbar.title = tabTitles.getOrElse(requestedPos) { getString(R.string.app_name) }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == MainPagerAdapter.POS_GAME && !isUserRegistered()) {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.registration),
                        Toast.LENGTH_SHORT
                    ).show()
                    viewPager.post {
                        viewPager.setCurrentItem(MainPagerAdapter.POS_REGISTRATION, false)
                    }
                    return
                }
                toolbar.title = tabTitles.getOrElse(position) { getString(R.string.app_name) }
            }
        })
    }

    fun lockNavigation(locked: Boolean) {
        navigationLocked = locked
        viewPager.isUserInputEnabled = !locked
    }

    private fun isUserRegistered(): Boolean {
        val prefs = getSharedPreferences("game_prefs", MODE_PRIVATE)
        return prefs.getBoolean("is_registered", false)
    }
}
