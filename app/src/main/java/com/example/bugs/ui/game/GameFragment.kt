package com.example.bugs.ui.game

import android.content.Context
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

/**
 *  - GameConfig       (настройки)
 *  - BugPool          (переиспользование View)
 *  - BugSpawner       (создание жуков)
 *  - BugAnimator      (анимация сегментов)
 *  - HammerManager    (бонус «молоток»)
 *  - ScoreTracker     (очки и промахи)
 */
class GameFragment : Fragment() {

    //View'шки
    private lateinit var container: FrameLayout
    private lateinit var tvScore: TextView
    private lateinit var tvMisses: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvBonus: TextView

    // Подсистемы
    private lateinit var config: GameConfig
    private lateinit var pool: BugPool
    private lateinit var spawner: BugSpawner
    private lateinit var hammer: HammerManager
    private val scoreTracker = ScoreTracker()

    // Состояние раунда
    private var isGameRunning = false
    private var timeLeft = 60
    private var timerJob: Job? = null

    private val handler = Handler(Looper.getMainLooper())
    private var gameLoop: Runnable? = null
    private var bonusLoop: Runnable? = null

    private val activeBugs = mutableListOf<Bug>()
    private val bugSize = 120

    // Lifecycle

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_game, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        container = view.findViewById(R.id.gameContainer)
        tvScore = view.findViewById(R.id.tvScore)
        tvMisses = view.findViewById(R.id.tvMisses)
        tvTimer = view.findViewById(R.id.tvTimer)
        tvBonus = view.findViewById(R.id.tvBonus)

        container.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && isGameRunning) {
                handleTap(event.x, event.y)
            }
            true
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
            initSubsystems()
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

    // Инициализация подсистем

    private fun initSubsystems() {
        pool = BugPool(requireContext(), container, bugSize)
        spawner = BugSpawner(container, pool, config) {
            scoreTracker.addMiss()
            tvMisses.text = "Промахи: ${scoreTracker.misses}"
        }
        hammer = HammerManager(
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

    // Заглушка регистрации

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
        activeBugs.forEach { it.animator?.cancel() }
        activeBugs.clear()
        container.removeAllViews()
        if (::pool.isInitialized) pool.clear()
    }

    // Игровой цикл

    private fun startGame() {
        scoreTracker.reset()
        timeLeft = config.roundDuration
        isGameRunning = true

        updateScoreUI()
        tvMisses.text = "Промахи: 0"
        tvTimer.text = "Время: $timeLeft"
        tvBonus.text = "Бонус: -"

        // Таймер раунда
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while (timeLeft > 0 && isGameRunning) {
                delay(1000L)
                timeLeft--
                tvTimer.text = "Время: $timeLeft"
            }
            endGame()
        }

        // Спавн жуков
        gameLoop = object : Runnable {
            override fun run() {
                if (isGameRunning) {
                    if (activeBugs.size < config.maxBugs) {
                        spawnAndAnimate()
                    }
                    handler.postDelayed(this, config.spawnDelayMs())
                }
            }
        }
        handler.post(gameLoop!!)

        // Спавн молотков
        bonusLoop = object : Runnable {
            override fun run() {
                if (isGameRunning) {
                    hammer.activate()
                    handler.postDelayed(this, config.bonusInterval * 1000L)
                }
            }
        }
        handler.postDelayed(bonusLoop!!, config.bonusInterval * 1000L)
    }

    private fun stopGame() {
        isGameRunning = false
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

    // Спавн и анимация. Я без понятия как много здесь придётся переписывать

    private fun spawnAndAnimate() {
        val bug = spawner.spawn(bugSize)
        activeBugs.add(bug)

        val (tx, ty) = spawner.randomInsidePoint(bugSize)
        animateSegment(bug, tx, ty, isExit = false)
    }

    private fun animateSegment(bug: Bug, ex: Float, ey: Float, isExit: Boolean) {
        val sx = bug.view.x
        val sy = bug.view.y

        BugAnimator(
            bug = bug,
            durationMs = config.segmentDurationMs(),
            onSegmentEnd = { finishedBug -> onSegmentFinished(finishedBug, isExit) }
        ).start(sx, sy, ex, ey)
    }

    private fun onSegmentFinished(bug: Bug, isExit: Boolean) {
        if (!isGameRunning) return

        if (isExit) {
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

    // Тап и попадание TODO: Возможно стоит на будущее сделать обводку границ попадания для тестов размера области попадания

    private fun handleTap(tapX: Float, tapY: Float) {
        val hitRadius = hammer.hitRadius

        var best: Bug? = null
        var bestDist = Float.MAX_VALUE
        for (bug in activeBugs) {
            val cx = bug.view.x + bug.size / 2f
            val cy = bug.view.y + bug.size / 2f
            val d = hypot((tapX - cx).toDouble(), (tapY - cy).toDouble()).toFloat()
            if (d <= hitRadius && d < bestDist) {
                best = bug
                bestDist = d
            }
        }

        if (best != null) {
            killBug(best)
        } else {
            scoreTracker.addMiss()
            tvMisses.text = "Промахи: ${scoreTracker.misses}"
        }
    }

    private fun killBug(bug: Bug) {
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