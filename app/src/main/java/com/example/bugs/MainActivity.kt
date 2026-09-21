package com.example.bugs

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.bugs.model.Gender
import com.example.bugs.model.Player
import com.example.bugs.model.Zodiac
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private var selectedDate: Calendar = Calendar.getInstance()

    @SuppressLint("SetTextI18n")
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
                R.id.rbMale -> Gender.MALE
                R.id.rbFemale -> Gender.FEMALE
                else -> Gender.UNKNOWN
            }

            val course = spinnerCourse.selectedItem.toString()
            val difficulty = seekBarDifficulty.progress
            val birthDate = dateFormat.format(selectedDate.time)

            val zodiac = Zodiac.fromDate(
                selectedDate.get(Calendar.MONTH) + 1,
                selectedDate.get(Calendar.DAY_OF_MONTH)
            )

            val player = Player(fullName, gender, course, difficulty, birthDate, zodiac)

            tvResult.text = """
                ФИО: ${player.fullName}
                Пол: ${player.gender.displayName}
                Курс: ${player.course}
                Уровень сложности: ${difficultyLevels[player.difficulty]}
                Дата рождения: ${player.birthDate}
                Знак зодиака: ${player.zodiac.displayName} ${player.zodiac.symbol}
            """.trimIndent()

            if (zodiac.symbol.isNotEmpty()) {
                tvZodiacSymbol.text = zodiac.symbol
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
}