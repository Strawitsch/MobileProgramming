package com.example.bugs.ui.game

import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.bugs.model.game.Bug
import com.example.bugs.model.game.BugColor
import kotlin.random.Random

/**
 * Здесь находится основная логика спавна и ухода жучков за пределом экрана.
 * Если здесб что-то сломается, то только бог сможет это починить, и даже в этом я не уверен.
 */

class BugSpawner(
    private val container: FrameLayout,
    private val pool: BugPool,
    private val config: GameConfig,
    private val onBugMissed: () -> Unit
) {

    /** Спавн нового жука */
    fun spawn(size: Int): Bug? {
        val width = container.width
        val height = container.height
        if (width <= size || height <= size) return null

        val view = pool.obtain()
        (view.parent as? ViewGroup)?.removeView(view)

        val color = BugColor.random()
        view.setImageDrawable(ColorDrawable(color))
        // TODO: заменить на setImageResource(R.drawable.bug_X)

        val (startX, startY) = randomEdgePoint(width, height, size)

        view.translationX = startX
        view.translationY = startY

        container.addView(view)

        val bug = Bug(
            view = view,
            size = size,
            color = color,
            currentX = startX,
            currentY = startY
        )
        bug.waypointsLeft = Random.nextInt(config.waypointsMin, config.waypointsMax + 1)
        return bug
    }

    /** Случайная точка внутри поля */
    fun randomInsidePoint(size: Int): Pair<Float, Float> {
        val width = container.width
        val height = container.height
        val x = Random.nextInt(size, (width - size).coerceAtLeast(size + 1)).toFloat()
        val y = Random.nextInt(size, (height - size).coerceAtLeast(size + 1)).toFloat()
        return Pair(x, y)
    }

    /** Случайная точка за границей поля */
    fun randomEdgePoint(width: Int, height: Int, size: Int): Pair<Float, Float> {
        return when (Random.nextInt(4)) {
            0 -> Pair(-size.toFloat(), Random.nextInt(0, height).toFloat())     // слева
            1 -> Pair(width.toFloat(), Random.nextInt(0, height).toFloat())     // справа
            2 -> Pair(Random.nextInt(0, width).toFloat(), -size.toFloat())      // сверху
            else -> Pair(Random.nextInt(0, width).toFloat(), height.toFloat())  // снизу
        }
    }

    fun onMiss() {
        onBugMissed()
    }
}