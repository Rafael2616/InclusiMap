package com.rafael.inclusimap.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rafael.inclusimap.data.dao.AppIntroDao
import com.rafael.inclusimap.data.dao.LoginDao
import com.rafael.inclusimap.domain.AppIntroEntity
import com.rafael.inclusimap.domain.LoginEntity

@Database(
    entities = [AppIntroEntity::class, LoginEntity::class], version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appIntroDao(): AppIntroDao
    abstract fun loginDao(): LoginDao


    companion object {
        const val DATABASE_NAME = "app_db"
    }

}