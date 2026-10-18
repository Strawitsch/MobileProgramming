package com.example.bugs.ui.game

import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import com.example.bugs.model.game.Bug
import kotlin.math.*
import kotlin.random.Random

class MoveAndSlideAnimator(
    private val bug: Bug,
    private val container: FrameLayout,
    private val speedMultiplier: Float
) {
    fun start() {
        val view = bug.view
        val width = container.width.toFloat()
        val height = container.height.toFloat()
        if (width <= bug.size || height <= bug.size) return

        var x = bug.currentX
        var y = bug.currentY
        val angle = Random.nextFloat() * 2f * PI.toFloat()
        val baseSpeed = 300f + Random.nextFloat() * 250f
        val speed = baseSpeed * (speedMultiplier * 1.8f)
        var vx = speed * cos(angle)
        var vy = speed * sin(angle)

        view.translationX = x
        view.translationY = y

        var lastTime = System.nanoTime()

        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            repeatCount = ValueAnimator.INFINITE
            duration = Long.MAX_VALUE
            interpolator = LinearInterpolator()
        }

        animator.addUpdateListener {
            if (!view.isAttachedToWindow || bug.isDead) return@addUpdateListener
            val currentTime = System.nanoTime()
            val dt = (currentTime - lastTime) / 1_000_000_000f
            lastTime = currentTime

            if (dt > 0.1f) return@addUpdateListener

            x += vx * dt
            y += vy * dt

            val minX = 0f
            val maxX = (container.width - bug.size).toFloat()
            val minY = 0f
            val maxY = (container.height - bug.size).toFloat()

            var collided = false
            if (x < minX) {
                x = minX
                vx = -vx
                collided = true
            } else if (x > maxX) {
                x = maxX
                vx = -vx
                collided = true
            }

            if (y < minY) {
                y = minY
                vy = -vy
                collided = true
            } else if (y > maxY) {
                y = maxY
                vy = -vy
                collided = true
            }

            if (collided) {
                val currentSpeed = hypot(vx.toDouble(), vy.toDouble()).toFloat()
                val newAngle = atan2(vy.toDouble(), vx.toDouble()) + (Random.nextFloat() - 0.5f) * 0.8f
                vx = currentSpeed * cos(newAngle).toFloat()
                vy = currentSpeed * sin(newAngle).toFloat()
            }

            bug.currentX = x
            bug.currentY = y
            view.translationX = x
            view.translationY = y

            view.rotation = Math.toDegrees(atan2(vy.toDouble(), vx.toDouble())).toFloat() + 90f
        }

        bug.animator = animator
        animator.start()
    }

    fun cancel() {
        bug.animator?.cancel()
        bug.animator = null
    }
}
