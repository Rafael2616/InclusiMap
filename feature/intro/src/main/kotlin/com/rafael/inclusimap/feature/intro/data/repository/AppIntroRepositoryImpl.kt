package com.rafael.inclusimap.feature.intro.data.repository

import com.rafael.inclusimap.feature.intro.data.dao.AppIntroDao
import com.rafael.inclusimap.feature.intro.domain.model.AppIntroEntity
import com.rafael.inclusimap.feature.intro.domain.repository.AppIntroRepository

class AppIntroRepositoryImpl(
    private val appIntroDao: AppIntroDao,
) : AppIntroRepository {
    override suspend fun getAppIntro(id: Int): AppIntroEntity? = appIntroDao.getAppIntro(id)
    override suspend fun updateAppIntro(appIntroEntity: AppIntroEntity) {
        appIntroDao.updateAppIntro(appIntroEntity)
    }
}
