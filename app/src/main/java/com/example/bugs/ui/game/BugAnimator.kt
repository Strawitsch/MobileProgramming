package com.example.bugs.ui.game

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import com.example.bugs.model.game.Bug
import com.example.bugs.model.game.Trajectory
import com.example.bugs.model.game.TrajectoryMath

/**
 * TODO: Возможно эту херь придётся где-то переписать, но я не уверен.
 */
class BugAnimator(
    private val bug: Bug,
    private val durationMs: Long,
    private val onSegmentEnd: (Bug) -> Unit
) {

    fun start(
        sx: Float, sy: Float,
        ex: Float, ey: Float
    ) {
        val view = bug.view
        if (bug.isDead) return
        val trajectory = Trajectory.random()
        val control = if (trajectory == Trajectory.BEZIER) {
            TrajectoryMath.randomControl(sx, sy, ex, ey)
        } else {
            Pair(0f, 0f)
        }

        view.rotation = TrajectoryMath.angle(sx, sy, ex, ey)

        view.translationX = sx
        view.translationY = sy
        bug.currentX = sx
        bug.currentY = sy

        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = durationMs
            this.interpolator = LinearInterpolator()
        }

        var cancelled = false

        animator.addUpdateListener { va ->
            if (!view.isAttachedToWindow) return@addUpdateListener
            val t = va.animatedValue as Float
            val (x, y) = when (trajectory) {
                Trajectory.LINEAR -> TrajectoryMath.linear(sx, sy, ex, ey, t)
                Trajectory.BEZIER -> TrajectoryMath.bezier(sx, sy, ex, ey, t, control)
                Trajectory.SEMICIRCLE -> TrajectoryMath.semicircle(sx, sy, ex, ey, t)
            }
            view.translationX = x
            view.translationY = y
            bug.currentX = x
            bug.currentY = y

            if (trajectory != Trajectory.LINEAR) {
                val t2 = (t + 0.01f).coerceAtMost(1f)
                val (x2, y2) = when (trajectory) {
                    Trajectory.BEZIER -> TrajectoryMath.bezier(sx, sy, ex, ey, t2, control)
                    else -> TrajectoryMath.semicircle(sx, sy, ex, ey, t2)
                }
                view.rotation = TrajectoryMath.angle(x, y, x2, y2)
            }
        }

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationCancel(animation: Animator) {
                cancelled = true
            }

            override fun onAnimationEnd(animation: Animator) {
                bug.animator = null
                if (cancelled) return
                if (bug.isDead) return
                onSegmentEnd(bug)
            }
        })

        bug.animator = animator
        animator.start()
    }

    fun cancel() {
        bug.animator?.cancel()
        bug.animator = null
    }
}