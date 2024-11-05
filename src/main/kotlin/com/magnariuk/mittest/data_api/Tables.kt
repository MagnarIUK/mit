package com.magnariuk.mittest.data_api

import org.jetbrains.exposed.sql.*


object Projects: Table() {
    val project_id: Column<Int> = integer("project_id").autoIncrement()
    val name: Column<String> = varchar("name", 20)
    val description: Column<String> = varchar("description", 1000)
    val created_at: Column<Long> = long("created_at")
    val author_id: Column<Int> = integer("author_id").references(Users.user_id)
    override val primaryKey = PrimaryKey(project_id, name = "projects_pk")
}

object Commits: Table(){
    val commit_id: Column<Int> = integer("commit_id").autoIncrement()
    val project_id: Column<Int> = integer("project_id").references(Projects.project_id)
    val commit_hash: Column<String> = varchar("commit_hash", 256)
    val message: Column<String> = varchar("message", 500)
    val status: Column<String> = varchar("status", 50)
    val author_id: Column<Int> = integer("author_id").references(Users.user_id)
    val commited_at: Column<Long> = long("commited_at")

    override val primaryKey = PrimaryKey(commit_id, name = "commits_pk")
}

object Users: Table() {
    val user_id: Column<Int> = integer("user_id").autoIncrement()
    val username: Column<String> = varchar("username", 500)
    val password: Column<String> = varchar("password", 256)
    val display_name: Column<String?> = varchar("display_name", 50).nullable()
    val email: Column<String?> = varchar("email", 200).nullable()
    val created_at: Column<Long> = long("created_at")
    override val primaryKey = PrimaryKey(user_id, name = "users_pk")
}

object Files: Table(){
    val file_id: Column<Int> = integer("file_id").autoIncrement()
    val commit_id: Column<Int> = integer("commit_id").references(Commits.commit_id)
    val file_path: Column<String> = varchar("file_path", 1000)
    val file_name: Column<String> = varchar("file_name", 50)
    val file_size: Column<Int> = integer("file_size")
    val added_at: Column<Long> = long("added_at")
    override val primaryKey = PrimaryKey(file_id, name = "files_pk")
}

object ProjectAccesses: Table(){
    val access_id: Column<Int> = integer("access_id").autoIncrement()
    val project_id: Column<Int> = integer("project_id").references(Projects.project_id)
    val user_id: Column<Int> = integer("user_id").references(Users.user_id)
    val access_level: Column<Int> = integer("access_level")

    override val primaryKey = PrimaryKey(access_id, name = "project_access_pk")
}

object Tags: Table(){
    val tag_id: Column<Int> = integer("tag_id").autoIncrement()
    val project_id: Column<Int> = integer("project_id").references(Projects.project_id)
    val tag_name: Column<String> = varchar("tag_name", 50)
    val commit_id: Column<Int> = integer("commit_id").references(Commits.commit_id)
    val tagged_at: Column<Long> = long("tagged_at")
    override val primaryKey = PrimaryKey(tag_id, name = "tags_pk")
}

object UserActivities: Table() {
    val activity_id: Column<Int> = integer("activity_id").autoIncrement()
    val user_id: Column<Int> = integer("user_id").references(Users.user_id)
    val activity_type: Column<String> = varchar("activity_type", 100)
    val description: Column<String> = varchar("description", 1000000)
    val created_at: Column<Long> = long("created_at")

    override val primaryKey = PrimaryKey(activity_id, name = "user_activity_pk")
}