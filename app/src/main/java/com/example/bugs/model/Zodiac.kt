package com.example.bugs.model

enum class Zodiac(val displayName: String, val symbol: String) {
    AQUARIUS("Водолей", "♒"),
    PISCES("Рыбы", "♓"),
    ARIES("Овен", "♈"),
    TAURUS("Телец", "♉"),
    GEMINI("Близнецы", "♊"),
    CANCER("Рак", "♋"),
    LEO("Лев", "♌"),
    VIRGO("Дева", "♍"),
    LIBRA("Весы", "♎"),
    SCORPIO("Скорпион", "♏"),
    SAGITTARIUS("Стрелец", "♐"),
    CAPRICORN("Козерог", "♑");

    companion object {
        fun fromDate(month: Int, day: Int): Zodiac {
            return when {
                (month == 1 && day >= 20) || (month == 2 && day <= 18) -> AQUARIUS
                (month == 2 && day >= 19) || (month == 3 && day <= 20) -> PISCES
                (month == 3 && day >= 21) || (month == 4 && day <= 19) -> ARIES
                (month == 4 && day >= 20) || (month == 5 && day <= 20) -> TAURUS
                (month == 5 && day >= 21) || (month == 6 && day <= 20) -> GEMINI
                (month == 6 && day >= 21) || (month == 7 && day <= 22) -> CANCER
                (month == 7 && day >= 23) || (month == 8 && day <= 22) -> LEO
                (month == 8 && day >= 23) || (month == 9 && day <= 22) -> VIRGO
                (month == 9 && day >= 23) || (month == 10 && day <= 22) -> LIBRA
                (month == 10 && day >= 23) || (month == 11 && day <= 21) -> SCORPIO
                (month == 11 && day >= 22) || (month == 12 && day <= 21) -> SAGITTARIUS
                else -> CAPRICORN
            }
        }
    }
}