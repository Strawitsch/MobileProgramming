package com.example.bugs.ui.game

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView

class BugPool(
    private val context: Context,
    private val bugSize: Int
) {
    private val pool = ArrayDeque<ImageView>()

    fun obtain(): ImageView {
        val view = if (pool.isNotEmpty()) {
            pool.removeFirst()
        } else {
            ImageView(context).apply {
                layoutParams = FrameLayout.LayoutParams(bugSize, bugSize)
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
        }

        (view.parent as? ViewGroup)?.removeView(view)
        return view
    }

    fun recycle(view: ImageView) {
        view.animate().cancel()
        view.rotation = 0f
        view.alpha = 1f
        view.x = 0f
        view.y = 0f
        view.setImageDrawable(null)

        (view.parent as? ViewGroup)?.removeView(view)

        if (!pool.contains(view)) {
            pool.addLast(view)
        }
    }

    fun clear() {
        pool.forEach { (it.parent as? ViewGroup)?.removeView(it) }
        pool.clear()
    }
}
