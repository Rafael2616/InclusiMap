package com.rafael.inclusimap.core.settings.data.repository

import com.rafael.inclusimap.core.settings.data.dao.SettingsDao
import com.rafael.inclusimap.core.settings.domain.model.SettingsEntity
import com.rafael.inclusimap.core.settings.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val settingsDao: SettingsDao,
) : SettingsRepository {
    init {
        println("Settings repository has been initialized")
    }

    override suspend fun getAllSettingsValues(id: Int): SettingsEntity? = settingsDao.getSettingsValues(id)

    override suspend fun setAllSettingsValues(settingsEntity: SettingsEntity) {
        settingsDao.setAllSettingsValues(settingsEntity)
    }
}
