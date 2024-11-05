package com.rafael.inclusimap.core.settings.domain.repository

import com.rafael.inclusimap.core.settings.domain.model.SettingsEntity

interface SettingsRepository {
    suspend fun getAllSettingsValues(id: Int): SettingsEntity?

    suspend fun setAllSettingsValues(settingsEntity: SettingsEntity)
}
