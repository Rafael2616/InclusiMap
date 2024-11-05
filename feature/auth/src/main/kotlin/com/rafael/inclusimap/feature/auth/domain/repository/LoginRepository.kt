package com.rafael.inclusimap.feature.auth.domain.repository

import com.rafael.inclusimap.feature.auth.domain.model.LoginEntity

interface LoginRepository {
    suspend fun getLoginInfo(id: Int): LoginEntity?

    suspend fun updateLoginInfo(loginEntity: LoginEntity)
}
