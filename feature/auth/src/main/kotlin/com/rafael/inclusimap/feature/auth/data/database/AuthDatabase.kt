package com.rafael.inclusimap.feature.auth.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rafael.inclusimap.feature.auth.data.dao.LoginDao
import com.rafael.inclusimap.feature.auth.domain.model.LoginEntity

@Database(
    entities = [LoginEntity::class],
    version = 4,
    exportSchema = false,
)
abstract class AuthDatabase : RoomDatabase() {
    abstract fun loginDao(): LoginDao

    companion object {
        const val DATABASE_NAME = "login_db"
    }
}
