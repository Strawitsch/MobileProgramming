package com.example.bugs.model.game

import android.graphics.Color
import kotlin.random.Random

/**
 * TODO: заменить на реальные R.drawable.bug_X после добавления ресурсов.
 */
object BugColor {
    private val palette = listOf(
        Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW,
        Color.MAGENTA, Color.CYAN, Color.parseColor("#FF6600"),
        Color.parseColor("#9933FF"), Color.parseColor("#33CC99")
    )

    fun random(): Int = palette.random()

    fun randomBright(): Int = Color.rgb(
        Random.nextInt(100, 255),
        Random.nextInt(100, 255),
        Random.nextInt(100, 255)
    )
}