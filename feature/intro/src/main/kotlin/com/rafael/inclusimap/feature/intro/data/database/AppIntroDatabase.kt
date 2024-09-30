package com.rafael.inclusimap.feature.intro.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rafael.inclusimap.feature.intro.data.dao.AppIntroDao
import com.rafael.inclusimap.feature.intro.domain.model.AppIntroEntity

@Database(
    entities = [AppIntroEntity::class], version = 1,
    exportSchema = false
)
abstract class AppIntroDatabase : RoomDatabase() {
    abstract fun appIntroDao(): AppIntroDao

    companion object {
        const val DATABASE_NAME = "app_intro_db"
    }
}
