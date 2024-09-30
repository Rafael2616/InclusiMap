package com.rafael.inclusimap.core.settings.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rafael.inclusimap.core.settings.data.dao.SettingsDao
import com.rafael.inclusimap.core.settings.domain.model.SettingsEntity

@Database(
    entities = [SettingsEntity::class], version = 1,
    exportSchema = false
)
abstract class SettingsDatabase : RoomDatabase() {
    abstract fun settingsDao(): SettingsDao

    companion object {
        const val DATABASE_NAME = "settings_db"
    }
}
