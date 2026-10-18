package com.example.bugs.ui.users

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bugs.R
import com.example.bugs.data.UserEntity

class UsersAdapter(
    private var users: List<UserEntity>,
    private val onUserSelected: (UserEntity) -> Unit,
    private val onUserEdit: (UserEntity) -> Unit,
    private val onUserDelete: (UserEntity) -> Unit
) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rbSelected: RadioButton = view.findViewById(R.id.rbUserSelected)
        val tvName: TextView = view.findViewById(R.id.tvUserName)
        val tvDetails: TextView = view.findViewById(R.id.tvUserDetails)
        val tvZodiac: TextView = view.findViewById(R.id.tvUserZodiac)
        val btnEdit: ImageView = view.findViewById(R.id.btnEditUser)
        val btnDelete: ImageView = view.findViewById(R.id.btnDeleteUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.tvName.text = user.fullName

        val difficultyLevels = holder.itemView.context.resources.getStringArray(R.array.difficulty_levels_array)
        val diffName = if (user.difficulty in difficultyLevels.indices) {
            difficultyLevels[user.difficulty]
        } else {
            user.difficulty.toString()
        }

        holder.tvDetails.text = "${user.course} | ${user.gender} | $diffName"
        holder.tvZodiac.text = user.zodiacSymbol
        holder.rbSelected.isChecked = user.isSelected

        holder.itemView.setOnClickListener {
            onUserSelected(user)
        }
        holder.rbSelected.setOnClickListener {
            onUserSelected(user)
        }
        holder.btnEdit.setOnClickListener {
            onUserEdit(user)
        }
        holder.btnDelete.setOnClickListener {
            onUserDelete(user)
        }
    }

    override fun getItemCount(): Int = users.size

    fun updateUsers(newUsers: List<UserEntity>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
