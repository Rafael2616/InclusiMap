package com.rafael.inclusimap.feature.auth.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "login_db")
data class LoginEntity(
    @PrimaryKey
    var id: Int,
    var isLoggedIn: Boolean,
    var userName: String?,
    var userPassword: String?,
    var userEmail: String?,
    var userId: String?
) {
    companion object {
        fun getDefault() = LoginEntity(
            id = 1,
            isLoggedIn = false,
            userName = null,
            userPassword = null,
            userEmail = null,
            userId = null
        )
    }
}
