package com.rafael.inclusimap.feature.auth.data.repository

import com.rafael.inclusimap.feature.auth.data.dao.LoginDao
import com.rafael.inclusimap.feature.auth.domain.model.LoginEntity
import com.rafael.inclusimap.feature.auth.domain.repository.LoginRepository

class LoginRepositoryImpl(
    private val loginDao: LoginDao,
) : LoginRepository {
    override suspend fun getLoginInfo(id: Int): LoginEntity? = loginDao.getLoginInfo(id)

    override suspend fun updateLoginInfo(loginEntity: LoginEntity) {
        loginDao.updateLoginInfo(loginEntity)
    }
}
