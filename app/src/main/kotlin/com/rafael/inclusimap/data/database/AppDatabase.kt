//package com.rafael.inclusimap.data.database
//
//import androidx.room.Database
//import androidx.room.RoomDatabase
//import com.rafael.inclusimap.data.dao.AppIntroDao
//import com.rafael.inclusimap.data.dao.LoginDao
//import com.rafael.inclusimap.domain.AppIntroEntity
//import com.rafael.inclusimap.domain.LoginEntity
//import com.rafael.inclusimap.core.settings.data.data.dao.SettingsDao
//import com.rafael.inclusimap.core.settings.domain.model.SettingsEntity
//
//@Database(
//    entities = [AppIntroEntity::class, LoginEntity::class, com.rafael.inclusimap.core.settings.domain.model.SettingsEntity::class], version = 2,
//    exportSchema = false
//)
//abstract class AppDatabase : RoomDatabase() {
//    abstract fun appIntroDao(): AppIntroDao
//    abstract fun loginDao(): LoginDao
//    abstract fun settingsDao(): com.rafael.inclusimap.core.settings.data.data.dao.SettingsDao
//
//    companion object {
//        const val DATABASE_NAME = "app_db"
//    }
//
//}
