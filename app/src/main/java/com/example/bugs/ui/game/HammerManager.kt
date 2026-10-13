package com.example.bugs.ui.game

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HammerManager(
    private val config: GameConfig,
    private val onTick: (secondsLeft: Int) -> Unit,
    private val onEnd: () -> Unit
) {

    var isActive: Boolean = false
        private set
    var timeLeft: Int = 0
        private set

    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    val hitRadius: Float
        get() = if (isActive) HAMMER_RADIUS else BASE_RADIUS

    fun activate() {
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
    }

    companion object {
        const val BASE_RADIUS = 60f
        const val HAMMER_RADIUS = BASE_RADIUS * 3
    }
}