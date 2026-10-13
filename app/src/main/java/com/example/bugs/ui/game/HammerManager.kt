package com.example.bugs.ui.game

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class HammerManager(
    private val context: Context,
    private val container: FrameLayout,
    private val config: GameConfig,
    private val onTick: (secondsLeft: Int) -> Unit,
    private val onEnd: () -> Unit
) {

    var isActive: Boolean = false
        private set
    var timeLeft: Int = 0
        private set

    private var hammerView: ImageView? = null
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    val hitRadius: Float
        get() = if (isActive) HAMMER_RADIUS else BASE_RADIUS

    fun spawnHammer() {
        if (hammerView != null) return
        if (container.width == 0 || container.height == 0) return

        val size = 100
        val view = ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(size, size)
            // TODO: заменить на setImageResource(R.drawable.hammer)
            setImageDrawable(ColorDrawable(Color.parseColor("#8B4513")))
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        val maxX = (container.width - size).coerceAtLeast(0)
        val maxY = (container.height - size).coerceAtLeast(0)
        view.translationX = Random.nextInt(0, maxX + 1).toFloat()
        view.translationY = Random.nextInt(0, maxY + 1).toFloat()

        view.setOnClickListener {
            activate()
            removeHammer()
        }

        (view.parent as? ViewGroup)?.removeView(view)
        container.addView(view)
        hammerView = view
    }

    private fun removeHammer() {
        hammerView?.let {
            container.removeView(it)
            it.setOnClickListener(null)
        }
        hammerView = null
    }

    private fun activate() {
        isActive = true
        timeLeft = config.hammerDuration
        onTick(timeLeft)

        job?.cancel()
        job = scope.launch {
            while (timeLeft > 0) {
                delay(1000L)
                timeLeft--
                onTick(timeLeft)
            }
            isActive = false
            onEnd()
        }
    }

    fun reset() {
        job?.cancel()
        job = null
        isActive = false
        timeLeft = 0
        removeHammer()
    }

    companion object {
        const val BASE_RADIUS = 60f
        const val HAMMER_RADIUS = 180f
    }
}