package com.magnariuk.mittest.util.config

import com.magnariuk.mittest.data_api.User
import com.magnariuk.mittest.data_api.UserActivity
import com.magnariuk.mittest.util.enums.ActivityTypes
import com.magnariuk.mittest.util.controllers.DbController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service


@Service
class UserService(
    @Autowired private val dbc: DbController,
) {
    val db = dbc.getDB()

    private val passwordEncoder = BCryptPasswordEncoder()

    fun getUserByUsername(username: String): User? {
        return db.getUserByUsername(username)

    }
    fun getUserById(id: Int): User? {
        return db.getUserById(id)

    }

    fun registerUser(username: String, rawPassword: String, displayName: String, email: String ) {
        db.InsertUser(username, hashPassword(rawPassword), displayName, email)
    }
    fun isPasswordValid(user: User, rawPassword: String): Boolean {
        return passwordEncoder.matches(rawPassword, user.password)
    }
    fun hashPassword(password: String): String {
        return passwordEncoder.encode(password)
    }

    fun updateUser(
        user: User,
        username: String? = null,
        rawPassword: String? = null,
        displayName: String? = null,
        email: String? = null
    ) {
        if(username != null){
            addActivity(user, ActivityTypes.USER_EDIT_USERNAME.type, "Змінено ім'я користувача з ${user.username} на $username")
        } else if(rawPassword != null){
            addActivity(user, ActivityTypes.USER_EDIT_PASSWORD.type, "")
        } else if(displayName != null){
            addActivity(user, ActivityTypes.USER_EDIT_DISPLAY_NAME.type, "Змінено видиме ім'я ${user.display_name} на $displayName")
        } else if(email != null){
            addActivity(user, ActivityTypes.USER_EDIT_EMAIL.type, "Змінено пошту ${user.email} на $email")
        }

        val updatedUser = User(
            id = user.id,
            username = username ?: user.username,
            display_name = displayName ?: user.display_name,
            email = email ?: user.email,
            password = rawPassword?.let { hashPassword(it) } ?: user.password,
            created_at = user.created_at
        )

        db.UpdateUser(updatedUser)
    }

    fun addActivity(user: User, activityType: String, description: String) = db.InsertUserActivity(user.id, activityType, description)
    fun getActivities(id: Int): List<UserActivity> = db.getUserActivityByUserId(id)


}