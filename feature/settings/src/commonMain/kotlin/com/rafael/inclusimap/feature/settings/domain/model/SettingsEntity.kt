package com.rafael.inclusimap.feature.settings.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    var id: Int,
    var isDarkThemeOn: Boolean,
    var isDynamicColorsOn: Boolean,
    var isFollowingSystemOn: Boolean,
    var mapType: Int,
    var appVersion: String,
    var searchHistoryEnabled: Boolean,
    var isProfileSettingsTipShown: Boolean,
) {
    companion object {
        fun getDefaultSettings(): SettingsEntity = SettingsEntity(
            id = 1,
            isDarkThemeOn = true,
            isDynamicColorsOn = true,
            isFollowingSystemOn = true,
            appVersion = "0",
            mapType = 1,
            searchHistoryEnabled = true,
            isProfileSettingsTipShown = false,
        )
    }
}
