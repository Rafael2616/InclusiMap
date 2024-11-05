package com.rafael.inclusimap.feature.map.search.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.rafael.inclusimap.feature.map.search.domain.model.MapSearchEntity

@Dao
interface MapSearchDao {

    @Query("SELECT * FROM map_search_db WHERE id = :id")
    suspend fun getHistory(id: Int): MapSearchEntity?

    @Upsert
    suspend fun updateHistory(localsEntity: MapSearchEntity)
}
