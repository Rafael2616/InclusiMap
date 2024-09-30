package com.rafael.inclusimap.feature.auth.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.rafael.inclusimap.feature.auth.domain.model.LoginEntity

@Dao
interface LoginDao {
    @Query("SELECT * FROM login_db WHERE id = :id")
    suspend fun getLoginInfo(id: Int): LoginEntity?

    @Upsert
    suspend fun updateLoginInfo(loginEntity: LoginEntity)
}
