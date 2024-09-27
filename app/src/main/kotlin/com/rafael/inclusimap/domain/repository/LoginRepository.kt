package com.rafael.inclusimap.domain.repository

import com.rafael.inclusimap.domain.LoginEntity

interface LoginRepository {
    suspend fun getLoginInfo(id: Int): LoginEntity?
    suspend fun updateLoginInfo(loginEntity: LoginEntity)
}