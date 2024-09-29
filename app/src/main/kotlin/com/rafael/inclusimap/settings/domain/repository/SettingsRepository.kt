package com.rafael.inclusimap.settings.domain.repository

import com.rafael.inclusimap.settings.domain.model.SettingsEntity

interface SettingsRepository {

    suspend fun getAllSettingsValues(id: Int): SettingsEntity?

    suspend fun setAllSettingsValues(settingsEntity: SettingsEntity)
}
