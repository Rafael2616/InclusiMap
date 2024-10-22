package com.rafael.inclusimap.feature.map.map.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rafael.inclusimap.feature.map.map.data.dao.AccessibleLocalsDao
import com.rafael.inclusimap.feature.map.map.data.dao.InclusiMapDao
import com.rafael.inclusimap.feature.map.map.domain.AccessibleLocalsEntity
import com.rafael.inclusimap.feature.map.map.domain.InclusiMapEntity

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
