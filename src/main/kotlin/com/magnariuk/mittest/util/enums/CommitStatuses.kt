package com.magnariuk.mittest.util.enums

enum class CommitStatuses(val label: String, val id: String) {
    PENDING("Очікується дія...", "pending"),
    ACCEPTED("Прийнято", "accepted"),
    REJECTED("Відхилено", "rejected"),
    AUTO_ACCEPTED("Автоматично прийнято", "auto-accepted");
    companion object {
        fun getById(id: String): CommitStatuses? = CommitStatuses.entries.find { it.id == id }
        fun getLabel(id: String): String? = CommitStatuses.entries.find { it.id == id }?.label
    }
}

enum class CommitStatusesCOMBO(val label: String, val id: String) {
    PENDING("Очікується дія...", "pending"),
    ACCEPTED("Прийнято", "accepted"),
    REJECTED("Відхилено", "rejected");
    companion object {
        fun getLabel(id: String): String? = CommitStatuses.entries.find { it.id == id }?.label
    }
}