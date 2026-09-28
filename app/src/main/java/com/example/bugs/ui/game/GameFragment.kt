package com.example.bugs.ui.game

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
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
    private val bugSize = 120

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

        container.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && isGameRunning) {
                misses++
                tvMisses.text = "Промахи: $misses"
            }
            false
        }
    }

    override fun onResume() {
        super.onResume()
            stopGame()
        if (!isUserRegistered()) {
            showRegistrationRequired()
        } else {
            clearContainer()
            loadSettings()
            startGame()
        }
    }

    override fun onPause() {
        super.onPause()
        stopGame()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopGame()
    }

    private fun isUserRegistered(): Boolean {
        val prefs = requireContext().getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_registered", false)
    }

    private fun showRegistrationRequired() {
        clearContainer()
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

    private fun clearContainer() {
        container.removeAllViews()
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

    private fun getBugFlightDurationMs(): Long {
        val normalized = (speed - 1) / 9f
        return (4000L - (3200L * normalized)).toLong()
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

    private fun stopGame() {
        isGameRunning = false
        gameLoop?.let { handler.removeCallbacks(it) }
        bonusLoop?.let { handler.removeCallbacks(it) }
        timerJob?.cancel()
        clearContainer()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun spawnBug(isBonus: Boolean) {
        val width = container.width
        val height = container.height
        if (width <= bugSize || height <= bugSize) return

        val size = if (isBonus) 150 else bugSize
        val bug = ImageView(requireContext())
        bug.setImageResource(
            if (isBonus) android.R.drawable.star_big_on
            else android.R.drawable.ic_menu_compass
        )
        bug.layoutParams = FrameLayout.LayoutParams(size, size)

        val side = Random.nextInt(4)

        val startX: Float
        val startY: Float
        val endX: Float
        val endY: Float

        when (side) {
            0 -> {
                startX = -size.toFloat()
                startY = Random.nextInt(0, height - size).toFloat()
                endX = width.toFloat()
                endY = startY
            }
            1 -> {
                startX = width.toFloat()
                startY = Random.nextInt(0, height - size).toFloat()
                endX = -size.toFloat()
                endY = startY
            }
            2 -> {
                startX = Random.nextInt(0, width - size).toFloat()
                startY = -size.toFloat()
                endX = startX
                endY = height.toFloat()
            }
            else -> {
                startX = Random.nextInt(0, width - size).toFloat()
                startY = height.toFloat()
                endX = startX
                endY = -size.toFloat()
            }
        }

        bug.x = startX
        bug.y = startY

        var isKilled = false
        bug.setOnClickListener {
            if (isKilled) return@setOnClickListener
            isKilled = true
            val points = if (isBonus) 50 else 10
            score += points
            tvScore.text = "Очки: $score"
            if (isBonus) tvBonus.text = "Бонус: +$points"
            (bug.animate() as? ObjectAnimator)?.cancel()
            bug.animate().cancel()
            container.removeView(bug)
        }

        container.addView(bug)

        val duration = getBugFlightDurationMs()
        val animX = ObjectAnimator.ofFloat(bug, "x", startX, endX)
        val animY = ObjectAnimator.ofFloat(bug, "y", startY, endY)
        animX.duration = duration
        animY.duration = duration

        animX.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (!isKilled) {
                    container.removeView(bug)
                }
            }
        })

        animX.start()
        animY.start()
    }

    private fun endGame() {
        isGameRunning = false
        gameLoop?.let { handler.removeCallbacks(it) }
        bonusLoop?.let { handler.removeCallbacks(it) }
        timerJob?.cancel()
        clearContainer()

        Toast.makeText(
            context,
            "Игра окончена! Очки: $score, Промахи: $misses",
            Toast.LENGTH_LONG
        ).show()
    }
}