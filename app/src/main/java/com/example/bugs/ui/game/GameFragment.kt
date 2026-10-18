package com.example.bugs.ui.game

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bugs.MainActivity
import com.example.bugs.R
import com.example.bugs.data.AppDatabase
import com.example.bugs.data.ScoreRecordEntity
import com.example.bugs.model.game.Bug
import androidx.lifecycle.lifecycleScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot
import androidx.core.graphics.toColorInt
import kotlin.time.Duration.Companion.milliseconds

class GameFragment : Fragment() {

    private lateinit var controlPanel: LinearLayout
    private lateinit var btnPlay: Button
    private lateinit var btnResume: Button
    private var pausedByLifecycle = false
    private var roundFinished = true
    private lateinit var hitIndicator: View
    private lateinit var container: FrameLayout
    private lateinit var tvScore: TextView
    private lateinit var tvMisses: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvBonus: TextView

    private lateinit var sound: SoundManager
    private lateinit var config: GameConfig
    private lateinit var pool: BugPool
    private lateinit var spawner: BugSpawner
    private lateinit var hammer: HammerManager
    private val scoreTracker = ScoreTracker()

    private var isGameRunning = false
    private var isPaused = false
    private var timeLeft = 60
    private var timerJob: Job? = null

    private val handler = Handler(Looper.getMainLooper())
    private var gameLoop: Runnable? = null
    private var bonusLoop: Runnable? = null

