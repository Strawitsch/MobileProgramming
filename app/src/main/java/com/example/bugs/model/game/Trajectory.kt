package com.example.bugs.model.game

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

enum class Trajectory {
    LINEAR,
    BEZIER,
    SEMICIRCLE;
    companion object {
        fun random(): Trajectory = entries.random()
    }
}

object TrajectoryMath {

    fun linear(
        sx: Float, sy: Float,
        ex: Float, ey: Float,
        t: Float
    ): Pair<Float, Float> {
        return Pair(sx + (ex - sx) * t, sy + (ey - sy) * t)
    }

    fun bezier(
        sx: Float, sy: Float,
        ex: Float, ey: Float,
        t: Float,
        control: Pair<Float, Float>
    ): Pair<Float, Float> {
        val (cx, cy) = control
        val oneMinusT = 1 - t
        val x = oneMinusT * oneMinusT * sx + 2 * oneMinusT * t * cx + t * t * ex
        val y = oneMinusT * oneMinusT * sy + 2 * oneMinusT * t * cy + t * t * ey
        return Pair(x, y)
    }

    fun randomControl(
        sx: Float, sy: Float,
        ex: Float, ey: Float,
        spread: Float = 400f
    ): Pair<Float, Float> {
        val cx = (sx + ex) / 2f + (Random.nextFloat() - 0.5f) * spread
        val cy = (sy + ey) / 2f + (Random.nextFloat() - 0.5f) * spread
        return Pair(cx, cy)
    }

    fun semicircle(
        sx: Float, sy: Float,
        ex: Float, ey: Float,
        t: Float
    ): Pair<Float, Float> {
        val cx = (sx + ex) / 2f
        val cy = (sy + ey) / 2f
        val r = hypot((ex - sx).toDouble(), (ey - sy).toDouble()).toFloat() / 2f
        val baseAngle = atan2((ey - sy).toDouble(), (ex - sx).toDouble())
        val angle = baseAngle + PI * t
        val x = cx + r * cos(angle).toFloat()
        val y = cy + r * sin(angle).toFloat()
        return Pair(x, y)
    }

    fun angle(sx: Float, sy: Float, ex: Float, ey: Float): Float {
        return Math.toDegrees(atan2((ey - sy).toDouble(), (ex - sx).toDouble())).toFloat()
    }
}