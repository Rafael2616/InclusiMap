package com.rafael.inclusimap.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.rafael.inclusimap.domain.LoginEntity

@Dao
interface LoginDao {
    @Query("SELECT * FROM login_db WHERE id = :id")
    suspend fun getLoginInfo(id: Int): LoginEntity?

    @Upsert
    suspend fun updateLoginInfo(loginEntity: LoginEntity)
}