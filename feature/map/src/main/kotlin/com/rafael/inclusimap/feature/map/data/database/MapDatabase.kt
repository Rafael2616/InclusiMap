package com.rafael.inclusimap.feature.map.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rafael.inclusimap.feature.map.data.dao.AccessibleLocalsDao
import com.rafael.inclusimap.feature.map.data.dao.InclusiMapDao
import com.rafael.inclusimap.feature.map.domain.AccessibleLocalsEntity
import com.rafael.inclusimap.feature.map.domain.InclusiMapEntity

@Database(
    entities = [AccessibleLocalsEntity::class, InclusiMapEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class MapDatabase : RoomDatabase() {
    abstract fun accessibleLocalsDao(): AccessibleLocalsDao

    abstract fun inclusiMapDao(): InclusiMapDao

    companion object {
        const val DATABASE_NAME = "map_db"
    }
}
