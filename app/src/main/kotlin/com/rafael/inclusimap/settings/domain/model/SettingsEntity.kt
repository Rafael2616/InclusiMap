package com.rafael.inclusimap.settings.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    var id: Int,
    var isDarkThemeOn: Boolean,
    var isDynamicColorsOn: Boolean,
    var isFollowingSystemOn: Boolean,
    var appVersion: String,
) {
    companion object {
        fun getDefaultSettings(): SettingsEntity = SettingsEntity(
            id = 1,
            isDarkThemeOn = true,
            isDynamicColorsOn = true,
            isFollowingSystemOn = true,
            appVersion = "0",
        )
    }
}


