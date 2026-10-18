package com.example.bugs

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val btnGame = findViewById<Button>(R.id.btnMenuGame)
        val btnRegistration = findViewById<Button>(R.id.btnMenuRegistration)
        val btnRecords = findViewById<Button>(R.id.btnMenuRecords)
        val btnRules = findViewById<Button>(R.id.btnMenuRules)
        val btnAuthors = findViewById<Button>(R.id.btnMenuAuthors)
        val btnSettings = findViewById<Button>(R.id.btnMenuSettings)

        btnGame.setOnClickListener {
            if (!isUserRegistered()) {
                Toast.makeText(this, getString(R.string.registration), Toast.LENGTH_SHORT).show()
                openTab(MainPagerAdapter.POS_REGISTRATION)
            } else {
                openTab(MainPagerAdapter.POS_GAME)
            }
        }

        btnRegistration.setOnClickListener {
            openTab(MainPagerAdapter.POS_REGISTRATION)
        }

        btnRecords.setOnClickListener {
            openTab(MainPagerAdapter.POS_RECORDS)
        }

        btnRules.setOnClickListener {
            openTab(MainPagerAdapter.POS_RULES)
        }

        btnAuthors.setOnClickListener {
            openTab(MainPagerAdapter.POS_AUTHORS)
        }

        btnSettings.setOnClickListener {
            openTab(MainPagerAdapter.POS_SETTINGS)
        }
    }

    private fun openTab(position: Int) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_TAB_INDEX, position)
        }
        startActivity(intent)
    }

    private fun isUserRegistered(): Boolean {
        val prefs = getSharedPreferences("game_prefs", MODE_PRIVATE)
        return prefs.getBoolean("is_registered", false)
    }
}
