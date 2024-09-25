package com.rafael.inclusimap.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rafael.inclusimap.data.dao.AppIntroDao
import com.rafael.inclusimap.domain.AppIntroEntity

@Database(
    entities = [AppIntroEntity::class], version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appIntroDao(): AppIntroDao

    companion object {
        const val DATABASE_NAME = "app_intro"
    }

}