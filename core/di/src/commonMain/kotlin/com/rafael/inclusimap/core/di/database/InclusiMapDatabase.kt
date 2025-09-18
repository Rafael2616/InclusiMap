package com.rafael.inclusimap.core.di.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.rafael.inclusimap.feature.auth.data.dao.LoginDao
import com.rafael.inclusimap.feature.auth.domain.model.LoginEntity
import com.rafael.inclusimap.feature.intro.data.dao.AppIntroDao
import com.rafael.inclusimap.feature.intro.domain.model.AppIntroEntity
import com.rafael.inclusimap.feature.map.map.data.dao.AccessibleLocalsDao
import com.rafael.inclusimap.feature.map.map.data.dao.InclusiMapDao
import com.rafael.inclusimap.feature.map.map.domain.model.AccessibleLocalsEntity
import com.rafael.inclusimap.feature.map.map.domain.model.InclusiMapEntity
import com.rafael.inclusimap.feature.map.search.data.dao.MapSearchDao
import com.rafael.inclusimap.feature.map.search.domain.model.MapSearchEntity
import com.rafael.inclusimap.feature.settings.data.dao.SettingsDao
import com.rafael.inclusimap.feature.settings.domain.model.SettingsEntity

@Database(
    entities = [
        AccessibleLocalsEntity::class,
        InclusiMapEntity::class,
        MapSearchEntity::class,
        SettingsEntity::class,
        LoginEntity::class,
        AppIntroEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
@ConstructedBy(InclusiMapDatabaseConstructor::class)
abstract class InclusiMapDatabase : RoomDatabase() {
    abstract fun accessibleLocalsDao(): AccessibleLocalsDao
    abstract fun inclusiMapDao(): InclusiMapDao
    abstract fun mapSearchDao(): MapSearchDao
    abstract fun settingsDao(): SettingsDao
    abstract fun loginDao(): LoginDao
    abstract fun appIntroDao(): AppIntroDao

    companion object {
        val DATABASE_NAME: String = "inclusimap"
    }
}

// The Room compiler will generate the `actual` implementations :)
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object InclusiMapDatabaseConstructor : RoomDatabaseConstructor<InclusiMapDatabase> {
    override fun initialize(): InclusiMapDatabase
}
