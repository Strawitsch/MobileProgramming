package com.example.bugs.ui.game

import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.bugs.R
import com.example.bugs.model.game.Bug
import com.example.bugs.model.game.BugColor
import kotlin.random.Random

class BugSpawner(
    private val container: FrameLayout,
    private val pool: BugPool,
    private val config: GameConfig,
    private val onBugMissed: () -> Unit
) {

    fun spawn(size: Int): Bug? {
        val width = container.width
        val height = container.height
        if (width <= size || height <= size) return null

        val view = pool.obtain()
        (view.parent as? ViewGroup)?.removeView(view)

        val color = BugColor.random()

        val bugImages = intArrayOf(
            R.drawable.bug_1,
            R.drawable.bug_2,
            R.drawable.bug_3,
            R.drawable.bug_4,
            R.drawable.bug_5
        )

        val random = bugImages.random()
        view.setImageResource(random)

        val (startX, startY) = randomEdgePoint(width, height, size)

        view.translationX = startX
        view.translationY = startY

        if (view.parent != null) {
            (view.parent as ViewGroup).removeView(view)
        }
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

    fun randomInsidePoint(size: Int): Pair<Float, Float> {
        val width = container.width
        val height = container.height
        val x = Random.nextInt(size, (width - size).coerceAtLeast(size + 1)).toFloat()
        val y = Random.nextInt(size, (height - size).coerceAtLeast(size + 1)).toFloat()
        return Pair(x, y)
    }

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