    private val activeBugs = mutableListOf<Bug>()
    private val bugSize = 120

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_game, container, false)

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sound = SoundManager(requireContext())

        container = view.findViewById(R.id.gameContainer)

        hitIndicator = View(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(0, 0)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setStroke(6, "#FF5722".toColorInt())
                setColor("#40FF5722".toColorInt())
            }
            elevation = 100f
            visibility = View.INVISIBLE
        }
        container.addView(hitIndicator)

        tvScore = view.findViewById(R.id.tvScore)
        tvMisses = view.findViewById(R.id.tvMisses)
        tvTimer = view.findViewById(R.id.tvTimer)
        tvBonus = view.findViewById(R.id.tvBonus)
        controlPanel = view.findViewById(R.id.controlPanel)
        btnPlay = view.findViewById(R.id.btnPlay)
        btnResume = view.findViewById(R.id.btnResume)

        config = GameConfig.load(requireContext())
        initSubsystems()

        container.post {
            if (isGameRunning) {
                scheduleGameLoop()
                scheduleBonusLoop()
            }
        }

        container.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && isGameRunning && !isPaused) {
                handleTap(event.x, event.y)
            }
            true
        }

        btnPlay.setOnClickListener {
            if (!isUserRegistered()) {
                Toast.makeText(context, getString(R.string.reg_message), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isGameRunning && roundFinished) {
                reloadGameConfig()
                startNewRound()
            }
        }

        btnResume.setOnClickListener {
            pausedByLifecycle = false
            resumeGame()
        }
    }

    override fun onResume() {
        super.onResume()

        if (!isUserRegistered()) {
            showRegistrationRequired()
            return
        }

        if (isGameRunning && pausedByLifecycle) {
            showPausedUI()
            lockNavigation(false)
        } else if (!isGameRunning) {
            reloadGameConfig()
            showIdleUI()
            lockNavigation(false)
        }
    }

    override fun onPause() {
        super.onPause()
        if (isGameRunning && !isPaused) {
            pauseGame()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopGame()
        if (::sound.isInitialized) sound.release()
    }

    private fun lockNavigation(locked: Boolean) {
        (activity as? MainActivity)?.lockNavigation(locked)
    }

    private fun reloadGameConfig() {
        clearContainer()
        if (::pool.isInitialized) pool.clear()
        config = GameConfig.load(requireContext())
        initSubsystems()
    }

    private fun showIdleUI() {
        controlPanel.visibility = View.VISIBLE
        btnPlay.visibility = View.VISIBLE
        btnPlay.isEnabled = true
        btnResume.visibility = View.GONE
    }

    private fun showPlayingUI() {
        controlPanel.visibility = View.GONE
    }

    private fun showPausedUI() {
        controlPanel.visibility = View.VISIBLE
        btnPlay.visibility = View.GONE
        btnResume.visibility = View.VISIBLE
    }

    // Пауза / продолжение

    @SuppressLint("SetTextI18n")
    private fun startNewRound() {
        if (!::config.isInitialized) {
            config = GameConfig.load(requireContext())
        }
        scoreTracker.reset()
        timeLeft = config.roundDuration
        isGameRunning = true
        isPaused = false
        pausedByLifecycle = false
        roundFinished = false

        updateScoreUI()
        tvMisses.text = getString(R.string.misses)
        tvTimer.text = getString(R.string.timer, timeLeft)
        tvBonus.text = getString(R.string.bonus)

        sound.init()
        sound.startMoveSound()

        scheduleTimer()
        container.post {
            if (isGameRunning && !isPaused) {
                scheduleGameLoop()
                scheduleBonusLoop()
            }
        }

        showPlayingUI()
        lockNavigation(true)
    }

    private fun pauseGame() {
        if (!isGameRunning || isPaused) return
        isPaused = true
        pausedByLifecycle = true

        sound.stopAll()

        activeBugs.forEach { bug ->
            bug.animator?.cancel()
            bug.animator = null
            bug.isDead = true
        }

        gameLoop?.let { handler.removeCallbacks(it) }
        bonusLoop?.let { handler.removeCallbacks(it) }
        timerJob?.cancel()

        showPausedUI()
        lockNavigation(false)
    }

    private fun resumeGame() {
        if (!isGameRunning || !isPaused) return
        isPaused = false

        sound.startMoveSound()

        activeBugs.forEach { pool.recycle(it.view) }
        activeBugs.clear()

        scheduleTimer()
        scheduleGameLoop()
        scheduleBonusLoop()

        showPlayingUI()
        lockNavigation(true)
    }

    @SuppressLint("SetTextI18n")
    private fun scheduleTimer() {
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while (timeLeft > 0 && isGameRunning && !isPaused) {
                delay(1000L.milliseconds)
                timeLeft--
                tvTimer.text = getString(R.string.timer_1, timeLeft)
            }
            if (timeLeft <= 0) endGame()
        }
    }

    private fun scheduleGameLoop() {
        gameLoop = object : Runnable {
            override fun run() {
                if (isGameRunning && !isPaused) {
                    if (activeBugs.size < config.maxBugs) {
                        spawnAndAnimate()
                    }
                    handler.postDelayed(this, config.spawnDelayMs())
                }
            }
        }
        handler.post(gameLoop!!)
    }

    private fun scheduleBonusLoop() {
        bonusLoop = object : Runnable {
            override fun run() {
                if (isGameRunning && !isPaused) {
                    hammer.spawnHammer()
                    handler.postDelayed(this, config.bonusInterval * 1000L)
                }
            }
        }
        handler.postDelayed(bonusLoop!!, config.bonusInterval * 1000L)
    }

    // Инициализация

    @SuppressLint("SetTextI18n")
    private fun initSubsystems() {
        if (!::config.isInitialized) {
            config = GameConfig.load(requireContext())
        }
        if (::pool.isInitialized) pool.clear()
        pool = BugPool(requireContext(), bugSize)
        spawner = BugSpawner(container, pool, config) {
            scoreTracker.addMiss()
            tvMisses.text = getString(R.string.misses1, scoreTracker.misses)
        }
        if (::hammer.isInitialized) hammer.reset()
        hammer = HammerManager(
            context = requireContext(),
            container = container,
            config = config,
            onTick = { seconds -> tvBonus.text = getString(R.string.bonus1, seconds) },
            onEnd = { tvBonus.text = getString(R.string.bonus2) }
        )
    }


    private fun isUserRegistered(): Boolean {
        val prefs = requireContext().getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_registered", false)
    }

    private fun showRegistrationRequired() {
        clearContainer()
        val tv = TextView(requireContext()).apply {
            text = context.getString(R.string.reg_message2)
            textSize = 20f
            gravity = android.view.Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        container.addView(tv)
        tvScore.text = getString(R.string.score)
        tvMisses.text = getString(R.string.misses2)
        tvTimer.text = getString(R.string.time)
        tvBonus.text = getString(R.string.bonus3)

        btnPlay.isEnabled = false
    }

    private fun clearContainer() {
        activeBugs.forEach { bug ->
            bug.animator?.cancel()
            bug.animator = null
            bug.isDead = true
            (bug.view.parent as? ViewGroup)?.removeView(bug.view)
        }
        activeBugs.clear()

        for (i in container.childCount - 1 downTo 0) {
            val child = container.getChildAt(i)
            if (child !== hitIndicator) {
                container.removeViewAt(i)
            }
        }
        if (::pool.isInitialized) pool.clear()
    }

    // Запуск / остановка

    private fun stopGame() {
        isGameRunning = false
        isPaused = false
        pausedByLifecycle = false
        roundFinished = true

        sound.stopAll()
        gameLoop?.let { handler.removeCallbacks(it) }
        bonusLoop?.let { handler.removeCallbacks(it) }
        timerJob?.cancel()
        if (::hammer.isInitialized) hammer.reset()
        clearContainer()
        lockNavigation(false)
    }

    private fun endGame() {
        isGameRunning = false
        isPaused = false
        pausedByLifecycle = false
        roundFinished = true

        sound.stopAll()
        gameLoop?.let { handler.removeCallbacks(it) }
        bonusLoop?.let { handler.removeCallbacks(it) }
        timerJob?.cancel()
        if (::hammer.isInitialized) hammer.reset()
        clearContainer()

        showIdleUI()
        lockNavigation(false)

        saveScoreToDatabase()

        Toast.makeText(
            context,
            getString(R.string.end_game_toast, scoreTracker.score, scoreTracker.misses),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun saveScoreToDatabase() {
        val prefs = requireContext().getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        val userName = prefs.getString("player_name", "Игрок") ?: "Игрок"
        val difficulty = prefs.getInt("player_difficulty", 0)
        val userId = prefs.getLong("active_user_id", 0L)

        val difficultyLevels = resources.getStringArray(R.array.difficulty_levels_array)
        val diffName = if (difficulty in difficultyLevels.indices) {
            difficultyLevels[difficulty]
        } else {
            difficulty.toString()
        }

        val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())

        val record = ScoreRecordEntity(
            userId = userId,
            userName = userName,
            score = scoreTracker.score,
            misses = scoreTracker.misses,
            difficulty = difficulty,
            difficultyName = diffName,
            date = dateStr
        )

        lifecycleScope.launch(Dispatchers.IO) {
            AppDatabase.getInstance(requireContext()).scoreDao().insertScore(record)
        }
    }

    // Спавн и анимация

    private fun spawnAndAnimate() {
        val bug = spawner.spawn(bugSize) ?: return
        activeBugs.add(bug)
        val speedFactor = config.speed / 5f
        MoveAndSlideAnimator(bug, container, speedFactor).start()
    }

    // Тап

    @SuppressLint("SetTextI18n")
    private fun handleTap(tapX: Float, tapY: Float) {
        val hitRadius = hammer.hitRadius

        showHitIndicator(tapX, tapY, hitRadius)

        val hitBugs = mutableListOf<Bug>()
        for (bug in activeBugs) {
            if (bug.isDead) continue
            val cx = bug.currentX + bug.size / 2f
            val cy = bug.currentY + bug.size / 2f
            val d = hypot((tapX - cx).toDouble(), (tapY - cy).toDouble()).toFloat()
            if (d <= hitRadius) {
                hitBugs.add(bug)
            }
        }

        if (hitBugs.isNotEmpty()) {
            hitBugs.forEach { killBug(it) }
        } else {
            scoreTracker.addMiss()
            tvMisses.text = getString(R.string.misses4, scoreTracker.misses)
        }
    }

    private fun showHitIndicator(x: Float, y: Float, radius: Float) {
        val size = (radius * 2).toInt()

        val lp = hitIndicator.layoutParams as FrameLayout.LayoutParams
        lp.width = size
        lp.height = size
        hitIndicator.layoutParams = lp

        hitIndicator.translationX = x - radius
        hitIndicator.translationY = y - radius

        hitIndicator.visibility = View.VISIBLE
        hitIndicator.alpha = 1f
        hitIndicator.bringToFront()

        hitIndicator.animate().cancel()
        hitIndicator.animate()
            .alpha(0f)
            .setDuration(300L)
            .withEndAction {
                hitIndicator.visibility = View.INVISIBLE
            }
            .start()
    }

    private fun killBug(bug: Bug) {
        if (bug.isDead) return
        bug.isDead = true
        bug.animator?.cancel()
        bug.animator = null
        activeBugs.remove(bug)
        pool.recycle(bug.view)
        scoreTracker.addKill(10)
        updateScoreUI()
    }

    @SuppressLint("SetTextI18n")
    private fun updateScoreUI() {
        tvScore.text = getString(R.string.score1, scoreTracker.score)
    }
}