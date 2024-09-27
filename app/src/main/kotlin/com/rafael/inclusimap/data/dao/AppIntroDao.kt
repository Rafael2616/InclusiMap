package com.rafael.inclusimap.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.rafael.inclusimap.domain.AppIntroEntity
import com.rafael.inclusimap.domain.LoginEntity

@Dao
interface AppIntroDao {
    @Query("SELECT * FROM app_intro WHERE id = :id")
    suspend fun getAppIntro(id: Int): AppIntroEntity?

    @Upsert
    suspend fun updateAppIntro(appIntroEntity: AppIntroEntity)
}