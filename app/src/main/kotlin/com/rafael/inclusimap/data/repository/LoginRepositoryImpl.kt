package com.rafael.inclusimap.data.repository

import com.rafael.inclusimap.data.dao.LoginDao
import com.rafael.inclusimap.domain.LoginEntity
import com.rafael.inclusimap.domain.repository.LoginRepository

class LoginRepositoryImpl(
    private val loginDao: LoginDao
) : LoginRepository {
    override suspend fun getLoginInfo(id: Int): LoginEntity? {
        return loginDao.getLoginInfo(id)
    }
    override suspend fun updateLoginInfo(loginEntity: LoginEntity) {
        loginDao.updateLoginInfo(loginEntity)
    }
}