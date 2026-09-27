package com.example.bugs.ui.registration

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.bugs.R
import com.example.bugs.model.Gender
import com.example.bugs.model.Player
import com.example.bugs.model.Zodiac
import java.text.SimpleDateFormat
import java.util.*

class RegistrationFragment : Fragment() {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private var selectedDate: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etFullName = view.findViewById<EditText>(R.id.etFullName)
        val rgGender = view.findViewById<RadioGroup>(R.id.rgGender)
        val spinnerCourse = view.findViewById<Spinner>(R.id.spinnerCourse)
        val seekBarDifficulty = view.findViewById<SeekBar>(R.id.seekBarDifficulty)
        val tvDifficultyLabel = view.findViewById<TextView>(R.id.tvDifficultyLabel)
        val etBirthDate = view.findViewById<EditText>(R.id.etBirthDate)
        val btnPickDate = view.findViewById<Button>(R.id.btnPickDate)
        val tvZodiacSymbol = view.findViewById<TextView>(R.id.tvZodiacSymbol)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val tvResult = view.findViewById<TextView>(R.id.tvResult)

        val courses = arrayOf("1 курс", "2 курс", "3 курс", "4 курс")
        spinnerCourse.adapter = ArrayAdapter(
            requireContext(),
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
                requireContext(),
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
                Toast.makeText(context, "Введите ФИО", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val birthDateText = etBirthDate.text.toString().trim()
            if (birthDateText.isEmpty()) {
                Toast.makeText(context, "Введите дату рождения", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val parsedDate = parseDate(birthDateText)
            if (parsedDate == null) {
                Toast.makeText(context, "Неверный формат даты", Toast.LENGTH_LONG).show()
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

            val prefs = requireContext().getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("player_name", player.fullName)
                .putInt("player_difficulty", player.difficulty)
                .putBoolean("is_registered", true)
                .apply()
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