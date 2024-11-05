package com.rafael.inclusimap.feature.auth.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "login_db")
data class LoginEntity(
    @PrimaryKey
    var id: Int,
    var isLoggedIn: Boolean,
    var userName: String?,
    var userPassword: String?,
    var userEmail: String?,
    var userId: String?,
    var userPathID: String?,
    var showProfilePictureOptedIn: Boolean,
    var profilePicture: ByteArray? = null,
    var tokenHash: String? = null,
    var recoveryToken: String? = null,
    var tokenExpirationDate: Long? = null,
) {
    companion object {
        fun getDefault() =
            LoginEntity(
                id = 1,
                isLoggedIn = false,
                userName = null,
                userPassword = null,
                userEmail = null,
                userId = null,
                showProfilePictureOptedIn = true,
                profilePicture = null,
                userPathID = null,
                tokenHash = null,
                recoveryToken = null,
                tokenExpirationDate = null,
            )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LoginEntity

        if (profilePicture != null) {
            if (other.profilePicture == null) return false
            if (!profilePicture.contentEquals(other.profilePicture)) return false
        } else if (other.profilePicture != null) {
            return false
        }

        return true
    }

    override fun hashCode(): Int = profilePicture?.contentHashCode() ?: 0
}
