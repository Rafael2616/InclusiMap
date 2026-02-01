package com.rafael.inclusimap.feature.settings.domain.repository

import com.rafael.inclusimap.feature.settings.domain.model.SettingsEntity

interface SettingsRepository {
    suspend fun getAllSettingsValues(id: Int): SettingsEntity?

    suspend fun setAllSettingsValues(settingsEntity: SettingsEntity)
}
