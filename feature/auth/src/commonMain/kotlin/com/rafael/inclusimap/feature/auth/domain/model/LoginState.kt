package com.rafael.inclusimap.feature.auth.domain.model

data class LoginState(
    val user: User? = null,
    val isNewUser: Boolean = false,
    val userPath: String? = null,
    val isLoggedIn: Boolean = true,
    val isRegistering: Boolean = false,
    val isUpdatingPassword: Boolean = false,
    val isSamePassword: Boolean = false,
    val isPasswordChanged: Boolean = false,
    val isLoginOut: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val deleteStep: DeleteProcess = DeleteProcess.NO_OP,
    val isAccountDeleted: Boolean = false,
    val userAlreadyRegistered: Boolean = true,
    val isPasswordCorrect: Boolean = true,
    var networkError: Boolean = false,
    val isUpdatingProfilePicture: Boolean = false,
    val isErrorUpdatingProfilePicture: Boolean = false,
    val isProfilePictureUpdated: Boolean = true,
    val isUpdatingUserName: Boolean = false,
    val isErrorUpdatingUserName: Boolean = false,
    val isUserNameUpdated: Boolean = true,
    val isAllowingPictureOptedIn: Boolean = false,
    val isErrorAllowingPictureOptedIn: Boolean = false,
    val isPictureOptedInSuccessfullyChanged: Boolean = true,
    val isRemovingProfilePicture: Boolean = false,
    val isErrorRemovingProfilePicture: Boolean = false,
    val isProfilePictureRemoved: Boolean = true,
    val userProfilePicture: ByteArray? = null,
    val recoveryToken: String? = null,
    val tokenHash: String? = null,
    val isTokenValid: Boolean = false,
    val tokenExpirationTime: Long? = null,
    val isSendingEmail: Boolean = false,
    val isEmailSent: Boolean = false,
    val isValidatingToken: Boolean = false,
    val isTokenValidated: Boolean = false,
    val tokenExpirationTimer: Long? = null,
    val userExists: Boolean = true,
    val isServerAvailable: Boolean = true,
    val isCheckingServerAvailability: Boolean = false,
    val isUserBanned: Boolean = false,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as LoginState

        if (isNewUser != other.isNewUser) return false
        if (isLoggedIn != other.isLoggedIn) return false
        if (isRegistering != other.isRegistering) return false
        if (isUpdatingPassword != other.isUpdatingPassword) return false
        if (isSamePassword != other.isSamePassword) return false
        if (isPasswordChanged != other.isPasswordChanged) return false
        if (isLoginOut != other.isLoginOut) return false
        if (isDeletingAccount != other.isDeletingAccount) return false
        if (isAccountDeleted != other.isAccountDeleted) return false
        if (userAlreadyRegistered != other.userAlreadyRegistered) return false
        if (isPasswordCorrect != other.isPasswordCorrect) return false
        if (networkError != other.networkError) return false
        if (isUpdatingProfilePicture != other.isUpdatingProfilePicture) return false
        if (isErrorUpdatingProfilePicture != other.isErrorUpdatingProfilePicture) return false
        if (isProfilePictureUpdated != other.isProfilePictureUpdated) return false
        if (isUpdatingUserName != other.isUpdatingUserName) return false
        if (isErrorUpdatingUserName != other.isErrorUpdatingUserName) return false
        if (isUserNameUpdated != other.isUserNameUpdated) return false
        if (isAllowingPictureOptedIn != other.isAllowingPictureOptedIn) return false
        if (isErrorAllowingPictureOptedIn != other.isErrorAllowingPictureOptedIn) return false
        if (isPictureOptedInSuccessfullyChanged != other.isPictureOptedInSuccessfullyChanged) return false
        if (isRemovingProfilePicture != other.isRemovingProfilePicture) return false
        if (isErrorRemovingProfilePicture != other.isErrorRemovingProfilePicture) return false
        if (isProfilePictureRemoved != other.isProfilePictureRemoved) return false
        if (isTokenValid != other.isTokenValid) return false
        if (tokenExpirationTime != other.tokenExpirationTime) return false
        if (isSendingEmail != other.isSendingEmail) return false
        if (isEmailSent != other.isEmailSent) return false
        if (isValidatingToken != other.isValidatingToken) return false
        if (isTokenValidated != other.isTokenValidated) return false
        if (tokenExpirationTimer != other.tokenExpirationTimer) return false
        if (userExists != other.userExists) return false
        if (isServerAvailable != other.isServerAvailable) return false
        if (isCheckingServerAvailability != other.isCheckingServerAvailability) return false
        if (isUserBanned != other.isUserBanned) return false
        if (user != other.user) return false
        if (userPath != other.userPath) return false
        if (deleteStep != other.deleteStep) return false
        if (!userProfilePicture.contentEquals(other.userProfilePicture)) return false
        if (recoveryToken != other.recoveryToken) return false
        if (tokenHash != other.tokenHash) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isNewUser.hashCode()
        result = 31 * result + isLoggedIn.hashCode()
        result = 31 * result + isRegistering.hashCode()
        result = 31 * result + isUpdatingPassword.hashCode()
        result = 31 * result + isSamePassword.hashCode()
        result = 31 * result + isPasswordChanged.hashCode()
        result = 31 * result + isLoginOut.hashCode()
        result = 31 * result + isDeletingAccount.hashCode()
        result = 31 * result + isAccountDeleted.hashCode()
        result = 31 * result + userAlreadyRegistered.hashCode()
        result = 31 * result + isPasswordCorrect.hashCode()
        result = 31 * result + networkError.hashCode()
        result = 31 * result + isUpdatingProfilePicture.hashCode()
        result = 31 * result + isErrorUpdatingProfilePicture.hashCode()
        result = 31 * result + isProfilePictureUpdated.hashCode()
        result = 31 * result + isUpdatingUserName.hashCode()
        result = 31 * result + isErrorUpdatingUserName.hashCode()
        result = 31 * result + isUserNameUpdated.hashCode()
        result = 31 * result + isAllowingPictureOptedIn.hashCode()
        result = 31 * result + isErrorAllowingPictureOptedIn.hashCode()
        result = 31 * result + isPictureOptedInSuccessfullyChanged.hashCode()
        result = 31 * result + isRemovingProfilePicture.hashCode()
        result = 31 * result + isErrorRemovingProfilePicture.hashCode()
        result = 31 * result + isProfilePictureRemoved.hashCode()
        result = 31 * result + isTokenValid.hashCode()
        result = 31 * result + (tokenExpirationTime?.hashCode() ?: 0)
        result = 31 * result + isSendingEmail.hashCode()
        result = 31 * result + isEmailSent.hashCode()
        result = 31 * result + isValidatingToken.hashCode()
        result = 31 * result + isTokenValidated.hashCode()
        result = 31 * result + (tokenExpirationTimer?.hashCode() ?: 0)
        result = 31 * result + userExists.hashCode()
        result = 31 * result + isServerAvailable.hashCode()
        result = 31 * result + isCheckingServerAvailability.hashCode()
        result = 31 * result + isUserBanned.hashCode()
        result = 31 * result + (user?.hashCode() ?: 0)
        result = 31 * result + (userPath?.hashCode() ?: 0)
        result = 31 * result + deleteStep.hashCode()
        result = 31 * result + (userProfilePicture?.contentHashCode() ?: 0)
        result = 31 * result + (recoveryToken?.hashCode() ?: 0)
        result = 31 * result + (tokenHash?.hashCode() ?: 0)
        return result
    }
}
