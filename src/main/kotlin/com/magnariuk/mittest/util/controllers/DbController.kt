package com.magnariuk.mittest.util.controllers

import com.magnariuk.mittest.data_api.DB
import com.magnariuk.mittest.data_api.DatabaseController
import org.springframework.stereotype.Service

@Service
class DbController {
    private final val dbc = DatabaseController().apply { init() }
    val db = DB(dbc)

    fun getDB(): DB = db
}