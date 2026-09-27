package com.example.bugs.ui.game

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bugs.R
import kotlinx.coroutines.*
import kotlin.random.Random

class GameFragment : Fragment() {

    private lateinit var container: FrameLayout
    private lateinit var tvScore: TextView
    private lateinit var tvMisses: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvBonus: TextView

    private var score = 0
    private var misses = 0
    private var timeLeft = 60
    private var isGameRunning = false

    private val handler = Handler(Looper.getMainLooper())
    private var gameLoop: Runnable? = null
    private var bonusLoop: Runnable? = null
    private var timerJob: Job? = null

    private var speed = 5
    private var maxBugs = 10
    private var bonusInterval = 15
    private var roundDuration = 60

    private val minDelayMs = 100L
    private val maxDelayMs = 1000L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        container = view.findViewById(R.id.gameContainer)
        tvScore = view.findViewById(R.id.tvScore)
        tvMisses = view.findViewById(R.id.tvMisses)
        tvTimer = view.findViewById(R.id.tvTimer)
        tvBonus = view.findViewById(R.id.tvBonus)

        val prefs = requireContext().getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        val isRegistered = prefs.getBoolean("is_registered", false)

        if (!isRegistered) {
            showRegistrationRequired()
            return
        }

        loadSettings()

        container.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && isGameRunning) {
                misses++
                tvMisses.text = "Промахи: $misses"
            }
            false
        }

        startGame()
    }

    private fun showRegistrationRequired() {
        container.removeAllViews()
        val tv = TextView(requireContext()).apply {
            text = "Сначала зарегистрируйтесь на вкладке «Игрок»"
            textSize = 20f
            gravity = android.view.Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        container.addView(tv)
        tvScore.text = "Очки: -"
        tvMisses.text = "Промахи: -"
        tvTimer.text = "Время: -"
        tvBonus.text = "Бонус: -"
    }

    private fun loadSettings() {
        val prefs = requireContext().getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        speed = prefs.getInt("speed", 5).coerceIn(1, 10)
        maxBugs = prefs.getInt("max_bugs", 10).coerceIn(1, 50)
        bonusInterval = prefs.getInt("bonus_interval", 15).coerceIn(1, 60)
        roundDuration = prefs.getInt("round_duration", 60).coerceIn(5, 300)
    }

    private fun getSpawnDelayMs(): Long {
        val normalized = (speed - 1) / 9f
        return (1000L - (900L * normalized)).toLong()
    }

    private fun startGame() {
        score = 0
        misses = 0
        timeLeft = roundDuration
        isGameRunning = true

        tvScore.text = "Очки: 0"
        tvMisses.text = "Промахи: 0"
        tvTimer.text = "Время: $timeLeft"
        tvBonus.text = "Бонус: -"

        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while (timeLeft > 0 && isGameRunning) {
                delay(1000L)
                timeLeft--
                tvTimer.text = "Время: $timeLeft"
            }
            endGame()
        }

        gameLoop = object : Runnable {
            override fun run() {
                if (isGameRunning) {
                    if (container.childCount < maxBugs) {
                        spawnBug(isBonus = false)
                    }
                    handler.postDelayed(this, getSpawnDelayMs())
                }
            }
        }
        handler.post(gameLoop!!)

        bonusLoop = object : Runnable {
            override fun run() {
                if (isGameRunning) {
                    spawnBug(isBonus = true)
                    handler.postDelayed(this, bonusInterval * 1000L)
                }
            }
        }
        handler.postDelayed(bonusLoop!!, bonusInterval * 1000L)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun spawnBug(isBonus: Boolean) {
        val size = if (isBonus) 150 else 120
        val bug = ImageView(requireContext())
        bug.setImageResource(
            if (isBonus) android.R.drawable.star_big_on
            else android.R.drawable.ic_menu_compass
        )
        bug.layoutParams = FrameLayout.LayoutParams(size, size)

        val maxX = container.width - size
        val maxY = container.height - size
        if (maxX <= 0 || maxY <= 0) return

        bug.x = Random.nextInt(0, maxX).toFloat()
        bug.y = Random.nextInt(0, maxY).toFloat()

        bug.setOnClickListener {
            val points = if (isBonus) 50 else 10
            score += points
            tvScore.text = "Очки: $score"
            if (isBonus) tvBonus.text = "Бонус: +$points"
            container.removeView(bug)
        }

        container.addView(bug)


        handler.postDelayed({
            if (container.indexOfChild(bug) != -1) {
                container.removeView(bug)
            }
        }, 5000L)
    }

    private fun endGame() {
        isGameRunning = false
        gameLoop?.let { handler.removeCallbacks(it) }
        bonusLoop?.let { handler.removeCallbacks(it) }
        timerJob?.cancel()
        container.removeAllViews()

        Toast.makeText(
            context,
            "Игра окончена! Очки: $score, Промахи: $misses",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isGameRunning = false
        gameLoop?.let { handler.removeCallbacks(it) }
        bonusLoop?.let { handler.removeCallbacks(it) }
        timerJob?.cancel()
    }
}