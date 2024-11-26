package com.rafael.inclusimap.core.settings.data.database

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

internal class Migrations {
    companion object {
        val migration1To2 =
            object : Migration(1, 2) {
                override fun migrate(connection: SQLiteConnection) {
                    val addSearchHistoryEnabled =
                        "ALTER TABLE 'settings' ADD COLUMN 'searchHistoryEnabled' INTEGER NOT NULL DEFAULT '1'"

                    connection.execSQL(addSearchHistoryEnabled)
                }
            }

        val migration2To3 =
            object : Migration(2, 3) {
                override fun migrate(connection: SQLiteConnection) {
                    val addIsProfileSettingsTipShown =
                        "ALTER TABLE 'settings' ADD COLUMN 'isProfileSettingsTipShown' INTEGER NOT NULL DEFAULT '0'"

                    connection.execSQL(addIsProfileSettingsTipShown)
                }
            }
    }
}
