package com.example.bugs.ui.navigation

data class NavItem(
    val position: Int,
    val title: String,
    val iconResId: Int,
    var isSelected: Boolean = false
)
