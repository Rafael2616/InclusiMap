package com.rafael.inclusimap.core.settings.data.database

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

internal class Migrations {
    companion object {
        val migration1To2 = object : Migration(1, 2) {
            override fun migrate(connection: SQLiteConnection) {
                val addSearchHistoryEnabled =
                    "ALTER TABLE 'settings' ADD COLUMN 'searchHistoryEnabled' INTEGER NOT NULL DEFAULT '1'"

                connection.execSQL(addSearchHistoryEnabled)
            }
        }
    }
}
