package com.example.bugs.ui.game

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bugs.R
import com.example.bugs.model.game.Bug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot

class GameFragment : Fragment() {

    private lateinit var hitIndicator: View
    private lateinit var container: FrameLayout
    private lateinit var tvScore: TextView
    private lateinit var tvMisses: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvBonus: TextView

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        container = view.findViewById(R.id.gameContainer)

        hitIndicator = View(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(0, 0)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setStroke(6, Color.parseColor("#FF5722"))
                setColor(Color.parseColor("#40FF5722"))
            }
            elevation = 100f
            visibility = View.INVISIBLE
        }
        container.addView(hitIndicator)

        tvScore = view.findViewById(R.id.tvScore)
        tvMisses = view.findViewById(R.id.tvMisses)
        tvTimer = view.findViewById(R.id.tvTimer)
        tvBonus = view.findViewById(R.id.tvBonus)

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
    }

    override fun onResume() {
        super.onResume()
        if (!isUserRegistered()) {
            stopGame()
            showRegistrationRequired()
            return
        }

        if (isGameRunning && isPaused) {
            resumeGame()
        } else {
            stopGame()
            clearContainer()
            loadSettings()
            initSubsystems()
            startGame()
        }
    }

    override fun onPause() {
        super.onPause()
        pauseGame()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopGame()
    }

    // Пауза / продолжение

    private fun pauseGame() {
        if (!isGameRunning) return
        isPaused = true

        activeBugs.forEach { it.animator?.pause() }

        gameLoop?.let { handler.removeCallbacks(it) }
        bonusLoop?.let { handler.removeCallbacks(it) }
        timerJob?.cancel()
    }

    private fun resumeGame() {
        isPaused = false

        activeBugs.forEach { it.animator?.resume() }

        scheduleGameLoop()
        scheduleBonusLoop()
        scheduleTimer()
    }

    private fun scheduleTimer() {
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while (timeLeft > 0 && isGameRunning && !isPaused) {
                delay(1000L)
                timeLeft--
                tvTimer.text = "Время: $timeLeft"
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

    private fun initSubsystems() {
        pool = BugPool(requireContext(), container, bugSize)
        spawner = BugSpawner(container, pool, config) {
            scoreTracker.addMiss()
            tvMisses.text = "Промахи: ${scoreTracker.misses}"
        }
        hammer = HammerManager(
            context = requireContext(),
            container = container,
            config = config,
            onTick = { seconds -> tvBonus.text = "МОЛОТОК! (${seconds}с)" },
            onEnd = { tvBonus.text = "Бонус: -" }
        )
    }

    private fun loadSettings() {
        config = GameConfig.load(requireContext())
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

    private fun startGame() {
        scoreTracker.reset()
        timeLeft = config.roundDuration
        isGameRunning = true
        isPaused = false

        updateScoreUI()
        tvMisses.text = "Промахи: 0"
        tvTimer.text = "Время: $timeLeft"
        tvBonus.text = "Бонус: -"

        scheduleTimer()

        container.post {
            if (isGameRunning && !isPaused) {
                scheduleGameLoop()
                scheduleBonusLoop()
            }
        }
    }

    private fun stopGame() {
        isGameRunning = false
        isPaused = false
        gameLoop?.let { handler.removeCallbacks(it) }
        bonusLoop?.let { handler.removeCallbacks(it) }
        timerJob?.cancel()
        if (::hammer.isInitialized) hammer.reset()
        clearContainer()
    }

    private fun endGame() {
        stopGame()
        Toast.makeText(
            context,
            "Игра окончена! Очки: ${scoreTracker.score}, Промахи: ${scoreTracker.misses}",
            Toast.LENGTH_LONG
        ).show()
    }

    // Спавн и анимация

    private fun spawnAndAnimate() {
        val bug = spawner.spawn(bugSize) ?: return
        activeBugs.add(bug)
        val (tx, ty) = spawner.randomInsidePoint(bugSize)
        animateSegment(bug, tx, ty, isExit = false)
    }

    private fun animateSegment(bug: Bug, ex: Float, ey: Float, isExit: Boolean) {
        val sx = bug.currentX
        val sy = bug.currentY

        BugAnimator(
            bug = bug,
            durationMs = config.segmentDurationMs(),
            onSegmentEnd = { finishedBug -> onSegmentFinished(finishedBug, isExit) }
        ).start(sx, sy, ex, ey)
    }

    private fun onSegmentFinished(bug: Bug, isExit: Boolean) {
        if (!isGameRunning || isPaused) return
        if (bug.isDead) return

        if (isExit) {
            bug.isDead = true
            activeBugs.remove(bug)
            pool.recycle(bug.view)
            scoreTracker.addMiss()
            tvMisses.text = "Промахи: ${scoreTracker.misses}"
            return
        }

        bug.waypointsLeft--

        if (bug.waypointsLeft <= 0) {
            val (exitX, exitY) = spawner.randomEdgePoint(
                container.width, container.height, bug.size
            )
            animateSegment(bug, exitX, exitY, isExit = true)
        } else {
            val (nx, ny) = spawner.randomInsidePoint(bug.size)
            animateSegment(bug, nx, ny, isExit = false)
        }
    }

    // Тап

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
            tvMisses.text = "Промахи: ${scoreTracker.misses}"
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

    private fun updateScoreUI() {
        tvScore.text = "Очки: ${scoreTracker.score}"
    }
}