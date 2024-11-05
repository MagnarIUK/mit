package com.magnariuk.mittest.util.enums

enum class AccessLevels(val displayName: String, val level: Int) {
    OWNER("Власник", 3),
    COMMITER("Вкладач", 2),
    VIEWER("Глядач", 1);
    companion object {
        fun getDisplayName(level: Int): String {
            return entries.find { it.level == level }?.displayName!!
        }
    }
}
enum class AccessLevelsAdd(val displayName: String, val level: Int) {
    COMMITER("Вкладач", 2),
    VIEWER("Глядач", 1);
    companion object {
        fun getDisplayName(level: Int): String {
            return entries.find { it.level == level }?.displayName!!
        }
    }
}
