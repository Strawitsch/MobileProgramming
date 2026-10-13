package com.example.bugs.ui.game

import android.content.Context
import android.widget.FrameLayout
import android.widget.ImageView

/**
 * Если ты задаешься воппросом что это, то это класс для создания пула жуков, чтобы после их уничтожения объекты не удалялись и создавали, а использовались снова. Экономит ресурсы и время при удалении и создании новых объектов жуков
 */
class BugPool(
    private val context: Context,
    private val container: FrameLayout,
    private val bugSize: Int
) {
    private val pool = ArrayDeque<ImageView>()

    /** Получить View: из пула или создать новый */
    fun obtain(): ImageView {
        return if (pool.isNotEmpty()) {
            pool.removeFirst()
        } else {
            ImageView(context).apply {
                layoutParams = FrameLayout.LayoutParams(bugSize, bugSize)
            }
        }
    }

    /** Вернуть View в пул */
    fun recycle(view: ImageView) {
        view.animate().cancel()
        view.rotation = 0f
        view.alpha = 1f
        view.x = 0f
        view.y = 0f
        view.setImageDrawable(null)
        container.removeView(view)
        pool.addLast(view)
    }

    /** Очистить пул. Вот она ваша прославленная авто сборка мусора */
    fun clear() {
        pool.clear()
    }
}