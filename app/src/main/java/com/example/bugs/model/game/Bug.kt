package com.example.bugs.model.game

import android.animation.ValueAnimator
import android.widget.ImageView

data class Bug(
    val view: ImageView,
    val size: Int,
    var waypointsLeft: Int = 0,
    var animator: ValueAnimator? = null,
    var color: Int = 0,
    var currentX: Float = 0f,
    var currentY: Float = 0f,
    var isDead: Boolean = false,
    var targetX: Float = 0f,
    var targetY: Float = 0f,
    var isExitSegment: Boolean = false
) {
    /**
     * Проверка попадания касания (tapX, tapY) с радиусом касания touchRadius
     * по хитбоксу жука (его изображению на экране).
     */
    fun isHit(tapX: Float, tapY: Float, touchRadius: Float): Boolean {
        if (isDead) return false
        val left = currentX
        val right = currentX + size
        val top = currentY
        val bottom = currentY + size

        val closestX = tapX.coerceIn(left, right)
        val closestY = tapY.coerceIn(top, bottom)
        val distX = tapX - closestX
        val distY = tapY - closestY
        val distanceSquared = distX * distX + distY * distY

        return distanceSquared <= touchRadius * touchRadius
    }
}
