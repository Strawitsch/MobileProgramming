package com.example.bugs

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

data class Player(
    val fullName: String,
    val gender: String,
    val course: String,
    val difficulty: Int,
    val birthDate: String,
    val zodiac: String
)

class MainActivity : AppCompatActivity() {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private var selectedDate: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val rgGender = findViewById<RadioGroup>(R.id.rgGender)
        val spinnerCourse = findViewById<Spinner>(R.id.spinnerCourse)
        val seekBarDifficulty = findViewById<SeekBar>(R.id.seekBarDifficulty)
        val tvDifficultyLabel = findViewById<TextView>(R.id.tvDifficultyLabel)
        val etBirthDate = findViewById<EditText>(R.id.etBirthDate)
        val btnPickDate = findViewById<Button>(R.id.btnPickDate)
        val tvZodiacSymbol = findViewById<TextView>(R.id.tvZodiacSymbol)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        val courses = arrayOf("1 курс", "2 курс", "3 курс", "4 курс")
        spinnerCourse.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            courses
        )

        val difficultyLevels = arrayOf("Очень легкий", "Легкий", "Средний", "Сложный", "Очень сложный")
        seekBarDifficulty.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvDifficultyLabel.text = difficultyLevels[progress]
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnPickDate.setOnClickListener {
            val dialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    selectedDate = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }
                    etBirthDate.setText(dateFormat.format(selectedDate.time))
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            )
            dialog.show()
        }

        btnRegister.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            if (fullName.isEmpty()) {
                Toast.makeText(this, "Введите ФИО", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val birthDateText = etBirthDate.text.toString().trim()
            if (birthDateText.isEmpty()) {
                Toast.makeText(this, "Введите или выберите дату рождения", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val parsedDate = parseDate(birthDateText)
            if (parsedDate == null) {
                Toast.makeText(this, "Неверный формат даты. Используйте ДД.ММ.ГГГГ", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            selectedDate = parsedDate

            val gender = when (rgGender.checkedRadioButtonId) {
                R.id.rbMale -> "Мужской"
                R.id.rbFemale -> "Женский"
                else -> "Не указан"
            }

            val course = spinnerCourse.selectedItem.toString()
            val difficulty = seekBarDifficulty.progress
            val birthDate = dateFormat.format(selectedDate.time)

            val zodiac = getZodiac(
                selectedDate.get(Calendar.MONTH) + 1,
                selectedDate.get(Calendar.DAY_OF_MONTH)
            )

            val player = Player(fullName, gender, course, difficulty, birthDate, zodiac)

            tvResult.text = """
                ФИО: ${player.fullName}
                Пол: ${player.gender}
                Курс: ${player.course}
                Уровень сложности: ${difficultyLevels[player.difficulty]}
                Дата рождения: ${player.birthDate}
                Знак зодиака: ${player.zodiac} ${getZodiacSymbol(player.zodiac)}
            """.trimIndent()

            val symbol = getZodiacSymbol(zodiac)
            if (symbol.isNotEmpty()) {
                tvZodiacSymbol.text = symbol
                tvZodiacSymbol.visibility = View.VISIBLE
            } else {
                tvZodiacSymbol.visibility = View.GONE
            }
        }
    }

    private fun parseDate(text: String): Calendar? {
        return try {
            val date = dateFormat.parse(text) ?: return null
            val cal = Calendar.getInstance()
            cal.time = date
            val check = dateFormat.format(cal.time)
            if (check != text) null else cal
        } catch (e: Exception) {
            null
        }
    }

    private fun getZodiac(month: Int, day: Int): String {
        return when {
            (month == 1 && day >= 20) || (month == 2 && day <= 18) -> "Водолей"
            (month == 2 && day >= 19) || (month == 3 && day <= 20) -> "Рыбы"
            (month == 3 && day >= 21) || (month == 4 && day <= 19) -> "Овен"
            (month == 4 && day >= 20) || (month == 5 && day <= 20) -> "Телец"
            (month == 5 && day >= 21) || (month == 6 && day <= 20) -> "Близнецы"
            (month == 6 && day >= 21) || (month == 7 && day <= 22) -> "Рак"
            (month == 7 && day >= 23) || (month == 8 && day <= 22) -> "Лев"
            (month == 8 && day >= 23) || (month == 9 && day <= 22) -> "Дева"
            (month == 9 && day >= 23) || (month == 10 && day <= 22) -> "Весы"
            (month == 10 && day >= 23) || (month == 11 && day <= 21) -> "Скорпион"
            (month == 11 && day >= 22) || (month == 12 && day <= 21) -> "Стрелец"
            else -> "Козерог"
        }
    }

    private fun getZodiacSymbol(zodiac: String): String {
        return when (zodiac) {
            "Водолей" -> "♒"
            "Рыбы" -> "♓"
            "Овен" -> "♈"
            "Телец" -> "♉"
            "Близнецы" -> "♊"
            "Рак" -> "♋"
            "Лев" -> "♌"
            "Дева" -> "♍"
            "Весы" -> "♎"
            "Скорпион" -> "♏"
            "Стрелец" -> "♐"
            "Козерог" -> "♑"
            else -> ""
        }
    }
}