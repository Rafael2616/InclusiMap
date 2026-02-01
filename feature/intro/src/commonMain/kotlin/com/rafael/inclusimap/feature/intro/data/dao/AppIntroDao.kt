package com.rafael.inclusimap.feature.intro.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.rafael.inclusimap.feature.intro.domain.model.AppIntroEntity

@Dao
interface AppIntroDao {
    @Query("SELECT * FROM app_intro WHERE id = :id")
    suspend fun getAppIntro(id: Int): AppIntroEntity?

    @Upsert
    suspend fun updateAppIntro(appIntroEntity: AppIntroEntity)
}
