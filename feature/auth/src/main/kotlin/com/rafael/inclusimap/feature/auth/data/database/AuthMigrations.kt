package com.rafael.inclusimap.feature.auth.data.database

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

internal class AuthMigrations {
    companion object {
        val migration1To2 = object : Migration(1, 2) {
            override fun migrate(connection: SQLiteConnection) {
                val addProfilePictureOptedIn =
                    "ALTER TABLE 'login_db' ADD COLUMN 'showProfilePictureOptedIn' INTEGER NOT NULL DEFAULT '1'"
                val addProfilePicture =
                    "ALTER TABLE 'login_db' ADD COLUMN 'profilePicture' BLOB DEFAULT null"

                connection.execSQL(addProfilePictureOptedIn)
                connection.execSQL(addProfilePicture)
            }
        }
        val migration2To3 = object : Migration(2, 3) {
            override fun migrate(connection: SQLiteConnection) {
                val addUserPathID =
                    "ALTER TABLE 'login_db' ADD COLUMN 'userPathID' TEXT DEFAULT 'null'"

                connection.execSQL(addUserPathID)
            }
        }

        val migration3To4 = object : Migration(3, 4) {
            override fun migrate(connection: SQLiteConnection) {
                val addRecoveryHash =
                    "ALTER TABLE 'login_db' ADD COLUMN 'recoveryToken' TEXT DEFAULT 'null'"
                val addTokenHash =
                    "ALTER TABLE 'login_db' ADD COLUMN 'tokenHash' TEXT DEFAULT 'null'"
                val addExpirationDate =
                    "ALTER TABLE 'login_db' ADD COLUMN 'tokenExpirationDate' INTEGER DEFAULT 'null'"

                connection.execSQL(addRecoveryHash)
                connection.execSQL(addTokenHash)
                connection.execSQL(addExpirationDate)
            }
        }
        val migration4To5 = object : Migration(3, 4) {
            override fun migrate(connection: SQLiteConnection) {
                val addIsBanned =
                    "ALTER TABLE 'login_db' ADD COLUMN 'isBanned' INTEGER NOT NULL DEFAULT '0'"
                val addIsAdmin =
                    "ALTER TABLE 'login_db' ADD COLUMN 'isAdmin' INTEGER NOT NULL DEFAULT '0'"
                val addShowFirstTimeAnimation =
                    "ALTER TABLE 'login_db' ADD COLUMN 'showFirstTimeAnimation' INTEGER NOT NULL DEFAULT '1'"

                connection.execSQL(addIsBanned)
                connection.execSQL(addIsAdmin)
                connection.execSQL(addShowFirstTimeAnimation)
            }
        }
    }
}
