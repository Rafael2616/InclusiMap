package com.rafael.inclusimap.data.repository

import com.rafael.inclusimap.data.dao.AppIntroDao
import com.rafael.inclusimap.domain.AppIntroEntity
import com.rafael.inclusimap.domain.repository.AppIntroRepository

class AppIntroRepositoryImpl(
    private val appIntroDao: AppIntroDao
): AppIntroRepository {
    override suspend fun getAppIntro(id: Int): AppIntroEntity? {
        return appIntroDao.getAppIntro(id)
    }
    override suspend fun updateAppIntro(appIntroEntity: AppIntroEntity) {
        appIntroDao.updateAppIntro(appIntroEntity)
    }
}