package com.example.bugs.model.game

import android.animation.ValueAnimator
import android.widget.ImageView

data class Bug(
    val view: ImageView,
    val size: Int,
    var waypointsLeft: Int = 0,
    var animator: ValueAnimator? = null
)