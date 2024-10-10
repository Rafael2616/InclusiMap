package com.rafael.inclusimap.feature.auth.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.rafael.inclusimap.feature.auth.data.dao.LoginDao
import com.rafael.inclusimap.feature.auth.domain.model.LoginEntity

@Database(
    entities = [LoginEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AuthDatabase : RoomDatabase() {
    abstract fun loginDao(): LoginDao

    companion object {
        const val DATABASE_NAME = "login_db"
    }
}

internal class Migrations {
    companion object {
        val migration1To2 = object : Migration(1, 2) {
            override fun migrate(connection: SQLiteConnection) {
                val addProfilePictureOptedIn=
                    "ALTER TABLE 'login_db' ADD COLUMN 'showProfilePictureOptedIn' INTEGER NOT NULL DEFAULT '1'"
                val addProfilePicture=
                    "ALTER TABLE 'login_db' ADD COLUMN 'profilePicture' BLOB DEFAULT null"

                connection.execSQL(addProfilePictureOptedIn)
                connection.execSQL(addProfilePicture)
            }
        }
    }
}
