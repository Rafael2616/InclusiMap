package com.rafael.inclusimap.feature.map.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rafael.inclusimap.feature.map.data.dao.AccessibleLocalsDao
import com.rafael.inclusimap.feature.map.domain.AccessibleLocalsEntity

@Database(
    entities = [AccessibleLocalsEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class MapDatabase : RoomDatabase() {
    abstract fun accessibleLocalsDao(): AccessibleLocalsDao

    companion object {
        const val DATABASE_NAME = "map_db"
    }
}
