package com.example.bugs.ui.registration

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bugs.R
import com.example.bugs.data.AppDatabase
import com.example.bugs.data.UserEntity
import com.example.bugs.model.Gender
import com.example.bugs.model.Player
import com.example.bugs.model.Zodiac
import com.example.bugs.ui.users.UsersAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class RegistrationFragment : Fragment() {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private var selectedDate: Calendar = Calendar.getInstance()

    private lateinit var layoutUserList: View
    private lateinit var layoutRegistrationForm: View
    private lateinit var rvUsers: RecyclerView
    private lateinit var usersAdapter: UsersAdapter
    private lateinit var cardActiveUser: CardView
    private lateinit var tvActiveUserName: TextView
    private lateinit var tvActiveUserDetails: TextView
    private lateinit var tvEmptyUsers: TextView
    private lateinit var btnAddUser: FloatingActionButton
    private lateinit var btnBackToList: Button
    private lateinit var btnEditActiveUser: Button

    private lateinit var etFullName: EditText
    private lateinit var rgGender: RadioGroup
    private lateinit var spinnerCourse: Spinner
    private lateinit var seekBarDifficulty: SeekBar
    private lateinit var tvDifficultyLabel: TextView
    private lateinit var etBirthDate: EditText
    private lateinit var btnPickDate: Button
    private lateinit var tvZodiacSymbol: TextView
    private lateinit var btnRegister: Button
    private lateinit var tvResult: TextView
    private lateinit var tvFormTitle: TextView

    private var editingUser: UserEntity? = null
    private var activeUser: UserEntity? = null

    private lateinit var courses: Array<String>
    private lateinit var difficultyLevels: Array<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutUserList = view.findViewById(R.id.layoutUserList)
        layoutRegistrationForm = view.findViewById(R.id.layoutRegistrationForm)
        rvUsers = view.findViewById(R.id.rvUsers)
        cardActiveUser = view.findViewById(R.id.cardActiveUser)
        tvActiveUserName = view.findViewById(R.id.tvActiveUserName)
        tvActiveUserDetails = view.findViewById(R.id.tvActiveUserDetails)
        tvEmptyUsers = view.findViewById(R.id.tvEmptyUsers)
        btnAddUser = view.findViewById(R.id.btnAddUser)
        btnBackToList = view.findViewById(R.id.btnBackToList)
        btnEditActiveUser = view.findViewById(R.id.btnEditActiveUser)

        rvUsers.layoutManager = LinearLayoutManager(requireContext())
        usersAdapter = UsersAdapter(
            emptyList(),
            onUserSelected = { user -> selectUser(user) },
            onUserEdit = { user -> startEditingUser(user) },
            onUserDelete = { user -> deleteUser(user) }
        )
        rvUsers.adapter = usersAdapter

        btnAddUser.setOnClickListener {
            startCreatingUser()
        }

        btnBackToList.setOnClickListener {
            showUserList()
        }

        btnEditActiveUser.setOnClickListener {
            activeUser?.let { startEditingUser(it) }
        }

        setupFormViews(view)
        loadUsers()
    }

    override fun onResume() {
        super.onResume()
        loadUsers()
    }

    private fun loadUsers() {
        lifecycleScope.launch {
            val ctx = context ?: return@launch
            val users = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(ctx).userDao().getAllUsers()
            }
            if (!isAdded) return@launch

            if (users.isEmpty()) {
                startCreatingUser()
                btnBackToList.visibility = View.GONE
            } else {
                btnBackToList.visibility = View.VISIBLE
                if (layoutRegistrationForm.visibility != View.VISIBLE) {
                    showUserList()
                }

                tvEmptyUsers.visibility = View.GONE
                rvUsers.visibility = View.VISIBLE
                usersAdapter.updateUsers(users)

                activeUser = users.find { it.isSelected } ?: users.firstOrNull()
                activeUser?.let { updateActiveUserUI(it) }
            }
        }
    }

    private fun selectUser(user: UserEntity) {
        lifecycleScope.launch {
            val ctx = context ?: return@launch
            withContext(Dispatchers.IO) {
                val db = AppDatabase.getInstance(ctx)
                db.userDao().setSelectedUser(user.id)
            }
            if (!isAdded) return@launch

            saveUserToPrefs(user)
            activeUser = user.copy(isSelected = true)
            updateActiveUserUI(user)
            loadUsers()

            Toast.makeText(
                ctx,
                getString(R.string.user_selected_toast, user.fullName),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateActiveUserUI(user: UserEntity) {
        if (!isAdded) return
        cardActiveUser.visibility = View.VISIBLE
        tvActiveUserName.text = user.fullName

        val diffName = if (user.difficulty in difficultyLevels.indices) {
            difficultyLevels[user.difficulty]
        } else {
            user.difficulty.toString()
        }

        tvActiveUserDetails.text = "${user.course} | ${user.gender} | $diffName"
    }

    private fun saveUserToPrefs(user: UserEntity) {
        val ctx = context ?: return
        val prefs = ctx.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        prefs.edit {
            putString("player_name", user.fullName)
                .putInt("player_difficulty", user.difficulty)
                .putLong("active_user_id", user.id)
                .putBoolean("is_registered", true)
        }
    }

    private fun deleteUser(user: UserEntity) {
        lifecycleScope.launch {
            val ctx = context ?: return@launch
            withContext(Dispatchers.IO) {
                val db = AppDatabase.getInstance(ctx)
                db.userDao().deleteUser(user)
            }
            if (!isAdded) return@launch

            val prefs = ctx.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
            if (user.isSelected) {
                val remainingUsers = withContext(Dispatchers.IO) {
                    AppDatabase.getInstance(ctx).userDao().getAllUsers()
                }
                if (remainingUsers.isNotEmpty()) {
                    selectUser(remainingUsers.first())
                } else {
                    prefs.edit {
                        remove("player_name")
                        remove("active_user_id")
                        putBoolean("is_registered", false)
                    }
                    cardActiveUser.visibility = View.GONE
                }
            }

            loadUsers()
            Toast.makeText(ctx, "Пользователь удален", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showUserList() {
        layoutUserList.visibility = View.VISIBLE
        layoutRegistrationForm.visibility = View.GONE
        editingUser = null
    }

    private fun startCreatingUser() {
        editingUser = null
        tvFormTitle.text = "Регистрация"
        btnRegister.text = "Зарегистрироваться"

        etFullName.setText("")
        rgGender.check(R.id.rbMale)
        spinnerCourse.setSelection(0)
        seekBarDifficulty.progress = 0
        etBirthDate.setText("")
        tvZodiacSymbol.visibility = View.GONE
        tvResult.text = ""
        selectedDate = Calendar.getInstance()

        layoutUserList.visibility = View.GONE
        layoutRegistrationForm.visibility = View.VISIBLE
    }

    private fun startEditingUser(user: UserEntity) {
        editingUser = user
        tvFormTitle.text = "Редактирование профиля"
        btnRegister.text = "Сохранить изменения"

        etFullName.setText(user.fullName)
        if (user.gender.contains("Жен", ignoreCase = true) || user.gender.contains("Fem", ignoreCase = true)) {
            rgGender.check(R.id.rbFemale)
        } else {
            rgGender.check(R.id.rbMale)
        }

        val courseIndex = courses.indexOf(user.course)
        if (courseIndex >= 0) {
            spinnerCourse.setSelection(courseIndex)
        }

        seekBarDifficulty.progress = user.difficulty
        tvDifficultyLabel.text = difficultyLevels.getOrElse(user.difficulty) { "" }

        etBirthDate.setText(user.birthDate)
        parseDate(user.birthDate)?.let {
            selectedDate = it
        }

        val zodiac = Zodiac.fromDate(
            selectedDate.get(Calendar.MONTH) + 1,
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )
        if (zodiac.symbol.isNotEmpty()) {
            tvZodiacSymbol.text = zodiac.symbol
            tvZodiacSymbol.visibility = View.VISIBLE
        } else {
            tvZodiacSymbol.visibility = View.GONE
        }

        tvResult.text = getString(
            R.string.form_result,
            user.fullName,
            user.gender,
            user.course,
            difficultyLevels.getOrElse(user.difficulty) { "" },
            user.birthDate,
            user.zodiacName,
            user.zodiacSymbol
        ).trimIndent()

        layoutUserList.visibility = View.GONE
        layoutRegistrationForm.visibility = View.VISIBLE
    }

    @SuppressLint("SetTextI18n")
    private fun setupFormViews(view: View) {
        etFullName = view.findViewById(R.id.etFullName)
        rgGender = view.findViewById(R.id.rgGender)
        spinnerCourse = view.findViewById(R.id.spinnerCourse)
        seekBarDifficulty = view.findViewById(R.id.seekBarDifficulty)
        tvDifficultyLabel = view.findViewById(R.id.tvDifficultyLabel)
        etBirthDate = view.findViewById(R.id.etBirthDate)
        btnPickDate = view.findViewById(R.id.btnPickDate)
        tvZodiacSymbol = view.findViewById(R.id.tvZodiacSymbol)
        btnRegister = view.findViewById(R.id.btnRegister)
        tvResult = view.findViewById(R.id.tvResult)
        tvFormTitle = view.findViewById(R.id.tvFormTitle)

        courses = arrayOf(
            getString(R.string.course1), getString(R.string.course2),
            getString(R.string.course3), getString(R.string.course4)
        )
        spinnerCourse.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            courses
        )

        difficultyLevels = resources.getStringArray(R.array.difficulty_levels_array)
        seekBarDifficulty.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvDifficultyLabel.text = difficultyLevels.getOrElse(progress) { "" }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnPickDate.setOnClickListener {
            val ctx = context ?: return@setOnClickListener
            val dialog = DatePickerDialog(
                ctx,
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
                Toast.makeText(context, getString(R.string.Input_name), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val birthDateText = etBirthDate.text.toString().trim()
            if (birthDateText.isEmpty()) {
                Toast.makeText(context, getString(R.string.input_birthdate), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val parsedDate = parseDate(birthDateText)
            if (parsedDate == null) {
                Toast.makeText(context, getString(R.string.vrong_date_format), Toast.LENGTH_LONG).show()
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

            tvResult.text = getString(
                R.string.form_result,
                player.fullName,
                player.gender.displayName,
                player.course,
                difficultyLevels[player.difficulty],
                player.birthDate,
                player.zodiac.displayName,
                player.zodiac.symbol
            ).trimIndent()

            if (zodiac.symbol.isNotEmpty()) {
                tvZodiacSymbol.text = zodiac.symbol
                tvZodiacSymbol.visibility = View.VISIBLE
            } else {
                tvZodiacSymbol.visibility = View.GONE
            }

            lifecycleScope.launch {
                val ctx = context ?: return@launch
                val currentEditing = editingUser

                if (currentEditing == null) {
                    val userEntity = UserEntity(
                        fullName = player.fullName,
                        gender = player.gender.displayName,
                        course = player.course,
                        difficulty = player.difficulty,
                        birthDate = player.birthDate,
                        zodiacSymbol = player.zodiac.symbol,
                        zodiacName = player.zodiac.displayName,
                        isSelected = true
                    )
                    val newUserId = withContext(Dispatchers.IO) {
                        val db = AppDatabase.getInstance(ctx)
                        val id = db.userDao().insertUser(userEntity)
                        db.userDao().setSelectedUser(id)
                        id
                    }
                    if (!isAdded) return@launch

                    saveUserToPrefs(userEntity.copy(id = newUserId))
                    showUserList()
                    loadUsers()
                    Toast.makeText(ctx, "Игрок зарегистрирован!", Toast.LENGTH_SHORT).show()
                } else {
                    val updatedEntity = currentEditing.copy(
                        fullName = player.fullName,
                        gender = player.gender.displayName,
                        course = player.course,
                        difficulty = player.difficulty,
                        birthDate = player.birthDate,
                        zodiacSymbol = player.zodiac.symbol,
                        zodiacName = player.zodiac.displayName
                    )
                    withContext(Dispatchers.IO) {
                        val db = AppDatabase.getInstance(ctx)
                        db.userDao().updateUser(updatedEntity)
                    }
                    if (!isAdded) return@launch

                    if (updatedEntity.isSelected) {
                        saveUserToPrefs(updatedEntity)
                    }
                    editingUser = null
                    showUserList()
                    loadUsers()
                    Toast.makeText(ctx, "Профиль обновлен!", Toast.LENGTH_SHORT).show()
                }
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
        } catch (_: Exception) {
            null
        }
    }
}
