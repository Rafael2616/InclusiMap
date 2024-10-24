package com.rafael.inclusimap.feature.map.search.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rafael.inclusimap.feature.map.search.data.dao.MapSearchDao
import com.rafael.inclusimap.feature.map.search.domain.model.MapSearchEntity

@Database(
    entities = [MapSearchEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class MapSearchDatabase : RoomDatabase() {
    abstract fun mapSearchDao(): MapSearchDao

    companion object {
        const val DATABASE_NAME = "map_search_db"
    }
}
