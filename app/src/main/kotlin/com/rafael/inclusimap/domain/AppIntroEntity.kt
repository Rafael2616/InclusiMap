package com.rafael.inclusimap.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_intro")
data class AppIntroEntity(
    @PrimaryKey
    var id: Int,
    var showAppIntro: Boolean,
    var isFirstTime: Boolean
) {
    companion object {
        fun getDefault() = AppIntroEntity(
            id = 1,
            showAppIntro = false,
            isFirstTime = true
        )
    }
}