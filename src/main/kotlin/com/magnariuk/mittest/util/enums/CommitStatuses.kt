package com.magnariuk.mittest.util.enums

enum class CommitStatuses(val label: String, val id: String) {
    PENDING("Очікується дія...", "pending"),
    ACCEPTED("Прийнято", "accepted"),
    REJECTED("Відхилено", "rejected"),
    AUTO_ACCEPTED("Авоматично прийнято", "auto-accepted");
    companion object {
        fun getLabel(id: String): String? = CommitStatuses.entries.find { it.id == id }?.label
    }
}