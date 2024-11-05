package com.magnariuk.mittest.util.config

import com.magnariuk.mittest.data_api.User
import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Service
import com.magnariuk.mittest.util.enums.ActivityTypes

@Service
class AuthService(private val session: HttpSession, private val userService: UserService) {

    fun login(username: String, password: String): String {
        val user = userService.getUserByUsername(username)

        if(user!= null){
            if(userService.isPasswordValid(user, password)) {
                session.setAttribute("user", user)
                userService.addActivity(user, ActivityTypes.USER_AUTH.type, "")
                return "true"
            } else{
                return "false:pnv"
            }

        } else{
            return "false:unf"
        }

    }

    fun register(username: String, password: String,display_name: String, email: String): Boolean {
        val user = userService.getUserByUsername(username)
        if( user == null){
            userService.registerUser(username, password, display_name, email)
            userService.addActivity(userService.getUserByUsername(username)!!, ActivityTypes.USER_CREATE_ACCOUNT.type, "")
            return true
        }
        return false
    }

    fun isUserLoggedIn(): Boolean {
        return session.getAttribute("user") != null
    }
    fun getLoggedInUser(): User? {
        return session.getAttribute("user") as? User
    }

    fun logout() {
        userService.addActivity(getLoggedInUser()!!, ActivityTypes.USER_DE_AUTH.type, "")
        session.invalidate()
        session.setAttribute("user", null)
    }
    fun updateLoggedUser(): User? {
        val old_user = getLoggedInUser()
        if(old_user != null){
            val new_user = userService.getUserByUsername(old_user.username)
            session.setAttribute("user", new_user?.copy())
            return new_user
        }
        return null
    }

}