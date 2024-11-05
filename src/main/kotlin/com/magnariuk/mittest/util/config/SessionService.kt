package com.magnariuk.mittest.util.config

import com.magnariuk.mittest.data_api.Converter
import com.magnariuk.mittest.data_api.DB
import com.magnariuk.mittest.data_api.DatabaseController
import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Service
class SessionService {


    fun getHttpSession(): HttpSession {
        val attributes = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes
        return attributes.request.session
    }

    fun storeDataInSession(key: String, value: Any){
        val session = getHttpSession()
        session.setAttribute(key, value)
    }

    fun getDataFromSession(key: String): Any? {
        val session = getHttpSession()
        return session.getAttribute(key)
    }

}