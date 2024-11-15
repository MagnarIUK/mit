package com.magnariuk.mittest.util.enums

enum class ActivityTypes(val label: String, val type: String, val private: Boolean) {
    USER_CREATE_ACCOUNT("Створення аккаунту", "create-account", true),
    USER_AUTH("Вхід в аккаунт", "user-auth", true),
    USER_DE_AUTH("Вихід з аккаунту", "user-deauth", true),
    USER_EDIT_USERNAME("Змінено ім'я користувача", "edit-username", true),
    USER_EDIT_PASSWORD("Змінено пароль", "edit-password", true),
    USER_EDIT_DISPLAY_NAME("Змінено видиме ім'я", "edit-display-name", true),
    USER_EDIT_EMAIL("Змінено електронну пошту", "edit-email", true),

    PROJECT_CREATE_NEW("Створено новий проєкт", "create-new-project", false),
    PROJECT_EDIT_NAME("Зміна назви проєкту", "edit-project-name", false),
    PROJECT_EDIT_DESCRIPTION("Зміна опису проєкту", "edit-project-description", false),

    PROJECT_ADD_MEMBER("Додано користувача до проєкту", "project-add-member", false),
    PROJECT_ADD_MEMBER_ACCESS("Надано права доступу до проєкту", "project-add-member-access", false),
    PROJECT_MEMBER_ACCESS_EDIT("Змінено права доступу", "project-member-access-edit", false),
    PROJECT_EDIT_MEMBER("Змінено користувача", "project-edit-member", false),
    PROJECT_MEMBER_ACCESS_REVOKED("Позбавлено доступу до проєкту", "project-member-access-revoked", false),
    PROJECT_REMOVE_MEMBER("Прибрано користувача з проєкту", "project-remove-member", false),
    PROJECT_OWNER_REVOKED("Позбавлено прав власника", "project-owner-revoked", false),
    PROJECT_OWNER_ADDED("Надано права власника", "project-owner-added", false),
    PROJECT_DELETED("Видалено проєкт", "project-deleted", false),


    COMMIT_MEMBER_CREATED("Ваш внесок створено", "commit-member-created", false),
    COMMIT_MEMBER_EDITED("Ваш внесок змінено", "commit-member-edited", false),
    COMMIT_MEMBER_DELETED("Ваш внесок видалено", "commit-member-deleted", false),
    COMMIT_MEMBER_ACCEPTED("Ваш внесок прийнято", "commit-member-accepted", false),
    COMMIT_MEMBER_REJECTED("Ваш внесок відхилено", "commit-member-rejected", false),

    COMMIT_CREATED("Сворено внесок", "commit-created", false),
    COMMIT_EDITED("Внесок змінено", "commit-edit", false),
    COMMIT_DELETED("Внесок видалено", "commit-deleted", false),
    COMMIT_ACCEPTED("Внесок прийнято", "commit-accepted", false),
    COMMIT_REJECTED("Внесок відхилено", "commit-rejected", false),

    ;

    companion object {
        fun getDisplayName(type: String): String {
            return ActivityTypes.entries.find { it.type == type }?.label ?: ""
        }
        fun isPrivate(type: String): Boolean {
            return  ActivityTypes.entries.find { it.type == type }!!.private
        }
    }

}