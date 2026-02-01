package com.rafael.inclusimap.feature.auth.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "login_table")
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
    var isBanned: Boolean = false,
    var isAdmin: Boolean = false,
    var showFirstTimeAnimation: Boolean = true,
) {
    companion object {
        fun getDefault() = LoginEntity(
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
            isBanned = false,
            isAdmin = false,
            showFirstTimeAnimation = true,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as LoginEntity

        if (id != other.id) return false
        if (isLoggedIn != other.isLoggedIn) return false
        if (showProfilePictureOptedIn != other.showProfilePictureOptedIn) return false
        if (tokenExpirationDate != other.tokenExpirationDate) return false
        if (isBanned != other.isBanned) return false
        if (isAdmin != other.isAdmin) return false
        if (showFirstTimeAnimation != other.showFirstTimeAnimation) return false
        if (userName != other.userName) return false
        if (userPassword != other.userPassword) return false
        if (userEmail != other.userEmail) return false
        if (userId != other.userId) return false
        if (userPathID != other.userPathID) return false
        if (!profilePicture.contentEquals(other.profilePicture)) return false
        if (tokenHash != other.tokenHash) return false
        if (recoveryToken != other.recoveryToken) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + isLoggedIn.hashCode()
        result = 31 * result + showProfilePictureOptedIn.hashCode()
        result = 31 * result + (tokenExpirationDate?.hashCode() ?: 0)
        result = 31 * result + isBanned.hashCode()
        result = 31 * result + isAdmin.hashCode()
        result = 31 * result + showFirstTimeAnimation.hashCode()
        result = 31 * result + (userName?.hashCode() ?: 0)
        result = 31 * result + (userPassword?.hashCode() ?: 0)
        result = 31 * result + (userEmail?.hashCode() ?: 0)
        result = 31 * result + (userId?.hashCode() ?: 0)
        result = 31 * result + (userPathID?.hashCode() ?: 0)
        result = 31 * result + (profilePicture?.contentHashCode() ?: 0)
        result = 31 * result + (tokenHash?.hashCode() ?: 0)
        result = 31 * result + (recoveryToken?.hashCode() ?: 0)
        return result
    }
}
