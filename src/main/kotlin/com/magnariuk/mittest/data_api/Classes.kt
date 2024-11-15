package com.magnariuk.mittest.data_api

import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails


data class Project(
    val project_id: Int,
    val name: String,
    val description: String,
    val author_id: Int,
    val created_at: Long
)

data class Commit(
    val commit_id: Int,
    val project_id: Int,
    val commit_hash: String,
    val message: String,
    val status: String,
    val author_id: Int,
    val commited_at: Long,
)


data class User(
    val id: Int,
    val username: String,
    val password: String,
    val display_name: String?,
    val email: String?,
    val created_at: Long,
)

data class File(
    val file_id: Int,
    val commit_id: Int,
    val file_path: String,
    val file_name: String,
    val file_size: Long,
    val added_at: Long
)

data class ProjectAccess(
    val access_id: Int,
    val project_id: Int,
    val user_id: Int,
    val access_level: Int
)


data class UserActivity(
    val activity_id: Int,
    val user_id: Int,
    val activity_type: String,
    val description: String,
    val created_at: Long
)

