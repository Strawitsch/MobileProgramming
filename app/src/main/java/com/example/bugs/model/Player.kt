package com.example.bugs.model

data class Player(
    val fullName: String,
    val gender: Gender,
    val course: String,
    val difficulty: Int,
    val birthDate: String,
    val zodiac: Zodiac
)